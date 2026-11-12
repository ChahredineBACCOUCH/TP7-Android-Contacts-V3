package com.example.contactsv3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter de la RecyclerView. Il reçoit la liste DÉJÀ filtrée : le clic renvoie le Contact
 * de la ligne (et donc son id), ce qui évite toute erreur d'indice après filtrage.
 */
class ContactAdapter(private val onClic: (Contact) -> Unit) :
    ListAdapter<Contact, ContactAdapter.ContactViewHolder>(Comparateur) {

    /** ViewHolder : garde les références vers les vues d'une ligne. */
    class ContactViewHolder(vue: View) : RecyclerView.ViewHolder(vue) {
        val photo: ImageView = vue.findViewById(R.id.imgPhoto)
        val nom: TextView = vue.findViewById(R.id.txtNomComplet)
        val telephone: TextView = vue.findViewById(R.id.txtTelephone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val vue = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(vue)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.nom.text = holder.itemView.context.getString(R.string.nom_complet, contact.prenom, contact.nom)
        holder.telephone.text = contact.telephone
        PhotoUtils.afficher(holder.photo, contact.photo, tailleMax = 128)
        holder.itemView.setOnClickListener { onClic(contact) }
    }

    /** DiffUtil : ne redessine que les lignes réellement modifiées. */
    private object Comparateur : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(ancien: Contact, nouveau: Contact) = ancien.id == nouveau.id
        override fun areContentsTheSame(ancien: Contact, nouveau: Contact) = ancien == nouveau
    }
}
