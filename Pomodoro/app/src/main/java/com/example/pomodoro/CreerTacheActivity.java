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

    private EditText retourneNomTache;
    private EditText retourneDureeTache;
    private EditText retourneDureePauseCourte;
    private EditText retourneDureePauseLongue;
    private EditText retourneNombreCycles;
    private Vector<Tache> taches;


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

        retourneNomTache = (EditText) findViewById(R.id.nomTache);
        retourneDureeTache = (EditText) findViewById(R.id.dureeTache);
        retourneDureePauseCourte = (EditText) findViewById(R.id.dureePauseCourte);
        retourneDureePauseLongue = (EditText) findViewById(R.id.dureePauseLongue);
        retourneNombreCycles = (EditText) findViewById(R.id.nombreDeCycles);

        taches = new Vector<>();

        boutonAccueil.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonAccueil");
                Intent retourAccueil = new Intent(CreerTacheActivity.this,PomodoroActivity.class);
                startActivity(retourAccueil);
            }
        });

        boutonEditer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonEditer");
                Intent retourEditer = new Intent(CreerTacheActivity.this,IHM_EditerTache.class);
                startActivity(retourEditer);
            }
        });

        boutonCreerLaTache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonCreerLaTache");
                creerUneNouvelleTache();
                Intent retourAccueil = new Intent(CreerTacheActivity.this,PomodoroActivity.class);
                startActivity(retourAccueil);
            }
        });

    }
    private void creerUneNouvelleTache()
    {
        Log.d(TAG, "creerUneNouvelleTache()");
        String NomTache = retourneNomTache.getText().toString();
        String DureeTache = retourneDureeTache.getText().toString();
        String DureePauseCourte = retourneDureePauseCourte.getText().toString();
        String DureePauseLongue = retourneDureePauseLongue.getText().toString();
        String NombreCycle = retourneNombreCycles.getText().toString();
        Log.d(TAG, "[RemplirNomTache] : " + NomTache);
        Log.d(TAG, "[remplirDureeTache] : " + DureeTache);
        Log.d(TAG, "[remplirDureePauseCourte] : " + DureePauseCourte);
        Log.d(TAG, "[remplirDureePauseLongue] : " + DureePauseLongue);
        Log.d(TAG, "[remplirNombreCycles] : " + NombreCycle);

        taches.add(new Tache());

    }
}