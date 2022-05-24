package com.example.pomodoro;

/**
 * @file Protocole.java
 * @brief Déclaration de la classe Protocole
 * @author Teddy ESTABLET
 */


/**
 * @class Protocole
 * @brief Défini le concept de protocole
 */
public class Protocole
{
    /**
     * @brief Sens : Application <-> Pomodoro
     */
    public static final String DEBUT_TRAME = "#";
    public static final String FIN_TRAME = "\r\n";
    public static final String DELIMITEUR_TRAME = "&";

    /**
     * @brief Sens : Application -> Pomodoro
     */
    public static final String CONFIGURATION_POMODORO= "P";
    public static final String CONFIGURATION_UTILISATEUR = "U";
    public static final String MODE_SONNERIE = "B";
    public static final String DEMARRER_TACHE = "T";
    public static final String DEMARRER_PAUSE = "R";
    public static final String ARRET_TACHE_PAUSE = "S";
    public static final String ANNULATION_TACHE_PAUSE = "X";
    public static final String MAINTIEN_CONNEXION = "H";

    /**
     * @brief Sens : Pomodoro -> Application
     */
    public static final String CHANGEMENT_ETAT = "E";
    public static final String ERREUR = "F";
    public static final String ACQUITTEMENT = "A";

    /**
     * @brief Position des champs dans une trame
     */
    public static final int TYPE_TRAME = 0;
    public static final int CHAMP_ETAT = 1;

    /**
     * @brief Différent état du type de trame : CHANGEMENT_ETAT
     */
    public static final String ETAT_ATTENTE = "-1";
    public static final String ETAT_TACHE_TERMINEE = "0";
    public static final String ETAT_TACHE_EN_COURS = "1";
    public static final String ETAT_PAUSE_COURTE_EN_COURS = "2";
    public static final String ETAT_PAUSE_COURTE_TERMINEE = "3";
    public static final String ETAT_PAUSE_LONGUE_EN_COURS = "4";
    public static final String ETAT_PAUSE_LONGUE_TERMINEE = "5";

    /**
     * @brief Trame envoyer par défaut
     */
    public static final String CONFIGURER_UN_POMODORO = "#P&25&5&15&4&1&1&1\n";
}
