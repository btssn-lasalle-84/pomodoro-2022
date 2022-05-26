--- LDD (langage de définition de données)

--- Supprime les tables

DROP TABLE IF EXISTS Preferences;
DROP TABLE IF EXISTS Pomodoro;
DROP TABLE IF EXISTS Tache;
DROP TABLE IF EXISTS Colonne;

--- Création des tables

-- Structure de la table Colonne

CREATE TABLE IF NOT EXISTS Colonne(idColonne INTEGER PRIMARY KEY, libelle VARCHAR);

-- Structure de la table Tache

CREATE TABLE IF NOT EXISTS Tache (
  idTache INTEGER PRIMARY KEY AUTOINCREMENT,
  nom VARCHAR NOT NULL,
  description VARCHAR,
  dateCreation DATETIME NOT NULL,
  dateDebut DATETIME,
  dateFin DATETIME,
  couleur VARCHAR,
  idColonne INTEGER NOT NULL,
  active INTEGER DEFAULT '0',
  CONSTRAINT Tache_fk_1 FOREIGN KEY (idColonne) REFERENCES Colonne(idColonne) ON DELETE CASCADE
);

-- Structure de la table Pomodoro

CREATE TABLE IF NOT EXISTS Pomodoro (
  idPomodoro INTEGER PRIMARY KEY AUTOINCREMENT,
  nom VARCHAR NOT NULL,
  dureePomodoro INTEGER DEFAULT '25',
  pauseCourte INTEGER DEFAULT '5',
  pauseLongue INTEGER DEFAULT '20',
  nbPomodori INTEGER DEFAULT '4',
  autoPomodoro INTEGER DEFAULT '0',
  autoPause INTEGER DEFAULT '1',
  UNIQUE(nom)
);

-- Structure de la table Preferences

CREATE TABLE IF NOT EXISTS Preferences (
  idPreferences INTEGER PRIMARY KEY AUTOINCREMENT,
  nom VARCHAR NOT NULL,
  prenom VARCHAR NOT NULL,
  idTache INTEGER NOT NULL,
  idPomodoro INTEGER NOT NULL,
  UNIQUE(nom,prenom),  
  CONSTRAINT Preferences_fk_1 FOREIGN KEY (idTache) REFERENCES Tache(idTache) ON DELETE CASCADE,
  CONSTRAINT Preferences_fk_2 FOREIGN KEY (idPomodoro) REFERENCES Pomodoro(idPomodoro) ON DELETE CASCADE
);
