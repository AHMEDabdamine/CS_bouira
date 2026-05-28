package com.example.data.model

import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class FileItem(
    val id: String,
    val moduleId: String,
    val name: String,
    val type: String, // "course", "exams", "resume", "TD&TP", "tests"
    val url: String,
    val downloadUrl: String = "",
    val size: Long? = null,
    val uploadedAt: String? = null,
    val uploader: String? = null
) {
    companion object {
        fun determineCategory(name: String): String {
            val nameLower = name.lowercase(Locale.ROOT)
            
            fun matchesAny(vararg keywords: String): Boolean {
                return keywords.any { nameLower.contains(it) }
            }
            
            return when {
                // Resume
                matchesAny("resume", "résumé", "synthese", "synthèse", "fiche de revision", "fiche de révision", "revision", "révision", "cheat sheet", "recap", "récap", "condens", "revis") -> "resume"
                
                // Tests
                matchesAny("test", "quiz", "interro", "devoir", "interrogation") -> "tests"
                
                // Exams
                matchesAny("examen", "exam", "emd", "controle final", "contrôle final", "sujet e", "conjoint", "sujet-exam", "sujet_exam", "final") -> "exams"
                
                // TD / TP / exercises
                matchesAny("td", "tp", "travaux dirig", "travaux prat", "fiche td", "fiche tp", "serie td", "série td", "série tp", "serie tp", "série", "serie", "travaux", "exercice", "exo") ||
                nameLower.split(Regex("[\\s_\\-\\.\\(\\)]")).any { it == "td" || it == "tp" } -> "TD&TP"
                
                // Default is course
                else -> "course"
            }
        }
    }
}
