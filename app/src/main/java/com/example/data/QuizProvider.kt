package com.example.data

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

object QuizProvider {
    fun getChaptersForSubject(subject: String): List<String> {
        return when (subject) {
            "Science" -> listOf("Chemical Reactions and Equations", "Light Reflection and Refraction", "Life Processes")
            "Maths" -> listOf("Arithmetic Progressions", "Trigonometry", "Quadratic Equations")
            "SST" -> listOf("Rise of Nationalism in Europe", "Federalism", "Resources and Development")
            else -> emptyList()
        }
    }

    fun getQuestions(subject: String, chapter: String): List<QuizQuestion> {
        return when (subject) {
            "Science" -> when (chapter) {
                "Chemical Reactions and Equations" -> listOf(
                    QuizQuestion(
                        1,
                        "What is the chemical formula of rust?",
                        listOf("Fe₂O₃ · xH₂O", "Fe₃O₄", "FeO", "Fe(OH)₃"),
                        0,
                        "Rust is hydrated ferric oxide, represented as Fe₂O₃ · xH₂O, formed due to oxidation of iron in moisture."
                    ),
                    QuizQuestion(
                        2,
                        "Which gas is usually liberated when an acid reacts with a metal?",
                        listOf("Oxygen", "Hydrogen", "Carbon Dioxide", "Nitrogen"),
                        1,
                        "Acids react with active metals to displace hydrogen gas (e.g., Zn + H₂SO₄ → ZnSO₄ + H₂↑)."
                    ),
                    QuizQuestion(
                        3,
                        "What kind of reaction is: 2H₂ + O₂ → 2H₂O?",
                        listOf("Combination", "Decomposition", "Single Displacement", "Double Displacement"),
                        0,
                        "This is a combination reaction where two reactants merge to form a single product."
                    )
                )
                "Light Reflection and Refraction" -> listOf(
                    QuizQuestion(
                        1,
                        "A spherical mirror whose reflecting surface is curved inwards is called a _______.",
                        listOf("Convex mirror", "Concave mirror", "Plane mirror", "Biconvex mirror"),
                        1,
                        "A concave mirror is curved inwards. It can form both real/inverted or virtual/erect images depending on position."
                    ),
                    QuizQuestion(
                        2,
                        "The refractive index of water is 1.33. What does this indicate?",
                        listOf("Speed of light is faster in water than vacuum", "Speed of light is 1.33 times slower in water than vacuum", "Water reflects 1.33% light", "Focal length of water lens"),
                        1,
                        "Refractive index (n) = c/v. A value of 1.33 means light travels 1.33 times slower in water than in a vacuum."
                    ),
                    QuizQuestion(
                        3,
                        "Where should an object be placed in front of a convex lens to get a real image of same size?",
                        listOf("At Focus (F)", "At Twice of Focal Length (2F)", "Between Optical Center & Focus", "At Infinity"),
                        1,
                        "Placing an object at 2F of a convex lens produces a real, inverted image of the same size as the object at 2F on the other side."
                    )
                )
                else -> getFallbackQuestions(subject, chapter)
            }
            "Maths" -> when (chapter) {
                "Arithmetic Progressions" -> listOf(
                    QuizQuestion(
                        1,
                        "In an Arithmetic Progression, if first term a = 5, and common difference d = 3, what is the 10th term?",
                        listOf("27", "32", "35", "30"),
                        1,
                        "The nth term formula is: an = a + (n - 1)d. So, a10 = 5 + (10 - 1)*3 = 5 + 27 = 32."
                    ),
                    QuizQuestion(
                        2,
                        "What is the common difference 'd' in the AP: 12, 7, 2, -3...",
                        listOf("5", "4", "-5", "-4"),
                        2,
                        "Common difference d is determined by subtracting consecutive terms: 7 - 12 = -5."
                    ),
                    QuizQuestion(
                        3,
                        "The sum of first 5 terms of an AP with first term 2 and last term 10 is:",
                        listOf("30", "25", "60", "20"),
                        0,
                        "Using Sn = n/2 * (a + l), S5 = 5/2 * (2 + 10) = 5/2 * 12 = 30."
                    )
                )
                "Trigonometry" -> listOf(
                    QuizQuestion(
                        1,
                        "What is the value of Sec² θ - Tan² θ?",
                        listOf("0", "1", "2", "-1"),
                        1,
                        "Sec² θ - Tan² θ = 1 is one of the fundamental Pythagorean trigonometric identities in Maths."
                    ),
                    QuizQuestion(
                        2,
                        "If Sin A = 3/5, what is the value of Cos A?",
                        listOf("4/5", "3/4", "5/3", "4/3"),
                        0,
                        "Since Sin² A + Cos² A = 1, Cos A = √(1 - 9/25) = √(16/25) = 4/5."
                    ),
                    QuizQuestion(
                        3,
                        "The value of Tan 45° is equal to:",
                        listOf("0", "1/2", "1", "√3"),
                        2,
                        "Standard trigonometric ratio states Tan 45° = Sin 45° / Cos 45° = (1/√2) / (1/√2) = 1."
                    )
                )
                else -> getFallbackQuestions(subject, chapter)
            }
            "SST" -> when (chapter) {
                "Rise of Nationalism in Europe" -> listOf(
                    QuizQuestion(
                        1,
                        "Who was Giuseppe Mazzini?",
                        listOf("An Italian revolutionary", "A French painter", "A German chancellor", "A British king"),
                        0,
                        "Giuseppe Mazzini was an Italian revolutionary who founded secretly 'Young Italy' and worked toward unification."
                    ),
                    QuizQuestion(
                        2,
                        "What did the Treaty of Constantinople (1832) recognize?",
                        listOf("Germany as independent", "Greece as an independent nation", "Italy as unified", "France as a Republic"),
                        1,
                        "The Treaty of Constantinople of 1832 recognized Greece as an independent sovereign nation."
                    ),
                    QuizQuestion(
                        3,
                        "What is the depiction on Frédéric Sorrieu's famous 1848 painting?",
                        listOf("A world of democratic & social republics", "A view of the French revolution", "A battlescape of Waterloo", "The signing of Magna Carta"),
                        0,
                        "Sorrieu's series of prints symbolized a utopian world made up of 'democratic and social republics'."
                    )
                )
                "Federalism" -> listOf(
                    QuizQuestion(
                        1,
                        "Which of the following is an example of a 'Coming Together' federation?",
                        listOf("India", "Spain", "Belgium", "USA"),
                        3,
                        "The USA is a 'Coming Together' federation where independent states pool sovereignty to form a larger union."
                    ),
                    QuizQuestion(
                        2,
                        "Subjects of national importance like Defence and Banking come under which list in India?",
                        listOf("State List", "Union List", "Concurrent List", "Residuary Subjects"),
                        1,
                        "Defence, Foreign Affairs, and Banking come under the Union List, regulated solely by the Central Parliament."
                    ),
                    QuizQuestion(
                        3,
                        "What is a concurrent list subject in Indian federalism?",
                        listOf("Agriculture", "Police", "Education", "Space Research"),
                        2,
                        "Education, Forest, Trade Unions, and Marriage are Concurrent list subjects on which both State and Union can legislate."
                    )
                )
                else -> getFallbackQuestions(subject, chapter)
            }
            else -> getFallbackQuestions(subject, chapter)
        }
    }

    private fun getFallbackQuestions(subject: String, chapter: String): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                1,
                "Which term refers to this fundamental topic in $subject's chapter: $chapter?",
                listOf("Concept Definition", "Applied Formulation", "Historical Paradigm", "Systematic Nomenclature"),
                0,
                "In learning $chapter, understanding the exact context and terminology forms the building block of further chapters."
            ),
            QuizQuestion(
                2,
                "True or False: The principles discussed in $chapter play a vital role in secondary exam preparation.",
                listOf("True", "False", "Partially True", "Depends on Board Syllabus"),
                0,
                "Yes! Board syllabi consistently prioritize standard conceptual definitions and numerical derivations from $chapter."
            )
        )
    }
}
