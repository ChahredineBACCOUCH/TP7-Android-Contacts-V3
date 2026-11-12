# TP Android — Gestion des contacts (Version 3)

Fragments · RecyclerView · Filtrage · Google Maps — Kotlin, interfaces XML.

## Architecture

| Composant | Rôle |
|---|---|
| `MainActivity` | activité unique : `MaterialToolbar` + `FragmentContainerView`, flèche de retour selon la pile |
| `ListeContactsFragment` | recherche + `RecyclerView` (`ListAdapter` / `DiffUtil` / ViewHolder) |
| `DetailContactFragment` | fonctionnalités V2 + latitude / longitude + « Voir sur la carte » |
| `CarteContactFragment` | `SupportMapFragment`, carte centrée, marqueur au nom du contact |
| `ContactViewModel` | ViewModel **partagé** (`activityViewModels`) : contacts en mémoire, `LiveData` |

Navigation : `replace()` + `addToBackStack()` ; l'**identifiant** du contact est passé par `Bundle` (`nouvelleInstance(id)`). Après enregistrement, le ViewModel met à jour la `LiveData` et la liste s'actualise d'elle-même. Le fragment initial n'est ajouté que si `savedInstanceState == null` (pas de doublon à la rotation).

## Filtrage

Champ de recherche au-dessus de la liste, filtrage à chaque frappe (`doAfterTextChanged`), insensible à la casse, sur **nom, prénom et téléphone** (espaces du numéro ignorés). Message « Aucun contact ne correspond » si la liste est vide. L'adapter reçoit la liste filtrée et renvoie le `Contact` cliqué : aucune erreur d'indice.

## Google Maps — clé API

La clé n'est **jamais** dans le dépôt. Elle est lue dans `local.properties` (exclu par `.gitignore`) et injectée dans le manifeste par `manifestPlaceholders` :

```properties
# local.properties
MAPS_API_KEY=votre_cle_api
```

Voir [`local.properties.example`](local.properties.example). Sans clé, l'écran carte affiche un message explicatif au lieu d'une carte vide. Pour la clé : projet Google Cloud → activer *Maps SDK for Android* → créer une clé restreinte au package `com.example.contactsv3`.

## Captures

| Liste | Filtre | Aucun résultat | Détail | Carte sans position |
|---|---|---|---|---|
| <img src="captures/01_liste_complete.png" width="150"> | <img src="captures/02_filtre_insensible_casse.png" width="150"> | <img src="captures/04_aucun_resultat.png" width="150"> | <img src="captures/06_detail_contact_filtre.png" width="150"> | <img src="captures/09_carte_sans_position.png" width="150"> |

Toutes les captures : [`captures/`](captures) · Compte rendu : [`compte-rendu/compte_rendu.pdf`](compte-rendu/compte_rendu.pdf)

> La capture de la carte avec marqueur nécessite une clé Google Maps personnelle : sans clé, la capture [`07_carte.png`](captures/07_carte.png) montre le message prévu.
