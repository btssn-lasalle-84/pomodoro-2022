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
    public int tempsTache = 25; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 50
    public int tempsPauseCourte = 5; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 20
    public int tempsPauseLongue = 10; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 30
    public int nombreDeCycles = 4; //!<

    /**
     * @brief Constructeur
     * @param nomTache
     * @param dureeTache
     * @param dureePauseCourte
     * @param dureePauseLongue
     * @param nombreCycle
     */
    public Tache(String nomTache, int dureeTache, int dureePauseCourte, int dureePauseLongue, int nombreCycle)
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
