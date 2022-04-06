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

/**
 * @def DEBUG
 * @brief A définir si on désire les message de debug sur le port série
 */
#define DEBUG

/**
 * @enum EtatPomodoro
 * @brief Les differents états du Pomodoro
 */
enum EtatPomodoro
{
  Indetermine = -1,
  Libre = 0,  // GREEN
  Absent = 1, // RED
  Occupe = 2,  // ORANGE
  Entrez // BLUE
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
 * @def PERIPHERIQUE_BLUETOOTH
 * @brief Définit le nom du préiphérique Bluetooth
 */
#define PERIPHERIQUE_BLUETOOTH  "pomodoro-1"
#define LONGUEUR_CHAMP          16
// Protocole (cf. Google Drive)
#define DELIMITEUR_CHAMP        ";"
#define DELIMITEURS_FIN         "\r\n"
#define DELIMITEUR_DATAS        ';'
#define DELIMITEUR_FIN          '\n'
#define EN_TETE_ETAT            "$"
#define EN_TETE_CMD             "$"
#define EN_TETE_ALIVE           "$A"
#define NB_PARAMETRES_POMODORO  3
#define TRAME_ETAT_LIBRE        0
#define TRAME_ETAT_ABSENT       1
#define TRAME_ETAT_OCCUPE       2
// $UTILISATEUR;NOM;PRENOM;FONCTION\r\n
#define EN_TETE_UTILISATEUR       "$UTILISATEUR"
#define NB_PARAMETRES_UTILISATEUR 2
// $TACHE;nom\r\n
#define EN_TETE_TACHE        "$TACHE"
#define NB_PARAMETRES_TACHE  1
/**
 * @enum TypeTrame
 * @brief Les differents types de trame reçue
 */
enum TypeTrame
{
    Inconnu = 0,
    Commande = 1,
    Utilisateur = 2,
    Tache = 3,
    Pomodoro = 4,
    Alive = 5
};

// Fonctions de service
void envoyerTrame();
void envoyerTrameAlive();
bool lireTrame(String &trame);
int compterParametres(const String &trame);
TypeTrame verifierTrame(String &trame);
void setEtatPomodoro(int etat);
bool getChangementEtatPomodoro();
void setChangementEtatPomodoro(bool etat);
void setActivationSonnette(int etat);
bool setEtatSonnette(int etat);
void setActivationDetecteurPresence(int etat);
void setEtatPresence(int etat);
bool extraireParametresPomodoro(String &trame);
bool extraireParametresUtilisateur(String &trame);
bool extraireParametresTache(String &trame);
bool extraireParametres(String &trame, const TypeTrame &typeTrame);
bool estEcheance(unsigned long intervalle);

bool cliquerBoutonSonnerie(Codingfield::UI::Button* widget, IdButton button, bool pressed);
bool eteindre(Codingfield::UI::Button* widget, IdButton button, bool pressed);
void controlerAffichageBoutonSonnerie();
void initialiserSD();
void initialiserEcran();
void getRSSI(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param);
void getEtatBluetooth(esp_spp_cb_event_t event, esp_spp_cb_param_t *param);
void lireNiveauBluetooth();
void lireNiveauBatterie();
void initialiserBluetooth();
void initialiserPreferences();
void afficherAccueil();
void afficherInformationsUtilisateur(bool redraw=false);
void afficherTache(bool redraw=false);
void afficherAdresse(bool redraw=false);
void rafraichirEcran(uint32_t tempo=100);

// Fonctions utilitaires
int count(const String& str, const String& sub);
void nettoyerCaractere(std::string &chaine, std::string caractere, char remplacement);
void nettoyerChaines();

#endif
