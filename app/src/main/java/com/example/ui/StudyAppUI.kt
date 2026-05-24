package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.DateFormat
import java.util.Date

// Color constants for educational subjects
val SciencePrimary = Color(0xFF00B4D8)
val ScienceSecondary = Color(0xFF90E0EF)
val MathsPrimary = Color(0xFFF77F00)
val MathsSecondary = Color(0xFFFCA311)
val SstPrimary = Color(0xFF2A9D8F)
val SstSecondary = Color(0xFFE9C46A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyAppMainContent(viewModel: StudyViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val streakState by viewModel.studyStreak.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Study Companion",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Small streak badge on Top Bar
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            onClick = { viewModel.setScreen(Screen.Progress) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥 ${streakState?.streakValue ?: 0}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            if (currentScreen !is Screen.ActiveQuiz) {
                StudyBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.setScreen(it) }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.Notebook -> NotebookScreen(viewModel)
                    is Screen.Formulas -> FormulasScreen(viewModel)
                    is Screen.QuizSelection -> QuizSelectionScreen(viewModel)
                    is Screen.ActiveQuiz -> ActiveQuizScreen(viewModel, targetScreen)
                    is Screen.Numericals -> StepByStepNumericalsScreen(viewModel)
                    is Screen.Progress -> ProgressAndSyncScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun StudyBottomNavBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Notebook,
            onClick = { onNavigate(Screen.Notebook) },
            icon = { Icon(Icons.Default.ImportContacts, contentDescription = "Notes") },
            label = { Text("Notes", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_notebook")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Formulas,
            onClick = { onNavigate(Screen.Formulas) },
            icon = { Icon(Icons.Default.Functions, contentDescription = "Formulas") },
            label = { Text("Formulas", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_formulas")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.QuizSelection || currentScreen is Screen.ActiveQuiz,
            onClick = { onNavigate(Screen.QuizSelection) },
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Quizzes") },
            label = { Text("Quizzes", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_quizzes")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Numericals,
            onClick = { onNavigate(Screen.Numericals) },
            icon = { Icon(Icons.Default.Calculate, contentDescription = "Numericals") },
            label = { Text("Numericals", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_numericals")
        )
        NavigationBarItem(
            selected = currentScreen is Screen.Progress,
            onClick = { onNavigate(Screen.Progress) },
            icon = { Icon(Icons.Default.Autorenew, contentDescription = "Progress") },
            label = { Text("Stats", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = Modifier.testTag("nav_progress")
        )
    }
}

// ================= NOTEBOOK SCREEN =================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotebookScreen(viewModel: StudyViewModel) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.notesSearchQuery.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.notesSelectedSubject.collectAsStateWithLifecycle()
    val selectedNoteForAI by viewModel.selectedNoteForAI.collectAsStateWithLifecycle()

    var showAddNoteDialog by remember { mutableStateOf(false) }

    // Filter notes based on selection and query
    val filteredNotes = remember(notes, searchQuery, selectedSubject) {
        notes.filter { note ->
            val matchesSubject = selectedSubject == null || note.subject.equals(selectedSubject, ignoreCase = true)
            val matchesSearch = searchQuery.isEmpty() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.chapter.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.tagList.contains(searchQuery, ignoreCase = true)
            matchesSubject && matchesSearch
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title Header with Search and Filter
            Text(
                text = "Study Revision Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            Text(
                text = "Search across syllabus and personalized revision flashcards instantly.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setNotesSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("notes_search_field"),
                placeholder = { Text("Search title, tags (#optics), or syllabus content...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setNotesSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Subject Filter Row Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Science", "Maths", "SST").forEach { subj ->
                    val filterVal = if (subj == "All") null else subj
                    val isSelected = selectedSubject == filterVal
                    val color = when (subj) {
                        "Science" -> SciencePrimary
                        "Maths" -> MathsPrimary
                        "SST" -> SstPrimary
                        else -> MaterialTheme.colorScheme.primary
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setNotesSubjectFilter(filterVal) },
                        label = { Text(subj) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = color,
                            selectedLeadingIconColor = color
                        ),
                        leadingIcon = {
                            val icon = when (subj) {
                                "Science" -> Icons.Default.Science
                                "Maths" -> Icons.Default.Calculate
                                "SST" -> Icons.Default.Public
                                else -> Icons.Default.LibraryBooks
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Notebook List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No study cards found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Try refining search tags or click '+' to add a custom note.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes) { note ->
                        NoteCard(
                            note = note,
                            onClick = { viewModel.selectNoteForAIExplanation(note) },
                            onDelete = { viewModel.removeNote(note) }
                        )
                    }
                }
            }
        }

        // Add Note FAB
        FloatingActionButton(
            onClick = { showAddNoteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp)
                .testTag("add_note_fab"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add custom study note")
        }
    }

    // Detail Bottom Sheet Simulator (Dialog)
    if (selectedNoteForAI != null) {
        NoteDetailDialog(
            note = selectedNoteForAI!!,
            onDismiss = { viewModel.selectNoteForAIExplanation(null) },
            viewModel = viewModel
        )
    }

    // Add study note dialog
    if (showAddNoteDialog) {
        AddStudyNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onSave = { subject, chapter, title, content, tags ->
                viewModel.addNote(subject, chapter, title, content, tags)
                showAddNoteDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    val subjectColor = when (note.subject) {
        "Science" -> SciencePrimary
        "Maths" -> MathsPrimary
        "SST" -> SstPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("note_item_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(subjectColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = note.subject,
                        color = subjectColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Custom user flag indicator
                    if (note.isCustom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .padding(end = 4.dp)
                        ) {
                            Text(
                                text = "Personal",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 10.sp
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Chapter: ${note.chapter}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = note.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tags row flow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                note.tagList.split(",").map { it.trim() }.forEach { tag ->
                    if (tag.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteDetailDialog(note: Note, onDismiss: () -> Unit, viewModel: StudyViewModel) {
    val isGeneratingAI by viewModel.isGeneratingAIExplanation.collectAsStateWithLifecycle()
    val aiExplanationText by viewModel.aiExplanationText.collectAsStateWithLifecycle()

    val hasApiKey = remember { GeminiService.isApiKeyAvailable() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Study Card",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = note.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${note.subject} • Chapter: ${note.chapter}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = note.content,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // --- AI WORKER PANEL ---
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Study Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isGeneratingAI) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Generating tutoring overview...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        } else if (aiExplanationText != null) {
                            Text(
                                text = aiExplanationText!!,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            if (hasApiKey) {
                                Text(
                                    text = "Need a quick summary? Ask the AI Tutor to outline the 3 key concepts from this card.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.selectNoteForAIExplanation(note) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Summarize with AI")
                                }
                            } else {
                                Text(
                                    text = "AI summary tutoring helper requires a Gemini API Key.\nConfigure your key securely in AI Studio's Secrets Manager panel under GEMINI_API_KEY to turn it on!",
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudyNoteDialog(onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Science") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    var expandedSubjectMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Custom Card",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject Select Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedSubjectMenu,
                    onExpandedChange = { expandedSubjectMenu = it }
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubjectMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSubjectMenu,
                        onDismissRequest = { expandedSubjectMenu = false }
                    ) {
                        listOf("Science", "Maths", "SST").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    subject = item
                                    expandedSubjectMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = chapter,
                    onValueChange = { chapter = it },
                    label = { Text("Chapter") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Card Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Core Study Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Search Tags (comma separated)") },
                    placeholder = { Text("e.g. physics, light, mirror") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty() && chapter.isNotEmpty()) {
                            onSave(subject, chapter, title, content, tags)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = title.isNotEmpty() && content.isNotEmpty() && chapter.isNotEmpty()
                ) {
                    Text("Save Study Card", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================= FORMULAS SCREEN =================

@Composable
fun FormulasScreen(viewModel: StudyViewModel) {
    val formulas by viewModel.formulas.collectAsStateWithLifecycle()
    val searchQuery by viewModel.formulasSearchQuery.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.formulasSelectedSubject.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.formulasOnlyFavorites.collectAsStateWithLifecycle()

    val filteredFormulas = remember(formulas, searchQuery, selectedSubject, onlyFavorites) {
        formulas.filter { form ->
            val matchesSubject = selectedSubject == null || form.subject.equals(selectedSubject, ignoreCase = true)
            val matchesFavorites = !onlyFavorites || form.isFavorite
            val matchesSearch = searchQuery.isEmpty() ||
                    form.title.contains(searchQuery, ignoreCase = true) ||
                    form.formulaExpression.contains(searchQuery, ignoreCase = true) ||
                    form.explanation.contains(searchQuery, ignoreCase = true)
            matchesSubject && matchesFavorites && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Formula Reference Book",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Essential equations of Maths, Physics, and Chemistry at your fingertips.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Query Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setFormulasSearchQuery(it) },
            placeholder = { Text("Search equation tags or mathematical symbol...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Split Tabs: Maths / Physics / Chemistry / Favorites-Only Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Maths", "Physics", "Chemistry").forEach { subj ->
                    val isSelected = selectedSubject == subj
                    val color = when (subj) {
                        "Maths" -> MathsPrimary
                        "Physics" -> SciencePrimary
                        "Chemistry" -> SstPrimary
                        else -> MaterialTheme.colorScheme.primary
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFormulasSubjectFilter(subj) },
                        label = { Text(subj, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = color
                        )
                    )
                }
            }

            // Favorites quick filter icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "★ Starred",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (onlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = onlyFavorites,
                    onCheckedChange = { viewModel.setFormulasOnlyFavorites(it) },
                    modifier = Modifier.testTag("favorites_toggle")
                )
            }
        }

        // List
        if (filteredFormulas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No formulas found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFormulas) { form ->
                    FormulaCard(form, onFavoriteToggle = { viewModel.toggleFormulaFavorite(form) })
                }
            }
        }
    }
}

@Composable
fun FormulaCard(formula: Formula, onFavoriteToggle: () -> Unit) {
    val categoryColor = when (formula.subject) {
        "Maths" -> MathsPrimary
        "Physics" -> SciencePrimary
        "Chemistry" -> SstPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Topic label
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = formula.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }

                // Favorite Star
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (formula.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite star",
                        tint = if (formula.isFavorite) MathsSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = formula.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // High Contrast Formula block with Monospace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .background(categoryColor.copy(alpha = 0.05f))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formula.formulaExpression,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = categoryColor,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = formula.explanation,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

// ================= QUIZZES & PRACTICE SECTION =================

@Composable
fun QuizSelectionScreen(viewModel: StudyViewModel) {
    val subjects = listOf("Science", "Maths", "SST")
    var selectedSubjectTab by remember { mutableStateOf("Science") }
    val chapters = remember(selectedSubjectTab) { QuizProvider.getChaptersForSubject(selectedSubjectTab) }
    val keyAvailable = remember { GeminiService.isApiKeyAvailable() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Practice & Learning Quizzes",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Test your board concept retention with revision chapter quizzes.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Subject Tabs selection
        TabRow(
            selectedTabIndex = subjects.indexOf(selectedSubjectTab),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            subjects.forEach { subj ->
                val color = when (subj) {
                    "Science" -> SciencePrimary
                    "Maths" -> MathsPrimary
                    "SST" -> SstPrimary
                    else -> MaterialTheme.colorScheme.primary
                }

                Tab(
                    selected = selectedSubjectTab == subj,
                    onClick = { selectedSubjectTab = subj },
                    text = {
                        Text(
                            text = subj,
                            color = if (selectedSubjectTab == subj) color else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        // Chapters List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chapters) { chap ->
                ChapterQuizLauncherCard(
                    subject = selectedSubjectTab,
                    chapter = chap,
                    apiKeyEnabled = keyAvailable,
                    onStartQuiz = { withAi ->
                        viewModel.startQuiz(selectedSubjectTab, chap, generateWithAI = withAi)
                    }
                )
            }
        }
    }
}

@Composable
fun ChapterQuizLauncherCard(subject: String, chapter: String, apiKeyEnabled: Boolean, onStartQuiz: (Boolean) -> Unit) {
    val subjectColor = when (subject) {
        "Science" -> SciencePrimary
        "Maths" -> MathsPrimary
        "SST" -> SstPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = chapter,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Includes 3 multiple-choice conceptual practice challenges to test and explain solutions.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Offline Local Quiz
                Button(
                    onClick = { onStartQuiz(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = subjectColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Instant Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // AI Generative Quiz if Key is there
                ElevatedButton(
                    onClick = { onStartQuiz(true) },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (apiKeyEnabled) "AI Generate MCQ" else "AI Quiz (No Key)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ================= ACTIVE QUIZ PLAYER SCREEN =================

@Composable
fun ActiveQuizScreen(viewModel: StudyViewModel, activeQuiz: Screen.ActiveQuiz) {
    val questions by viewModel.activeQuizQuestions.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedAnswerIndex.collectAsStateWithLifecycle()
    val isRevealed by viewModel.isOptionAnswerRevealed.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()
    val isCompleted by viewModel.isQuizCompletedState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingQuiz.collectAsStateWithLifecycle()

    val subjectColor = when (activeQuiz.subject) {
        "Science" -> SciencePrimary
        "Maths" -> MathsPrimary
        "SST" -> SstPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = subjectColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (activeQuiz.isAiQuiz) 
                             "Tuning Gemini AI Tutor..." 
                           else "Loading concept test bank...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Preparing 3 conceptual challenges",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    if (questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Failed to load quiz.")
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { viewModel.endQuizAndReturn() }) {
                    Text("Return")
                }
            }
        }
        return
    }

    if (isCompleted) {
        // CONFETTI COMPLETION VIEW
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 Quiz Completed! 🎉",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = subjectColor,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${activeQuiz.chapter}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Score Circle
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(subjectColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$score / ${questions.size}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = subjectColor
                            )
                            Text(
                                text = "Correct",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val scorePercentage = (score.toFloat() / questions.size * 100).toInt()
                    val feedbackText = when {
                        scorePercentage == 100 -> "Outstanding! You have mastered this chapter. 🔥"
                        scorePercentage >= 60 -> "Good work! Review your wrong answers to score 100%!"
                        else -> "Keep studying! Review your notes and try again."
                    }

                    Text(
                        text = feedbackText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    
                    Text(
                        text = "🔥 Daily Study streak updated in stats!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { viewModel.endQuizAndReturn() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finish and Exit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val currentQuestion = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back Button & Progress Head
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.endQuizAndReturn() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Question ${currentIndex + 1} of ${questions.size}",
                fontWeight = FontWeight.Bold,
                color = subjectColor
            )

            // Silent placeholder to balance row
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Live Progress list line
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = subjectColor,
            trackColor = subjectColor.copy(alpha = 0.2f)
        )

        // Question display card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = currentQuestion.question,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quiz Options
        currentQuestion.options.forEachIndexed { optIndex, optText ->
            val isSelected = selectedIndex == optIndex
            val isCorrectIndex = currentQuestion.correctIndex == optIndex

            val backgroundColor = when {
                !isRevealed -> if (isSelected) subjectColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                isSelected && isCorrectIndex -> Color(0xFFE8F5E9)      // Right selection: light green
                isSelected && !isCorrectIndex -> Color(0xFFFFEBEE)     // Wrong selection: light red
                isCorrectIndex -> Color(0xFFE8F5E9)                    // Reveal correct one: light green
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                !isRevealed -> if (isSelected) subjectColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                isSelected && isCorrectIndex -> Color(0xFF4CAF50)
                isSelected && !isCorrectIndex -> Color(0xFFE53935)
                isCorrectIndex -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }

            val icon = when {
                !isRevealed -> null
                isCorrectIndex -> Icons.Default.Check
                isSelected && !isCorrectIndex -> Icons.Default.Close
                else -> null
            }

            val tint = if (isCorrectIndex) Color(0xFF2E7D32) else Color(0xFFC62828)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable(enabled = !isRevealed) {
                        viewModel.selectQuizAnswer(optIndex)
                    }
                    .testTag("quiz_option_$optIndex"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, borderColor),
                colors = CardDefaults.cardColors(containerColor = backgroundColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = optText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Solution Explanatory Accordion once click validated
        if (isRevealed) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, subjectColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = subjectColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Answer Explanation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentQuestion.explanation,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.advanceQuizQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = subjectColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentIndex + 1 == questions.size) "Complete Quiz" else "Next Challenge",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ================= STEP BY STEP NUMERICAL PRACTICE SCREEN =================

@Composable
fun StepByStepNumericalsScreen(viewModel: StudyViewModel) {
    val problems by viewModel.numericals.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.numericalsSelectedSubject.collectAsStateWithLifecycle()

    val filteredProblems = remember(problems, selectedSubject) {
        problems.filter { prob ->
            selectedSubject == null || prob.subject.equals(selectedSubject, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Step-by-Step Numericals",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Practice chemical balancing and physics calculation algorithms block-by-block.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Switch to separate subjects
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Physics", "Chemistry").forEach { subj ->
                val isSelected = selectedSubject == subj
                val color = if (subj == "Physics") SciencePrimary else SstPrimary

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setNumericalsSubjectFilter(subj) },
                    label = { Text(subj) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.2f),
                        selectedLabelColor = color
                    )
                )
            }
        }

        // List
        if (filteredProblems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No problems populated.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProblems) { problem ->
                    NumericalStepCard(problem)
                }
            }
        }
    }
}

@Composable
fun NumericalStepCard(problem: NumericalProblem) {
    val color = if (problem.subject == "Physics") SciencePrimary else SstPrimary
    val steps = remember(problem.stepListJson) { problem.stepListJson.split("||").map { it.trim() }.filter { it.isNotEmpty() } }
    
    // Track how many steps are currently unveiled
    var unveiledStepsCount by remember { mutableStateOf(0) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = problem.subject,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Text(
                    text = problem.topic,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = problem.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Step unveiling mechanism heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Methodological Resolution Steps",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "$unveiledStepsCount of ${steps.size} Unveiled",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Steps block column
            steps.forEachIndexed { sIndex, sContent ->
                val isVisible = sIndex < unveiledStepsCount
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sIndex + 1}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sContent,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action triggers to unveil step-by-step
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (unveiledStepsCount < steps.size) {
                    Button(
                        onClick = { unveiledStepsCount++ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (unveiledStepsCount == 0) "Show Step 1" else "Unveil Next Step",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Explanatory completed state
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✔ Solutions Completed!",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (unveiledStepsCount > 0) {
                    TextButton(
                        onClick = { unveiledStepsCount = 0 },
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Show final answer at the very end
            if (unveiledStepsCount == steps.size) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Stars, contentDescription = null, tint = color)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Final Answer: ${problem.finalAnswer}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

// ================= STATS & SYNC PANEL SCREEN =================

@Composable
fun ProgressAndSyncScreen(viewModel: StudyViewModel) {
    val streakState by viewModel.studyStreak.collectAsStateWithLifecycle()
    val historyAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()

    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncStatusText by viewModel.syncMessage.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Personal Progress Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Visualize learning streaks, view past quiz attempts, and securely sync your studies.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Streak Card 🔥
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔥", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Current Study Streak",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${streakState?.streakValue ?: 1} Consecutive Days",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Daily Calendar checkboxes row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Habit Calendar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val progressMask = streakState?.weeklyProgressMask ?: 0
                    
                    days.forEachIndexed { dIndex, day ->
                        val dayBit = 1 shl dIndex
                        val isStudied = (progressMask and dayBit) != 0

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isStudied) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isStudied) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = day,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cloud backup & sync options
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cloud Synchronization Node",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Export local revision lists, personalized formulas, and streaking metadata securely to study lockers.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Last Synchronized:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastSyncTime,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { viewModel.executeCloudSync() },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("sync_cloud_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Now")
                    }
                }

                if (isSyncing || syncStatusText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = syncStatusText ?: "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        // Quizzes taken logs history listing
        Text(
            text = "Quiz History & AnalyticsLogs",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (historyAttempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No completed chapter tests yet! Go take chapter quizzes to unlock.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        } else {
            Card(
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    historyAttempts.forEachIndexed { hIndex, log ->
                        val subColor = when (log.subject) {
                            "Science" -> SciencePrimary
                            "Maths" -> MathsPrimary
                            "SST" -> SstPrimary
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.chapter,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${log.subject} • ${DateFormat.getDateInstance().format(Date(log.timestamp))}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(subColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${log.score} / ${log.totalQuestions}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = subColor
                                )
                            }
                        }
                        if (hIndex < historyAttempts.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
