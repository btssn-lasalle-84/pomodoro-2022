package com.example.pomodoro;

public class Tache
{
    private String _idTache = "#Tache1";
    private String _nom = "Tache 1";
    private String couleurTache = "Rouge"; //Couleur disponible : Bleue, Noire, Rouge, Vert
    private int _tempsTache = 25; //Valeur exprimée en minutes | ne peux pas être plus grand que 50
    private int _tempsPauseCourte = 5; //Valeur exprimée en minutes | ne peux pas être plus grand que 20
    private int _tempsPauseLongue = 10; //Valeur exprimée en minutes | ne peux pas être plus grand que 30
    private int _nombreDeCycles = 4;
    private int _nombreDeSessions = 4;



    public void editerTaches()
    {
    }

    public void supprimerTache()
    {
    }
}
