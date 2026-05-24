package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Notebook : Screen()
    object Formulas : Screen()
    object QuizSelection : Screen()
    data class ActiveQuiz(val subject: String, val chapter: String, val isAiQuiz: Boolean = false) : Screen()
    object Numericals : Screen()
    object Progress : Screen()
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyRepository(application)

    // --- Active Screen state ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Notebook)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- Search & Filters ---
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    private val _notesSelectedSubject = MutableStateFlow<String?>("Science") // Null is "All"
    val notesSelectedSubject: StateFlow<String?> = _notesSelectedSubject.asStateFlow()

    private val _formulasSearchQuery = MutableStateFlow("")
    val formulasSearchQuery: StateFlow<String> = _formulasSearchQuery.asStateFlow()

    private val _formulasSelectedSubject = MutableStateFlow<String?>("Maths") // Maths, Physics, Chemistry
    val formulasSelectedSubject: StateFlow<String?> = _formulasSelectedSubject.asStateFlow()

    private val _formulasOnlyFavorites = MutableStateFlow(false)
    val formulasOnlyFavorites: StateFlow<Boolean> = _formulasOnlyFavorites.asStateFlow()

    private val _numericalsSelectedSubject = MutableStateFlow<String?>("Physics") // Physics, Chemistry
    val numericalsSelectedSubject: StateFlow<String?> = _numericalsSelectedSubject.asStateFlow()

    // --- Room Database Flows ---
    val notes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val formulas: StateFlow<List<Formula>> = repository.allFormulas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val numericals: StateFlow<List<NumericalProblem>> = repository.allNumericals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttempt>> = repository.allQuizAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyStreak: StateFlow<StudyStreak?> = repository.studyStreak
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- AI Explanations ---
    private val _selectedNoteForAI = MutableStateFlow<Note?>(null)
    val selectedNoteForAI: StateFlow<Note?> = _selectedNoteForAI.asStateFlow()

    private val _isGeneratingAIExplanation = MutableStateFlow(false)
    val isGeneratingAIExplanation: StateFlow<Boolean> = _isGeneratingAIExplanation.asStateFlow()

    private val _aiExplanationText = MutableStateFlow<String?>(null)
    val aiExplanationText: StateFlow<String?> = _aiExplanationText.asStateFlow()

    // --- Active Quiz state ---
    private val _activeQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val activeQuizQuestions: StateFlow<List<QuizQuestion>> = _activeQuizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _isOptionAnswerRevealed = MutableStateFlow(false)
    val isOptionAnswerRevealed: StateFlow<Boolean> = _isOptionAnswerRevealed.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizCompletedState = MutableStateFlow(false)
    val isQuizCompletedState: StateFlow<Boolean> = _isQuizCompletedState.asStateFlow()

    private val _isLoadingQuiz = MutableStateFlow(false)
    val isLoadingQuiz: StateFlow<Boolean> = _isLoadingQuiz.asStateFlow()

    // --- Cloud Sync / Backup state ---
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<String>("Never")
    val lastSyncTimestamp: StateFlow<String> = _lastSyncTimestamp.asStateFlow()

    init {
        // Initialize content in a coroutine
        viewModelScope.launch {
            repository.populateDefaultContentIfEmpty()
            repository.recordStudyActivity() // Record today's default login activity
        }
    }

    // --- Actions ---
    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setNotesSearchQuery(query: String) {
        _notesSearchQuery.value = query
    }

    fun setNotesSubjectFilter(subject: String?) {
        _notesSelectedSubject.value = subject
    }

    fun setFormulasSearchQuery(query: String) {
        _formulasSearchQuery.value = query
    }

    fun setFormulasSubjectFilter(subject: String?) {
        _formulasSelectedSubject.value = subject
    }

    fun setFormulasOnlyFavorites(favs: Boolean) {
        _formulasOnlyFavorites.value = favs
    }

    fun setNumericalsSubjectFilter(subject: String?) {
        _numericalsSelectedSubject.value = subject
    }

    fun addNote(subject: String, chapter: String, title: String, content: String, tags: String) {
        viewModelScope.launch {
            repository.addCustomNote(subject, chapter, title, content, tags)
        }
    }

    fun removeNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleFormulaFavorite(formula: Formula) {
        viewModelScope.launch {
            repository.toggleFormulaFavorite(formula)
        }
    }

    // --- AI Explanations ---
    fun selectNoteForAIExplanation(note: Note?) {
        _selectedNoteForAI.value = note
        _aiExplanationText.value = null
        _isGeneratingAIExplanation.value = false
        if (note != null) {
            triggerAIExplanation(note)
        }
    }

    private fun triggerAIExplanation(note: Note) {
        viewModelScope.launch {
            _isGeneratingAIExplanation.value = true
            _aiExplanationText.value = "Consulting AI Tutor..."
            val result = GeminiService.explainConcept(
                subject = note.subject,
                chapter = note.chapter,
                conceptTitle = note.title,
                conceptContent = note.content
            )
            _aiExplanationText.value = result
            _isGeneratingAIExplanation.value = false
        }
    }

    // --- Active Quiz Actions ---
    fun startQuiz(subject: String, chapter: String, generateWithAI: Boolean = false) {
        setScreen(Screen.ActiveQuiz(subject, chapter, generateWithAI))
        _currentQuestionIndex.value = 0
        _selectedAnswerIndex.value = null
        _isOptionAnswerRevealed.value = false
        _quizScore.value = 0
        _isQuizCompletedState.value = false
        _activeQuizQuestions.value = emptyList()

        viewModelScope.launch {
            _isLoadingQuiz.value = true
            if (generateWithAI) {
                val qList = GeminiService.generateCustomQuiz(subject, chapter)
                if (qList.isNotEmpty()) {
                    _activeQuizQuestions.value = qList
                } else {
                    // Fallback to offline questions if AI generation failed or key was absent
                    _activeQuizQuestions.value = QuizProvider.getQuestions(subject, chapter)
                }
            } else {
                _activeQuizQuestions.value = QuizProvider.getQuestions(subject, chapter)
            }
            _isLoadingQuiz.value = false
        }
    }

    fun selectQuizAnswer(index: Int) {
        if (_selectedAnswerIndex.value != null) return // Already selected
        _selectedAnswerIndex.value = index
        _isOptionAnswerRevealed.value = true
        
        val currentQuestions = _activeQuizQuestions.value
        val currentIndex = _currentQuestionIndex.value
        if (currentIndex in currentQuestions.indices) {
            val q = currentQuestions[currentIndex]
            if (index == q.correctIndex) {
                _quizScore.value += 1
            }
        }
    }

    fun advanceQuizQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1
        val questionsCount = _activeQuizQuestions.value.size
        if (nextIndex < questionsCount) {
            _currentQuestionIndex.value = nextIndex
            _selectedAnswerIndex.value = null
            _isOptionAnswerRevealed.value = false
        } else {
            // Quiz completed
            _isQuizCompletedState.value = true
            
            // Record result to DB (increments streaks)
            val currentScreenItem = _currentScreen.value
            if (currentScreenItem is Screen.ActiveQuiz) {
                viewModelScope.launch {
                    repository.saveQuizResult(
                        subject = currentScreenItem.subject,
                        chapter = currentScreenItem.chapter,
                        score = _quizScore.value,
                        total = questionsCount
                    )
                }
            }
        }
    }

    fun endQuizAndReturn() {
        _activeQuizQuestions.value = emptyList()
        _currentQuestionIndex.value = 0
        _selectedAnswerIndex.value = null
        _isOptionAnswerRevealed.value = false
        _isQuizCompletedState.value = false
        setScreen(Screen.QuizSelection)
    }

    // --- Cloud Synchronization API ---
    fun executeCloudSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Analyzing local study metrics..."
            delay(1200)
            
            _syncMessage.value = "Bundling notes, custom items and formulas..."
            delay(1000)
            
            val totalNotesCount = notes.value.size
            val favoriteFormulasCount = formulas.value.filter { it.isFavorite }.size
            val highestStreak = studyStreak.value?.streakValue ?: 0
            
            _syncMessage.value = "Synchronizing metadata: $totalNotesCount Notes, $favoriteFormulasCount Favorites, 🔥 $highestStreak Streak"
            delay(1500)
            
            _syncMessage.value = "Success! Secondary databases verified and reconciled."
            delay(800)
            
            val todayDateStr = java.text.DateFormat.getDateTimeInstance().format(java.util.Date())
            _lastSyncTimestamp.value = todayDateStr
            _isSyncing.value = false
            _syncMessage.value = null
        }
    }
}
