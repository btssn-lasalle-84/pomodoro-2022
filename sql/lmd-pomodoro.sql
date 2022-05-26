--- LMD (langage de manipulation de données)

--- Contenu des tables (tests)

-- Table Colonne

INSERT INTO Colonne(idColonne,libelle) VALUES
(1,'A faire'),
(2,'En cours'),
(3,'Terminées');

-- Table Tache

INSERT INTO Tache (nom,description,dateCreation,dateDebut,dateFin,idColonne) VALUES ('Planifier les tâches','Identifier et prioriser les tâches',DATETIME('now'),'2022-03-30 08:15:00','2022-03-30 08:40:00',1);
INSERT INTO Tache (nom,description,dateCreation,idColonne) VALUES ('Maquette IHM','Définir une interface Homme-Machine',DATETIME('now'),1);
INSERT INTO Tache (nom,description,dateCreation,idColonne) VALUES ('Classes du domaine','Réaliser le diagramme de classes du domaine',DATETIME('now'),1);
INSERT INTO Tache (nom,description,dateCreation,idColonne) VALUES ('Implémenter squelettes','Coder les squelettes des classes',DATETIME('now'),1);

-- Structure de la table Pomodoro

INSERT INTO Pomodoro (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Classique','1500','300','1200','4','0','1');
INSERT INTO Pomodoro (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Personnel','1800','60','1500','4','0','1');
INSERT INTO Pomodoro (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Travail','3000','600','1200','2','0','1');

-- Table Preferences

INSERT INTO Preferences (nom,prenom,idTache,idPomodoro) VALUES ('ESTABLET','Teddy',1,1);
