package com.example.pomodoro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @file SQLite.java
 * @brief Déclaration de la classe SQLite
 * @author Thierry VAIRA
 */

/**
 * @class SQLite
 * @brief Classe qui permet la création de la base de données
 */
public class SQLite extends SQLiteOpenHelper
{
    private static final String TAG = "_SQLite";  //!< TAG pour les logs
    public static final String  DATABASE_NAME = "pomodoro.db";//!< Le nom de la base de données
    public static final int     DATABASE_VERSION = 3;//!< La version de la base de données

    /**
     * Requêtes de création des tables
     */
    private static final String CREATE_TABLE_COLONNE = "CREATE TABLE IF NOT EXISTS Colonne(idColonne INTEGER PRIMARY KEY, libelle VARCHAR);";
    private static final String CREATE_TABLE_TACHE = "CREATE TABLE IF NOT EXISTS Tache(idTache INTEGER PRIMARY KEY AUTOINCREMENT,nom VARCHAR NOT NULL,description VARCHAR,dateCreation DATETIME NOT NULL,dateDebut DATETIME,dateFin DATETIME,couleur VARCHAR,idColonne INTEGER NOT NULL,active INTEGER DEFAULT '0',CONSTRAINT Tache_fk_1 FOREIGN KEY (idColonne) REFERENCES Colonne(idColonne) ON DELETE CASCADE);";
    private static final String CREATE_TABLE_POMODORO = "CREATE TABLE IF NOT EXISTS Pomodoro(idPomodoro INTEGER PRIMARY KEY AUTOINCREMENT,nom VARCHAR NOT NULL,dureePomodoro INTEGER DEFAULT '25',pauseCourte INTEGER DEFAULT '5',pauseLongue INTEGER DEFAULT '20',nbPomodori INTEGER DEFAULT '4',autoPomodoro INTEGER DEFAULT '0',autoPause INTEGER DEFAULT '1',UNIQUE(nom));";
    private static final String CREATE_TABLE_PREFERENCES = "CREATE TABLE IF NOT EXISTS Preferences(idPreferences INTEGER PRIMARY KEY AUTOINCREMENT,nom VARCHAR NOT NULL,prenom VARCHAR NOT NULL,idTache INTEGER NOT NULL,idPomodoro INTEGER NOT NULL,UNIQUE(nom,prenom),CONSTRAINT Preferences_fk_1 FOREIGN KEY (idTache) REFERENCES Tache(idTache) ON DELETE CASCADE,CONSTRAINT Preferences_fk_2 FOREIGN KEY (idPomodoro) REFERENCES Pomodoro(idPomodoro) ON DELETE CASCADE);";

    /**
     * Requêtes d'insertion de initiales dans la base de données
     */
    private static final String INSERT_TABLE_COLONNE = "INSERT INTO Colonne(idColonne,libelle) VALUES(1,'A faire'),(2,'En cours'),(3,'Terminées');";

    /**
     * Requêtes de test
     */
    private static final String INSERT_TABLE_TACHE_1 = "INSERT INTO Tache (nom,description,dateCreation,dateDebut,dateFin,idColonne) VALUES ('Planifier les tâches','Identifier et prioriser les tâches',DATETIME('now'),'2022-03-30 08:15:00','2022-03-30 08:40:00',1);";
    private static final String INSERT_TABLE_TACHE_2 = "INSERT INTO Tache (nom,description,dateCreation,idColonne) VALUES ('Maquette IHM','Définir une interface Homme-Machine',DATETIME('now'),1);";
    private static final String INSERT_TABLE_TACHE_3 = "INSERT INTO Tache (nom,description,dateCreation,idColonne) VALUES ('Classes du domaine','Réaliser le diagramme de classes du domaine',DATETIME('now'),1);";
    private static final String INSERT_TABLE_TACHE_4 = "INSERT INTO Tache (nom,description,dateCreation,idColonne) VALUES ('Implémenter squelettes','Coder les squelettes des classes',DATETIME('now'),1);";
    private static final String INSERT_TABLE_POMODORO_1 = "INSERT INTO Pomodoro (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Classique','1500','300','1200','4','0','1');";
    private static final String INSERT_TABLE_POMODORO_2 = "INSERT INTO Pomodoro (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Personnel','1800','60','1500','4','0','1');";
    private static final String INSERT_TABLE_POMODORO_3 = "INSERT INTO Pomodoro (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('Travail','3000','600','1200','2','0','1');";
    private static final String INSERT_TABLE_PREFERENCES_1 = "INSERT INTO Preferences (nom,prenom,idTache,idPomodoro) VALUES ('ESTABLET','Teddy',1,1);";

    /**
     * @brief Constructeur de la classe SQLite
     */
    public SQLite(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * @brief Méthode appelée à la création de la base de données
     * @param db La base de données créée
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d(TAG, "onCreate() path = " + db.getPath());
        creerTables(db);
        insererDonneesInitiales(db);
    }

    /**
     * @brief Méthode appelée lors de la mise à jour de la base de données
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.d(TAG, "onUpgrade()");
        db.execSQL("DROP TABLE IF EXISTS Preferences;");
        db.execSQL("DROP TABLE IF EXISTS Pomodoro;");
        db.execSQL("DROP TABLE IF EXISTS Tache;");
        db.execSQL("DROP TABLE IF EXISTS Colonne;");
        onCreate(db);
    }

    /**
     * @brief Méthode permettant d'éxecuter les requêtes de création des tables
     * @param db La base de données sur laquelle éxecuter les requêtes
     */
    private void creerTables(SQLiteDatabase db)
    {
        Log.d(TAG, "creerTables()");
        db.execSQL(CREATE_TABLE_COLONNE);
        db.execSQL(CREATE_TABLE_TACHE);
        db.execSQL(CREATE_TABLE_POMODORO);
        db.execSQL(CREATE_TABLE_PREFERENCES);
    }

    /**
     * @brief Méthode permettant d'éxecuter les requêtes d'insertion de données initiales
     * @param db La base de données sur laquelle éxecuter les requêtes
     */
    private void insererDonneesInitiales(SQLiteDatabase db)
    {
        Log.d(TAG, "insererDonneesInitiales()");
        db.execSQL(INSERT_TABLE_COLONNE);

        // Tests
        db.execSQL(INSERT_TABLE_TACHE_1);
        db.execSQL(INSERT_TABLE_TACHE_2);
        db.execSQL(INSERT_TABLE_TACHE_3);
        db.execSQL(INSERT_TABLE_TACHE_4);
        db.execSQL(INSERT_TABLE_POMODORO_1);
        db.execSQL(INSERT_TABLE_POMODORO_2);
        db.execSQL(INSERT_TABLE_POMODORO_3);
        db.execSQL(INSERT_TABLE_PREFERENCES_1);
    }
}
