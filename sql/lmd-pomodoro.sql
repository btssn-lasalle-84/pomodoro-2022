--- LMD (langage de manipulation de données)

--- Contenu des tables (tests)

-- Table Colonne

INSERT INTO Colonne(idColonne,libelle) VALUES
(1,'A faire'),
(2,'En cours'),
(3,'Terminées');

-- Table Categorie

INSERT INTO Categorie(idCategorie,libelle) VALUES
(1,'Développement'),
(2,'Gestion de projet'),
(3,'Test');

-- Table Mode

INSERT INTO Mode(idMode,libelle) VALUES
(1,'Minuteur'),
(2,'Chronomètre');

-- Table Projet

INSERT INTO Projet (titre,description,dateCreation,idCategorie,actif) VALUES ('Pendu','Mini-projet',DATETIME('now'),1,1);
INSERT INTO Projet (titre,description,dateCreation,idCategorie,actif) VALUES ('Wordle','Mini-projet',DATETIME('now'),1,0);

-- Table Tache

INSERT INTO Tache (titre,description,dateCreation,dateDebut,dateFin,idProjet,idColonne,idCategorie,position) VALUES ('Planifier les tâches','Identifier et prioriser les tâches',DATETIME('now'),'2022-03-30 08:15:00','2022-03-30 08:40:00',1,3,1,1);
INSERT INTO Tache (titre,description,dateCreation,idProjet,idColonne,idCategorie,position) VALUES ('Maquette IHM','Définir une interface Homme-Machine',DATETIME('now'),1,2,1,1);
INSERT INTO Tache (titre,description,dateCreation,idProjet,idColonne,idCategorie,position) VALUES ('Classes du domaine','Réaliser le diagramme de classes du domaine',DATETIME('now'),1,1,1,1);
INSERT INTO Tache (titre,description,dateCreation,idProjet,idColonne,idCategorie,position) VALUES ('Implémenter squelettes','Coder les squelettes des classes',DATETIME('now'),1,1,1,2);

-- Table DonneeTache

-- Structure de la table Modele

INSERT INTO Modele (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Classique','1500','300','1200','4','0','1');
INSERT INTO Modele (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Personnel','1800','60','1500','4','0','1');
INSERT INTO Modele (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Travail','3000','600','1200','2','0','1');

-- Table Etat

INSERT INTO Etat(idEtat,libelle) VALUES
(0,'prêt'),
(1,'démarré'),
(2,'pause'),
(3,'terminé');

-- Table Pomodoro

INSERT INTO Pomodoro (idTache,idModele,position,debut,idEtat) VALUES (2,1,0,DATETIME('now'),1);
INSERT INTO Pomodoro (idTache,idModele,position) VALUES (3,1,1);
INSERT INTO Pomodoro (idTache,idModele,position) VALUES (4,1,2);

-- Table Preferences

INSERT INTO Preferences (nom,prenom,idProjet,idPomodoro,idModele) VALUES ('ESTABLET','Teddy',1,2,1);
