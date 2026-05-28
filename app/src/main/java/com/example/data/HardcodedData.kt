package com.example.data

object HardcodedData {
    val yearOrder = listOf(
        "Licence 1", "Licence 2", "Licence 3 SI",
        "Master 1 GSI", "Master 1 ISIL", "Master 1 IA",
        "Master 2 GSI", "Master 2 ISIL", "Master 2 IA"
    )

    val yearIdMap = mapOf(
        "Licence 1" to "L1", "Licence 2" to "L2", "Licence 3 SI" to "L3",
        "Master 1 GSI" to "M1_GSI", "Master 1 ISIL" to "M1_ISIL", "Master 1 IA" to "M1_IA",
        "Master 2 GSI" to "M2_GSI", "Master 2 ISIL" to "M2_ISIL", "Master 2 IA" to "M2_IA"
    )

    val semesterMap = mapOf(
        "S01" to 1, "S02" to 2, "S03" to 1, "S04" to 2,
        "S05" to 1, "S06" to 2, "S07" to 1, "S08" to 2, "S09" to 1
    )

    val categoryTypes = mapOf(
        "Cours" to "course", "Exams" to "exams",
        "Résumé" to "resume", "TDs & TPs" to "TD&TP", "Tests" to "tests"
    )

    private val yearDescriptions = mapOf(
        "Licence 1" to "First year. Foundational programming, algebra, analysis, and machinery.",
        "Licence 2" to "Second year. Advanced databases, operating systems, and object modeling.",
        "Licence 3 SI" to "Third year. Software engineering, compilation, networks, information systems.",
        "Master 1 GSI" to "M1 GSI. IT project management, governance, enterprise architecture.",
        "Master 1 ISIL" to "M1 ISIL. Software engineering, security, distributed systems.",
        "Master 1 IA" to "M1 IA. AI, machine learning, intelligent systems.",
        "Master 2 GSI" to "M2 GSI. Advanced IT governance and strategic management.",
        "Master 2 ISIL" to "M2 ISIL. Advanced software engineering and cybersecurity.",
        "Master 2 IA" to "M2 IA. Deep learning, NLP, and advanced AI."
    )

    fun getYearDescription(name: String): String = yearDescriptions[name] ?: ""
    fun getYearId(name: String): String = yearIdMap[name] ?: name
    fun getSemester(semKey: String): Int? = semesterMap[semKey]

    fun getGlobalSemester(yearName: String, localSemester: Int): Int {
        val yearIndex = yearOrder.indexOf(yearName).coerceAtLeast(0)
        return yearIndex * 2 + localSemester
    }

    fun getSemesterLabel(yearName: String, localSemester: Int): String {
        val yearId = getYearId(yearName)
        val globalSem = getGlobalSemester(yearName, localSemester)
        return "$yearId S$globalSem"
    }
}
