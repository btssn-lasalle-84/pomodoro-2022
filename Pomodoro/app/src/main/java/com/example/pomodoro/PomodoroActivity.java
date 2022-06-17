package com.example.pomodoro;

/**
 * @file PomodoroActivity.java
 * @brief Déclaration de la classe PomodoroActivity
 * @author Teddy ESTABLET
 */

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * @class PomodoroActivity
 * @brief L'activité principale de l'application Pomodoro
 */
public class PomodoroActivity extends AppCompatActivity
{
    /**
     * Constantes
     */
    private static final String TAG = "_PomodoroActivity";  //!< TAG pour les logs
    private static final String TAG_DEMO = "_Demo"; //!< TAG_DEMO pour les logs de la démonstration
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
    Vector<List<String>> taches; //! liste des tâches
    private Timer timerMinuteur = null;;
    private TimerTask tacheMinuteur;

    private long dureeEnCours;
    private long debutMinuteur;

    private int numeroNotification = 1;
    private NotificationManager notificationManager = null;

    boolean estAppuyee = false;
    boolean estGele = false;

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

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch modeFonctionnement;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch modeSonnerie;

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        minuteur = new Minuteur();
        tache = new Tache(); // la tâche en cours
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
        boutonEditerTache = (AppCompatButton) findViewById(R.id.boutonEditerTacheActivity);
        boutonSeConnecterAuPomodoro = (AppCompatButton) findViewById(R.id.boutonSeConnecterAuPomodoro);
        horloge = (TextView) findViewById(R.id.horloge);
        spinner = (AppCompatSpinner) findViewById(R.id.spinner);
        modeFonctionnement = (Switch) findViewById(R.id.switchMinuteur);
        modeSonnerie = (Switch) findViewById(R.id.switchSonnerie);

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
                choisirModeSonnerie();
                peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.DEMARRER_TACHE+Protocole.DELIMITEUR_TRAME+tache.getNom()+Protocole.FIN_TRAME);// Trame envoyé : #T&Nom de la tâche\r\n
                geler();
                estAppuyee = true;
            }
        });

        boutonDemarrer.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                Log.d(TAG,"clic long boutonDemarrer");
                /*
                 * Boite de dialogue pour annuler une tâche ou une pause
                 */
                AlertDialog.Builder builder = new AlertDialog.Builder(PomodoroActivity.this);
                builder.setTitle("Vous êtes sur le point d'annuler votre tâche !");
                builder.setMessage("Nom de la tâche : "+tache.getNom());
                builder.setCancelable(false);
                builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.ANNULATION_TACHE_PAUSE+Protocole.FIN_TRAME);
                        boutonDemarrer.setText(R.string.DemarrerPause);
                        horloge.setBackgroundResource(R.drawable.horloge);
                    }
                });
                builder.setNegativeButton("Retour",new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });

        boutonEditerTache.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonEditerTache");
                Log.d(TAG, "[Tache] " + "id = " + tache.getId() + " - nom = " + tache.getNom());

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
        /*
         * Liste déroulante affichant les tâches crées
         */
        mettreAJourListeTaches();
        nomTaches = new ArrayList<>();
        for (int i = 0; i < taches.size(); i++)
        {
            nomTaches.add(taches.get(i).get(BaseDeDonnees.INDEX_COLONNE_TACHE_NOM));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nomTaches);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                Log.d(TAG_DEMO, "sélection = " + position + " -> " + nomTaches.get(position));
                Log.d(TAG_DEMO, "idTache = " + taches.get(position).get(BaseDeDonnees.INDEX_COLONNE_TACHE_ID_TACHE) + " -> " + taches.get(position).get(BaseDeDonnees.INDEX_COLONNE_TACHE_NOM));
                tache.setId(Integer.parseInt(taches.get(position).get(BaseDeDonnees.INDEX_COLONNE_TACHE_ID_TACHE)));
                tache.setNom(nomTaches.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

    }

    /**
     * @brief Initialise le Bluetooth
     * @fn initialiserBluetooth()
     */
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
     * @fn chercherMinuteur()
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
                Log.d(TAG_DEMO,"[chercherMinuteur] minuteur trouvé : " + device.getName() + " [" + device.getAddress() + "]");
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
                            Log.d(TAG, "[changementEtatBluetooth] état : " + state);
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

    /**
     * @brief Initialise l'accès au minuteur Bluetooth
     */
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

    /**
     * @brief Initialise le Handler qui permet de communiquer entre threads
     */
    private void initialiserHandler()
    {
        this.handler = new Handler(this.getMainLooper())
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void handleMessage(@NonNull Message message)
            {
                Log.d(TAG, "[Handler] id du message = " + message.what);
                Log.d(TAG_DEMO, "[Handler] contenu du message = " + message.obj.toString());
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
                        Log.d(TAG_DEMO, "[Handler] RECEPTION = " + message.obj.toString());
                        String trame = "";
                        // Vérification
                        if(message.obj.toString().startsWith(Protocole.DEBUT_TRAME))
                        {
                            Log.v(TAG_DEMO, "[Handler] Trame valide");
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
                                estAppuyee = false;
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
                                    Toast toast = Toast.makeText(getApplicationContext(), "Tache terminée", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP,20,30);
                                    toast.show();
                                    notifierEvenement("Vous avez terminé la tâche : "+tache.getNom());
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
                                    Toast toast = Toast.makeText(getApplicationContext(), "Pause courte terminée", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP,20,30);
                                    toast.show();
                                    notifierEvenement("Vous avez terminé votre pause courte, retournez à la tâche : "+tache.getNom());
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
                                    Toast toast = Toast.makeText(getApplicationContext(), "Pause longue terminée", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.TOP,20,30);
                                    toast.show();
                                    notifierEvenement("Vous avez terminé votre pause longue, retournez à la tâche : "+tache.getNom());
                                    Log.v(TAG, "[Handler] Changement d'état : Bouton = Pause Longue Terminée");
                                }
                                break;
                        }
                        break;
                }
            }
        };
    }

    /**
     * @brief Permet la mise à jour du bouton Connexion/Déconnexion
     */
    private void mettreAJourBoutonConnexion(boolean etatConnexion)
    {
        if(etatConnexion)
            boutonSeConnecterAuPomodoro.setText("Se déconnecter");
        else
            boutonSeConnecterAuPomodoro.setText("Se connecter");
    }

    /**
     * @brief Permet la mise à jour de la liste des tâches
     */
    private void mettreAJourListeTaches()
    {
        taches = baseDeDonnees.getTaches();
    }

    /**
     * @brief Méthode qui démarre le minuteur pour une durée
     */
    private void demarrerMinuteur(int duree)
    {
        if(timerMinuteur != null){
            timerMinuteur.cancel();
        }

        timerMinuteur = new Timer();
        dureeEnCours = duree;
        debutMinuteur = Calendar.getInstance().getTime().getTime();
        horloge.setText(getMMSS(duree));

        choisirModeHorloge();
        Log.d(TAG_DEMO,"Minuteur démarré");
    }

    /**
     * @brief Arrête le minuteur
     */
    private void arreterMinuteur()
    {
        if (timerMinuteur != null)
        {
            timerMinuteur.cancel();
            timerMinuteur = null;
            horloge.setText("00:00");
            Log.d(TAG_DEMO,"Minuteur arrêté");
        }
    }

    /**
     * @brief Assure la gestion de la minuterie
     */
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

    public void geler()
    {
        if (estAppuyee == true)
        {
            peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.GEL+Protocole.FIN_TRAME);
            boutonDemarrer.setText(tache.getNom()+"\r\nen Pause");
            horloge.setBackgroundResource(R.drawable.horloge_pause);
            if (timerMinuteur != null)
            {
                timerMinuteur.cancel();
                Log.d(TAG_DEMO,"Minuteur arrêté");
            }
            estGele = true;
        }
        estAppuyee = false;
    }

    public void reprise()
    {
        if (estGele == true && estAppuyee == true)
        {
            peripherique.envoyer("#D&10\r\n"); // TEST
            estGele = false;
        }
    }
    
    /**
     * @brief Méthode qui de chronométrer
     */
    public void chronometrer()
    {
        tacheMinuteur = new TimerTask() {
            public void run() {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
                long enCours = Calendar.getInstance().getTime().getTime() - debutMinuteur;
                Date affichageMinuteur = new Date(enCours);
                final String strDate = simpleDateFormat.format(affichageMinuteur);
                runOnUiThread(new Runnable() {
                    public void run() {
                        horloge.setText(strDate);
                    }
                });
            }
        };

        timerMinuteur.schedule(tacheMinuteur, 1000, 1000);
    }

    /**
     * @brief Méthode qui permet d'obtenir le temps avec le format MM:SS
     */
    private String getMMSS(long valeur)
    {
        long minutes = (valeur % 3600) / 60;
        String minuteStr = minutes < 10 ? "0" + Integer.toString((int) minutes) : Integer.toString((int)minutes);

        long secondes = valeur % 60;
        String secondesStr = secondes < 10 ? "0" + Integer.toString((int)secondes) : Integer.toString((int)secondes);

        return minuteStr + ":" + secondesStr;
    }

    /**
     * @brief Méthode qui permet d'activer/désactiver le mode sonnerie
     */
    private void choisirModeSonnerie()
    {
        if(modeSonnerie.isChecked())
        {
            peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.MODE_SONNERIE+Protocole.DELIMITEUR_TRAME+1+Protocole.FIN_TRAME);
            Log.d(TAG_DEMO,"Sonnerie activé");
        }
        else
        {
            peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.MODE_SONNERIE+Protocole.DELIMITEUR_TRAME+0+Protocole.FIN_TRAME);
            Log.d(TAG_DEMO,"Sonnerie désactivé");
        }
    }

    /**
     * @brief Méthode qui permet de chosir le mode minuteur ou chronomètre
     */
    private void choisirModeHorloge()
    {
        if(modeFonctionnement.isChecked())
        {
            chronometrer();
            peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.MODE_MINUTEUR+Protocole.DELIMITEUR_TRAME+1+Protocole.FIN_TRAME);
            Log.d(TAG,"Mode chronomètre");
        }
        else
        {
            minuter();
            peripherique.envoyer(Protocole.DEBUT_TRAME+Protocole.MODE_MINUTEUR+Protocole.DELIMITEUR_TRAME+0+Protocole.FIN_TRAME);
            Log.d(TAG,"Mode minuteur");
        }
    }

    /**
     * @brief Méthode qui notifie des évènements au système Android
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifierEvenement(String message)
    {
        //Création du gestionnaire de notification
        CharSequence name = getString(R.string.app_name);
        String description = "Fin de la tâche : "+tache.getNom()+" bonne pause";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        //NotificationChannel channel = new NotificationChannel("Nom du channel", name, importance);
        //channel.setDescription(description);
        //notificationManager.createNotificationChannel(channel);
        //Définition du titre de la notification
        String titreNotification = getApplicationName(getApplicationContext());
        //Définition du texte qui caractérise la notification
        String texteNotification = message;
        //On crée la notification
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,"Nom du channel")
                .setSmallIcon(R.mipmap.logo_pomodoro)
                .setContentTitle(titreNotification)
                .setContentText(texteNotification);
        //Création d'une nouvelle activité
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        //Association de la notification à l'intent
        notification.setContentIntent(pendingIntent);
        notification.setAutoCancel(true);
        //Ajout d'une vibration
        notification.setVibrate(new long[] {0,200,100,200,100,200});
        //Ajout de la notification et son ID au gestionnaire de notification
        notificationManager.notify(numeroNotification++, notification.build());
    }

    /**
     * @brief Méthode qui permet de récupérer le nom de l'application
     */
    public static String getApplicationName(Context context)
    {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }
}
