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

/**
 * @todo Faire la classe Protocole
 */
public class Protocole
{
    /**
     * @brief Constantes
     */

    /**
     * @brief sens : Application <-> Pomodoro
     */
    private static String DEBUT_TRAME = "#";
    private static String FIN_TRAME = "\r\n";
    private static String DELIMITEUR_TRAME = "&";

    /**
     * @brief sens : Application -> Pomodoro
     */
    private static String CONFIGURATION_POMODORO= "P";
    private static String CONFIGURATION_UTILISATEUR = "U";
    private static String MODE_SONNERIE = "B";
    private static String DEMARRER_TACHE = "T";
    private static String DEMARRER_PAUSE = "R";
    private static String ARRET_TACHE_PAUSE = "S";
    private static String ANNULATION_TACHE_PAUSE = "X";
    private static String MAINTIEN_CONNEXION = "H";

    /**
     * @brief sens : Pomodoro -> Application
     */
    private static String CHANGEMENT_ETAT = "E";
    private static String ERREUR = "F";
    private static String ACQUITTEMENT = "A";
}
