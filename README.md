# Le projet pomodoro-2022

- [Le projet pomodoro-2022](#le-projet-pomodoro-2022)
  - [Minuteur connecté](#minuteur-connecté)
  - [Fonctionnalités](#fonctionnalités)
  - [TODO](#todo)
  - [Historique des versions](#historique-des-versions)
  - [Auteur](#auteur)
  - [Kanban](#kanban)

La **technique Pomodoro** est une technique de gestion du temps développée par Francesco Cirillo à la fin des années 1980. Cette méthode se base sur l'usage d'un minuteur permettant de respecter des périodes de 20 minutes appelées pomodori (qui signifie en italien « tomates »).

Francesco Cirillo propose une approche se basant notamment sur un minuteur mécanique. L’idée du projet est donc de conserver un “objet concret” pour en faire un minuteur connecté.

Le système est composé :

- d’un minuteur connecté (ESP32, écran tactile, avertisseur sonore et éventuellement boutons)
- d’une tablette tactile (Android)  pour le pilotage à distance et la gestion des tâches

## Minuteur connecté 

- Version simulateur :

![](images/simulateur-pomodoro.jpg)

## Fonctionnalités

- Gérer un pomodoro (démarrer, arrêter, ...)

![](images/PomodoroActivity_TacheActive.png)

- Afficher l'état du minuteur

![](images/PomodoroActivity_PauseCourteActive.png)

![](images/PomodoroActivity_PauseLongueActive.png)

- Éditer une tâche

![](images/EditerTacheActivity_Accueil.png)

- Configurer les minuteries (la longueur des pomodoros, des pauses courtes et des pauses longues, mode manuel ou automatique)

![](images/CreerTacheActivity_ModificationTache.png)

- Dialoguer avec le minuteur connecté

## TODO

- [ ] Activer/Désactiver l'alarme sonore
- [ ] Notifier les événements
- [ ] Enregistrer le suivi des tâches (statistiques)

## Historique des versions

- Version 0.2 : 25/05/2022
- Version 0.1 : 13/04/2022

## Auteur

- Version Mobile Android : Teddy Establet <<teddyestablet84@gmail.com>>

## Kanban

[pomodoro-2022](https://github.com/btssn-lasalle-84/pomodoro-2022/projects/1)
