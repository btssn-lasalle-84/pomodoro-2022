#include "pomodoro.h"
#include "temps.h"
#include <Ticker.h>

// Bluetooth
static BluetoothSerial SerialBT; //!< objet pour la communication Bluetooth
static bool connecte = false; //!< état de la connexion Bluetooth
const String nomsTrame[TypeTrame::NB_TRAMES] = { "START", "PAUSE_COURTE", "PAUSE_LONGUE", "ATTENTE", "RESUME", "STOP", "RESET", "SET", "ALIVE", "ACK", "ERREUR", "ETAT" }; //!< nom des trames dans le protocole
static String entete = String(EN_TETE_TRAME); // caractère séparateur
static String separateur = String(DELIMITEUR_CHAMP); // caractère séparateur
static String delimiteurFin = String(DELIMITEURS_FIN); // fin de trame
const String nomsTypeConfiguration[TypeConfiguration::NB_TYPES_CONFIGURATION] = { "TACHE", "UTILISATEUR", "POMODORO", "SONNETTE" }; //!< nom des type de configuration
static int rssi = 0; //!< le niveau RSSI du Bluetooth
static String adresseBluetooth; //!< l'adresse MAC Bluetooth du Pomodoro
static uint8_t adresseDistante[ESP_BD_ADDR_LEN] = {0, 0, 0, 0, 0, 0}; //!< l'adresse MAC du Bluetooth connecté
static uint8_t adressePomodoro[ESP_BD_ADDR_LEN] = {0, 0, 0, 0, 0, 0}; //!< l'adresse MAC Bluetooth du Pomodoro

// Pomodoro
static Pomodoro pomodoro;
static int nbPomodori = 0;
static EtatPomodoro etatPrecedent;
static bool changementEtatPomodoro = false; //!< indique un changement d'état du Pomodoro
Temps tempsHorloge;

// Preferences
static Preferences preferences;  //!< objet pour le stockage interne
static std::string nomUtilisateur; //!< le nom de l'utilisateur
static std::string prenomUtilisateur; //!< le prenom de l'utilisateur
static std::string nomTache; //!< le nom de la tâche

// Timers
Ticker timerPomodoro;          //!< le timer pour gérer un pomodoro
Ticker timerHorloge;          //!< le timer pour gérer l'affichage du temps

// GUI
static AppScreen* screen;
static HeaderBar* topBar;
static Codingfield::UI::Label* labelUtilisateur;
static Codingfield::UI::Label* labelEtatPomodoro;
static Codingfield::UI::Label* labelNomTache;
static Codingfield::UI::Label* labelNbPomodori;
static Codingfield::UI::Button* boutonPomodoro;
static Codingfield::UI::Button* boutonOff;
static Codingfield::UI::Image* logoLasalle;
static Codingfield::UI::Image* logoPomodoro;
static std::string titre = "Pomodoro";

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
    Serial.print("< ");
    if(trame.indexOf('\r') != -1)
      trame.remove(trame.indexOf('\r'), 2);
    Serial.println(trame);
    trame.concat(delimiteurFin);
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
  String format;

  for(int i=0;i<TypeTrame::NB_TRAMES;i++)
  {
    //Format : ${TYPE}\r\n
    format = entete + nomsTrame[i];
    #ifdef DEBUG_VERIFICATION
    Serial.print("Verification trame : ");
    Serial.print(format);
    #endif
    if(trame.startsWith(format))
    {
      return (TypeTrame)i;
    }
    else
    {
      #ifdef DEBUG_VERIFICATION
      Serial.println("");
      #endif
    }
  }
  #ifdef DEBUG_VERIFICATION
  Serial.println("Type de trame : inconnu");
  #endif
  return Inconnu;
}

TypeConfiguration verifierTypeConfiguration(String &type)
{
  String typeConfiguration;

  for(int i=0;i<TypeConfiguration::NB_TYPES_CONFIGURATION;i++)
  {
    //Format : $SET;{TYPE};...\r\n
    typeConfiguration = nomsTypeConfiguration[i];
    #ifdef DEBUG_VERIFICATION
    Serial.print("Verification type : ");
    Serial.print(typeConfiguration);
    #endif
    if(type == typeConfiguration)
    {
      return (TypeConfiguration)i;
    }
    else
    {
      #ifdef DEBUG_VERIFICATION
      Serial.println("");
      #endif
    }
  }
  #ifdef DEBUG_VERIFICATION
  Serial.println("Type inconnu");
  #endif
  return Invalide;
}

String getNomTrame(TypeTrame typeTrame)
{
  return nomsTrame[typeTrame];
}

/**
 * @brief Envoie une trame Pomodoro via le Bluetooth
 *
 * @fn envoyerTrame()
 */
void envoyerTrame()
{
  char trameEnvoi[64];

  // Trame envoyée : $ETAT;SONNETTE;PRESENCE;MODE_SONNETTE;MODE_PRESENCE\r\n
  //sprintf((char *)trameEnvoi, "$%d;%d;%d;%d;%d\r\n", (int)etatPomodoro, (int)sonnette, (int)presence, (int)activationSonnette, (int)activationDetecteurPresence);
  //SerialBT.write((uint8_t*)trameEnvoi, strlen((char *)trameEnvoi));
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
  char trameEnvoi[64];

  // Trame envoyée : $ALIVE\r\n
  sprintf((char *)trameEnvoi, "%s%s\r\n", entete.c_str(), nomsTrame[TypeTrame::ALIVE].c_str());
  SerialBT.write((uint8_t*)trameEnvoi, strlen((char *)trameEnvoi));
  #ifdef DEBUG
  Serial.println(String(trameEnvoi));
  #endif
}

/**
 * @brief Envoie une trame d'erreur via le Bluetooth
 *
 */
void envoyerTrameErreur(int code)
{
  char trameEnvoi[64];

  //Format : $ERREUR;{CODE}\r\n
  sprintf((char *)trameEnvoi, "%s%s;%d\r\n", entete.c_str(), nomsTrame[TypeTrame::ERREUR].c_str(), code);

  SerialBT.write((uint8_t*)trameEnvoi, strlen((char *)trameEnvoi));
  #ifdef DEBUG
  String trame = String(trameEnvoi);
  trame.remove(trame.indexOf("\r"), 2);
  Serial.print("> ");
  Serial.println(trame);
  #endif
}

/**
 * @brief Envoie une trame d'acquittement via le Bluetooth
 *
 */
void envoyerTrameAcquittement()
{
  char trameEnvoi[64];

  //Format : $ACK\r\n
  sprintf((char *)trameEnvoi, "%s%s\r\n", entete.c_str(), nomsTrame[TypeTrame::ACK].c_str());

  SerialBT.write((uint8_t*)trameEnvoi, strlen((char *)trameEnvoi));
  #ifdef DEBUG
  String trame = String(trameEnvoi);
  trame.remove(trame.indexOf("\r"), 2);
  Serial.print("> ");
  Serial.println(trame);
  #endif
}

/**
 * @brief Envoie une trame d'état via le Bluetooth
 *
 */
void envoyerTrameEtat(int etat)
{
  char trameEnvoi[64];

  //Format : $ETAT;{ETAT}\r\n
  sprintf((char *)trameEnvoi, "%s%s;%d\r\n", entete.c_str(), nomsTrame[TypeTrame::ETAT].c_str(), etat);

  SerialBT.write((uint8_t*)trameEnvoi, strlen((char *)trameEnvoi));
  #ifdef DEBUG
  String trame = String(trameEnvoi);
  trame.remove(trame.indexOf("\r"), 2);
  Serial.print("> ");
  Serial.println(trame);
  #endif
}

/**
 * @brief Extrait un champ d'une trame Pomodoro
 *
 * @fn extraireChamp(String &trame, unsigned int numeroChamp)
 * @param trame
 * @param numeroChamp
 * @return String
 */
String extraireChamp(String &trame, unsigned int numeroChamp)
{
  String champ;
  unsigned int compteurCaractere = 0;
  unsigned int compteurDelimiteur = 0;
  char fin = '\n';

  if(delimiteurFin.length() > 0)
    fin = delimiteurFin[0];

  for(unsigned int i = 0; i < trame.length(); i++)
  {
    if(trame[i] == separateur[0] || trame[i] == fin)
    {
      compteurDelimiteur++;
    }
    else if(compteurDelimiteur == numeroChamp)
    {
        champ += trame[i];
        compteurCaractere++;
    }
  }

  return champ;
}

void setActivationPomodoro(bool etat)
{
  pomodoro.actif = etat;
  preferences.putInt("actif", (int)pomodoro.actif);
}

void enregistreEtatPomodoro(EtatPomodoro etat)
{
  etatPrecedent = pomodoro.etat;
  pomodoro.etat = etat;
  changementEtatPomodoro = true;
  preferences.putInt("etat", (int)pomodoro.etat);
}

/**
 * @brief Met à jour l'état du Pomodoro
 *
 * @fn setEtatPomodoro(int etat)
 * @param etat
 */
void setEtatPomodoro(int etat)
{
  std::string texteEtat = "";

  if(pomodoro.etat != (EtatPomodoro)etat)
  {
    enregistreEtatPomodoro((EtatPomodoro)etat);
    switch(pomodoro.etat)
    {
    case EnAttente:
      arreterTimerHorloge();
      setActivationPomodoro(false);
      tempsHorloge.reset();
      boutonPomodoro->SetText("START");
      break;
    case Termine:
      arreterTimerHorloge();
      setActivationPomodoro(false);
      tempsHorloge.reset();
      texteEtat = "Fini";
      labelEtatPomodoro->SetText("00:00");
      //labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Bleu);
      boutonPomodoro->SetText("START");
      if(pomodoro.sonnette)
      {
        M5.Speaker.tone(432, 500);
      }
      /**
       * @todo gérer l'autopause et l'autopomodoro
       *
       */
      ++nbPomodori;
      preferences.putInt("nb", nbPomodori);
      afficherNbPomodori();
      if(nbPomodori == pomodoro.nbPomodori)
      {
        nbPomodori = 0;
        preferences.putInt("nb", nbPomodori);
      }
      break;
    case EnCours:
      if(!pomodoro.actif)
      {
        tempsHorloge.setTemps(pomodoro.duree);
        labelEtatPomodoro->SetText(tempsHorloge.getMMSS().c_str());
        demarrerTimerPomodoro(pomodoro.duree);
        setActivationPomodoro(true);
      }
      demarrerTimerHorloge();
      texteEtat = "EnCours";
      labelEtatPomodoro->SetFont(&FreeSansBold24pt7b);
      labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Rouge);
      boutonPomodoro->SetText("PAUSE");
      break;
    case EnCourtePause:
      if(!pomodoro.actif)
      {
        tempsHorloge.setTemps(pomodoro.pauseCourte);
        labelEtatPomodoro->SetText(tempsHorloge.getMMSS().c_str());
        demarrerTimerPomodoro(pomodoro.pauseCourte);
        setActivationPomodoro(true);
      }
      demarrerTimerHorloge();
      texteEtat = "PAUSE";
      labelEtatPomodoro->SetFont(&FreeSans24pt7b);
      labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Jaune);
      boutonPomodoro->SetText("PAUSE");
      break;
    case FinCourtePause:
      arreterTimerHorloge();
      setActivationPomodoro(false);
      tempsHorloge.reset();
      texteEtat = "Fini";
      labelEtatPomodoro->SetText("00:00");
      //labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Bleu);
      boutonPomodoro->SetText("START");
      if(pomodoro.sonnette)
      {
        M5.Speaker.tone(1230, 500);
      }
      break;
    case EnLonguePause:
      if(!pomodoro.actif)
      {
        tempsHorloge.setTemps(pomodoro.pauseLongue);
        labelEtatPomodoro->SetText(tempsHorloge.getMMSS().c_str());
        demarrerTimerPomodoro(pomodoro.pauseLongue);
        setActivationPomodoro(true);
      }
      demarrerTimerHorloge();
      texteEtat = "PAUSE";
      labelEtatPomodoro->SetFont(&FreeSans24pt7b);
      labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Vert);
      boutonPomodoro->SetText("PAUSE");
      break;
    case FinLonguePause:
      arreterTimerHorloge();
      setActivationPomodoro(false);
      tempsHorloge.reset();
      texteEtat = "Fini";
      labelEtatPomodoro->SetText("00:00");
      //labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Bleu);
      boutonPomodoro->SetText("START");
      if(pomodoro.sonnette)
      {
        M5.Speaker.tone(1230, 500);
      }
      break;
    case Gele:
      arreterTimerHorloge();
      texteEtat = "EnAttente";
      labelEtatPomodoro->SetFont(&FreeSans24pt7b);
      labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Orange);
      boutonPomodoro->SetText("REPRISE");
      break;
    case Reprise:
      demarrerTimerHorloge();
      texteEtat = "EnCours";
      boutonPomodoro->SetText("PAUSE");
      break;
    default:
      break;
    }
    //labelEtatPomodoro->SetText(texteEtat);
    controlerAffichageBouton();
  }
}

EtatPomodoro getEtatPomodoro()
{
  return pomodoro.etat;
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
  if(pomodoro.sonnette != (Etat)etat)
  {
    pomodoro.sonnette = (Etat)etat;
    preferences.putInt("sonnette", (int)pomodoro.sonnette);
  }
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

bool configrerTache(String &trame)
{
  int nbParametres = compterParametres(trame);
  if(nbParametres == NB_PARAMETRES_TACHE)
  {
    String champ = extraireChamp(trame, CHAMP_NOM_TACHE);
    size_t retour = preferences.putString("nomTache", champ.c_str());
    if(retour == champ.length())
    {
      nomTache = champ.c_str();
      #ifdef DEBUG
      Serial.print("nomTache : ");
      Serial.println(nomTache.c_str());
      #endif
      afficherTache(true);
      return true;
    }
  }
  return false;
}

bool configurerUtilisateur(String &trame)
{
  int nbParametres = compterParametres(trame);
  if(nbParametres == NB_PARAMETRES_UTILISATEUR)
  {
    String champ = extraireChamp(trame, CHAMP_NOM_UTILISATEUR);
    size_t retour = preferences.putString("nom", champ.c_str());
    if(retour == champ.length())
      nomUtilisateur = champ.c_str();
    #ifdef DEBUG
    Serial.print("nomUtilisateur : ");
    Serial.println(nomUtilisateur.c_str());
    #endif
    champ = extraireChamp(trame, CHAMP_PRENOM_UTILISATEUR);
    retour = preferences.putString("prenom", champ.c_str());
    if(retour == champ.length())
      prenomUtilisateur = champ.c_str();
    #ifdef DEBUG
    Serial.print("prenomUtilisateur : ");
    Serial.println(prenomUtilisateur.c_str());
    #endif
    afficherInformationsUtilisateur(true);
    return true;
  }
  return false;
}

bool configurerPomodoro(String &trame)
{
  int nbParametres = compterParametres(trame);
  if(nbParametres == NB_PARAMETRES_POMODORO)
  {
    String champ = extraireChamp(trame, CHAMP_DUREE);
    size_t retour = preferences.putInt("duree", champ.toInt());
    if(retour != 0)
    {
      pomodoro.duree = champ.toInt();
    }
    champ = extraireChamp(trame, CHAMP_PAUSE_COURTE);
    retour = preferences.putInt("pauseCourte", champ.toInt());
    if(retour != 0)
    {
      pomodoro.pauseCourte = champ.toInt();
    }
    champ = extraireChamp(trame, CHAMP_PAUSE_LONGUE);
    retour = preferences.putInt("pauseLongue", champ.toInt());
    if(retour != 0)
    {
      pomodoro.pauseLongue = champ.toInt();
    }
    champ = extraireChamp(trame, CHAMP_NB_POMODORI);
    retour = preferences.putInt("nbPomodori", champ.toInt());
    if(retour != 0)
    {
      pomodoro.nbPomodori = champ.toInt();
    }
    champ = extraireChamp(trame, CHAMP_AUTO_POMODORO);
    retour = preferences.putInt("autoPomodoro", champ.toInt());
    if(retour != 0)
    {
      pomodoro.autoPomodoro = champ.toInt();
    }
    champ = extraireChamp(trame, CHAMP_AUTO_PAUSE);
    retour = preferences.putInt("autoPause", champ.toInt());
    if(retour != 0)
    {
      pomodoro.autoPause = champ.toInt();
    }
    champ = extraireChamp(trame, CHAMP_AUTO_PAUSE);
    retour = preferences.putInt("mode", (Mode)champ.toInt());
    if(retour != 0)
    {
      pomodoro.mode = (Mode)champ.toInt();
    }
    labelEtatPomodoro->SetFont(&FreeSansBold24pt7b);
    tempsHorloge.setTemps(pomodoro.duree);
    labelEtatPomodoro->SetText(tempsHorloge.getMMSS().c_str());
    labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Rouge);
    controlerAffichageBouton();
    return true;
  }
  return false;
}

bool configurerSonnette(String &trame)
{
  int nbParametres = compterParametres(trame);
  if(nbParametres == NB_PARAMETRES_SONNETTE)
  {
    String champ = extraireChamp(trame, CHAMP_ACTIVATION_SONNETTE);
    size_t retour = preferences.putInt("sonnette", champ.toInt());
    if(retour != 0)
    {
      pomodoro.sonnette = champ.toInt();
      setActivationSonnette(pomodoro.sonnette);
    }
    #ifdef DEBUG
    Serial.print("sonnette : ");
    Serial.println(pomodoro.sonnette);
    #endif
    return true;
  }
  return false;
}

/**
* @brief Fontion de callback pour le clic sur le bouton
*
* @fn cliquerBoutonPomodoro(Button* widget, IdButton button, bool pressed)
*
* @param widget le widget sur lequel on a cliqué
* @param button l'identifiant du bouton
* @param pressed vrai si appui sinon faux pour relachement
*
* @return bool vrai
*/
bool cliquerBoutonPomodoro(Codingfield::UI::Button* widget, IdButton button, bool pressed)
{
  if(pressed)
  {
    /*#ifdef DEBUG
    Serial.print("etatPomodoro = ");
    Serial.println((int)pomodoro.etat);
    #endif*/
    widget->SetBackgroundColor(WHITE);
    widget->SetTextColor(DARKGREY);
    M5.Speaker.tone(432, 400);
    //delay(400);
    //M5.Speaker.tone(1230, 400);
    switch(pomodoro.etat)
    {
    case EnAttente:
      setEtatPomodoro(EnCours);
      envoyerTrameEtat((int)getEtatPomodoro());
      break;
    case EnCours:
      setEtatPomodoro(Gele);
      envoyerTrameEtat((int)getEtatPomodoro());
      break;
    case EnCourtePause:
      setEtatPomodoro(Gele);
      envoyerTrameEtat((int)getEtatPomodoro());
      break;
    case EnLonguePause:
      setEtatPomodoro(Gele);
      envoyerTrameEtat((int)getEtatPomodoro());
      break;
    case Gele:
      setEtatPomodoro(etatPrecedent);
      envoyerTrameEtat((int)getEtatPomodoro());
      break;
    default:
      break;
    }
  }
  else
  {
    widget->SetBackgroundColor(DARKGREY);
    widget->SetTextColor(WHITE);
  }
  return true;
}

void controlerAffichageBouton(bool actif/*=true*/)
{
  if(actif)
  {
    boutonPomodoro->Show();
    boutonPomodoro->EnableControls();
  }
  else
  {
    boutonPomodoro->DisableControls();
    boutonPomodoro->Hide();
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

  screen = new AppScreen(Size(M5_WIDTH, M5_HEIGHT), LIGHTGREY, topBar, nullptr, nullptr);

  logoLasalle = new Codingfield::UI::Image(screen, Point(5, 160), Size(0,0));
  //logoLasalle = new Codingfield::UI::Image(screen, Point(0,25), Size(0,0)); // en fond
  logoLasalle->SetBackgroundColor(LIGHTGREY);
  logoLasalle->SetImage(&SD, "/pomodoro-icone.png"); // icone-pomodoro.png
  //logoLasalle->SetScale(0.25);

  logoPomodoro = new Codingfield::UI::Image(screen, Point(230, 150), Size(0,0));
  logoPomodoro->SetBackgroundColor(LIGHTGREY);
  logoPomodoro->SetImage(&SD, "/pomodoro-logo.png");

  labelUtilisateur = new Codingfield::UI::Label(screen, Point(M5_MARGE_DROITE, M5_MARGE_HAUT), Size(M5_WIDTH - M5_MARGE_GAUCHE - M5_MARGE_GAUCHE, M5_HAUTEUR_TEXTE_12), &FreeSerifBoldItalic12pt7b);
  labelUtilisateur->SetBackgroundColor(LIGHTGREY); // ORANGE YELLOW PURPLE GREEN MAROON
  labelUtilisateur->SetTextColor(BLACK);
  labelUtilisateur->SetText(" ");

  labelEtatPomodoro = new Codingfield::UI::Label(screen, Point(40, 60), Size(240, 60), &FreeSansBold24pt7b);
  labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Rouge); // ORANGE YELLOW PURPLE GREEN MAROON
  labelEtatPomodoro->SetTextColor(WHITE);
  labelEtatPomodoro->SetRounded(true, 4);
  labelEtatPomodoro->Hide();

  labelNomTache = new Codingfield::UI::Label(screen, Point(10, 125), Size(300, 20), &FreeMonoBold9pt7b);
  labelNomTache->SetBackgroundColor(LIGHTGREY);
  labelNomTache->SetTextColor(BLACK);
  labelNomTache->SetText(" ");

  labelNbPomodori = new Codingfield::UI::Label(screen, Point(90, 165), Size(140,20), &FreeSansBold9pt7b);
  labelNbPomodori->SetBackgroundColor(LIGHTGREY);
  labelNbPomodori->SetTextColor(BLACK);
  labelNbPomodori->SetText(" ");

  boutonPomodoro = new Codingfield::UI::Button(screen, Point(100,190), Size(120,40), &FreeSansBold12pt7b);
  boutonPomodoro->SetBackgroundColor(DARKGREY);
  boutonPomodoro->SetTextColor(WHITE);
  boutonPomodoro->SetBorderColor(WHITE);
  boutonPomodoro->SetBorder(true, 1);
  boutonPomodoro->SetText("START");
  boutonPomodoro->SetPressedCallback(Codingfield::UI::ButtonB, cliquerBoutonPomodoro);
  boutonPomodoro->SetReleasedCallback(Codingfield::UI::ButtonB, cliquerBoutonPomodoro);

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
    #ifdef DEBUG_RSSI
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
    controlerAffichageBouton();
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
    boutonPomodoro->Hide();
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
    #ifdef DEBUG_BATTERIE
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
  valeur = preferences.getString("nomTache", String());
  if(!valeur.isEmpty())
    nomTache = valeur.c_str();

  // initialise la partie Pomodoro
  initialiserPomodoro();

  //setEtatPomodoro(pomodoro.etat);
  setEtatPomodoro(EnAttente);
  setActivationSonnette(pomodoro.sonnette);

  labelEtatPomodoro->SetFont(&FreeSansBold24pt7b);
  tempsHorloge.setTemps(pomodoro.duree);
  labelEtatPomodoro->SetText(tempsHorloge.getMMSS().c_str());
  labelEtatPomodoro->SetBackgroundColor(CouleurPomodoro::Rouge);
  controlerAffichageBouton();

  #ifdef DEBUG
  Serial.println("[initialiserPreferences]");
  Serial.print("nomUtilisateur = ");
  Serial.println(nomUtilisateur.c_str());
  Serial.print("prenomUtilisateur = ");
  Serial.println(prenomUtilisateur.c_str());
  Serial.print("nomTache = ");
  Serial.println(nomTache.c_str());
  Serial.print("etatPomodoro = ");
  Serial.println((int)pomodoro.etat);
  Serial.print("activationSonnette = ");
  Serial.println((int)pomodoro.sonnette);
  #endif
}

void initialiserPomodoro()
{
  int id = preferences.getInt("id", 0);
  bool actif = preferences.getInt("actif", 0);
  int sonnette = preferences.getInt("sonnette", 1);
  int etat = preferences.getInt("etat", (EtatPomodoro)EnAttente);
  int duree = preferences.getInt("duree", DUREE_POMODORO);
  int pauseCourte = preferences.getInt("pauseCourte", PAUSE_COURTE_POMODORO);
  int pauseLongue = preferences.getInt("pauseLongue", PAUSE_LONGUE_POMODORO);
  int nbPomodori = preferences.getInt("nbPomodori", NB_POMODORI);
  int autoPomodoro = preferences.getInt("autoPomodoro", 0);
  int autoPause = preferences.getInt("autoPause", 0);
  int mode = preferences.getInt("mode", (Mode)Minuteur);
  int nb = preferences.getInt("nb", 0);
  pomodoro.id = id;
  pomodoro.actif = actif;
  pomodoro.sonnette = sonnette;
  pomodoro.etat = (EtatPomodoro)etat;
  pomodoro.duree = duree;
  pomodoro.pauseCourte = pauseCourte;
  pomodoro.pauseLongue = pauseLongue;
  pomodoro.nbPomodori = nbPomodori;
  pomodoro.autoPomodoro = autoPomodoro;
  pomodoro.autoPause = autoPause;
  pomodoro.mode = (Mode)mode;
  nbPomodori = nb;
}

void initialiserTimers()
{

}

void demarrerTimerPomodoro(int duree)
{
  timerPomodoro.once(duree, terminerTimerPomodoro, pomodoro.id);
}

void arreterTimerPomodoro()
{
  timerPomodoro.detach();
}

void terminerTimerPomodoro(int id)
{
  if(!pomodoro.actif)
    return;
  switch(pomodoro.etat)
  {
  case EnCours:
    #ifdef DEBUG
    Serial.println("Fin du pomodoro");
    #endif
    setEtatPomodoro(Termine);
    envoyerTrameEtat((int)getEtatPomodoro());
    setEtatPomodoro(EnAttente);
    envoyerTrameEtat((int)getEtatPomodoro());
    break;
  case EnCourtePause:
    #ifdef DEBUG
    Serial.println("Fin de la pause courte");
    #endif
    setEtatPomodoro(FinCourtePause);
    envoyerTrameEtat((int)getEtatPomodoro());
    setEtatPomodoro(EnAttente);
    envoyerTrameEtat((int)getEtatPomodoro());
    break;
  case EnLonguePause:
    #ifdef DEBUG
    Serial.println("Fin de la pause longue");
    #endif
    setEtatPomodoro(FinLonguePause);
    envoyerTrameEtat((int)getEtatPomodoro());
    setEtatPomodoro(EnAttente);
    envoyerTrameEtat((int)getEtatPomodoro());
    break;
  default:
    break;
  }
}

void demarrerTimerHorloge()
{
  timerHorloge.attach(1, afficherHorloge); // toutes les s
}

void arreterTimerHorloge()
{
  timerHorloge.detach();
}

void afficherHorloge()
{
  if(tempsHorloge.getValeur() > 0)
    tempsHorloge -= 1;
  #ifdef DEBUG_HORLOGE
  Serial.print(tempsHorloge.getMMSS());
  Serial.print(" -> ");
  Serial.print(tempsHorloge.getValeur());
  Serial.println(" s");
  #endif
  labelEtatPomodoro->SetText(tempsHorloge.getMMSS().c_str());
}

/**
 * @brief Affiche les informations pour l'écran d'accueil
 *
 */
void afficherAccueil()
{
  labelEtatPomodoro->Show();
  afficherInformationsUtilisateur();
  afficherNbPomodori();
  afficherTache();
  std::string titreComplet = std::string(PERIPHERIQUE_BLUETOOTH) + " " + adresseBluetooth.c_str();
  topBar->SetTitle(titreComplet);
  topBar->SetStatusLeft(HeaderBar::BatteryStatuses::FullBattery);
  topBar->SetStatusRight(HeaderBar::WirelessStatuses::NoSignal);
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
  #ifdef DEBUG
  Serial.print("nomTache.length() = ");
  Serial.println(nomTache.length());
  #endif
  if(nomTache.length() > LONGUEUR_MAX)
  {
    nomTache.resize(LONGUEUR_MAX-3);
    nomTache.resize(LONGUEUR_MAX, '.');
  }
  labelNomTache->SetText(nomTache);
  if(redraw)
    screen->Draw();
}

void afficherNbPomodori(bool redraw)
{
  String strNbPomodori = String(nbPomodori);
  labelNbPomodori->SetText(std::string("Pomodori : ") + strNbPomodori.c_str());
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
