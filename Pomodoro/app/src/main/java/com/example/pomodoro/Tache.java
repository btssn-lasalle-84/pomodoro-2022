package com.example.pomodoro;

/**
 * @file Tache.java
 * @brief Déclaration de la classe Tache
 * @author Teddy ESTABLET
 */

import android.util.Log;

import java.io.Serializable;

/**
 * @class Tache
 * @brief Définit le concept de tâche
 */
public class Tache implements Serializable
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
    private int id = -1; //!< L'id' de la tâche
    private String nom = ""; //!< Le nom de la tâche
    private CouleurTache couleurTache = CouleurTache.Rouge; //!< La couleur par défaut
    public int duree = 25; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 50
    public int dureePauseCourte = 5; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 20
    public int dureePauseLongue = 10; //!< Valeur exprimée en minutes | ne peux pas être plus grand que 30
    public int nombreDeCycles = 4; //!< Nombre de pomodoros successifs
    public boolean automatique = false; //!< Pomodoro automatique

    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getNom()
    {
        return this.nom;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    public int getDuree()
    {
        return duree;
    }

    public int getDureePauseCourte()
    {
        return dureePauseCourte;
    }

    public int getDureePauseLongue()
    {
        return dureePauseLongue;
    }

    public int getNombreDeCycles()
    {
        return nombreDeCycles;
    }

    /**
     * @brief Méthode qui permet de configurer les durées pomodoro de la tâche
     */
    public void configurer(int dureeTache, int dureePauseCourte, int dureePauseLongue, int nombreCycle)
    {
        this.duree = dureeTache;
        this.dureePauseCourte = dureePauseCourte;
        this.dureePauseLongue = dureePauseLongue;
        this.nombreDeCycles = nombreCycle;
    }

    /**
     * @brief Efface le nom de la tâche
     */
    public void supprimer()
    {
        Log.d(TAG, "supprimer()");
        this.nom = " ";
    }
}
