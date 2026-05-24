package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- NOTE ENTITY ---
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // "Science", "Maths", "SST"
    val chapter: String,
    val title: String,
    val content: String,
    val tagList: String, // Comma-separated tags (e.g. "physics,force,motion")
    val isCustom: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

// --- FORMULA ENTITY ---
@Entity(tableName = "formulas")
data class Formula(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // "Maths", "Physics", "Chemistry"
    val title: String,
    val formulaExpression: String, // e.g. "F = G * (m1*m2)/r²"
    val explanation: String,
    val isFavorite: Boolean = false
)

// --- NUMERICAL PROBLEM ENTITY ---
@Entity(tableName = "numericals")
data class NumericalProblem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // "Physics", "Chemistry"
    val topic: String, // e.g., "Light - Lens Formula"
    val question: String,
    val stepListJson: String, // Delimited by "||" for step text
    val finalAnswer: String
)

// --- QUIZ ATTEMPT ENTITY ---
@Entity(tableName = "quiz_attempts")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val chapter: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// --- STUDY STREAK ENTITY ---
@Entity(tableName = "study_streak")
data class StudyStreak(
    @PrimaryKey val id: Int = 1,
    val streakValue: Int = 0,
    val lastStudyDateTimestamp: Long = 0, // Time in millis
    val weeklyProgressMask: Int = 0 // Bitmap for Mon-Sun activity where bit0 is Mon...
)

// --- DAO ---
@Dao
interface StudyDao {
    @Query("SELECT * FROM notes ORDER BY lastUpdated DESC")
    fun getAllNotesFlow(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM formulas ORDER BY id ASC")
    fun getAllFormulasFlow(): Flow<List<Formula>>

    @Update
    suspend fun updateFormula(formula: Formula)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(formulas: List<Formula>)

    @Query("SELECT * FROM numericals ORDER BY id ASC")
    fun getAllNumericalsFlow(): Flow<List<NumericalProblem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNumericals(problems: List<NumericalProblem>)

    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllQuizAttemptsFlow(): Flow<List<QuizAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttempt)

    @Query("SELECT * FROM study_streak WHERE id = 1")
    fun getStudyStreakFlow(): Flow<StudyStreak?>

    @Query("SELECT * FROM study_streak WHERE id = 1")
    suspend fun getStudyStreakDirect(): StudyStreak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: StudyStreak)
}

// --- DATABASE ---
@Database(
    entities = [Note::class, Formula::class, NumericalProblem::class, QuizAttempt::class, StudyStreak::class],
    version = 1,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
