--- Exemples et exercices d'accompagnement : déterminer les requêtes SQL

-- Liste des colonnes (kanban)

SELECT * FROM Colonne;

|idColonne|libelle|
|---|---|
|1|A faire|
|2|En cours|
|3|Terminées|

-- Liste des categories


-- Lise des modes



-- Lise des projets

SELECT * FROM Projet;

|idProjet|titre|description|dateCreation|dateDebut|dateFin|couleur|idCategorie|actif|tempsDepense|tempsEstime|priorite|
|---|---|---|---|---|---|---|---|---|---|---|---|
|1|Pendu|Mini-projet|2022-03-01 15:31:59|NULL|NULL|NULL|1|1|0.0|0.0|0|
|2|Wordle|Mini-projet|2022-03-01 15:31:59|NULL|NULL|NULL|1|0|0.0|0.0|0|

-- Liste des tâches



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



|titre|libelle|position|libelle|nom|dureePomodoro|pauseCourte|pauseLongue|nbPomodori|
|---|---|---|---|---|---|---|---|---|
|Maquette IHM|En cours|0|démarré|Classique|1500|300|1200|4|
|Classes du domaine|A faire|1|prêt|Classique|1500|300|1200|4|
|Implémenter squelettes|A faire|2|prêt|Classique|1500|300|1200|4|
