package com.example.contactsv3

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** Liste des contacts avec recherche au fil de la saisie. */
class ListeContactsFragment : Fragment(R.layout.fragment_liste_contacts) {

    // ViewModel partagé avec les autres fragments (portée de l'activité)
    private val viewModel: ContactViewModel by activityViewModels()

    private lateinit var adapter: ContactAdapter
    private lateinit var txtAucunResultat: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtAucunResultat = view.findViewById(R.id.txtAucunResultat)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerContacts)
        adapter = ContactAdapter { contact -> ouvrirDetail(contact.id) }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Filtrage au fur et à mesure de la saisie, sans bouton de validation
        val etRecherche = view.findViewById<EditText>(R.id.etRecherche)
        etRecherche.doAfterTextChanged { texte ->
            viewModel.filtre = texte?.toString().orEmpty()
            afficherListe()
        }

        // Toute modification enregistrée dans le détail actualise immédiatement la liste
        viewModel.contacts.observe(viewLifecycleOwner) { afficherListe() }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.titre_liste)
    }

    /** Applique le filtre courant et affiche un message si aucun contact ne correspond. */
    private fun afficherListe() {
        val resultat = ContactViewModel.filtrer(viewModel.contacts.value.orEmpty(), viewModel.filtre)
        adapter.submitList(resultat)
        txtAucunResultat.visibility = if (resultat.isEmpty()) View.VISIBLE else View.GONE
    }

    /** Navigation vers le détail : seul l'identifiant du contact est transmis (Bundle). */
    private fun ouvrirDetail(id: Long) {
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, DetailContactFragment.nouvelleInstance(id))
            addToBackStack("detail")
        }
    }
}
