# Simulateur POMODORO 2022

- [Simulateur POMODORO 2022](#simulateur-pomodoro-2022)
  - [Configuration du simulateur](#configuration-du-simulateur)
  - [Protocole](#protocole)
  - [Fonctionnement](#fonctionnement)
  - [platform.ini](#platformini)
  - [Auteur](#auteur)

Ce document présente rapidement le fonctionnement du simulateur ainsi que le protocole implémenté. Le protocole complet est disponible dans Google Drive. Actuellement, la version du protocole est la **0.2** (12 avril 2022).

![](./simulateur-pomodoro.jpg)

## Configuration du simulateur

Pour la simulation, les durées en minutes sont transformées en secondes :

```cpp
#define DUREE_POMODORO        25 //1500 // 25 minutes
#define PAUSE_COURTE_POMODORO 5  //300  // 5 minutes
#define PAUSE_LONGUE_POMODORO 20 //1200 // 20 minutes
#define NB_POMODORI           4
```

## Protocole

Les éléments de base du protocole :

```cpp
#define PERIPHERIQUE_BLUETOOTH  "pomodoro-1"
#define EN_TETE_TRAME           "#"
#define DELIMITEUR_CHAMP        "&"
#define DELIMITEURS_FIN         "\r\n"
#define DELIMITEUR_DATAS        '&'
```

Les différentes types de trame :

```cpp
enum TypeTrame
{
  Inconnu = -1,
  POMODORO, USER, BELL, TASK, REST, STOP, RESET, WAIT, DARN, HEARTBEAT, STATE, ERROR, ACK,
//"P",      "U",  "B",  "T",  "R",  "S",  "X",   "W",  "D",  "H",       "E",   "F",   "A"
  NB_TRAMES
};
```

Le format général des trames est le suivant : `#{TypeTrame}[&...]\r\n`

Chaque trame reçue entraîne l'envoi d'une trame d'acquittement `#A\r\n` ou d'erreur `#F&{CODE}\r\n` par le simulateur.

Les codes d'erreur :

```cpp
#define ERREUR_TRAME_INCONNUE       0
#define ERREUR_TRAME_NON_SUPPORTEE  1
#define ERREUR_COMMANDE             2
#define ERREUR_PARAMETRE            3
#define ERREUR_CONFIGURATION        4
```

La trame `ETAT` précise l'état du pomodoro : `#E&{EtatPomodoro}\r\n`

Les différents états sont :

```cpp
enum EtatPomodoro
{
  EnAttente       = -1,
  Termine         =  0,
  EnCours         =  1,
  EnCourtePause   =  2,
  FinCourtePause  =  3,
  EnLonguePause   =  4,
  FinLonguePause  =  5,
  Gele            =  6,
  Reprise         =  7
};
```

La trame `T` de démarrage d'une tâche possède le format suivant : `#T&[NOM]\r\n` où il est possible de donner un nom à la tâche.

La trame `R` de démarrage d'une pause possède le format suivant : `#R&{TypePause}\r\n`

```cpp
enum TypePause
{
  Invalide = -1,
  COURTE, LONGUE,
  NB_TYPES_PAUSE
};
```

Les possibilités de configuration sont :

```
#P&duree&pauseCourte&pauseLongue&nbPomodori&autoPomodoro&autoPause&mode\r\n

#U&NOM&PRENOM\r\n

#B&ACTIVATION\r\n
```

Le champ `mode` admet deux valeurs :

```cpp
enum Mode
{
  Minuteur = 0,
  Chronometre = 1
};
```

_Remarque :_ le mode `Chronometre` n'est pas géré actuellement.

En option (on géré actuellement) : Les trames `#W\r\n` (WAIT) et `#D\r\n` (DARN) permettent respectivement de geler le temps (d'une tâche ou d'une pause) ou de le reprendre.

## Fonctionnement

Pour l'instant, le simulateur ne fonctionne qu'en mode `Minuteur`.

## platform.ini

```ini
[env:lolin32]
platform = espressif32
board = lolin32
framework = arduino
lib_deps =
  thingpulse/ESP8266 and ESP32 OLED driver for SSD1306 displays @ ^4.2.0
upload_port = /dev/ttyUSB0
upload_speed = 115200
monitor_port = /dev/ttyUSB0
monitor_speed = 115200
```

## Auteur

- Thierry Vaira <<tvaira@free.fr>>
