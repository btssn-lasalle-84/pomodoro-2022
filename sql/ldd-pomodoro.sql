--- LDD (langage de définition de données)

--- Supprime les tables

DROP TABLE IF EXISTS Preferences;
DROP TABLE IF EXISTS Pomodoro;
DROP TABLE IF EXISTS Etat;
DROP TABLE IF EXISTS Modele;
DROP TABLE IF EXISTS DonneeTache;
DROP TABLE IF EXISTS Tache;
DROP TABLE IF EXISTS Projet;
DROP TABLE IF EXISTS Mode;
DROP TABLE IF EXISTS Categorie;
DROP TABLE IF EXISTS Colonne;

--- Création des tables

-- Structure de la table Colonne

CREATE TABLE IF NOT EXISTS Colonne(idColonne INTEGER PRIMARY KEY, libelle VARCHAR);

-- Structure de la table Categorie

CREATE TABLE IF NOT EXISTS Categorie(idCategorie INTEGER PRIMARY KEY, libelle VARCHAR);

-- Structure de la table Mode

CREATE TABLE IF NOT EXISTS Mode(idMode INTEGER PRIMARY KEY, libelle VARCHAR);

-- Structure de la table Projet

CREATE TABLE IF NOT EXISTS Projet (
  idProjet INTEGER PRIMARY KEY AUTOINCREMENT,
  titre VARCHAR NOT NULL,
  description VARCHAR,
  dateCreation DATETIME NOT NULL,
  dateDebut DATETIME,
  dateFin DATETIME,
  couleur VARCHAR,
  idCategorie INTEGER NOT NULL,
  actif INTEGER DEFAULT '1',
  tempsDepense FLOAT DEFAULT '0',
  tempsEstime FLOAT DEFAULT '0',
  priorite INTEGER DEFAULT '0',
  CONSTRAINT Projet_fk_3 FOREIGN KEY (idCategorie) REFERENCES Categorie(idCategorie) ON DELETE CASCADE
);

-- Structure de la table Tache

CREATE TABLE IF NOT EXISTS Tache (
  idTache INTEGER PRIMARY KEY AUTOINCREMENT,
  titre VARCHAR NOT NULL,
  description VARCHAR,
  dateCreation DATETIME NOT NULL,
  dateDebut DATETIME,
  dateFin DATETIME,
  couleur VARCHAR,
  idProjet INTEGER NOT NULL,
  idColonne INTEGER NOT NULL,
  idCategorie INTEGER NOT NULL,
  position INTEGER DEFAULT '0',
  active INTEGER DEFAULT '0',
  tempsDepense INTEGER DEFAULT '0',
  tempsEstime INTEGER DEFAULT '0',
  priorite INTEGER DEFAULT '0',
  CONSTRAINT Tache_fk_1 FOREIGN KEY (idProjet) REFERENCES Projet(idProjet) ON DELETE CASCADE,
  CONSTRAINT Tache_fk_2 FOREIGN KEY (idColonne) REFERENCES Colonne(idColonne) ON DELETE CASCADE,
  CONSTRAINT Tache_fk_3 FOREIGN KEY (idCategorie) REFERENCES Categorie(idCategorie) ON DELETE CASCADE
);

-- Structure de la table DonneeTache

CREATE TABLE DonneeTache (
  idTache INTEGER NOT NULL,
  nom VARCHAR NOT NULL,
  valeur VARCHAR DEFAULT '',
  UNIQUE(idTache,nom),
  CONSTRAINT DonneeTache_fk_1 FOREIGN KEY (idTache) REFERENCES Tache(idTache) ON DELETE CASCADE
);

-- Structure de la table Modele

CREATE TABLE IF NOT EXISTS Modele (
  idModele INTEGER PRIMARY KEY AUTOINCREMENT,
  nom VARCHAR NOT NULL,
  idMode INTEGER DEFAULT '1',
  dureePomodoro INTEGER DEFAULT '1500',
  pauseCourte INTEGER DEFAULT '300',
  pauseLongue INTEGER DEFAULT '1200',
  nbPomodori INTEGER DEFAULT '4',
  autoPomodoro INTEGER DEFAULT '0',
  autoPause INTEGER DEFAULT '1',
  UNIQUE(nom),
  CONSTRAINT Modele_fk_1 FOREIGN KEY (idMode) REFERENCES Mode(idMode) ON DELETE CASCADE
);

-- Structure de la table Etat

CREATE TABLE IF NOT EXISTS Etat(idEtat INTEGER PRIMARY KEY, libelle VARCHAR);

-- Structure de la table Pomodoro

CREATE TABLE IF NOT EXISTS Pomodoro (
  idPomodoro INTEGER PRIMARY KEY AUTOINCREMENT,
  idTache INTEGER NOT NULL,
  idModele INTEGER NOT NULL,
  position INTEGER DEFAULT '0',
  debut DATETIME,
  fin DATETIME,
  idEtat INTEGER DEFAULT '0',
  duree INTEGER DEFAULT '0',
  CONSTRAINT Pomodoro_fk_1 FOREIGN KEY (idTache) REFERENCES Tache(idTache) ON DELETE CASCADE,
  CONSTRAINT Pomodoro_fk_2 FOREIGN KEY (idModele) REFERENCES Modele(idModele) ON DELETE CASCADE,
  CONSTRAINT Pomodoro_fk_3 FOREIGN KEY (idEtat) REFERENCES Etat(idEtat) ON DELETE CASCADE
);

-- Structure de la table Preferences

CREATE TABLE IF NOT EXISTS Preferences (
  idPreferences INTEGER PRIMARY KEY AUTOINCREMENT,
  nom VARCHAR NOT NULL,
  prenom VARCHAR,
  idProjet INTEGER NOT NULL,
  idPomodoro INTEGER NOT NULL,
  idModele INTEGER NOT NULL,
  UNIQUE(nom,prenom),
  --CONSTRAINT Preferences_fk_1 FOREIGN KEY (idProjet) REFERENCES Projet(idProjet) ON DELETE CASCADE,
  --CONSTRAINT Preferences_fk_2 FOREIGN KEY (idPomodoro) REFERENCES Pomodoro(idPomodoro) ON DELETE CASCADE,
  --CONSTRAINT Preferences_fk_3 FOREIGN KEY (idModele) REFERENCES Modele(idModele) ON DELETE CASCADE
);
