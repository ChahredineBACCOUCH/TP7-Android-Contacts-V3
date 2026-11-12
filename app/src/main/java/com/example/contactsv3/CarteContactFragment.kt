package com.example.contactsv3

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/** Affiche la position GPS d'un contact sur Google Maps, avec un marqueur portant son nom. */
class CarteContactFragment : Fragment(R.layout.fragment_carte_contact) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val nom = args.getString(ARG_NOM).orEmpty()
        val latitude = if (args.containsKey(ARG_LAT)) args.getDouble(ARG_LAT) else null
        val longitude = if (args.containsKey(ARG_LON)) args.getDouble(ARG_LON) else null

        val txtMessage = view.findViewById<TextView>(R.id.txtMessageCarte)
        val txtCoordonnees = view.findViewById<TextView>(R.id.txtCoordonnees)
        val conteneurCarte = view.findViewById<View>(R.id.carte)

        // Coordonnées absentes ou invalides : message explicite à la place de la carte
        if (latitude == null || longitude == null || latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
            afficherMessage(txtMessage, conteneurCarte, getString(R.string.msg_aucune_position, nom))
            txtCoordonnees.visibility = View.GONE
            return
        }
        txtCoordonnees.text = getString(R.string.coordonnees, nom, latitude, longitude)

        // Clé API absente de local.properties : la carte ne pourrait pas se charger
        if (cleApiManquante()) {
            afficherMessage(txtMessage, conteneurCarte, getString(R.string.msg_cle_api_manquante))
            return
        }

        val position = LatLng(latitude, longitude)
        val carte = childFragmentManager.findFragmentById(R.id.carte) as SupportMapFragment
        carte.getMapAsync { googleMap ->
            // Marqueur portant le nom du contact, carte centrée sur sa position
            googleMap.addMarker(MarkerOptions().position(position).title(nom))?.showInfoWindow()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM))
            googleMap.uiSettings.isZoomControlsEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.titre_carte)
    }

    private fun afficherMessage(txtMessage: TextView, conteneurCarte: View, message: String) {
        txtMessage.text = message
        txtMessage.visibility = View.VISIBLE
        conteneurCarte.visibility = View.GONE
    }

    /** Lit la clé injectée dans le manifeste depuis local.properties (MAPS_API_KEY). */
    private fun cleApiManquante(): Boolean {
        val contexte = requireContext()
        val infos = contexte.packageManager.getApplicationInfo(contexte.packageName, PackageManager.GET_META_DATA)
        return infos.metaData?.getString("com.google.android.geo.API_KEY").isNullOrBlank()
    }

    companion object {
        private const val ARG_NOM = "nom"
        private const val ARG_LAT = "latitude"
        private const val ARG_LON = "longitude"
        private const val ZOOM = 13f

        fun nouvelleInstance(nom: String, latitude: Double?, longitude: Double?) = CarteContactFragment().apply {
            arguments = bundleOf(ARG_NOM to nom).also { b ->
                if (latitude != null && longitude != null) {
                    b.putDouble(ARG_LAT, latitude)
                    b.putDouble(ARG_LON, longitude)
                }
            }
        }
    }
}
