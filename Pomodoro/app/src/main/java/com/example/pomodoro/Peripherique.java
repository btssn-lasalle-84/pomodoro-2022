package com.example.pomodoro;

/**
 * @file Peripherique.java
 * @brief Déclaration de la classe Peripherique
 * @author Teddy ESTABLET
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

/**
 * @class Peripherique
 * @brief Définit le concept de périphérique bluetooth
 */

public class Peripherique extends Thread
{
    private static final String TAG = "_Peripherique";  //!< TAG pour les logs

    private String nom;
    private String adresse;
    private final Handler handler;
    private static BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private InputStream receiveStream = null;
    private OutputStream sendStream = null;
    private TReception tReception;
    public final static int CODE_CREATION = 1;
    public final static int CODE_CONNEXION = 2;
    public final static int CODE_RECEPTION = 3;
    public final static int CODE_DECONNEXION = 4;

    @SuppressLint("MissingPermission")
    public Peripherique(Handler handler)
    {
        this.nom = "";
        this.adresse = "";
        this.handler = handler;

        activerBluetooth();
    }

    @SuppressLint("MissingPermission")
    private void activerBluetooth()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled())
        {
            Log.d(TAG, "Activation bluetooth");
            bluetoothAdapter.enable();
        }
    }

    @SuppressLint("MissingPermission")
    public boolean rechercherPomodoro(String idPomodoro)
    {
        Set<BluetoothDevice> appareilsAppaires = bluetoothAdapter.getBondedDevices();

        Log.d(TAG, "Recherche bluetooth : " + idPomodoro);
        for(BluetoothDevice appareil : appareilsAppaires)
        {
            Log.d(TAG, "Nom : " + appareil.getName() + " | Adresse : " + appareil.getAddress());
            if(appareil.getName().equals(idPomodoro) || appareil.getAddress().equals(idPomodoro))
            {
                device = appareil;
                this.nom = device.getName();
                this.adresse = device.getAddress();
                creerSocket();
                return true; // Trouvé !
            }
        }

        return false;
    }

    @SuppressLint("MissingPermission")
    private void creerSocket()
    {
        if(device == null)
            return;
        try
        {
            Log.d(TAG, "Création socket");
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            receiveStream = socket.getInputStream();
            sendStream = socket.getOutputStream();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Log.d(TAG, "Erreur création socket !");
            socket = null;
        }

        if(socket != null)
        {
            tReception = new TReception(handler, receiveStream);
            Message message = new Message();
            message.what = CODE_CREATION;
            message.obj = this.nom;
            handler.sendMessage(message);
        }
    }

    /**
     * @brief Accesseurs
     */
    public String getNom()
    {
        return nom;
    }

    public String getAdresse()
    {
        return adresse;
    }

    /**
     * @brief Mutateurs
     */
    public void setNom(String nom)
    {
        this.nom = nom;
    }

    /**
     * @brief Récupérer l'état de la connexion
     */
    public boolean estConnecte()
    {
        if(socket == null)
            return false;
        else
            return socket.isConnected();
    }

    /**
     * @brief Envoie des données sur la connexion bluetooth
     */
    public void envoyer(String donnees)
    {
        String data = null;

        if(socket == null)
            return;

        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    if(socket.isConnected())
                    {
                        sendStream.write(data.getBytes());
                        sendStream.flush();
                    }
                }
                catch(IOException e)
                {
                    System.out.println("<Socket> Erreur d'envoi");
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * @brief Débute la connexion bluetooth
     */
    public void connecter()
    {
        if(socket == null)
            return;

        Log.d(TAG, "Connexion du module " + this.nom + " | Adresse : " + this.adresse);

        new Thread()
        {
            @SuppressLint("MissingPermission")
            @Override
            public void run()
            {
                try
                {
                    socket.connect();

                    Message msg = Message.obtain();
                    msg.arg1 = CODE_CONNEXION;
                    handler.sendMessage(msg);

                    tReception.start();
                }
                catch(IOException e)
                {
                    System.out.println("<Socket> erreur de connexion");
                    e.printStackTrace();
                }
            }
        }.start();

    }

    /**
     * @brief Met fin à la connexion bluetooth
     */
    public void deconnecter()
    {
        if (device == null || socket == null)
            return;

        Log.d(TAG,"Déconnexion du module " + this.nom + " | Adresse : " + this.adresse);
    }
}
