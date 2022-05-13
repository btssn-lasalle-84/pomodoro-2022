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

        import java.lang.reflect.Array;
        import java.util.ArrayList;
        import java.util.List;


/**
 * @class EditerTacheActivity
 * @brief L'activité d'edition d'une tache de l'application Pomodoro
 */
public class IHM_EditerTache extends AppCompatActivity
{
    /**
     * @brief Constantes
     */
    private static final String TAG = "_IHM_EditerTache";  //!< TAG pour les logs

    /**
     * @brief Ressources IHM
     */
    private Button boutonAccueil;//!< Le bouton permettant de retourner à l'accueil
    private Button boutonCreerTache;//!< Le bouton permettant de créer une tache


    Spinner spinnerTachesExistante;
    List<String> nomTaches;
    ArrayAdapter<String> adapter;

    /**
     * @brief Méthode appelée à la création de l'activité
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ihm_editer_tache);
        Log.d(TAG, "onCreate()");

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
        boutonCreerTache = (Button) findViewById(R.id.boutonCreerTache);
        spinnerTachesExistante =(Spinner) findViewById(R.id.spinnerTache);

        boutonCreerTache.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.v(TAG, "clic boutonCreerTache");
                Intent CreerTache = new Intent(IHM_EditerTache.this,CreerTacheActivity.class);
                startActivity(CreerTache);
            }
        });

        boutonAccueil.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "clic boutonEditerTache");
                Intent RetourAccueil = new Intent(IHM_EditerTache.this,PomodoroActivity.class);
                startActivity(RetourAccueil);
            }
        });

        /**
         * @brief Spinner affichant les tâches crées
         */
        nomTaches = new ArrayList<>();
        nomTaches.add("Coder le projet");
        nomTaches.add("Obtenir le diplome");
        nomTaches.add("Coder le projet");
        nomTaches.add("Obtenir le diplome");
        nomTaches.add("Coder le projet");
        nomTaches.add("Obtenir le diplome");
        nomTaches.add("Coder le projet");
        nomTaches.add("Obtenir le diplome");


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nomTaches);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        spinnerTachesExistante.setAdapter(adapter);

    }
}