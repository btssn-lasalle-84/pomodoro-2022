package com.example.pomodoro;

/**
 * @file Minuteur.java
 * @brief Déclaration de la classe Minuteur
 * @author Teddy ESTABLET
 */

import android.os.Bundle;
import android.util.Log;

/**
 * @class Minuteur
 * @brief Définit le concept de minuteur
 */

public class Minuteur
{
    /**
     * Constantes
     */
    private static final String TAG = "_Minuteur";  //!< TAG pour les logs
    private static final String Classique = "Classique";  //!< Mode classique
    private static final String Personnel = "Personnel";  //!< Mode personnel
    private static final String Travail = "Travail";  //!< Mode travail
    private static final int MINUTEUR = 0;  //!< Mode par défaut
    private static final int CHRONOMETRE = 1;  //!< non utilisé
    public static final int ETAT_MINUTEUR_ATTENTE = -1;
    public static final int ETAT_MINUTEUR_TACHE_TERMINEE = 0;
    public static final int ETAT_MINUTEUR_TACHE_EN_COURS = 1;
    public static final int ETAT_MINUTEUR_PAUSE_COURTE_EN_COURS = 2;
    public static final int ETAT_MINUTEUR_PAUSE_COURTE_TERMINEE = 3;
    public static final int ETAT_MINUTEUR_PAUSE_LONGUE_EN_COURS = 4;
    public static final int ETAT_MINUTEUR_PAUSE_LONGUE_TERMINEE = 5;

    /**
     * Attributs
     */
    private int longueur; //!< La longueur du POMODORO : de 01 à 59
    private int dureePauseCourte; //!< Durée des pauses courtes : de 01 à 59
    private int dureePauseLongue; //!< Durée des pauses longues : de 01 à 59
    private int nbCycles; //!< Nombre de cycles : de 01 à 10
    private boolean modeAutomatique; //!< Mode automatique POMODORO : booléen 0 ou 1
    private boolean modeAutomatiquePause; //!< Mode automatique PAUSE : booléen 0 ou 1
    private int mode = MINUTEUR; //!< Mode : 0 pour Minuteur ou 1 pour Chronomètre
    private int etat = ETAT_MINUTEUR_ATTENTE;

    public Minuteur()
    {
        this.longueur = 25; // 25 minutes
        this.dureePauseCourte = 5; // 5 minutes
        this.dureePauseLongue = 20; // 20 minutes
        this.nbCycles = 4;
        this.modeAutomatique = false;
        this.modeAutomatiquePause = true;
    }

    public Minuteur(int longueur, int dureePauseCourte, int dureePauseLongue, int nbCycles, boolean modeAutomatique, boolean modeAutomatiquePause)
    {
        this.longueur = longueur;
        this.dureePauseCourte = dureePauseCourte;
        this.dureePauseLongue = dureePauseLongue;
        this.nbCycles = nbCycles;
        this.modeAutomatique = modeAutomatique;
        this.modeAutomatiquePause = modeAutomatiquePause;
    }

    public int getLongueur()
    {
        return longueur;
    }

    public int getDureePauseCourte()
    {
        return dureePauseCourte;
    }

    public int getDureePauseLongue()
    {
        return dureePauseLongue;
    }

    public int getNbCycles()
    {
        return nbCycles;
    }

    public boolean estModeAutomatique()
    {
        return modeAutomatique;
    }

    public boolean estModeAutomatiquePause()
    {
        return modeAutomatiquePause;
    }

    public int getMode()
    {
        return mode;
    }

    public int getEtat()
    {
        return etat;
    }

    public void setEtat(int etat)
    {
        this.etat = etat;
    }

    public String preparerRequete(String nom)
    {
        Log.d(TAG, "preparerRequete() " + nom);
        String requete = "";
        String requeteUpdate = "UPDATE Modele SET dureePomodoro='"+Integer.toString(this.longueur)+"',pauseCourte='"+Integer.toString(this.dureePauseCourte)+"',pauseLongue='"+Integer.toString(this.dureePauseLongue)+"',nbPomodori='"+Integer.toString(this.nbCycles)+"',autoPomodoro='"+Boolean.toString(this.modeAutomatique)+"',autoPause='"+Boolean.toString(this.modeAutomatiquePause)+"' WHERE nom='"+nom+"';";
        String requeteInsert = "INSERT INTO Modele (nom,dureePomodoro,pauseCourte,pauseLongue,nbPomodori,autoPomodoro,autoPause) VALUES ('"+nom+"','"+Integer.toString(this.longueur)+"','"+Integer.toString(this.dureePauseCourte)+"','"+Integer.toString(this.dureePauseLongue)+"','"+Integer.toString(this.nbCycles)+"','"+Boolean.toString(this.modeAutomatique)+"','"+Boolean.toString(this.modeAutomatiquePause)+"');";
        return requete;
    }
}