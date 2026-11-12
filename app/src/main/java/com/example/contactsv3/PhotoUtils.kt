package com.example.contactsv3

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.FileProvider
import java.io.File

/** Outils de gestion des photos des contacts (stockage interne de l'application). */
object PhotoUtils {

    private fun dossierPhotos(context: Context) =
        File(context.filesDir, "photos").apply { mkdirs() }

    /** Crée un fichier vide destiné à recevoir une photo prise par l'appareil photo. */
    fun nouveauFichier(context: Context): File =
        File(dossierPhotos(context), "photo_${System.currentTimeMillis()}.jpg")

    /** URI partageable (FileProvider) permettant à l'application Caméra d'écrire dans [fichier]. */
    fun uriPartageable(context: Context, fichier: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fichier)

    /**
     * Copie l'image choisie dans la galerie vers le stockage interne : l'URI renvoyée par
     * le sélecteur n'est lisible que temporairement, la copie reste disponible.
     * @return le chemin de la copie, ou null si la lecture a échoué
     */
    fun copierDepuisGalerie(context: Context, uri: Uri): String? {
        return try {
            val destination = nouveauFichier(context)
            val entree = context.contentResolver.openInputStream(uri) ?: return null
            entree.use { destination.outputStream().use { sortie -> it.copyTo(sortie) } }
            destination.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** Affiche la photo [chemin] dans [imageView] (réduite pour économiser la mémoire), ou l'avatar par défaut. */
    fun afficher(imageView: ImageView, chemin: String?, tailleMax: Int = 512) {
        val fichier = chemin?.let { File(it) }
        if (fichier == null || !fichier.exists() || fichier.length() == 0L) {
            imageView.setImageResource(R.drawable.ic_avatar)
            return
        }
        // 1er passage : lecture des dimensions seulement, pour calculer le facteur de réduction
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(fichier.absolutePath, options)
        var facteur = 1
        while (options.outWidth / (facteur * 2) >= tailleMax && options.outHeight / (facteur * 2) >= tailleMax) {
            facteur *= 2
        }
        val bitmap = BitmapFactory.decodeFile(
            fichier.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = facteur }
        )
        if (bitmap != null) imageView.setImageBitmap(bitmap) else imageView.setImageResource(R.drawable.ic_avatar)
    }
}
