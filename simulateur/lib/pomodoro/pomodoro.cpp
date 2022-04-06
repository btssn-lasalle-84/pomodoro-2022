#include "pomodoro.h"

// Bluetooth
static BluetoothSerial SerialBT; //!< objet pour la communication Bluetooth
static bool connecte = false; //!< état de la connexion Bluetooth

// Pomodoro
static EtatPomodoro etatPomodoro = Indetermine; //!< l'état du Pomodoro
static bool changementEtatPomodoro = false; //!< indique un changement d'état du Pomodoro
static Etat activationSonnette = Aucun; //!< l'état d'activation de la sonnette
static Etat activationDetecteurPresence = Aucun; //!< l'état d'activation du détecteur de présence
static Etat sonnette = OFF; //!< l'état de la sonette
static Etat presence = OFF; //!< l'état du détecteur de présence
static int rssi = 0; //!< le niveau RSSI du Bluetooth
static String adresseBluetooth; //!< l'adresse MAC Bluetooth du Pomodoro
static uint8_t adresseDistante[ESP_BD_ADDR_LEN] = {0, 0, 0, 0, 0, 0}; //!< l'adresse MAC du Bluetooth connecté
static uint8_t adressePomodoro[ESP_BD_ADDR_LEN] = {0, 0, 0, 0, 0, 0}; //!< l'adresse MAC Bluetooth du Pomodoro

// Preferences
static Preferences preferences;  //!< objet pour le stockage interne
static std::string nomUtilisateur; //!< le nom de l'utilisateur
static std::string prenomUtilisateur; //!< le prenom de l'utilisateur
static std::string nomTache; //!< le nom de la tâche

// GUI
static AppScreen* screen;
static HeaderBar* topBar;
static Codingfield::UI::Label* labelUtilisateur;
static Codingfield::UI::Label* labelEtatPomodoro;
static Codingfield::UI::Label* labelNomTache;
static Codingfield::UI::Label* labelAdresse;
static Codingfield::UI::Button* boutonSonnerie;
static Codingfield::UI::Button* boutonOff;
static Codingfield::UI::Image* logoLasalle;
static Codingfield::UI::Image* logoPomodoro;
static std::string titre = "Pomodoro";

/**
 * @brief Envoie une trame Pomodoro via le Bluetooth
 *
 * @fn envoyerTrame()
 */
void envoyerTrame()
{
  char trameEnvoi[64];

  // Trame envoyée : $ETAT;SONNETTE;PRESENCE;MODE_SONNETTE;MODE_PRESENCE\r\n
  sprintf((char *)trameEnvoi, "$%d;%d;%d;%d;%d\r\n", (int)etatPomodoro, (int)sonnette, (int)presence, (int)activationSonnette, (int)activationDetecteurPresence);
  SerialBT.write((uint8_t*)trameEnvoi, strlen((char *)trameEnvoi));
  #ifdef DEBUG
  Serial.println(String(trameEnvoi));
  #endif
}

/**
 * @brief Envoie une trame Alive via le Bluetooth
 *
 * @fn envoyerTrameAlive()
 */
void envoyerTrameAlive()
{
  char trameEnvoi[64] = "$A\r\n";

  // Trame envoyée : $A\r\n
  SerialBT.write((uint8_t*)trameEnvoi, strlen((char *)trameEnvoi));
  #ifdef DEBUG
  Serial.println(String(trameEnvoi));
  #endif
}

/**
 * @brief Lit une trame via le Bluetooth
 *
 * @fn lireTrame(String &trame)
 * @param trame la trame reçue
 * @return bool true si une trame a été lue, sinon false
 */
bool lireTrame(String &trame)
{
  if(SerialBT.available())
  {
    trame.clear();
    trame = SerialBT.readStringUntil(DELIMITEUR_FIN);
    trame.concat(DELIMITEUR_FIN); // remet le DELIMITEUR_FIN
    #ifdef DEBUG
    Serial.print("bluetooth -> ");
    Serial.println(trame);
    #endif
    return true;
  }

  return false;
}

/**
 * @brief Compte le nombre de paramètres (champ délimité) d'une trame
 *
 * @fn compterParametres(const String &trame)
 * @param trame
 * @return int le nombre de paramètres contenu dans la trame
 */
int compterParametres(const String &trame)
{
  int n = 0;
  for(int i=0;i< trame.length();i++)
  {
    if(trame[i] == DELIMITEUR_DATAS)
      ++n;
  }
  return n;
}

/**
 * @brief Vérifie si la trame reçue est valide et retorune le type de la trame
 *
 * @fn verifierTrame(String &trame)
 * @param trame
 * @return TypeTrame le type de la trame
 */
TypeTrame verifierTrame(String &trame)
{
  //trame.toUpperCase();

  String entete = String(EN_TETE_CMD);
  entete.toUpperCase();
  if(trame.startsWith(entete))
  {
    return Commande;
  }

  entete = String(EN_TETE_UTILISATEUR);
  entete.toUpperCase();
  if(trame.startsWith(entete))
  {
    return Utilisateur;
  }

  entete = String(EN_TETE_TACHE);
  entete.toUpperCase();
  if(trame.startsWith(entete))
  {
    return Tache;
  }

  entete = String(EN_TETE_ETAT);
  entete.toUpperCase();
  if(trame.startsWith(entete))
  {
    return Pomodoro;
  }

  entete = String(EN_TETE_ALIVE);
  entete.toUpperCase();
  if(trame.startsWith(entete))
  {
    return Alive;
  }

  return Inconnu;
}

/**
 * @brief Met à jour l'état du Pomodoro
 *
 * @fn setEtatPomodoro(int etat)
 * @param etat (Libre, Absent ou Occupe)
 */
void setEtatPomodoro(int etat)
{
  std::string texteEtat = "";

  if(etatPomodoro != (EtatPomodoro)etat)
  {
    etatPomodoro = (EtatPomodoro)etat;
    changementEtatPomodoro = true;

    preferences.putInt("etatPomodoro", (int)etatPomodoro);
    switch(etatPomodoro)
    {
    case Libre:
      texteEtat = "LIBRE";
      labelEtatPomodoro->SetFont(&FreeSansBold24pt7b);
      labelEtatPomodoro->SetBackgroundColor(GREENYELLOW);
      break;
    case Absent:
      texteEtat = "ABSENT";
      labelEtatPomodoro->SetFont(&FreeSans24pt7b);
      labelEtatPomodoro->SetBackgroundColor(MAROON);
      break;
    case Occupe:
      texteEtat = "OCCUPE";
      labelEtatPomodoro->SetFont(&FreeSans24pt7b);
      labelEtatPomodoro->SetBackgroundColor(ORANGE);
      break;
    case Entrez:
      texteEtat = "ENTREZ";
      labelEtatPomodoro->SetBackgroundColor(BLUE);
      break;
    default:
      break;
    }
    labelEtatPomodoro->SetText(texteEtat);
    controlerAffichageBoutonSonnerie();
  }
  if(etatPomodoro == Entrez)
  {
    M5.Speaker.tone(432, 500);
  }
}

bool getChangementEtatPomodoro()
{
  return changementEtatPomodoro;
}

void setChangementEtatPomodoro(bool etat)
{
  changementEtatPomodoro = etat;
}

/**
 * @brief Met à jour l'état d'activation de la sonnette
 *
 * @fn setActivationSonnette(int etat)
 * @param etat (ON ou OFF)
 */
void setActivationSonnette(int etat)
{
  if(activationSonnette != (Etat)etat)
  {
    activationSonnette = (Etat)etat;
    preferences.putInt("sonnette", (int)activationSonnette);
    changementEtatPomodoro = true;
    controlerAffichageBoutonSonnerie();
  }
}

/**
 * @brief Met à jour l'état de la sonnette
 *
 * @fn setEtatSonnette(int etat)
 * @param etat (ON ou OFF)
 */
bool setEtatSonnette(int etat)
{
  if(etatPomodoro == Absent)
    return false;
  if(activationSonnette == ON)
  {
    sonnette = (Etat)etat;
    return true;
  }
  else
    sonnette = OFF;
  return false;
}

/**
 * @brief Met à jour l'état d'activation du détecteur de présence
 *
 * @fn setActivationDetecteurPresence(int etat)
 * @param etat (ON ou OFF)
 */
void setActivationDetecteurPresence(int etat)
{
  if(activationDetecteurPresence != (Etat)etat)
  {
    activationDetecteurPresence = (Etat)etat;
    preferences.putInt("presence", (int)activationDetecteurPresence);
    changementEtatPomodoro = true;
  }
}

/**
 * @brief Met à jour l'état de la détection de présence
 *
 * @fn setEtatPresence(int etat)
 * @param etat (ON ou OFF)
 */
void setEtatPresence(int etat)
{
  if(etatPomodoro == Absent)
    return;
  if(activationDetecteurPresence == ON)
    presence = (Etat)etat;
  else
    presence = OFF;
}

/**
 * @brief Extrait les paramètres d'une trame Pomodoro
 *
 * @fn extraireParametresPomodoro(String &trame)
 * @param trame
 * @return bool true si les paramètres ont été extraits, sinon false
 */
bool extraireParametresPomodoro(String &trame)
{
  // $ETAT;SONNETTE;PRESENCE\r\n
  trame.toUpperCase();

  int positionDebut = trame.indexOf(";")+1;
  int positionFin = 0;
  String valeur;

  if(positionDebut == 0)
  {
      return false;
  }

  if(compterParametres(trame) != NB_PARAMETRES_POMODORO)
  {
      return false;
  }

  positionFin = trame.indexOf(";", positionDebut);
  valeur = trame.substring(positionDebut, positionFin);
  setEtatPomodoro(valeur.toInt());

  positionDebut = trame.indexOf(";", positionFin)+1;
  positionFin = trame.indexOf(";", positionDebut);
  valeur = trame.substring(positionDebut, positionFin);
  setActivationSonnette((Etat)valeur.toInt());

  positionDebut = trame.indexOf(";", positionFin)+1;
  positionFin = trame.indexOf(";", positionDebut);
  valeur = trame.substring(positionDebut, positionFin);
  setActivationDetecteurPresence((Etat)valeur.toInt());

  return true;
}

/**
 * @brief Extrait les paramètres d'une trame UTILISATEUR (nom et prénom de l'utilisateur)
 *
 * @fn extraireParametresUtilisateur(String &trame)
 * @param trame
 * @return bool true si les paramètres ont été extraits, sinon false
 */
bool extraireParametresUtilisateur(String &trame)
{
  // $UTILISATEUR;NOM;PRENOM\r\n
  //trame.toUpperCase();

  int positionDebut = trame.indexOf(";")+1;
  int positionFin = 0;
  String valeur;

  if(positionDebut == 0)
  {
      return false;
  }

  if(compterParametres(trame) != NB_PARAMETRES_UTILISATEUR)
  {
      return false;
  }

  positionFin = trame.indexOf(";", positionDebut);
  valeur = trame.substring(positionDebut, positionFin);
  size_t retour = preferences.putString("nom", valeur.c_str());
  if(retour == valeur.length())
    nomUtilisateur = valeur.c_str();
  #ifdef DEBUG
  Serial.print("nom : ");
  Serial.println(nomUtilisateur.c_str());
  #endif

  positionDebut = trame.indexOf(";", positionFin)+1;
  positionFin = trame.indexOf(";", positionDebut);
  valeur = trame.substring(positionDebut, positionFin);
  retour = preferences.putString("prenom", valeur.c_str());
  if(retour == valeur.length())
    prenomUtilisateur = valeur.c_str();
  #ifdef DEBUG
  Serial.print("prénom : ");
  Serial.println(prenomUtilisateur.c_str());
  #endif

  afficherInformationsUtilisateur();

  envoyerTrame();

  return true;
}

/**
 * @brief Extrait les paramètres d'une trame TACHE
 *
 * @fn extraireParametresTache(String &trame)
 * @param trame
 * @return bool true si les paramètres ont été extraits, sinon false
 */
bool extraireParametresTache(String &trame)
{
  // $TACHE;nom\r\n
  //trame.toUpperCase();

  int positionDebut = trame.indexOf(";")+1;
  int positionFin = 0;
  String valeur;

  if(positionDebut == 0)
  {
      return false;
  }

  if(compterParametres(trame) != NB_PARAMETRES_TACHE)
  {
      return false;
  }

  positionFin = trame.indexOf(";", positionDebut);
  valeur = trame.substring(positionDebut, positionFin);
  size_t retour = preferences.putString("nomTache", valeur.c_str());
  if(retour == valeur.length())
    nomTache = valeur.c_str();

  afficherTache();

  return true;
}

/**
 * @brief Extrait les paramètres d'une trame en fonction de son type
 *
 * @fn extraireParametres(String &trame, const TypeTrame &typeTrame)
 * @param trame
 * @param typeTrame
 * @return bool true si les paramètres ont été extraits, sinon false
 */
bool extraireParametres(String &trame, const TypeTrame &typeTrame)
{
  switch (typeTrame)
  {
    case Inconnu:
      break;
    case Commande:
      return extraireParametresPomodoro(trame);
      break;
    case Utilisateur:
      return extraireParametresUtilisateur(trame);
      break;
    case Tache:
      return extraireParametresTache(trame);
      break;
    case Pomodoro:
      changementEtatPomodoro = true;
      return true;
      break;
    case Alive:
      envoyerTrameAlive();
      return true;
      break;
    default:
      break;
  }
  return false;
}

/**
* @brief Retourne true si l'échéance de la période fixée a été atteinte
*
* @fn estEcheance(unsigned long intervalle)
*
* @param intervalle unsigned long l'intervalle de temps en millisecondes entre deux échéances
*
* @return bool vrai si l'échéance de la période a été atteinte
*/
bool estEcheance(unsigned long intervalle)
{
  static unsigned long tempsPrecedent = millis();
  unsigned long temps = millis();
  if (temps - tempsPrecedent >= intervalle)
  {
      tempsPrecedent = temps;
      return true;
  }
  return false;
}

/**
* @brief Fontion de callback pour le clic sur le bouton Sonnette
*
* @fn cliquerBoutonSonnerie(Button* widget, IdButton button, bool pressed)
*
* @param widget le widget sur lequel on a cliqué
* @param button l'identifiant du bouton
* @param pressed vrai si appui sinon faux pour relachement
*
* @return bool vrai
*/
bool cliquerBoutonSonnerie(Codingfield::UI::Button* widget, IdButton button, bool pressed)
{
  if(pressed)
  {
    if(setEtatSonnette(ON))
    {
      envoyerTrame();
      widget->SetBackgroundColor(WHITE);
      widget->SetTextColor(DARKGREY);
      M5.Speaker.tone(432, 400);
      //delay(400);
      //M5.Speaker.tone(1230, 400);
    }
  }
  else
  {
    if(activationSonnette == ON)
    {
      setEtatSonnette(OFF);
      widget->SetBackgroundColor(DARKGREY);
      widget->SetTextColor(WHITE);
    }
  }
  return true;
}

void controlerAffichageBoutonSonnerie()
{
  if(etatPomodoro == Absent || !activationSonnette)
  {
    boutonSonnerie->DisableControls();
    boutonSonnerie->Hide();
  }
  else
  {
    boutonSonnerie->Show();
    boutonSonnerie->EnableControls();
  }
}

bool eteindre(Codingfield::UI::Button* widget, IdButton button, bool pressed)
{
  //M5.Power.deepSleep(60000000); // réveil au bout de 60 s
  M5.Power.deepSleep(); // réveil avec la touche A
  //M5.Power.lightSleep(); //
  return true;
}

void initialiserSD()
{
  bool ok = false;
  int nbTentatives = 3;

  do
  {
    ok = SD.begin(TFCARD_CS_PIN, SPI, 40000000);
    delay(50);
  } while (!ok && nbTentatives--);

}

/**
* @brief Initialise les ressources pour l'écran
*
* @fn initialiserEcran()
*/
void initialiserEcran()
{
  M5.Lcd.setBrightness(50);

  topBar = new HeaderBar();
  topBar->SetTitle(titre);
  topBar->SetStatusLeft(HeaderBar::BatteryStatuses::FullBattery);
  topBar->SetStatusRight(HeaderBar::WirelessStatuses::FullSignal);

  screen = new AppScreen(Size(320, 240), LIGHTGREY, topBar, nullptr, nullptr);

  //logoLasalle = new Codingfield::UI::Image(screen, Point(5,185), Size(0,0));
  //logoLasalle = new Codingfield::UI::Image(screen, Point(0,25), Size(0,0)); // en fond
  //logoLasalle->SetBackgroundColor(LIGHTGREY);
  //logoLasalle->SetImage(&SD, "/lasalle.png");
  //logoLasalle->SetScale(0.25);

  //logoPomodoro = new Codingfield::UI::Image(screen, Point(240,185), Size(0,0));
  //logoPomodoro->SetBackgroundColor(LIGHTGREY);
  //logoPomodoro->SetImage(&SD, "/pomodoro.png");

  labelUtilisateur = new Codingfield::UI::Label(screen, Point(10,25), Size(300,30), &FreeSerifBoldItalic12pt7b);
  labelUtilisateur->SetBackgroundColor(LIGHTGREY); // ORANGE YELLOW PURPLE GREEN MAROON
  labelUtilisateur->SetTextColor(BLACK);
  labelUtilisateur->SetText(" ");

  labelEtatPomodoro = new Codingfield::UI::Label(screen, Point(40,70), Size(240,70), &FreeSansBold24pt7b);
  labelEtatPomodoro->SetBackgroundColor(BLUE); // ORANGE YELLOW PURPLE GREEN MAROON
  labelEtatPomodoro->SetTextColor(WHITE);
  labelEtatPomodoro->SetRounded(true, 4);
  labelEtatPomodoro->Hide();

  labelNomTache = new Codingfield::UI::Label(screen, Point(10,145), Size(300,20), &FreeMono9pt7b);
  labelNomTache->SetBackgroundColor(LIGHTGREY);
  labelNomTache->SetTextColor(BLACK);
  labelNomTache->SetText(" ");

  labelAdresse = new Codingfield::UI::Label(screen, Point(10,165), Size(300,20), &FreeMono9pt7b);
  labelAdresse->SetBackgroundColor(LIGHTGREY);
  labelAdresse->SetTextColor(BLACK);
  labelAdresse->SetText(" ");

  boutonSonnerie = new Codingfield::UI::Button(screen, Point(100,190), Size(120,40), &FreeSansBold12pt7b);
  boutonSonnerie->SetBackgroundColor(DARKGREY);
  boutonSonnerie->SetTextColor(WHITE);
  boutonSonnerie->SetBorderColor(WHITE);
  boutonSonnerie->SetBorder(true, 1);
  boutonSonnerie->SetText("SONNEZ");
  boutonSonnerie->SetPressedCallback(Codingfield::UI::ButtonB, cliquerBoutonSonnerie);
  boutonSonnerie->SetReleasedCallback(Codingfield::UI::ButtonB, cliquerBoutonSonnerie);

  boutonOff = new Codingfield::UI::Button(screen, Point(280,190), Size(0,0), &FreeSansBold12pt7b);
  boutonOff->SetPressedCallback(Codingfield::UI::ButtonC, eteindre);
  boutonOff->Hide();
}

/**
* @brief Lit le RSSI de la connexion sans fil
*
*/
void getRSSI(esp_bt_gap_cb_event_t event, esp_bt_gap_cb_param_t *param)
{
  if (event == ESP_BT_GAP_READ_RSSI_DELTA_EVT)
  {
    rssi = param->read_rssi_delta.rssi_delta;
    #ifdef DEBUG
    if(rssi != 0)
    {
      char adresse[32];
      sprintf(adresse, "%02X:%02X:%02X:%02X:%02X:%02X", (uint8_t)adresseDistante[0], (uint8_t)adresseDistante[1], (uint8_t)adresseDistante[2], (uint8_t)adresseDistante[3], (uint8_t)adresseDistante[4], (uint8_t)adresseDistante[5]);
      Serial.print("mac = ");
      Serial.print(adresse);
      Serial.print(" -> rssi = ");
      Serial.println(rssi);
    }
    #endif
  }
}

/**
* @brief Lit l'état de la connexion sans fil
*
*/
void getEtatBluetooth(esp_spp_cb_event_t event, esp_spp_cb_param_t *param)
{
  // Serveur : connexion ?
  if (event == ESP_SPP_SRV_OPEN_EVT)
  {
    connecte = true;
    controlerAffichageBoutonSonnerie();
    memcpy(adresseDistante, param->srv_open.rem_bda, ESP_BD_ADDR_LEN);
    char adresse[32];
    sprintf(adresse, "%02X:%02X:%02X:%02X:%02X:%02X", (uint8_t)adresseDistante[0], (uint8_t)adresseDistante[1], (uint8_t)adresseDistante[2], (uint8_t)adresseDistante[3], (uint8_t)adresseDistante[4], (uint8_t)adresseDistante[5]);
    #ifdef DEBUG
    Serial.print("mac = ");
    Serial.println(adresse);
    #endif
  }
  else if(event == ESP_SPP_CLOSE_EVT) // déconnexion ?
  {
    connecte = false;
    memset(adresseDistante, 0, ESP_BD_ADDR_LEN);
    topBar->SetStatusRight(HeaderBar::WirelessStatuses::NoSignal);
    boutonSonnerie->Hide();
  }
}

/**
* @brief Lit et affiche le RSSI de la connexion sans fil Bluetooth
*
*/
void lireNiveauBluetooth()
{
  if(connecte)
  {
    esp_bt_gap_read_rssi_delta(adresseDistante);
    if(rssi >= ESP_BT_GAP_RSSI_HIGH_THRLD)
    {
        topBar->SetStatusRight(HeaderBar::WirelessStatuses::FullSignal);
    }
    else if(rssi >= (ESP_BT_GAP_RSSI_HIGH_THRLD-10))
    {
        topBar->SetStatusRight(HeaderBar::WirelessStatuses::MediumSignal);
    }
    else if(rssi >= ESP_BT_GAP_RSSI_LOW_THRLD)
    {
        topBar->SetStatusRight(HeaderBar::WirelessStatuses::WeakSignal);
    }
    else
    {
        topBar->SetStatusRight(HeaderBar::WirelessStatuses::NoSignal);
    }
  }
}

/**
* @brief Lit et affiche le niveau de batterie
*
*/
void lireNiveauBatterie()
{
  if(!M5.Power.canControl())
  {
    #ifdef DEBUG
    Serial.println("Pas de controle de la batterie");
    #endif
  }
  else
  {
    uint8_t bat = M5.Power.getBatteryLevel();
    if(bat >= 75)
    {
        topBar->SetStatusLeft(HeaderBar::BatteryStatuses::FullBattery);
    }
    else if(bat >= 50)
    {
        topBar->SetStatusLeft(HeaderBar::BatteryStatuses::MediumBattery);
    }
    else if(bat >= 25)
    {
        topBar->SetStatusLeft(HeaderBar::BatteryStatuses::WeakBattery);
    }
    else
    {
        topBar->SetStatusLeft(HeaderBar::BatteryStatuses::LowBattery);
    }
    #ifdef DEBUG
    char msg[8];
    sprintf(msg, "%d", bat);
    if (M5.Power.isCharging())
      Serial.println("batterie en charge");
    Serial.print("niveau batterie : ");
    Serial.print(msg);
    Serial.println(" %");
    #endif
  }
}

/**
 * @brief Initialise le Bluetooth
 *
 */
void initialiserBluetooth()
{
  SerialBT.begin(PERIPHERIQUE_BLUETOOTH);
  esp_bt_gap_register_callback(getRSSI);
  SerialBT.register_callback(getEtatBluetooth);
  const uint8_t* adresseLocale = esp_bt_dev_get_address();
  memcpy(adressePomodoro, adresseLocale, ESP_BD_ADDR_LEN);
  char adresse[32];
  sprintf(adresse, "%02X:%02X:%02X:%02X:%02X:%02X", (uint8_t)adressePomodoro[0], (uint8_t)adressePomodoro[1], (uint8_t)adressePomodoro[2], (uint8_t)adressePomodoro[3], (uint8_t)adressePomodoro[4], (uint8_t)adressePomodoro[5]);
  adresseBluetooth = String(adresse);
  #ifdef DEBUG
  Serial.print("mac bluetooth = ");
  Serial.println(adresse);
  #endif
}

/**
 * @brief Initialise les préférences stockées
 *
 */
void initialiserPreferences()
{
  // initialise le stockage interne
  preferences.begin("eeprom", false); // false pour r/w

  // récupère les données stockées à partir des clés
  String valeur;
  valeur = preferences.getString("nom", String());
  if(!valeur.isEmpty())
    nomUtilisateur = valeur.c_str();
  valeur = preferences.getString("prenom", String());
  if(!valeur.isEmpty())
    prenomUtilisateur = valeur.c_str();
  int etat = preferences.getInt("etatPomodoro", (int)Occupe);
  setEtatPomodoro(etat);
  etat = preferences.getInt("sonnette", (int)OFF);
  setActivationSonnette(etat);
  etat = preferences.getInt("presence", (int)OFF);
  setActivationDetecteurPresence(etat);
  valeur = preferences.getString("nomTache", String());
  if(!valeur.isEmpty())
    nomTache = valeur.c_str();
  #ifdef DEBUG
  Serial.println("[initialiserPreferences]");
  Serial.print("nomUtilisateur = ");
  Serial.println(nomUtilisateur.c_str());
  Serial.print("prenomUtilisateur = ");
  Serial.println(prenomUtilisateur.c_str());
  Serial.print("nomTache = ");
  Serial.println(nomTache.c_str());
  Serial.print("etatPomodoro = ");
  Serial.println((int)etatPomodoro);
  Serial.print("activationSonnette = ");
  Serial.println((int)activationSonnette);
  #endif
}

/**
 * @brief Affiche les informations pour l'écran d'accueil
 *
 */
void afficherAccueil()
{
  labelEtatPomodoro->Show();
  afficherInformationsUtilisateur();
  afficherAdresse();
  afficherTache();
  topBar->SetStatusLeft(HeaderBar::BatteryStatuses::FullBattery);
  topBar->SetStatusRight(HeaderBar::WirelessStatuses::NoSignal);
  boutonSonnerie->Hide();
  screen->Draw();
}

/**
 * @brief Affiche les informations sur l'utilisateur
 *
 */
void afficherInformationsUtilisateur(bool redraw)
{
  nettoyerChaines();
  labelUtilisateur->SetText(prenomUtilisateur + " " + nomUtilisateur);
  if(redraw)
    screen->Draw();
}

/**
 * @brief Affiche la tâche
 *
 */
void afficherTache(bool redraw)
{
  nettoyerChaines();
  Serial.print("nomTache.length() = ");
  Serial.println(nomTache.length());
  labelNomTache->SetText(nomTache);
  if(redraw)
    screen->Draw();
}

void afficherAdresse(bool redraw)
{
  nettoyerChaines();
  labelAdresse->SetText(adresseBluetooth.c_str());
  if(redraw)
    screen->Draw();
}

void rafraichirEcran(uint32_t tempo)
{
  M5.update();
  screen->Run();
  //delay(tempo);
}

// Fonctions utilitaires
int count(const String& str, const String& sub)
{
    if (sub.length() == 0) return 0;
    int count = 0;
    for (int offset = str.indexOf(sub); offset != -1; offset = str.indexOf(sub, offset + sub.length()))
    {
        ++count;
    }
    return count;
}

void nettoyerCaractere(std::string &chaine, std::string caractere, char remplacement)
{
  std::size_t found = chaine.find(caractere);
  while (found !=std::string::npos )
  {
    chaine.replace(found, 1, 1, remplacement);
    found = chaine.find(caractere);
  }
}

void nettoyerChaines()
{
  nettoyerCaractere(nomUtilisateur, "é", 'e');
  nettoyerCaractere(nomUtilisateur, "è", 'e');
  nettoyerCaractere(nomUtilisateur, "ê", 'e');
  nettoyerCaractere(prenomUtilisateur, "é", 'e');
  nettoyerCaractere(prenomUtilisateur, "è", 'e');
  nettoyerCaractere(prenomUtilisateur, "ê", 'e');
  nettoyerCaractere(nomTache, "é", 'e');
  nettoyerCaractere(nomTache, "è", 'e');
  nettoyerCaractere(nomTache, "ê", 'e');
  nettoyerCaractere(nomTache, "à", 'a');
}
