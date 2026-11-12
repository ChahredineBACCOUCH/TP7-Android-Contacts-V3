package com.example.contactsv3

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import java.io.File

/** Consultation, modification et actions (appel, e-mail, photo, carte) d'un contact. */
class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    private val viewModel: ContactViewModel by activityViewModels()

    private var contact: Contact? = null

    private lateinit var imgPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    /** Photo affichée (pas encore enregistrée tant qu'on n'a pas cliqué sur Enregistrer). */
    private var photoCourante: String? = null

    /** Fichier en attente de la photo prise par la caméra. */
    private var fichierCamera: String? = null

    /** Les champs ont-ils déjà été remplis à partir du modèle ? (évite d'écraser une saisie en cours) */
    private var champsRemplis = false

    private val choisirPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) {
            toast(R.string.msg_selection_annulee)
            return@registerForActivityResult
        }
        val copie = PhotoUtils.copierDepuisGalerie(requireContext(), uri)
        if (copie == null) {
            toast(R.string.msg_photo_illisible)
        } else {
            photoCourante = copie
            PhotoUtils.afficher(imgPhoto, photoCourante)
        }
    }

    private val prendrePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { reussi ->
        val chemin = fichierCamera
        fichierCamera = null
        if (reussi && chemin != null && File(chemin).length() > 0) {
            photoCourante = chemin
            PhotoUtils.afficher(imgPhoto, photoCourante)
        } else {
            chemin?.let { File(it).delete() }
            toast(R.string.msg_photo_annulee)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            photoCourante = savedInstanceState.getString(CLE_PHOTO)
            fichierCamera = savedInstanceState.getString(CLE_FICHIER_CAMERA)
            champsRemplis = true // les EditText restaurent eux-mêmes leur texte
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Le contact est retrouvé par son identifiant transmis dans le Bundle
        val id = requireArguments().getLong(ARG_ID)
        contact = viewModel.getById(id)
        val c = contact
        if (c == null) {
            toast(R.string.msg_contact_introuvable)
            parentFragmentManager.popBackStack()
            return
        }

        imgPhoto = view.findViewById(R.id.imgPhoto)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)

        if (!champsRemplis) {
            etNom.setText(c.nom)
            etPrenom.setText(c.prenom)
            etTelephone.setText(c.telephone)
            etEmail.setText(c.email)
            etLatitude.setText(c.latitude?.toString().orEmpty())
            etLongitude.setText(c.longitude?.toString().orEmpty())
            photoCourante = c.photo
            champsRemplis = true
        }
        PhotoUtils.afficher(imgPhoto, photoCourante)

        view.findViewById<Button>(R.id.btnGalerie).setOnClickListener { ouvrirGalerie() }
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener { ouvrirCamera() }
        view.findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }
        view.findViewById<Button>(R.id.btnCarte).setOnClickListener { voirSurLaCarte() }
        view.findViewById<Button>(R.id.btnEnregistrer).setOnClickListener { enregistrer() }
        view.findViewById<Button>(R.id.btnAnnuler).setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.titre_detail)
    }

    // ---------- Photo ----------

    private fun ouvrirGalerie() {
        try {
            choisirPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_aucune_galerie)
        }
    }

    private fun ouvrirCamera() {
        val contexte = requireContext()
        if (!contexte.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            toast(R.string.msg_camera_indisponible)
            return
        }
        val fichier = PhotoUtils.nouveauFichier(contexte)
        fichierCamera = fichier.absolutePath
        try {
            prendrePhoto.launch(PhotoUtils.uriPartageable(contexte, fichier))
        } catch (e: ActivityNotFoundException) {
            fichierCamera = null
            toast(R.string.msg_camera_indisponible)
        } catch (e: SecurityException) {
            fichierCamera = null
            toast(R.string.msg_camera_refusee)
        }
    }

    // ---------- Intents implicites ----------

    private fun appeler() {
        val numero = etTelephone.text.toString().trim()
        if (numero.isEmpty()) {
            etTelephone.error = getString(R.string.erreur_obligatoire)
            return
        }
        lancer(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(numero))), R.string.msg_aucune_app_telephone)
    }

    private fun envoyerEmail() {
        val adresse = etEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(adresse).matches()) {
            etEmail.error = getString(R.string.erreur_email)
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(adresse))
        }
        lancer(intent, R.string.msg_aucune_app_email)
    }

    private fun lancer(intent: Intent, messageErreur: Int) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(messageErreur)
        }
    }

    // ---------- Coordonnées GPS ----------

    /** Résultat de la lecture des champs latitude / longitude. */
    private sealed class Position {
        data object Absente : Position()
        data class Valide(val latitude: Double, val longitude: Double) : Position()
        data object Invalide : Position()
    }

    /**
     * Valide les coordonnées saisies : les deux champs vides = aucune position ;
     * sinon les deux doivent être numériques, latitude dans [−90 ; 90], longitude dans [−180 ; 180].
     */
    private fun lirePosition(): Position {
        val texteLat = etLatitude.text.toString().trim().replace(',', '.')
        val texteLon = etLongitude.text.toString().trim().replace(',', '.')
        if (texteLat.isEmpty() && texteLon.isEmpty()) return Position.Absente

        var valide = true
        val lat = texteLat.toDoubleOrNull()
        val lon = texteLon.toDoubleOrNull()
        when {
            texteLat.isEmpty() -> { etLatitude.error = getString(R.string.erreur_coordonnee_manquante); valide = false }
            lat == null -> { etLatitude.error = getString(R.string.erreur_non_numerique); valide = false }
            lat !in -90.0..90.0 -> { etLatitude.error = getString(R.string.erreur_latitude); valide = false }
        }
        when {
            texteLon.isEmpty() -> { etLongitude.error = getString(R.string.erreur_coordonnee_manquante); valide = false }
            lon == null -> { etLongitude.error = getString(R.string.erreur_non_numerique); valide = false }
            lon !in -180.0..180.0 -> { etLongitude.error = getString(R.string.erreur_longitude); valide = false }
        }
        return if (valide && lat != null && lon != null) Position.Valide(lat, lon) else Position.Invalide
    }

    /** Ouvre CarteContactFragment avec les coordonnées actuellement saisies. */
    private fun voirSurLaCarte() {
        val position = lirePosition()
        if (position is Position.Invalide) {
            toast(R.string.msg_coordonnees_invalides)
            return
        }
        val nom = getString(R.string.nom_complet, etPrenom.text.toString().trim(), etNom.text.toString().trim())
        val fragment = when (position) {
            is Position.Valide -> CarteContactFragment.nouvelleInstance(nom, position.latitude, position.longitude)
            else -> CarteContactFragment.nouvelleInstance(nom, null, null)
        }
        parentFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, fragment)
            addToBackStack("carte")
        }
    }

    // ---------- Enregistrement ----------

    private fun enregistrer() {
        val c = contact ?: return
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        var valide = true
        if (nom.isEmpty()) { etNom.error = getString(R.string.erreur_obligatoire); valide = false }
        if (prenom.isEmpty()) { etPrenom.error = getString(R.string.erreur_obligatoire); valide = false }
        if (telephone.isEmpty()) { etTelephone.error = getString(R.string.erreur_obligatoire); valide = false }
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.erreur_obligatoire); valide = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.erreur_email); valide = false
        }
        val position = lirePosition()
        if (position is Position.Invalide) valide = false
        if (!valide) {
            toast(R.string.msg_champs_invalides)
            return
        }

        val modifie = c.copy(
            nom = nom, prenom = prenom, telephone = telephone, email = email, photo = photoCourante,
            latitude = (position as? Position.Valide)?.latitude,
            longitude = (position as? Position.Valide)?.longitude
        )
        // Le ViewModel partagé notifie la liste, qui s'actualise dès le retour
        viewModel.mettreAJour(modifie)
        toast(R.string.msg_enregistre)
        parentFragmentManager.popBackStack()
    }

    private fun toast(message: Int) = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CLE_PHOTO, photoCourante)
        outState.putString(CLE_FICHIER_CAMERA, fichierCamera)
    }

    companion object {
        private const val ARG_ID = "id_contact"
        private const val CLE_PHOTO = "photo"
        private const val CLE_FICHIER_CAMERA = "fichier_camera"

        fun nouvelleInstance(id: Long) = DetailContactFragment().apply {
            arguments = bundleOf(ARG_ID to id)
        }
    }
}
