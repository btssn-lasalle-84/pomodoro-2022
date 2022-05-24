package com.example.pomodoro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ListIterator;
import java.util.Vector;

/**
 * @file BaseDeDonnees.java
 * @brief Déclaration de la classe BaseDeDonnees
 * @author Thierry VAIRA
 * @author Teddy ESTABLET
 */

/**
 * @class BaseDeDonnees
 * @brief Classe permettant de manipuler la base de données
 */

public class BaseDeDonnees
{
    private static final String TAG = "_BaseDeDonnees";//<! TAG pour les logs
    private SQLiteDatabase bdd = null;//<! L'accès à la base de données
    private SQLite sqlite = null;//<! Permet l'initialisation de la base de données

    /**
     * Constantes
     */
    private static final int INDEX_COLONNE_ID_COLONNE = 0;
    private static final int INDEX_COLONNE_LIBELLE = 1;
    private static final int INDEX_CATEGORIE_ID_CATEGORIE = 0;
    private static final int INDEX_CATEGORIE_LIBELLE = 1;
    private static final int INDEX_COLONNE_TACHE_ID_TACHE = 0;
    private static final int INDEX_COLONNE_TACHE_TITRE = 1;
    private static final int INDEX_COLONNE_TACHE_DESCRIPTION = 2;
    private static final int INDEX_COLONNE_TACHE_DATE_CREATION = 3;
    private static final int INDEX_COLONNE_TACHE_DATE_DEBUT = 4;
    private static final int INDEX_COLONNE_TACHE_DATE_FIN = 5;
    private static final int INDEX_COLONNE_TACHE_COULEUR = 6;
    private static final int INDEX_COLONNE_TACHE_ID_PROJET = 7;
    private static final int INDEX_COLONNE_TACHE_ID_COLONNE = 8;
    private static final int INDEX_COLONNE_TACHE_ID_CATEGRIE = 9;
    private static final int INDEX_COLONNE_TACHE_POSITION = 10;
    private static final int INDEX_COLONNE_TACHE_ACTIVE = 11;
    private static final int INDEX_COLONNE_TACHE_TEMPS_DEPENSE = 12;
    private static final int INDEX_COLONNE_TACHE_TEMPS_ESTIME = 13;
    private static final int INDEX_COLONNE_TACHE_PRIORITE = 14;

    private static final String DEBUT_REQUETE_INSERTION_POMODORO =  "INSERT INTO Pomodoro(idTache,idModele,position,debut,idEtat) VALUES (";
    private static final String DEBUT_REQUETE_TERMINER_POMODORO = "UPDATE Pomodoro SET fin=DATETIME('now'), idEtat=3 WHERE idPomodoro=";

    /**
     * @brief Constructeur de la classe BaseDeDonnees
     * @param context le contexte dans lequel l'objet est créé
     */
    public BaseDeDonnees(Context context)
    {
        this.sqlite = new SQLite(context);
    }

    /**
     * @brief Ouvre un accés à la base de données
     */
    private void ouvrir()
    {
        Log.d(TAG, "ouvrir()");
        if (bdd == null)
            bdd = sqlite.getWritableDatabase();
    }

    /**
     * @brief Ferme l'accés à la base de données
     */
    private void fermer()
    {
        Log.d(TAG, "fermer()");
        if (bdd != null)
            if (bdd.isOpen())
                bdd.close();
    }

    /**
     * @brief Permet d'effectuer une requete sur la base de données
     * @param requete la requete a éffectuer
     */
    private Cursor effectuerRequete(String requete)
    {
        ouvrir();

        Cursor curseurResultat = bdd.rawQuery(requete,null);
        Log.d(TAG,"effectuerRequete() : Exécution de la requete : " + requete);
        Log.d(TAG,"effectuerRequete() : Nombre de résultats : " + Integer.toString(curseurResultat.getCount()));

        return curseurResultat;
    }

    /**
     * @brief Permet d'effectuer une requete pour récupérer tous les libellés des colonnes
     * @return Les objets libellés récupérés
     */
    public Vector<String> getNomColonnes()
    {
        Vector<String> colonnes = new Vector<String>();
        String requete = "SELECT * FROM Colonne";

        Cursor curseurResultat = effectuerRequete(requete);

        for (int i = 0; i < curseurResultat.getCount(); i++)
        {
            curseurResultat.moveToNext();
            Log.d(TAG, "libelle = " + curseurResultat.getString(INDEX_COLONNE_LIBELLE));
            colonnes.add(new String(curseurResultat.getString(INDEX_COLONNE_LIBELLE)));
        }

        return colonnes;
    }

    /**
     * @brief Permet d'effectuer une requete pour récupérer touts les noms de tâche
     * @return Les objets libellés récupérés
     */
    public Vector<String> getNomTaches()
    {
        Vector<String> nomsTache = new Vector<String>();
        String requete = "SELECT * FROM Tache";

        Cursor curseurResultat = effectuerRequete(requete);

        for (int i = 0; i < curseurResultat.getCount(); i++)
        {
            curseurResultat.moveToNext();
            Log.d(TAG, "nom tâche = " + curseurResultat.getString(INDEX_COLONNE_TACHE_TITRE));
            nomsTache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_TITRE)));
        }

        return nomsTache;
    }

    /**
     * @brief Permet d'effectuer une requete de type INSERT, UPDATE ou DELETE
     * @param requete La requête SQL à exécuter
     */
    public void executerRequete(String requete)
    {
        ouvrir();
        bdd.execSQL(requete);
    }
}
