package com.example.pomodoro;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
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
    public static final int INDEX_COLONNE_ID_COLONNE = 0;
    public static final int INDEX_COLONNE_LIBELLE = 1;
    public static final int INDEX_COLONNE_TACHE_ID_TACHE = 0;
    public static final int INDEX_COLONNE_TACHE_NOM = 1;
    public static final int INDEX_COLONNE_TACHE_DESCRIPTION = 2;
    public static final int INDEX_COLONNE_TACHE_DATE_CREATION = 3;
    public static final int INDEX_COLONNE_TACHE_DATE_DEBUT = 4;
    public static final int INDEX_COLONNE_TACHE_DATE_FIN = 5;
    public static final int INDEX_COLONNE_TACHE_COULEUR = 6;
    public static final int INDEX_COLONNE_TACHE_ID_COLONNE = 7;
    public static final int INDEX_COLONNE_TACHE_ACTIVE = 8;
    public static final int INDEX_COLONNE_POMODORO_ID_POMODORO = 0;
    public static final int INDEX_COLONNE_POMODORO_NOM = 1;
    public static final int INDEX_COLONNE_POMODORO_DUREE_POMODORO = 2;
    public static final int INDEX_COLONNE_POMODORO_PAUSE_COURTE = 3;
    public static final int INDEX_COLONNE_POMODORO_PAUSE_LONGUE = 4;
    public static final int INDEX_COLONNE_POMODORO_NB_POMODORI = 5;
    public static final int INDEX_COLONNE_POMODORO_AUTO_POMODORO = 6;
    public static final int INDEX_COLONNE_POMODORO_AUTO_PAUSE = 7;
    public static final int INDEX_COLONNE_PREFERENCES_NOM = 1;
    public static final int INDEX_COLONNE_PREFERENCES_PRENOM = 2;
    public static final int INDEX_COLONNE_PREFERENCES_ID_TACHE = 3;
    public static final int INDEX_COLONNE_PREFERENCES_ID_POMODORO = 4;

    /**
     * @brief Requête permettant de modifier la base de donnée
     */
    public static final String SELECT_ID_TACHE = "SELECT idTache FROM Tache WHERE nom=";
    public static final String UPDATE_NOM_TACHE = "UPDATE Tache SET nom=";

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
            Log.d(TAG, "nom tâche = " + curseurResultat.getString(INDEX_COLONNE_TACHE_NOM));
            nomsTache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_NOM)));
        }

        return nomsTache;
    }

    /**
     * @brief Permet d'effectuer une requete pour récupérer touts les noms de tâche
     * @return Les objets libellés récupérés
     */
    public Vector<List<String>> getTaches()
    {
        Vector<List<String>> taches = new Vector<List<String>>();

        String requete = "SELECT * FROM Tache WHERE active='0'";
        Cursor curseurResultat = effectuerRequete(requete);

        for (int i = 0; i < curseurResultat.getCount(); i++)
        {
            curseurResultat.moveToNext();
            Log.d(TAG, "[Tache] " + "id = " + curseurResultat.getString(INDEX_COLONNE_TACHE_ID_TACHE) + " - nom = " + curseurResultat.getString(INDEX_COLONNE_TACHE_NOM));
            List<String> tache = new Vector<String>();
            tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_ID_TACHE)));
            tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_NOM)));
            tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_DESCRIPTION)));
            tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_DATE_CREATION)));
            if(curseurResultat.getString(INDEX_COLONNE_TACHE_DATE_DEBUT) != null)
                tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_DATE_DEBUT)));
            else
                tache.add(new String(""));
            if(curseurResultat.getString(INDEX_COLONNE_TACHE_DATE_FIN) != null)
                tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_DATE_FIN)));
            else
                tache.add(new String(""));
            if(curseurResultat.getString(INDEX_COLONNE_TACHE_COULEUR) != null)
                tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_COULEUR)));
            else
                tache.add(new String(""));
            if(curseurResultat.getString(INDEX_COLONNE_TACHE_ID_COLONNE) != null)
                tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_ID_COLONNE)));
            else
                tache.add(new String(""));
            tache.add(new String(curseurResultat.getString(INDEX_COLONNE_TACHE_ACTIVE)));
            taches.add(tache);
        }

        return taches;
    }

    /**
     * @brief Permet d'effectuer une requete de type INSERT, UPDATE ou DELETE
     * @param requete La requête SQL à exécuter
     */
    public String executerRequete(String requete)
    {
        ouvrir();
        bdd.execSQL(requete);
        return requete;
    }
}
