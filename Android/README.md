# Documentation de l'Application Android de Communication UDP

## Aperçu
Cette application Android est conçue pour communiquer avec un périphérique distant en utilisant le protocole UDP (User Datagram Protocol). Elle affiche les valeurs de température et de luminosité reçues du périphérique distant et permet aux utilisateurs d'envoyer une commande à ce périphérique.

## Fonctionnalités
- Affichage des valeurs de température et de luminosité dans un RecyclerView.
- Possibilité de faire glisser et réorganiser les éléments dans le RecyclerView.
- Envoi d'une commande au périphérique distant pour demander les valeurs de température et de luminosité.
- Mise à jour dynamique de l'interface utilisateur avec les valeurs reçues.

## Classes

### MainActivity
- L'activité principale qui initialise les composants de l'interface utilisateur, configure le RecyclerView et gère la communication UDP.
- Implémente `View.OnClickListener` pour gérer les clics sur les boutons.

### DragItemTouchHelperCallback
- Une classe interne étendant `ItemTouchHelper.Callback` pour permettre le glissement et la réorganisation des éléments dans le RecyclerView.

## Variables Membres

### `items`
- Liste des valeurs de température et de luminosité représentées sous forme de paires clé-valeur.

### `adapter`
- Adaptateur personnalisé pour le RecyclerView pour gérer les données.

### `queue`
- Une file d'attente bloquante pour la communication entre les threads.

### `IP`
- L'adresse IP du périphérique distant.

### `PORT`
- Le numéro de port UDP pour la communication.

### `address`
- Un objet représentant l'adresse résolue du périphérique distant.

### `UDPSocket`
- DatagramSocket pour la communication UDP.

## Méthodes

### `onCreate(Bundle savedInstanceState)`
- Initialise les composants de l'interface utilisateur, configure le RecyclerView et démarre les threads de communication UDP.

### `onClick(View v)`
- Gère les événements de clic sur les boutons.
- Met à jour l'adresse IP si elle est saisie et envoie une commande au périphérique distant.

### `DragItemTouchHelperCallback`
- Gère le glissement et la réorganisation des éléments dans le RecyclerView.

### `run()`
- S'exécute dans des threads distincts pour l'envoi de commandes et la réception de données via UDP.
- Envoie la commande "getValues()" au périphérique distant pour demander les valeurs de température et de luminosité.
- Analyse les données JSON reçues et met à jour l'interface utilisateur avec les valeurs de température et de luminosité.

## Composants de l'Interface Utilisateur
- RecyclerView (`recycler_view`) : Affiche les valeurs de température et de luminosité.
- Bouton (`button`) : Déclenche l'envoi d'une commande au périphérique distant.
- TextInputEditText (`textInputEditText3`) : Permet aux utilisateurs de saisir l'adresse IP du périphérique distant.
- TextViews (`textView7`, `textView8`) : Affichent les valeurs de température et de luminosité.

## Dépendances
- Bibliothèques androidx pour les composants de l'interface utilisateur et le RecyclerView.
- Bibliothèque Material Components de Google pour la conception de l'interface utilisateur.

## Utilisation
1. Lancez l'application.
2. Saisissez l'adresse IP du périphérique distant dans TextInputEditText.
3. Cliquez sur le bouton pour envoyer une commande au périphérique distant.
4. Visualisez et réorganisez les valeurs de température et de luminosité dans le RecyclerView.

**Remarque :** Remplacez l'adresse IP par défaut par l'adresse IP du périphérique cible.

## Protocole de Communication
- Commandes :
  - "getValues()" : Demande les valeurs de température et de luminosité.
  - Chaîne de deux caractères : Commande spécifique basée sur les premiers caractères des clés de température et de luminosité.

## Gestion des Erreurs
- Les exceptions sont journalisées à des fins de débogage.
- L'application peut ne pas gérer toutes les erreurs potentielles dans un environnement de production. Une gestion supplémentaire des erreurs peut être nécessaire.

## Licence
Cette application est fournie telle quelle sous une licence open-source. Consultez le dépôt du projet pour plus de détails.

**Remarque :** Cette documentation donne un aperçu du code. Des détails supplémentaires, des optimisations et des améliorations peuvent être nécessaires en fonction des exigences spécifiques du projet.
