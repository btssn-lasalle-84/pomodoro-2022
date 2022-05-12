package com.example.pomodoro;

/**
 * @file PomodoroActivity.java
 * @brief Déclaration de la classe PomodoroActivity
 * @author Teddy ESTABLET
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * @class PomodoroActivity
 * @brief L'activité principale de l'application Pomodoro
 */
public class
PomodoroActivity extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG = "_PomodoroActivity";  //!< TAG pour les logs
    // Pour les tests
    private static final String NOM_MINUTEUR = "pomodoro-1";
    private static final String ADRESSE_MINUTEUR = "A4:CF:12:6D:F3:6E";

    private final static int CODE_DEMANDE_ENABLE_BLUETOOTH = 0;
    private final static int CODE_DEMANDE_BLUETOOTH_CONNECT = 1;
    private final static int CODE_DEMANDE_ACCESS_FINE_LOCATION = 2;

    /**
     * Attributs
     */
    private BluetoothAdapter bluetooth = null;  //!< L'adaptateur Bluetooth de la tablette
    private Peripherique peripherique = null;
    private Handler handler = null;
    private BaseDeDonnees baseDeDonnees = null;
    private Minuteur minuteur; //!< le minuteur
    private Tache tache; //!< une tâche
    private Timer timerMinuteur = null;;
    private TimerTask tacheMinuteur;
    private long dureeEnCours;

    /**
     * Ressources IHM
     */
    private Button boutonDemarrer;//!< Le bouton permettant de démarrer un pomodoro
    private Button boutonEditerTache;//!< Le bouton permettant d'éditer une tâche
    private Button boutonSupprimerTache;//!< Le bouton permettant de supprimer une tâche
    private Button boutonSeConnecterAuPomodoro;//!< Le bouton permettant de se connecter au pomodoro
    private TextView horloge;

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        minuteur = new Minuteur();
        tache = new Tache();

        baseDeDonnees = new BaseDeDonnees(this);
        // Test BDD
        Vector<String> nomColonnes = baseDeDonnees.getNomColonnes();

        initialiserHandler();
        initialiserBluetooth();
        initialiserIHM();
    }

    /**
     * @brief Méthode appelée au démarrage après le onCreate() ou un restart après un onStop()
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    /**
     * @brief Méthode appelée après onStart() ou après onPause()
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    /**
     * @brief Méthode appelée après qu'une boîte de dialogue s'est affichée (on reprend sur un onResume()) ou avant onStop() (activité plus visible)
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    /**
     * @brief Méthode appelée lorsque l'activité n'est plus visible
     */
    @Override
    protected void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop()");
        peripherique.deconnecter();
    }

    /**
     * @brief Méthode appelée à la destruction de l'application (après onStop() et détruite par le système Android)
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    /**
     * @brief Initialise l'activité
     */
    private void initialiserIHM()
    {
        Log.d(TAG, "initialiserIHM()");
        boutonDemarrer = (Button) findViewById(R.id.boutonDemarrer);
        boutonEditerTache = (Button) findViewById(R.id.boutonEditerTache);
        boutonSupprimerTache = (Button) findViewById(R.id.boutonSupprimerTache);
        boutonSeConnecterAuPomodoro = (Button) findViewById(R.id.boutonSeConnecterAuPomodoro);
        horloge = (TextView) findViewById(R.id.horloge);

        boutonDemarrer.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonDemarrer");
                /**
                 * @todo Gérer le minuteur connecté
                 */
            }
        });
        boutonEditerTache.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonEditerTache");
                Log.d(TAG, "[Tache] nom = " + tache.getNom());
                tache.editerTaches();
                Intent IHM_EditerTache = new Intent(PomodoroActivity.this,IHM_EditerTache.class);
                startActivity(IHM_EditerTache);
            }
        });
        boutonSupprimerTache.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonSupprimerTache");
                Log.d(TAG, "[Tache] nom = " + tache.getNom());
                tache.supprimerTache();
                Log.d(TAG, "[Tache] nom = " + tache.getNom());
            }
        });

        boutonSeConnecterAuPomodoro.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if(v.getId() == R.id.boutonSeConnecterAuPomodoro)
                {
                    if(!peripherique.estConnecte())
                        chercherMinuteur();
                    else
                        peripherique.deconnecter();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void initialiserBluetooth()
    {
        bluetooth = BluetoothAdapter.getDefaultAdapter();
        if(bluetooth == null)
        {
            Log.d(TAG, "Bluetooth non disponible");
            Toast.makeText(getApplicationContext(), "Bluetooth non disponible", Toast.LENGTH_SHORT).show();
        }
        else
        {
            //demanderPermissions(Manifest.permission.ACCESS_FINE_LOCATION, CODE_DEMANDE_ACCESS_FINE_LOCATION);
            IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter1.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter1.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            this.registerReceiver(changementEtatBluetooth(), filter1);

            IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter2.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(detectionPeripherique(), filter2);

            if(!bluetooth.isEnabled())
            {
                Toast.makeText(getApplicationContext(), "Bluetooth non activé", Toast.LENGTH_SHORT).show();
                //demanderPermissions(Manifest.permission.BLUETOOTH_CONNECT, CODE_DEMANDE_BLUETOOTH_CONNECT);
                bluetooth.enable();
            }
            else
            {
                //Toast.makeText(getApplicationContext(), "Bluetooth activé", Toast.LENGTH_SHORT).show();
                chercherMinuteur();
            }
        }
    }

    /**
     * @brief Méthode qui permet de chercher un périphérique bluetooth pomodoro déjà appairé
     */
    @SuppressLint("MissingPermission")
    private void chercherMinuteur()
    {
        Set<BluetoothDevice> devices;

        Log.d(TAG,"[chercherMinuteur] liste des périphériques Bluetooth appairés");
        devices = bluetooth.getBondedDevices();
        for(BluetoothDevice device : devices)
        {
            //Log.d(TAG,"[chercherMinuteur] device : " + device.getName() + " [" + device.getAddress() + "]");
            if(device.getName().equals(NOM_MINUTEUR) || device.getAddress().equals(ADRESSE_MINUTEUR))
            {
                Log.d(TAG,"[chercherMinuteur] minuteur trouvé : " + device.getName() + " [" + device.getAddress() + "]");
                initialiserPeripherique();
                return;
            }
        }

        // sinon :
        Log.d(TAG,"[chercherMinuteur] minuteur non trouvé !");
        demarrerRecherche();
    }

    /**
     * @brief Méthode qui permet de lancer la recherche de périphériques
     */
    @SuppressLint("MissingPermission")
    private void demarrerRecherche()
    {
        if(bluetooth.isDiscovering())
            bluetooth.cancelDiscovery();
        bluetooth.startDiscovery();
        Log.d(TAG,"[demarrerRecherche] démarrage découverte bluetooth");
    }

    /**
     * @brief Méthode qui permet de demander le droit de permission location
     */
    private void demanderPermissions(String permission, int code)
    {
        Log.d(TAG, "demanderPermissions()");
        if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{permission}, code);
        }
    }

    /**
     * @brief Méthode qui permet de gérer la réponse à la demande de permission location
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult() code : " + requestCode);
        switch(requestCode)
        {
            case CODE_DEMANDE_ENABLE_BLUETOOTH:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "onRequestPermissionsResult() permission ENABLE_BLUETOOTH accordée");
                }
                else
                {
                    onDestroy();
                }
                return;
            }
            case CODE_DEMANDE_BLUETOOTH_CONNECT:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "onRequestPermissionsResult() permission BLUETOOTH_CONNECT accordée");
                }
                else
                {
                    onDestroy();
                }
                return;
            }
            case CODE_DEMANDE_ACCESS_FINE_LOCATION:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Log.d(TAG, "onRequestPermissionsResult() permission ACCESS_FINE_LOCATION accordée");
                }
                else
                {
                    onDestroy();
                }
                return;
            }
        }
    }

    /**
     * @brief Détection de changement d'état du bluetooth
     * @return BroadcastReceiver le receveur de Bluetooth
     */
    private final BroadcastReceiver changementEtatBluetooth()
    {
        BroadcastReceiver receiverEtatBluetooth = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                final String action = intent.getAction();
                Log.d(TAG, "[changementEtatBluetooth] action : " + action);
                if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state)
                    {
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG, "[changementEtatBluetooth] bluetooth désactivé !");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG, "[changementEtatBluetooth] bluetooth en cours de désactivation !");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG, "[changementEtatBluetooth] bluetooth activé !");
                            chercherMinuteur();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Log.d(TAG, "[changementEtatBluetooth] bluetooth en cours d'activation !");
                            break;
                        default:
                            Log.d(TAG, "[changementEtatBluetooth] etat : " + state);
                            break;
                    }
                }
                else if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
                {
                    Log.d(TAG, "[changementEtatBluetooth] bluetooth déconnecté !");
                }
                else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
                {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    switch(state)
                    {
                        case BluetoothDevice.BOND_NONE:
                            Log.d(TAG, "[changementEtatBluetooth] non appairé !");
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            Log.d(TAG, "[changementEtatBluetooth] en cours d'appairage !");
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            Log.d(TAG, "[changementEtatBluetooth] appairé !");
                            break;
                    }
                }
            }
        };

        return receiverEtatBluetooth;
    }

    /**
     * @brief Détection des périphériques Bluetooth
     * @return BroadcastReceiver le receveur
     */
    private final BroadcastReceiver detectionPeripherique()
    {
        BroadcastReceiver receiverDetectionBluetooth = new BroadcastReceiver()
        {
            @SuppressLint("MissingPermission")
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                Log.d(TAG, "[detectionPeripherique] action : " + action);

                if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
                {
                    Log.d(TAG, "[detectionPeripherique] découverte démarrée");
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
                {
                    Log.d(TAG, "[detectionPeripherique] découverte terminée");
                }
                else if(BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //demanderPermissions(Manifest.permission.BLUETOOTH_CONNECT, CODE_DEMANDE_BLUETOOTH_CONNECT);
                    Log.d(TAG, "[detectionPeripherique] périphérique détecté : " + device.getName() + " [" + device.getAddress() + "]");
                }
            }
        };

        return receiverDetectionBluetooth;
    };

    private void initialiserPeripherique()
    {
        Log.d(TAG,"initialiserPeripherique()");
        if(peripherique == null)
            peripherique = new Peripherique(handler);
        if(peripherique.rechercherPomodoro(NOM_MINUTEUR))
        {
            if(!peripherique.estConnecte())
                peripherique.connecter();
        }
    }

    private void initialiserHandler()
    {
        this.handler = new Handler(this.getMainLooper())
        {
            @Override
            public void handleMessage(@NonNull Message message)
            {
                Log.d(TAG, "[Handler] id du message = " + message.what);
                Log.d(TAG, "[Handler] contenu du message = " + message.obj.toString());

                switch (message.what)
                {
                    case Peripherique.CODE_CREATION:
                        Log.d(TAG, "[Handler] CREATION = " + message.obj.toString());
                        break;
                    case Peripherique.CODE_CONNEXION:
                        Log.d(TAG, "[Handler] CODE_CONNEXION = " + message.obj.toString());
                        boutonSeConnecterAuPomodoro.setText("Se déconnecter");
                        String trameConfiguration = "#P&"+Integer.toString(minuteur.getLongueur())+"&"+Integer.toString(minuteur.getDureePauseCourte())+"&"+Integer.toString(minuteur.getDureePauseLongue())+"&"+Integer.toString(minuteur.getNbCycles())+"&"+(minuteur.estModeAutomatique() ? "1" : "0")+"&"+(minuteur.estModeAutomatiquePause() ? "1" : "0")+"&0\n";
                        //peripherique.envoyer(Protocole.CONFIGURER_UN_POMODORO);
                        //Log.v(TAG, "Trame = " + Protocole.CONFIGURER_UN_POMODORO);
                        peripherique.envoyer(trameConfiguration);
                        Log.v(TAG, "Trame = " + trameConfiguration);
                        break;
                    case Peripherique.CODE_DECONNEXION:
                        Log.d(TAG, "[Handler] DECONNEXION = " + message.obj.toString());
                        boutonSeConnecterAuPomodoro.setText("Se connecter");
                        break;
                    case Peripherique.CODE_RECEPTION:
                        Log.d(TAG, "[Handler] RECEPTION = " + message.obj.toString());
                        String trame = "";
                        // Vérification
                        if(message.obj.toString().startsWith(Protocole.DEBUT_TRAME))
                        {
                            Log.v(TAG, "[Handler] Trame valide");
                            trame = message.obj.toString().replace(Protocole.DEBUT_TRAME, "");
                        } else
                        {
                            return;
                        }
                        String[] champs = trame.split(Protocole.DELIMITEUR_TRAME);
                        // Debug
                        for(int i = 0; i < champs.length; i++)
                        {
                            Log.v(TAG, "[Handler] champs[" + i + "] = " + champs[i]);
                        }

                        /**
                         * @todo Traiter et décoder les trames reçues
                         */
                        switch(champs[Protocole.TYPE_TRAME])
                        {
                            case Protocole.CHANGEMENT_ETAT:
                                Log.v(TAG, "[Handler] Changement d’état : " + champs[Protocole.CHAMP_ETAT]);
                                /**
                                 * @todo Gérer l'affichage du minuteur
                                 */
                                minuteur.setEtat(Integer.parseInt(champs[Protocole.CHAMP_ETAT]));
                                if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_ATTENTE))
                                {
                                    boutonDemarrer.setText("Démarrer");
                                    arreterMinuteur();
                                    Log.v(TAG,"[Handler] Changement d'état : Bouton = Démarrer");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_TACHE_EN_COURS))
                                {
                                    boutonDemarrer.setText("Tâche");
                                    demarrerMinuteur(minuteur.getLongueur());
                                    Log.v(TAG,"[Handler] Changement d'état : Bouton = Pause");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_TACHE_TERMINEE))
                                {
                                    boutonDemarrer.setText("Tâche terminée");
                                    arreterMinuteur();
                                    Log.v(TAG,"[Handler] Changement d'état : Bouton = Tache Terminée");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_COURTE_EN_COURS))
                                {
                                    boutonDemarrer.setText("Pause courte");
                                    demarrerMinuteur(minuteur.getDureePauseCourte());
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Courte");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_COURTE_TERMINEE))
                                {
                                    boutonDemarrer.setText("Pause courte terminée");
                                    arreterMinuteur();
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Courte terminée");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_LONGUE_EN_COURS))
                                {
                                    boutonDemarrer.setText("Pause longue");
                                    demarrerMinuteur(minuteur.getDureePauseLongue());
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Longue");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_LONGUE_TERMINEE))
                                {
                                    boutonDemarrer.setText("Pause longue terminée");
                                    arreterMinuteur();
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Longue Terminée");
                                }
                                break;
                        }
                        break;
                }
            }
        };
    }

    private void demarrerMinuteur(int duree)
    {
        if(timerMinuteur != null){
            timerMinuteur.cancel();
        }

        timerMinuteur = new Timer();
        dureeEnCours = duree;
        horloge.setText(getMMSS(duree));

        minuter();
    }

    private void arreterMinuteur()
    {
        if (timerMinuteur != null)
        {
            timerMinuteur.cancel();
            timerMinuteur = null;
            horloge.setText("00:00");
        }
    }

    public void minuter()
    {
        tacheMinuteur = new TimerTask() {
            public void run() {
                dureeEnCours--;
                runOnUiThread(new Runnable() {
                    public void run() {
                        horloge.setText(getMMSS(dureeEnCours));
                    }
                });
            }
        };

        timerMinuteur.schedule(tacheMinuteur, 1000, 1000);
    }

    private String getMMSS(long valeur)
    {
        long minutes = (valeur % 3600) / 60;
        String minuteStr = minutes < 10 ? "0" + Integer.toString((int) minutes) : Integer.toString((int)minutes);

        long secondes = valeur % 60;
        String secondesStr = secondes < 10 ? "0" + Integer.toString((int)secondes) : Integer.toString((int)secondes);

        return minuteStr + ":" + secondesStr;
    }
}
