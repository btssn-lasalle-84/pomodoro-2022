#ifndef POMODORO_H
#define POMODORO_H

#include <Arduino.h>
#include "BluetoothSerial.h"
#include <Wire.h>
#include <Preferences.h>
#include <M5Stack.h>
#include <Screen.h>
#include <HeaderBar.h>
#include <Button.h>
#include <Label.h>
#include <Image.h>
#include <string>
#include <time.h>
#include <sys/time.h>
#include <AppScreen.h>
#include "esp_bt_device.h"
#include "esp_gap_bt_api.h"
#include <esp_spp_api.h>

using namespace Codingfield::UI;

#define M5_WIDTH            320
#define M5_HEIGHT           240
#define M5_MARGE_HAUT       25
#define M5_MARGE_BAS        10
#define M5_MARGE_DROITE     10
#define M5_MARGE_GAUCHE     10
#define M5_HAUTEUR_TEXTE_12 30
/**
 * @def DEBUG
 * @brief A définir si on désire les message de debug sur le port série
 */
#define DEBUG
//#define DEBUG_RSSI
//#define DEBUG_BATTERIE
//#define DEBUG_HORLOGE

/**
 * @enum TypeTrame
 * @brief Les differents types de trame
 */
enum TypeTrame
{
  Inconnu = -1,
  START, PAUSE_COURTE, PAUSE_LONGUE, ATTENTE, RESUME, STOP, RESET, SET, ALIVE, ACK, ERREUR, ETAT,
  NB_TRAMES
};

/**
 * @enum TypeConfiguration
 * @brief Les differents types de configuration
 */
enum TypeConfiguration
{
  Invalide = -1,
  TACHE, UTILISATEUR, POMODORO, SONNETTE,
  NB_TYPES_CONFIGURATION
};

#define CHAMP_TYPES_CONFIGURATION 1

/**
 * @enum EtatPomodoro
 * @brief Les differents états du Pomodoro
 */
enum EtatPomodoro
{
  EnAttente = -1,
  Termine = 0,
  EnCours,
  EnCourtePause,
  FinCourtePause,
  EnLonguePause,
  FinLonguePause,
  Gele,
  Reprise
};

/**
 * @enum Etat
 * @brief Les differents états
 */
enum Etat
{
  Aucun = -1,
  OFF = 0,
  ON = 1
};

/**
 * @enum CouleurPomodoro
 * @brief Les differents états
 */
enum CouleurPomodoro
{
  Noir = 0x0000,      /*   0,   0,   0 */
  Navy = 0x000F,      /*   0,   0, 128 */
  VertClair = 0x03E0,      /*   0, 128,   0 */
  CyanFonce = 0x03EF,      /*   0, 128, 128 */
  Marron = 0x7800,      /* 128,   0,   0 */
  Violet = 0x780F,      /* 128,   0, 128 */
  Olive = 0x7BE0,      /* 128, 128,   0 */
  GrisClair = 0xC618,      /* 192, 192, 192 */
  GrisFonce = 0x7BEF,      /* 128, 128, 128 */
  Bleu = 0x001F,      /*   0,   0, 255 */
  Vert = 0x07E0,      /*   0, 255,   0 */
  Cyan = 0x07FF,      /*   0, 255, 255 */
  Rouge = 0xF800,      /* 255,   0,   0 */
  Magenta = 0xF81F,      /* 255,   0, 255 */
  Jaune = 0xFFE0,      /* 255, 255,   0 */
  Blanc = 0xFFFF,      /* 255, 255, 255 */
  Orange = 0xFD20,      /* 255, 165,   0 */
  VertJaune = 0xAFE5,      /* 173, 255,  47 */
  Rose = 0xF81F,
  NbCouleurs
};

/**
 * @enum Mode
 * @brief Les differents modes
 */
enum Mode
{
  Minuteur = 0,
  Chronometre = 1
};

#define DUREE_POMODORO        25 //1500 // 25 minutes
#define PAUSE_COURTE_POMODORO 5 //300  // 5 minutes
#define PAUSE_LONGUE_POMODORO 20 //1200 // 20 minutes
#define NB_POMODORI           4

/**
 * @struct Pomodoro
 * @brief Paramètres d'un pomodoro
 */
struct Pomodoro
{
  int id = 0; //!< l'identifiant
  bool actif = false; //!< avec sonnette ou pas
  bool sonnette = false; //!< avec sonnette ou pas
  EtatPomodoro etat; //!< état du pomodoro
  int duree; //!< durée d'un pomodoro en secondes
  int pauseCourte; //!< durée de la pause courte en secondes
  int pauseLongue; //!< durée de la pause longue en secondes
  int nbPomodori; //!< nombre de pomodori avant une pause longue
  bool autoPomodoro = false; //!< 
  bool autoPause = false; //!< passe automatiquement en pause
  Mode mode; //!< minuteur ou chronomètre
};


/**
 * @def PERIPHERIQUE_BLUETOOTH
 * @brief Définit le nom du préiphérique Bluetooth
 */
#define PERIPHERIQUE_BLUETOOTH  "pomodoro-1"
#define EN_TETE_TRAME           "$"
#define DELIMITEUR_CHAMP        ";"
#define DELIMITEURS_FIN         "\r\n"
#define DELIMITEUR_DATAS        ';'
#define DELIMITEUR_FIN          '\n'

#define ERREUR_TRAME_INCONNUE       0
#define ERREUR_TRAME_NON_SUPPORTEE  1
#define ERREUR_TYPE_INCONNU         2
#define ERREUR_CONFIGURATION        3

// $SET;UTILISATEUR;NOM;PRENOM\r\n
#define NB_PARAMETRES_UTILISATEUR   3
#define CHAMP_NOM_UTILISATEUR       2
#define CHAMP_PRENOM_UTILISATEUR    3
// $SET;TACHE;NOM\r\n
#define NB_PARAMETRES_TACHE         2
#define CHAMP_NOM_TACHE             2
#define LONGUEUR_MAX                28
// $SET;SONNETTE;ACTIVATION\r\n
#define NB_PARAMETRES_SONNETTE      2
#define CHAMP_ACTIVATION_SONNETTE   2
// $SET;POMODORO;duree;pauseCourte;pauseLongue;nbPomodori;autoPomodoro;autoPause;mode\r\n
#define NB_PARAMETRES_POMODORO      8
#define CHAMP_DUREE                 2
#define CHAMP_PAUSE_COURTE          3
#define CHAMP_PAUSE_LONGUE          4
#define CHAMP_NB_POMODORI           5
#define CHAMP_AUTO_POMODORO         6
#define CHAMP_AUTO_PAUSE            7
#define CHAMP_MODE                  8

// Fonctions de service
bool lireTrame(String &trame);
int compterParametres(const String &trame);
TypeTrame verifierTrame(String &trame);
TypeConfiguration verifierTypeConfiguration(String &type);
String getNomTrame(TypeTrame typeTrame);
void envoyerTrame();
void envoyerTrameAlive();
void envoyerTrameErreur(int code);
void envoyerTrameAcquittement();
void envoyerTrameEtat(int etat);
String extraireChamp(String &trame, unsigned int numeroChamp);

void setActivationPomodoro(bool etat);
void enregistreEtatPomodoro(EtatPomodoro etat);
void setEtatPomodoro(int etat);
EtatPomodoro getEtatPomodoro();
bool getChangementEtatPomodoro();
void setChangementEtatPomodoro(bool etat);
void setActivationSonnette(int etat);
bool estEcheance(unsigned long intervalle);
bool configrerTache(String &trame);
bool configurerUtilisateur(String &trame);
bool configurerPomodoro(String &trame);
bool configurerSonnette(String &trame);

bool cliquerBoutonPomodoro(Codingfield::UI::Button* widget, IdButton button, bool pressed);
bool eteindre(Codingfield::UI::Button* widget, IdButton button, bool pressed);
void controlerAffichageBouton(bool actif=true);
void initialiserSD();
void initialiserEcran();
void getRSSI(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param);
void getEtatBluetooth(esp_spp_cb_event_t event, esp_spp_cb_param_t *param);
void lireNiveauBluetooth();
void lireNiveauBatterie();
void initialiserBluetooth();
void initialiserPreferences();
void initialiserPomodoro();
void initialiserTimers();
void demarrerTimerPomodoro(int duree);
void arreterTimerPomodoro();
void terminerTimerPomodoro(int id);
void demarrerTimerHorloge();
void arreterTimerHorloge();
void afficherHorloge();
void afficherAccueil();
void afficherInformationsUtilisateur(bool redraw=false);
void afficherTache(bool redraw=false);
void afficherNbPomodori(bool redraw=false);
void rafraichirEcran(uint32_t tempo=100);

// Fonctions utilitaires
int count(const String& str, const String& sub);
void nettoyerCaractere(std::string &chaine, std::string caractere, char remplacement);
void nettoyerChaines();

#endif
