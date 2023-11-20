# Objet Connecter

## Composition

Le projet comprend :
- L'affichage sur un ecran des valeurs temperature et luminositer des capteurs externes
- Mise au format json des données du capteur
- Le chiffrement des données
- La reception et dechiffrement de l'ordre d'affichage depuis l'app mobile
- La modification de l'affichage en fonction de la reception et de si le receiver me correspond

## Utilisation

make cbi

## Librairie

- tiny_aes -> chiffrement
- ArduinoJson -> json decode lors de la reception

## Participants au code

Lazare CHEVEREAU
Mathis MALEK
