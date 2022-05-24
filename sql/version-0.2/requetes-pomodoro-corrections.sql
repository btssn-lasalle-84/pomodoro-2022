--- Exemples et exercices d'accompagnement : déterminer les requêtes SQL

-- Liste des colonnes (kanban)

SELECT * FROM Colonne;

|idColonne|libelle|
|---|---|
|1|A faire|
|2|En cours|
|3|Terminées|

-- Liste des categories

SELECT * FROM Categorie;

|idCategorie|libelle|
|---|---|
|1|Développement|
|2|Gestion de projet|
|3|Test|

-- Lise des modes

SELECT * FROM Mode;

|idMode|libelle|
|---|---|
|1|Minuteur|
|2|Chronomètre|

-- Lise des projets

SELECT * FROM Projet;

|idProjet|titre|description|dateCreation|dateDebut|dateFin|couleur|idCategorie|actif|tempsDepense|tempsEstime|priorite|
|---|---|---|---|---|---|---|---|---|---|---|---|
|1|Pendu|Mini-projet|2022-03-01 15:31:59|NULL|NULL|NULL|1|1|0.0|0.0|0|
|2|Wordle|Mini-projet|2022-03-01 15:31:59|NULL|NULL|NULL|1|0|0.0|0.0|0|

-- Liste des tâches

SELECT * FROM Tache;

-- Liste des tâches d'un projet

SELECT Projet.titre,Tache.titre,Tache.description,Colonne.libelle FROM Tache
INNER JOIN Projet ON Projet.idProjet=Tache.idProjet
INNER JOIN Colonne ON Colonne.idColonne=Tache.idColonne
WHERE Projet.idProjet='1';

|titre|titre|description|libelle|
|---|---|---|---|
|Pendu|Planifier les tâches|Identifier et prioriser les tâches|Terminées|
|Pendu|Maquette IHM|Définir une interface Homme-Machine|En cours|
|Pendu|Classes du domaine|Réaliser le diagramme de classes du domaine|A faire|
|Pendu|Implémenter squelettes|Coder les squelettes des classes|A faire|

-- Liste des pomodoros

SELECT Tache.titre,Colonne.libelle,Pomodoro.position,Etat.libelle,Modele.nom,Modele.dureePomodoro,Modele.pauseCourte,Modele.pauseLongue,Modele.nbPomodori FROM Pomodoro
INNER JOIN Etat ON Etat.idEtat=Pomodoro.idEtat
INNER JOIN Tache ON Tache.idTache=Pomodoro.idTache
INNER JOIN Colonne ON Colonne.idColonne=Tache.idColonne
INNER JOIN Modele ON Modele.idModele=Pomodoro.idModele;

|titre|libelle|position|libelle|nom|dureePomodoro|pauseCourte|pauseLongue|nbPomodori|
|---|---|---|---|---|---|---|---|---|
|Maquette IHM|En cours|0|démarré|Classique|1500|300|1200|4|
|Classes du domaine|A faire|1|prêt|Classique|1500|300|1200|4|
|Implémenter squelettes|A faire|2|prêt|Classique|1500|300|1200|4|
