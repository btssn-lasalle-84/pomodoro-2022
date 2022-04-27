package com.example.pomodoro;

/**
 * @file PomodoroActivity.java
 * @brief Déclaration de la classe PomodoroActivity
 * @author Teddy ESTABLET
 */

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;

import java.time.Instant;

/**
 * @class PomodoroActivity
 * @brief L'activité principale de l'application Pomodoro
 */
public class PomodoroActivity extends AppCompatActivity {

    /**
     * Constantes
     */
    private static final String TAG = "_PomodoroActivity";  //!< TAG pour les logs
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;

    /**
     * Ressources IHM
     */
    private Button boutonDemarrer;//!< Le bouton permettant de démarrer un pomodoro
    private Button boutonEditerTache;//!< Le bouton permettant d'éditer une tâche
    private Button boutonSupprimerTache;//!< Le bouton permettant de supprimer une tâche

    /**
     * Ressources IHM
     */
    Tache tache; //!< une tâche

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        tache = new Tache();
        initialiserIHM();

        /**
         * @brief Vérification du fonctionnement du bluetooth
         */
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth non activé", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(!bluetoothAdapter.isEnabled())
            {
                Toast.makeText(getApplicationContext(), "Bluetooth non activé", Toast.LENGTH_SHORT).show();
                //Possibilité 1 :
                //Intent activeBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(activeBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
                //Possibilité 2 :
                bluetoothAdapter.enable();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Bluetooth activé", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Possibilité 1 :
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return;
        if(resultCode == RESULT_OK)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth activé", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth non activé", Toast.LENGTH_SHORT).show();
        }
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
        boutonDemarrer = (Button)findViewById(R.id.boutonDemarrer);
        boutonEditerTache = (Button)findViewById(R.id.boutonEditerTache);
        boutonSupprimerTache = (Button)findViewById(R.id.boutonSupprimerTache);

        boutonDemarrer.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Log.d(TAG, "clic boutonDemarrer");
                    }
                });
        boutonEditerTache.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Log.d(TAG, "clic boutonEditerTache");
                        Log.d(TAG, "[Tache] nom = " + tache.getNom());
                        tache.editerTaches();
                    }
                });
        boutonSupprimerTache.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        Log.d(TAG, "clic boutonSupprimerTache");
                        Log.d(TAG, "[Tache] nom = " + tache.getNom());
                        tache.supprimerTache();
                        Log.d(TAG, "[Tache] nom = " + tache.getNom());
                    }
                });
    }
}
