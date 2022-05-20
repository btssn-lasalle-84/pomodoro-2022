package com.example.pomodoro;

/**
 * @file Tache.java
 * @brief Déclaration de la classe Tache
 * @author Teddy ESTABLET
 */


import android.util.Log;


/**
 * @class Tache
 * @brief Définit le concept de tâche
 */
public class Tache
{
    /**
     * Constantes
     */
    private static final String TAG = "_Tache";  //!< TAG pour les logs

    public enum CouleurTache {
        Bleue, Noire, Rouge, Verte;
    }

    /**
     * Attributs
     */
    private String nom = "Tache"; //!< Le nom de la tâche
    private CouleurTache couleurTache = CouleurTache.Rouge; //!< La couleur par défaut
    private int tempsTache = 25; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 50
    private int tempsPauseCourte = 5; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 20
    private int tempsPauseLongue = 10; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 30
    private int nombreDeCycles = 4; //!<
    private int nombreDeSessions = 4; //!<

    /**
     * @brief Constructeur
     */
    public Tache()
    {

    }

    public String getNom()
    {
        return this.nom;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    public void editerTaches()
    {
        Log.d(TAG, "editerTaches()");
    }

    public void supprimerTache()
    {
        Log.d(TAG, "supprimerTache()");
        this.nom = "";
    }
}
