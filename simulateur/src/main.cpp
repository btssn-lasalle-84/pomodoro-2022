#include <Arduino.h>
#include "BluetoothSerial.h"
#include <M5Stack.h>
#include "pomodoro.h"

EtatPomodoro etatPrecedent;

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
  initialiserSD();

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
    #ifdef DEBUG
    if(typeTrame > Inconnu && typeTrame < NB_TRAMES)
      Serial.println("Trame : " + getNomTrame(typeTrame));
    #endif
    switch (typeTrame)
    {
      case Inconnu:
        envoyerTrameErreur(ERREUR_TRAME_INCONNUE);
        break;
      case TypeTrame::START:
        if(getEtatPomodoro() == EnAttente)
        {
          envoyerTrameAcquittement();
          setEtatPomodoro(EnCours);
          envoyerTrameEtat((int)getEtatPomodoro());
          etatPrecedent = getEtatPomodoro();
          #ifdef DEBUG
          Serial.println("Démarrage tâche");
          #endif
        }
        break;
      case TypeTrame::PAUSE_COURTE:
        if(getEtatPomodoro() == EnAttente)
        {
          envoyerTrameAcquittement();
          setEtatPomodoro(EnCourtePause);
          envoyerTrameEtat((int)getEtatPomodoro());
          etatPrecedent = getEtatPomodoro();
          #ifdef DEBUG
          Serial.println("Tâche en pause courte");
          #endif
        }
        break;
      case TypeTrame::PAUSE_LONGUE:
        if(getEtatPomodoro() == EnAttente)
        {
          envoyerTrameAcquittement();
          setEtatPomodoro(EnLonguePause);
          envoyerTrameEtat((int)getEtatPomodoro());
          etatPrecedent = getEtatPomodoro();
          #ifdef DEBUG
          Serial.println("Tâche en pause longue");
          #endif
        }
        break;
      case TypeTrame::ATTENTE:
        if(getEtatPomodoro() == EnCours || getEtatPomodoro() == EnCourtePause || getEtatPomodoro() == EnLonguePause)
        {
          envoyerTrameAcquittement();
          setEtatPomodoro(Gele);
          envoyerTrameEtat((int)getEtatPomodoro());
          #ifdef DEBUG
          Serial.println("Tâche gelée");
          #endif
        }
        break;
      case TypeTrame::RESUME:
        if(getEtatPomodoro() == Gele)
        {
          envoyerTrameAcquittement();
          setEtatPomodoro(etatPrecedent);
          envoyerTrameEtat((int)getEtatPomodoro());
          #ifdef DEBUG
          Serial.println("Tâche en reprise");
          #endif
        }
        break;
      case TypeTrame::STOP:
        if(getEtatPomodoro() != Termine)
        {
          #ifdef DEBUG
          if(getEtatPomodoro() != EnCours)
            Serial.println("Tâche terminée");
          else if(getEtatPomodoro() != EnCourtePause)
            Serial.println("Pause courte terminée");
          else if(getEtatPomodoro() != EnLonguePause)
            Serial.println("Pause longue terminée");
          #endif
          envoyerTrameAcquittement();
          if(getEtatPomodoro() != EnCours)
            setEtatPomodoro(Termine);
          else if(getEtatPomodoro() != EnCourtePause)
            setEtatPomodoro(FinCourtePause);
          else if(getEtatPomodoro() != EnLonguePause)
            setEtatPomodoro(FinLonguePause);
          envoyerTrameEtat((int)getEtatPomodoro());
          setEtatPomodoro(EnAttente);
          envoyerTrameEtat((int)getEtatPomodoro());
        }
        break;
      case TypeTrame::RESET:
        envoyerTrameAcquittement();
        setEtatPomodoro(EnAttente);
        envoyerTrameEtat((int)getEtatPomodoro());
        #ifdef DEBUG
        Serial.println("Tâche ou pause annulée");
        #endif
        break;
      case TypeTrame::SET:
        if(getEtatPomodoro() == EnAttente)
        {
          #ifdef DEBUG
          Serial.println("Configuration");
          #endif
          int nbParametres = compterParametres(trame);
          #ifdef DEBUG_VERIFICATION
          Serial.print("nbParametres = ");
          Serial.println(nbParametres);
          String champ;
          for(int i=0;i<nbParametres+1;++i)
          {
            champ = extraireChamp(trame, i);
            Serial.print(i);
            Serial.print(" = ");
            Serial.println(champ);
          }
          #endif
          if(nbParametres > CHAMP_TYPES_CONFIGURATION)
          {
            String type = extraireChamp(trame, CHAMP_TYPES_CONFIGURATION);
            #ifdef DEBUG_VERIFICATION
            Serial.print("type = ");
            Serial.println(type);
            #endif
            TypeConfiguration typeConfiguration = verifierTypeConfiguration(type);
            switch(typeConfiguration)
            {
              case Invalide:
                envoyerTrameErreur(ERREUR_TYPE_INCONNU);
                break;
              case TACHE:
                if(configrerTache(trame))
                {
                  envoyerTrameAcquittement();
                }
                else
                {
                  envoyerTrameErreur(ERREUR_CONFIGURATION);
                }
                break;
              case UTILISATEUR:
                if(configurerUtilisateur(trame))
                {
                  envoyerTrameAcquittement();
                }
                else
                {
                  envoyerTrameErreur(ERREUR_CONFIGURATION);
                }
                break;
              case POMODORO:
                if(configurerPomodoro(trame))
                {
                  envoyerTrameAcquittement();
                }
                else
                {
                  envoyerTrameErreur(ERREUR_CONFIGURATION);
                }
                break;
              case SONNETTE:
                if(configurerSonnette(trame))
                {
                  envoyerTrameAcquittement();
                }
                else
                {
                  envoyerTrameErreur(ERREUR_CONFIGURATION);
                }
                break;
              default:
                #ifdef DEBUG
                Serial.println("Type invalide !");
                #endif
                break;
            }
          }
        }
        break;
      case TypeTrame::ACK:
        break;
      default:
        #ifdef DEBUG
        Serial.println("Trame invalide !");
        #endif
        break;
    }
  }

  if(estEcheance(5000))
  {
    lireNiveauBluetooth();
    lireNiveauBatterie();
  }

  rafraichirEcran();
}
