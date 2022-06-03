package com.example.pomodoro;

/**
 * @file PomodoroActivity.java
 * @brief Déclaration de la classe PomodoroActivity
 * @author Teddy ESTABLET
 */

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
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

    public final static String afficheTacheTermine = "Tâche terminé";
    public final static String afficheTacheDemarrer = "Démarrer";
    public final static String affichePauseCourte = "Pause courte";
    public final static String affichePauseCourteTerminee = "Pause courte terminée";
    public final static String affichePauseLongue = "Pause longue";
    public final static String affichePauseLongueTerminee = "Pause longue terminée";

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
    private AppCompatButton boutonDemarrer;//!< Le bouton permettant de démarrer un pomodoro
    private AppCompatButton boutonEditerTache;//!< Le bouton permettant d'éditer une tâche
    private AppCompatButton boutonSeConnecterAuPomodoro;//!< Le bouton permettant de se connecter au pomodoro
    private AppCompatSpinner spinner;
    private List<String> nomTaches;
    private ArrayAdapter<String> adapter;
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
        tache = new Tache("Coder le projet", 25,5,20,4);
        Log.d(TAG, "[Tache] nom = " + tache.getNom());

        baseDeDonnees = new BaseDeDonnees(this);

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
        boutonDemarrer = (AppCompatButton) findViewById(R.id.boutonDemarrer);
        boutonEditerTache = (AppCompatButton) findViewById(R.id.boutonEditerTache);
        boutonSeConnecterAuPomodoro = (AppCompatButton) findViewById(R.id.boutonSeConnecterAuPomodoro);
        horloge = (TextView) findViewById(R.id.horloge);
        spinner = (AppCompatSpinner) findViewById(R.id.spinner);

        boutonDemarrer.setBackgroundResource(R.drawable.bouton_demarrer);
        boutonEditerTache.setBackgroundResource(R.drawable.bouton_editer);
        boutonSeConnecterAuPomodoro.setBackgroundResource(R.drawable.bouton_se_connecter);
        horloge.setBackgroundResource(R.drawable.horloge);
        //spinner.setBackgroundResource(R.drawable.spinner);

        boutonDemarrer.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonDemarrer");
                peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.DEMARRER_TACHE+Protocole.DELIMITEUR_TRAME+tache.getNom()+Protocole.FIN_TRAME);
            }
        });
        boutonEditerTache.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonEditerTache");
                Log.d(TAG, "[Tache] nom = " + tache.getNom());

                Intent editerTache = new Intent(PomodoroActivity.this, EditerTacheActivity.class);
                // passe la tache à l'activité
                editerTache.putExtra("tache", tache);
                startActivity(editerTache);
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
        /**
         * @brief Spinner affichant les tâches crées
         */
        mettreAJourListeTaches();
        nomTaches = new ArrayList<>();
        Vector<String> nomsTache = baseDeDonnees.getNomTaches();
        if(tache != null && !tache.getNom().isEmpty())
            nomTaches.add(tache.getNom());
        for (int i = 0; i < nomsTache.size(); i++)
        {
            nomTaches.add(nomsTache.get(i));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nomTaches);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                Log.d(TAG, "sélection = " + position + " -> " + nomTaches.get(position));
                tache.setNom(nomTaches.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
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
                        mettreAJourBoutonConnexion(true);
                        String trameConfiguration = "#P&"+Integer.toString(minuteur.getLongueur())+"&"+Integer.toString(minuteur.getDureePauseCourte())+"&"+Integer.toString(minuteur.getDureePauseLongue())+"&"+Integer.toString(minuteur.getNbCycles())+"&"+(minuteur.estModeAutomatique() ? "1" : "0")+"&"+(minuteur.estModeAutomatiquePause() ? "1" : "0")+"&0\n";
                        //peripherique.envoyer(Protocole.CONFIGURER_UN_POMODORO);
                        //Log.v(TAG, "Trame = " + Protocole.CONFIGURER_UN_POMODORO);
                        peripherique.envoyer(trameConfiguration);
                        Log.v(TAG, "Trame = " + trameConfiguration);
                        break;
                    case Peripherique.CODE_DECONNEXION:
                        Log.d(TAG, "[Handler] DECONNEXION = " + message.obj.toString());
                        mettreAJourBoutonConnexion(false);
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

                        switch(champs[Protocole.TYPE_TRAME])
                        {
                            case Protocole.CHANGEMENT_ETAT:
                                Log.v(TAG, "[Handler] Changement d’état : " + champs[Protocole.CHAMP_ETAT]);
                                minuteur.setEtat(Integer.parseInt(champs[Protocole.CHAMP_ETAT]));
                                if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_ATTENTE))
                                {
                                    boutonDemarrer.setText(afficheTacheDemarrer);
                                    arreterMinuteur();
                                    Log.v(TAG,"[Handler] Changement d'état : Bouton = Démarrer");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_TACHE_EN_COURS))
                                {
                                    boutonDemarrer.setText(tache.getNom());
                                    horloge.setBackgroundResource(R.drawable.horloge);
                                    demarrerMinuteur(minuteur.getLongueur());
                                    Log.v(TAG,"[Handler] Changement d'état : Bouton = Tâche");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_TACHE_TERMINEE))
                                {
                                    boutonDemarrer.setText(afficheTacheDemarrer);
                                    horloge.setBackgroundResource(R.drawable.horloge);
                                    arreterMinuteur();
                                    Log.v(TAG,"[Handler] Changement d'état : Bouton = Tache Terminée");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_COURTE_EN_COURS))
                                {
                                    boutonDemarrer.setText(affichePauseCourte);
                                    horloge.setBackgroundResource(R.drawable.horloge_jaune);
                                    demarrerMinuteur(minuteur.getDureePauseCourte());
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Courte");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_COURTE_TERMINEE))
                                {
                                    boutonDemarrer.setText(affichePauseCourteTerminee);
                                    horloge.setBackgroundResource(R.drawable.horloge_jaune);
                                    arreterMinuteur();
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Courte terminée");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_LONGUE_EN_COURS))
                                {
                                    boutonDemarrer.setText(affichePauseLongue);
                                    horloge.setBackgroundResource(R.drawable.horloge_verte);
                                    demarrerMinuteur(minuteur.getDureePauseLongue());
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Longue");
                                }
                                else if(champs[Protocole.CHAMP_ETAT].equals(Protocole.ETAT_PAUSE_LONGUE_TERMINEE))
                                {
                                    boutonDemarrer.setText(affichePauseLongueTerminee);
                                    horloge.setBackgroundResource(R.drawable.horloge_verte);
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

    private void mettreAJourBoutonConnexion(boolean etatConnexion)
    {
        if(etatConnexion)
            boutonSeConnecterAuPomodoro.setText("Se déconnecter");
        else
            boutonSeConnecterAuPomodoro.setText("Se connecter");
    }

    private void mettreAJourListeTaches()
    {
        tache = (Tache) getIntent().getSerializableExtra("tache");
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
