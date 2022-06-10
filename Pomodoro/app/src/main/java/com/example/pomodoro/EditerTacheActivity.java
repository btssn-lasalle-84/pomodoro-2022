package com.example.pomodoro;

/**
 * @file EditerTacheActivity.java
 * @brief Déclaration de la classe EditerTacheActivity
 * @author Teddy ESTABLET
 */

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @class EditerTacheActivity
 * @brief L'activité d'edition d'une tache de l'application Pomodoro
 */
public class EditerTacheActivity extends AppCompatActivity
{
    /**
     * @brief Constantes
     */
    private static final String TAG = "_EditerTacheActivity";  //!< TAG pour les logs
    private static final String TAG_DEMO = "_Demo"; //!< TAG_DEMO pour les logs de la démonstration

    /**
     * @brief Ressources IHM
     */
    private Button boutonAccueil;//!< Le bouton permettant de retourner à l'accueil
    private Button boutonCreerTache;//!< Le bouton permettant de créer une tache
    private Button boutonSupprimerTache;

    /**
     * @brief Attributs
     */
    private Spinner spinnerTachesExistante;
    private List<String> nomTaches;
    private ArrayAdapter<String> adapter;
    private Tache tache = null;
    Vector<List<String>> taches; //! liste des tâches
    private BaseDeDonnees baseDeDonnees = null;

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editer_tache);
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
        boutonAccueil = (Button) findViewById(R.id.boutonPomodoroActivity);
        boutonCreerTache = (Button) findViewById(R.id.boutonCreerTache);
        boutonSupprimerTache = (Button) findViewById(R.id.boutonSupprimerTache);
        spinnerTachesExistante = (Spinner) findViewById(R.id.spinnerTache);

        boutonCreerTache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonCreerTache");

                Intent creerTache = new Intent(EditerTacheActivity.this,CreerTacheActivity.class);
                // passe la tache à l'activité
                creerTache.putExtra("tache", tache);
                startActivity(creerTache);
            }
        });

        boutonAccueil.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonEditerTache");
                Intent retourAccueil = new Intent(EditerTacheActivity.this,PomodoroActivity.class);
                retourAccueil.putExtra("tache", tache);
                startActivity(retourAccueil);
            }
        });

        boutonSupprimerTache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "clic boutonSupprimerTache");
                tache.supprimer();
                //String requete = "DELETE FROM Tache WHERE idTache = '" +  "'";
                //baseDeDonnees.executerRequete(requete);
            }
        });

        /**
         * @brief Spinner affichant les tâches crées &| modifiées
         */
        nomTaches = new ArrayList<>();
        taches = baseDeDonnees.getTaches();
        for (int i = 0; i < taches.size(); i++)
        {
            nomTaches.add(taches.get(i).get(BaseDeDonnees.INDEX_COLONNE_TACHE_NOM));
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nomTaches);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerTachesExistante.setAdapter(adapter);

        spinnerTachesExistante.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                Log.d(TAG, "sélection = " + position + " -> " + nomTaches.get(position));
                Log.d(TAG, "idTache = " + taches.get(position).get(BaseDeDonnees.INDEX_COLONNE_TACHE_ID_TACHE) + " -> " + taches.get(position).get(BaseDeDonnees.INDEX_COLONNE_TACHE_NOM));
                tache.setId(Integer.parseInt(taches.get(position).get(BaseDeDonnees.INDEX_COLONNE_TACHE_ID_TACHE)));
                tache.setNom(nomTaches.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });
    }
}