#include <Arduino.h>
#include "BluetoothSerial.h"
#include <M5Stack.h>
#include "pomodoro.h"

/**
 * @brief Initialise les ressources
 *
 * @fn setup()
 */
void setup()
{
  M5.begin(true, false, true, true);
  M5.Power.begin();

  #ifdef DEBUG
  Serial.println(F("Simulateur Bluetooth POMODORO"));
  #endif

  // initialise le support SD
  //initialiserSD();

  // initialise l'écran
  initialiserEcran();

  // initialise le bluetooth
  initialiserBluetooth();

  // Initialise les préférences stockées
  initialiserPreferences();

  afficherAccueil();
}

/**
 * @brief Boucle principale
 *
 * @fn loop()
 */
void loop()
{
  String trame;
  TypeTrame typeTrame;

  if(lireTrame(trame))
  {
    typeTrame = verifierTrame(trame);
    extraireParametres(trame, typeTrame);
  }

  if(getChangementEtatPomodoro())
  {
    envoyerTrame();
    setChangementEtatPomodoro(false);
  }

  if(estEcheance(5000))
  {
    lireNiveauBluetooth();
    lireNiveauBatterie();
  }

  rafraichirEcran();
}
