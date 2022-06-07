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

/**
 * @class CreerTacheActivity
 * @brief L'activité creer une tache de l'application Pomodoro
 */
public class CreerTacheActivity extends AppCompatActivity
{
    /**
     * @brief Constantes
     */
    private static final String TAG = "_CreerTacheActivity";  //!< TAG pour les logs

    /**
     * @brief Ressources IHM
     */
    private Button boutonPomodoroActivity;//!< Le bouton permettant de retourner à l'accueil
    private Button boutonEditerTacheActivity;//!< Le bouton permettant de retourner au menu Editer
    private Button boutonModifierTache;

    /**
     * @brief Attributs
     */
    private EditText editNomTache;
    private EditText dureeTache;
    private EditText dureePauseCourte;
    private EditText dureePauseLongue;
    private EditText nombreCycles;
    private Tache tache = null;
    String nomTache;
    private BaseDeDonnees baseDeDonnees = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creer_tache);
        Log.d(TAG, "onCreate()");

        // récupère la tâche
        tache = (Tache) getIntent().getSerializableExtra("tache");
        Log.d(TAG, "[Tache] " + "id = " + tache.getId() + " - nom = " + tache.getNom());

        baseDeDonnees = new BaseDeDonnees(this);
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
        boutonPomodoroActivity = (Button) findViewById(R.id.boutonPomodoroActivity);
        boutonEditerTacheActivity = (Button) findViewById(R.id.boutonEditerTacheActivity);
        boutonModifierTache = (Button) findViewById(R.id.boutonModifierTache);

        editNomTache = (EditText) findViewById(R.id.nomTache);
        dureeTache = (EditText) findViewById(R.id.dureeTache);
        dureePauseCourte = (EditText) findViewById(R.id.dureePauseCourte);
        dureePauseLongue = (EditText) findViewById(R.id.dureePauseLongue);
        nombreCycles = (EditText) findViewById(R.id.nombreDeCycles);

        if(tache != null)
        {
            editNomTache.setText(tache.getNom());
            dureeTache.setText(Integer.toString(tache.getDuree()));
            dureePauseCourte.setText(Integer.toString(tache.getDureePauseCourte()));
            dureePauseLongue.setText(Integer.toString(tache.getDureePauseLongue()));
            nombreCycles.setText(Integer.toString(tache.getNombreDeCycles()));
        }

        boutonPomodoroActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonPomodoroActivity");
                Intent retourAccueil = new Intent(CreerTacheActivity.this,PomodoroActivity.class);
                retourAccueil.putExtra("tache",tache);
                startActivity(retourAccueil);
            }
        });

        boutonEditerTacheActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonEditerTacheActivity");
                Intent retourEditer = new Intent(CreerTacheActivity.this, EditerTacheActivity.class);
                // passe la tache à l'activité
                retourEditer.putExtra("tache", tache);
                startActivity(retourEditer);
            }
        });

        boutonModifierTache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonCreerLaTache");
                recupererDonneesTache();
                modifierTacheBDD();
                Intent retourEditer = new Intent(CreerTacheActivity.this,EditerTacheActivity.class);
                retourEditer.putExtra("tache", tache);
                startActivity(retourEditer);
            }
        });

    }

    private void creerTacheBDD()
    {
        Log.d(TAG, "creerTacheBDD()");
        /**
         * @todo Effectuer requête INSERT
         */
        //baseDeDonnees.executerRequete();
        /**
         * @todo Récupérer le nouvel id et l'affecter à l'objet tache
         */
    }

    private void modifierTacheBDD()
    {
        Log.d(TAG, "modifierTacheBDD()");
        Log.d(TAG, "[modifierTacheBDD] nomTache = " + nomTache);
        baseDeDonnees.executerRequete(BaseDeDonnees.UPDATE_NOM_TACHE+"'"+nomTache+"' WHERE idTache='"+String.valueOf(tache.getId())+"'");
    }

    private void recupererDonneesTache()
    {
        Log.d(TAG, "recupererDonneesTache()");

        // Récupère la saisie de l'utilisateur
        nomTache = this.editNomTache.getText().toString();

        // Convertit les saisies en entier pour les durées et le nombre de cycle
        int dureeTacheEntier = Integer.parseInt(dureeTache.getText().toString());
        int dureePauseCourteEntier = Integer.parseInt(dureePauseCourte.getText().toString());
        int dureePauseLongueEntier = Integer.parseInt(dureePauseLongue.getText().toString());
        int nombreCycleEntier = Integer.parseInt(nombreCycles.getText().toString());
        Log.d(TAG, "[recupererDonneesTache] " + nomTache + " - " + dureeTacheEntier + " - " + dureePauseCourteEntier + " - "  + dureePauseLongueEntier + " - "  + nombreCycleEntier);

        // Ajout des valeurs saisies dans la tache
        if(tache != null)
        {
            Log.d(TAG, "[recupererDonneesTache] ancien nom = " + tache.getNom());
            tache.setNom(nomTache);
            Log.d(TAG, "[recupererDonneesTache] nouveau nom = " + tache.getNom());
            tache.configurer(dureeTacheEntier, dureePauseCourteEntier, dureePauseLongueEntier, nombreCycleEntier);
        }
    }
}