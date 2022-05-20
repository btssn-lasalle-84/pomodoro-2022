package com.example.pomodoro;

/**
 * @file CreerTacheActivity.java
 * @brief Déclaration de la classe CreerTacheActivity
 * @author Teddy ESTABLET
 */

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Vector;

/**
 * @class CreerTacheActivity
 * @brief L'activité creer une tache de l'application Pomodoro
 */
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

    private EditText remplirNomTache;
    private EditText remplirDureeTache;
    private EditText remplirDureePauseCourte;
    private EditText remplirDureePauseLongue;
    private EditText remplirNombreCycles;
    Vector<Tache> taches;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creer_tache);

        initialiserIHM();
        creerUneNouvelleTache();
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

        remplirNomTache = (EditText) findViewById(R.id.nomTache);
        remplirDureeTache = (EditText) findViewById(R.id.dureeTache);
        remplirDureePauseCourte = (EditText) findViewById(R.id.dureePauseCourte);
        remplirDureePauseLongue = (EditText) findViewById(R.id.dureePauseLongue);
        remplirNombreCycles = (EditText) findViewById(R.id.nombreDeCycles);

        taches = new Vector<>();

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
                Intent RetourEditer = new Intent(CreerTacheActivity.this,IHM_EditerTache.class);
                startActivity(RetourEditer);
            }
        });

        boutonCreerLaTache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonCreerLaTache");
                creerUneNouvelleTache();
                Intent RetourEditer = new Intent(CreerTacheActivity.this,PomodoroActivity.class);
                startActivity(RetourEditer);
            }
        });

    }
    private void creerUneNouvelleTache()
    {
        Log.d(TAG, "creerUneNouvelleTache()");
        remplirNomTache.getText().toString();
        remplirDureeTache.getText().toString();
        remplirDureePauseCourte.getText().toString();
        remplirDureePauseLongue.getText().toString();
        remplirNombreCycles.getText().toString();
        Log.d(TAG, "[RemplirNomTache] : " + remplirNomTache);
        Log.d(TAG, "[remplirDureeTache] : " + remplirDureeTache);
        Log.d(TAG, "[remplirDureePauseCourte] : " + remplirDureePauseCourte);
        Log.d(TAG, "[remplirDureePauseLongue] : " + remplirDureePauseLongue);
        Log.d(TAG, "[remplirNombreCycles] : " + remplirNombreCycles);

        //taches.add(new Tache(remplirNomTache, remplirDureeTache, remplirDureePauseCourte, remplirDureePauseLongue, remplirNombreCycles));

    }
}