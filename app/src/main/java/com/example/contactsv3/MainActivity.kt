package com.example.contactsv3

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar

/**
 * Unique activité de l'application : elle héberge les fragments dans un FragmentContainerView
 * et affiche la barre d'application (titre + flèche de retour).
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val conteneur = findViewById<View>(R.id.fragmentContainer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val barres = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            toolbar.setPadding(barres.left, barres.top, barres.right, 0)
            conteneur.setPadding(barres.left, 0, barres.right, barres.bottom)
            insets
        }
        setSupportActionBar(toolbar)

        // Flèche de retour visible dès qu'un fragment est empilé
        supportFragmentManager.addOnBackStackChangedListener { actualiserFlecheRetour() }
        actualiserFlecheRetour()

        // Le fragment initial n'est ajouté qu'au premier lancement : après une rotation,
        // le FragmentManager restaure lui-même la pile (pas de fragments en double).
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, ListeContactsFragment())
            }
        }
    }

    private fun actualiserFlecheRetour() {
        supportActionBar?.setDisplayHomeAsUpEnabled(supportFragmentManager.backStackEntryCount > 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
