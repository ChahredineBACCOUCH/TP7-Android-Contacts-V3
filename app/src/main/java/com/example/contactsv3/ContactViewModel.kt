package com.example.contactsv3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel partagé par les fragments (portée : MainActivity).
 *
 * Il conserve la collection de contacts en mémoire, survit à la rotation de l'écran et
 * notifie automatiquement la liste (LiveData) quand un contact est enregistré dans le détail.
 */
class ContactViewModel : ViewModel() {

    private val _contacts = MutableLiveData(
        listOf(
            Contact(1, "Ben Ali", "Ahmed", "+216 22 345 678", "ahmed.benali@example.com", latitude = 36.8065, longitude = 10.1815),
            Contact(2, "Trabelsi", "Sarra", "+216 98 765 432", "sarra.trabelsi@example.com", latitude = 34.7406, longitude = 10.7603),
            Contact(3, "Gharbi", "Mohamed", "+216 55 123 456", "mohamed.gharbi@example.com", latitude = 35.8256, longitude = 10.6084),
            Contact(4, "Jaziri", "Ines", "+216 29 876 543", "ines.jaziri@example.com"),
            Contact(5, "Mansouri", "Youssef", "+216 50 111 222", "youssef.mansouri@example.com", latitude = 37.2744, longitude = 9.8739),
            Contact(6, "Chaabane", "Amira", "+216 97 333 444", "amira.chaabane@example.com"),
            Contact(7, "Bouazizi", "Karim", "+216 23 456 789", "karim.bouazizi@example.com", latitude = 33.8869, longitude = 9.5375),
            Contact(8, "Hammami", "Nour", "+216 92 000 111", "nour.hammami@example.com", latitude = 36.4000, longitude = 10.6167)
        )
    )

    /** Liste complète des contacts, observée par ListeContactsFragment. */
    val contacts: LiveData<List<Contact>> = _contacts

    /** Texte du filtre de recherche (conservé lors de la navigation). */
    var filtre: String = ""

    fun getById(id: Long): Contact? = _contacts.value?.firstOrNull { it.id == id }

    /** Remplace le contact portant le même identifiant ; la liste observée est actualisée. */
    fun mettreAJour(contact: Contact) {
        _contacts.value = _contacts.value.orEmpty().map { if (it.id == contact.id) contact else it }
    }

    companion object {
        /**
         * Filtre insensible à la casse sur le nom, le prénom et le téléphone.
         * Pour le téléphone, les espaces sont ignorés (« 22345 » trouve « +216 22 345 678 »).
         */
        fun filtrer(contacts: List<Contact>, texte: String): List<Contact> {
            val recherche = texte.trim()
            if (recherche.isEmpty()) return contacts
            val rechercheChiffres = recherche.replace(" ", "")
            return contacts.filter { c ->
                c.nom.contains(recherche, ignoreCase = true) ||
                    c.prenom.contains(recherche, ignoreCase = true) ||
                    "${c.prenom} ${c.nom}".contains(recherche, ignoreCase = true) ||
                    c.telephone.replace(" ", "").contains(rechercheChiffres, ignoreCase = true)
            }
        }
    }
}
