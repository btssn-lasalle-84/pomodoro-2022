package com.example.pomodoro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class CreerTacheActivity extends AppCompatActivity
{
    /**
     * @brief Constantes
     */
    private static final String TAG = "_EditerTacheActivity";  //!< TAG pour les logs

    /**
     * @brief Ressources IHM
     */
    private Button boutonAccueil;//!< Le bouton permettant de retourner à l'accueil
    private Button boutonEditer;//!< Le bouton permettant de retourner au menu Editer
    private Button boutonCreerLaTache;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creer_tache);

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
        boutonAccueil = (Button) findViewById(R.id.boutonAccueil);
        boutonEditer = (Button) findViewById(R.id.boutonEditerTache);
        boutonCreerLaTache = (Button) findViewById(R.id.boutonCreerLaTache);

        boutonAccueil.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonAccueil");
                Intent RetourAccueil = new Intent(CreerTacheActivity.this,PomodoroActivity.class);
                startActivity(RetourAccueil);
            }
        });

        boutonEditer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonEditer");
                Intent RetourEditer = new Intent(CreerTacheActivity.this, EditerTacheActivity.class);
                startActivity(RetourEditer);
            }
        });

        boutonCreerLaTache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonCreerLaTache");

            }
        });
    }
}