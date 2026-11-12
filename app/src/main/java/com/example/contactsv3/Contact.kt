package com.example.contactsv3

/**
 * Modèle d'un contact.
 *
 * @property id identifiant unique : c'est lui qui est transmis entre fragments, jamais la position
 * @property photo chemin d'une image copiée dans le stockage interne, ou null (avatar par défaut)
 * @property latitude latitude GPS (−90 à 90) ou null si aucune position n'est définie
 * @property longitude longitude GPS (−180 à 180) ou null si aucune position n'est définie
 */
data class Contact(
    val id: Long,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photo: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    /** Vrai si le contact possède une position GPS complète et valide. */
    val aUnePosition: Boolean
        get() = latitude != null && longitude != null &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0
}
