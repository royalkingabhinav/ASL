package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class StudyRepository(context: Context) {
    private val studyDao = StudyDatabase.getDatabase(context).studyDao()

    val allNotes: Flow<List<Note>> = studyDao.getAllNotesFlow()
    val allFormulas: Flow<List<Formula>> = studyDao.getAllFormulasFlow()
    val allNumericals: Flow<List<NumericalProblem>> = studyDao.getAllNumericalsFlow()
    val allQuizAttempts: Flow<List<QuizAttempt>> = studyDao.getAllQuizAttemptsFlow()
    val studyStreak: Flow<StudyStreak?> = studyDao.getStudyStreakFlow()

    suspend fun addCustomNote(subject: String, chapter: String, title: String, content: String, tags: String) {
        val note = Note(
            subject = subject,
            chapter = chapter,
            title = title,
            content = content,
            tagList = tags,
            isCustom = true,
            lastUpdated = System.currentTimeMillis()
        )
        studyDao.insertNote(note)
    }

    suspend fun deleteNote(note: Note) {
        studyDao.deleteNote(note)
    }

    suspend fun toggleFormulaFavorite(formula: Formula) {
        studyDao.updateFormula(formula.copy(isFavorite = !formula.isFavorite))
    }

    suspend fun saveQuizResult(subject: String, chapter: String, score: Int, total: Int) {
        // Record the attempt
        val attempt = QuizAttempt(
            subject = subject,
            chapter = chapter,
            score = score,
            totalQuestions = total,
            timestamp = System.currentTimeMillis()
        )
        studyDao.insertQuizAttempt(attempt)

        // Increment user streak and active days
        recordStudyActivity()
    }

    suspend fun recordStudyActivity() {
        val currentStreak = studyDao.getStudyStreakDirect() ?: StudyStreak(id = 1)
        val todayCalendar = Calendar.getInstance()
        val todayStart = getStartOfDayMillis(todayCalendar)

        val lastStudyMillis = currentStreak.lastStudyDateTimestamp
        val lastStudyCalendar = Calendar.getInstance().apply { timeInMillis = lastStudyMillis }
        val lastStudyStart = getStartOfDayMillis(lastStudyCalendar)

        val diffDays = ((todayStart - lastStudyStart) / (24 * 60 * 60 * 1000)).toInt()

        val newStreakVal = when {
            lastStudyMillis == 0L -> 1 // First time starting streak
            diffDays == 0 -> currentStreak.streakValue // Already studied today, keep current value
            diffDays == 1 -> currentStreak.streakValue + 1 // Consecutive day, increment
            else -> 1 // Gap of 2+ days, reset streak to 1
        }

        // Determine current day of week (Monday = 1, Sunday = 7)
        val dayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK) // Sunday=1, Monday=2...
        val dayBit = when (dayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 8
            Calendar.FRIDAY -> 16
            Calendar.SATURDAY -> 32
            Calendar.SUNDAY -> 64
            else -> 0
        }

        val updatedMask = currentStreak.weeklyProgressMask or dayBit

        studyDao.insertOrUpdateStreak(
            StudyStreak(
                id = 1,
                streakValue = newStreakVal,
                lastStudyDateTimestamp = System.currentTimeMillis(),
                weeklyProgressMask = updatedMask
            )
        )
    }

    private fun getStartOfDayMillis(calendar: Calendar): Long {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Preloads structured default study content if lists are empty
    suspend fun populateDefaultContentIfEmpty() {
        // 1. Populate default Notes
        val currentNotes = studyDao.getAllNotesFlow().firstOrNull() ?: emptyList()
        if (currentNotes.isEmpty()) {
            val defaultNotes = listOf(
                Note(
                    subject = "Science",
                    chapter = "Light Reflection and Refraction",
                    title = "Rules for Image Formation by Spherical Mirrors",
                    content = "1. A ray parallel to principal axis passes through principal focus (F) after reflection.\n2. A ray passing through focus (F) becomes parallel to axis.\n3. A ray passing through center of curvature (C) is reflected back along same path because it falls normal to surface.",
                    tagList = "optics, physics, class10, revision"
                ),
                Note(
                    subject = "Science",
                    chapter = "Chemical Reactions and Equations",
                    title = "Types of Chemical Reactions",
                    content = "- Combination Reaction: Two / more reactants form a single product. (A + B → AB)\n- Decomposition Reaction: Single reactant breaks down into multiple products. (AB → A + B)\n- Displacement Reaction: High reactive element displaces lower reactive element. (A + BC → AC + B)\n- Redox Reaction: Simultaneous reduction and oxidation.",
                    tagList = "chemistry, equations, basics, revision"
                ),
                Note(
                    subject = "Maths",
                    chapter = "Arithmetic Progressions",
                    title = "Sum of n Terms of an AP",
                    content = "The sum of the first n terms of an arithmetic progression is given by:\nSn = n/2 * [2a + (n - 1)d]\nor Sn = n/2 * [a + l], where 'l' is the last term (an = a + (n-1)d).\nHere 'a' is the first term, and 'd' is the common difference.",
                    tagList = "algebra, ap, sequences, arithmetic"
                ),
                Note(
                    subject = "Maths",
                    chapter = "Trigonometry",
                    title = "Trigonometric Ratios & Identities",
                    content = "In a right-angled triangle:\n- sin θ = opposite / hypotenuse\n- cos θ = adjacent / hypotenuse\n- tan θ = opposite / adjacent\nFundamental Pythogorean Identity:\nsin² θ + cos² θ = 1\n1 + tan² θ = sec² θ\n1 + cot² θ = cosec² θ",
                    tagList = "geometry, trigonometry, triangle, angles"
                ),
                Note(
                    subject = "SST",
                    chapter = "Rise of Nationalism in Europe",
                    title = "The Idea of Nation-State",
                    content = "During the 19th century, nationalism emerged as a force which swept Europe. It led to the creation of the 'nation-state' in place of multi-national dynastic empires. This nation-state had shared identity, common history, and active citizenship instead of sovereign monarchs.",
                    tagList = "history, nationalism, europe, class10"
                ),
                Note(
                    subject = "SST",
                    chapter = "Federalism",
                    title = "Key Features of Federalism",
                    content = "Federalism is a system of government in which power is divided between central authority and constituent state units:\n1. Two or more levels of government exist.\n2. Each level has its own specific jurisdiction.\n3. Jurisdictions are constitutionally guaranteed.\n4. Courts have power to interpret the Constitution.",
                    tagList = "civics, federalism, politics, constitution"
                )
            )
            for (note in defaultNotes) {
                studyDao.insertNote(note)
            }
        }

        // 2. Populate Formulas
        val currentFormulas = studyDao.getAllFormulasFlow().firstOrNull() ?: emptyList()
        if (currentFormulas.isEmpty()) {
            val defaultFormulas = listOf(
                Formula(
                    subject = "Maths",
                    title = "Quadratic Formula",
                    formulaExpression = "x = [-b ± √(b² - 4ac)] / 2a",
                    explanation = "Solves any quadratic equation in the standard form ax² + bx + c = 0."
                ),
                Formula(
                    subject = "Maths",
                    title = "Trigonometric Identity",
                    formulaExpression = "sin²θ + cos²θ = 1",
                    explanation = "The fundamental Pythagorean identity relating sine and cosine functions."
                ),
                Formula(
                    subject = "Physics",
                    title = "Mirror Formula",
                    formulaExpression = "1/f = 1/v + 1/u",
                    explanation = "Relates focal length (f), image distance (v), and object distance (u) for spherical mirrors."
                ),
                Formula(
                    subject = "Physics",
                    title = "Ohm's Law",
                    formulaExpression = "V = I * R",
                    explanation = "Voltage (V) is directly proportional to current (I) times electric resistance (R)."
                ),
                Formula(
                    subject = "Chemistry",
                    title = "pH Equation",
                    formulaExpression = "pH = -log₁₀[H⁺]",
                    explanation = "Defines the pH value as the negative logarithm of hydronium ion concentration in a solution."
                ),
                Formula(
                    subject = "Chemistry",
                    title = "Ideal Gas Law",
                    formulaExpression = "P * V = n * R * T",
                    explanation = "Relates pressure (P), volume (V), moles status (n), gas constant (R), and absolute temperature (T)."
                )
            )
            studyDao.insertFormulas(defaultFormulas)
        }

        // 3. Populate Numericals with Step-by-Step guides
        val currentNumericals = studyDao.getAllNumericalsFlow().firstOrNull() ?: emptyList()
        if (currentNumericals.isEmpty()) {
            val defaultNumericals = listOf(
                NumericalProblem(
                    subject = "Physics",
                    topic = "Mirror Formula: Concave Mirror",
                    question = "An object of size 5 cm is placed 20 cm in front of a concave mirror of focal length 15 cm. Find the position of the image and its nature.",
                    stepListJson = "Given: Object height h = +5 cm, Object distance u = -20 cm (Cartesian sign convention), Focal length f = -15 cm (Concave mirror has negative f).||Use Mirror Formula: 1/f = 1/v + 1/u. Substituting values: 1/(-15) = 1/v + 1/(-20).||Solving for 1/v: 1/v = 1/(-15) - 1/(-20) => 1/v = -1/15 + 1/20 = (-4 + 3) / 60 = -1/60.||So, v = -60 cm. The image is formed at a distance of 60 cm in front of the mirror. It is a real and inverted image.||Calculate Magnification: m = -v/u = -(-60)/(-20) = -3. Height of image h' = m * h = -3 * 5 = -15 cm. The image is magnified three times.",
                    finalAnswer = "v = -60 cm (Real & Inverted, size = 15 cm)"
                ),
                NumericalProblem(
                    subject = "Physics",
                    topic = "Electricity: Parallel Resistance",
                    question = "Two resistors of 10 Ω and 15 Ω are connected in parallel to a 12V battery. Determine the equivalent resistance and the total current flowing through the circuit.",
                    stepListJson = "Given: Resistance R₁ = 10 Ω, Resistance R₂ = 15 Ω, Source Voltage V = 12 V.||Use Parallel Combination Formula: 1/Rp = 1/R₁ + 1/R₂. Or Rp = (R₁ * R₂) / (R₁ + R₂).||Substituting values: Rp = (10 * 15) / (10 + 15) = 150 / 25 = 6 Ω.||Calculate Total Circuit Current using Ohm's Law (I = V / Rp): I = 12 V / 6 Ω = 2 A.",
                    finalAnswer = "Equivalent Resistance = 6 Ω, Current = 2 A"
                ),
                NumericalProblem(
                    subject = "Chemistry",
                    topic = "pH of a Acidic Solution",
                    question = "Calculate the pH of a 1.0 × 10⁻³ M solution of Hydrochloric Acid (HCl).",
                    stepListJson = "Identify Acid Dissociation: HCl is a strong acid and dissociates completely in water: HCl → H⁺ + Cl⁻. Therefore, [H⁺] concentration is equal to HCl concentration: [H⁺] = 1.0 × 10⁻³ M.||Apply pH Formula: pH = -log₁₀[H⁺].||Substitute values: pH = -log₁₀(1.0 × 10⁻³) = -(-3) * log₁₀(10).||Since log₁₀(10) = 1, we get: pH = 3.",
                    finalAnswer = "pH = 3.0 (Strongly Acidic)"
                ),
                NumericalProblem(
                    subject = "Chemistry",
                    topic = "Stoichiometry & Mole Calculations",
                    question = "How many grams of Carbon Dioxide (CO₂) are produced when 24 grams of pure Carbon (C) is burned completely in oxygen?",
                    stepListJson = "Write the Balanced Chemical Equation: C + O₂ → CO₂. Mass of 1 mole of C = 12g, Mass of 1 mole of CO₂ = 12 + 2*16 = 44g.||Convert starting Carbon to Moles: n(Carbon) = Giver mass / Molar mass = 24g / 12g/mol = 2 moles.||Use mole ratios from the equation: 1 mole of C reacts to produce 1 mole of CO₂. Therefore, 2 moles of C will produce 2 moles of CO₂.||Convert moles of CO₂ back to grams: Mass of CO₂ = moles * molar mass = 2 * 44g/mol = 88 grams.",
                    finalAnswer = "88 grams of CO₂"
                )
            )
            studyDao.insertNumericals(defaultFormRules)
        }
    }

    private val defaultFormRules = listOf(
        NumericalProblem(
            subject = "Physics",
            topic = "Mirror Formula: Concave Mirror",
            question = "An object of size 5 cm is placed 20 cm in front of a concave mirror of focal length 15 cm. Find the position of the image and its nature.",
            stepListJson = "Given: Object height h = +5 cm, Object distance u = -20 cm (Cartesian sign convention), Focal length f = -15 cm (Concave mirror has negative f).||Use Mirror Formula: 1/f = 1/v + 1/u. Substituting values: 1/(-15) = 1/v + 1/(-20).||Solving for 1/v: 1/v = 1/(-15) - 1/(-20) => 1/v = -1/15 + 1/20 = (-4 + 3) / 60 = -1/60.||So, v = -60 cm. The image is formed at a distance of 60 cm in front of the mirror. It is a real and inverted image.||Calculate Magnification: m = -v/u = -(-60)/(-20) = -3. Height of image h' = m * h = -3 * 5 = -15 cm. The image is magnified three times.",
            finalAnswer = "v = -60 cm (Real & Inverted, size = 15 cm)"
        ),
        NumericalProblem(
            subject = "Physics",
            topic = "Electricity: Parallel Resistance",
            question = "Two resistors of 10 Ω and 15 Ω are connected in parallel to a 12V battery. Determine the equivalent resistance and the total current flowing through the circuit.",
            stepListJson = "Given: Resistance R₁ = 10 Ω, Resistance R₂ = 15 Ω, Source Voltage V = 12 V.||Use Parallel Combination Formula: 1/Rp = 1/R₁ + 1/R₂. Or Rp = (R₁ * R₂) / (R₁ + R₂).||Substituting values: Rp = (10 * 15) / (10 + 15) = 150 / 25 = 6 Ω.||Calculate Total Circuit Current using Ohm's Law (I = V / Rp): I = 12 V / 6 Ω = 2 A.",
            finalAnswer = "Equivalent Resistance = 6 Ω, Current = 2 A"
        ),
        NumericalProblem(
            subject = "Chemistry",
            topic = "pH of an Acidic Solution",
            question = "Calculate the pH of a 1.0 × 10⁻³ M solution of Hydrochloric Acid (HCl).",
            stepListJson = "Identify Acid Dissociation: HCl is a strong acid and dissociates completely in water: HCl → H⁺ + Cl⁻. Therefore, [H⁺] concentration is equal to HCl concentration: [H⁺] = 1.0 × 10⁻³ M.||Apply pH Formula: pH = -log₁₀[H⁺].||Substitute values: pH = -log₁₀(1.0 × 10⁻³) = -(-3) * log₁₀(10).||Since log₁₀(10) = 1, we get: pH = 3.",
            finalAnswer = "pH = 3.0 (Strongly Acidic)"
        ),
        NumericalProblem(
            subject = "Chemistry",
            topic = "Stoichiometry & Mole Calculations",
            question = "How many grams of Carbon Dioxide (CO₂) are produced when 24 grams of pure Carbon (C) is burned completely in oxygen?",
            stepListJson = "Write the Balanced Chemical Equation: C + O₂ → CO₂. Mass of 1 mole of C = 12g, Mass of 1 mole of CO₂ = 12 + 2*16 = 44g.||Convert starting Carbon to Moles: n(Carbon) = Given mass / Molar mass = 24g / 12g/mol = 2 moles.||Use mole ratios from the equation: 1 mole of C reacts to produce 1 mole of CO₂. Therefore, 2 moles of C will produce 2 moles of CO₂.||Convert moles of CO₂ back to grams: Mass of CO₂ = moles * molar mass = 2 * 44g/mol = 88 grams.",
            finalAnswer = "88 grams of CO₂"
        )
    )
}
