package com.example.pomodoro;

/**
 * @file Peripherique.java
 * @brief Déclaration de la classe Peripherique
 * @author Teddy ESTABLET
 */

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @class Peripherique
 * @brief Définit le concept de périphérique bluetooth
 */

public class Peripherique extends Thread
{
    private static final String TAG = "_Peripherique";  //!< TAG pour les logs

    private final BluetoothDevice device;
    private final String nom;
    private final String adresse;
    private final Handler handler;
    private BluetoothSocket socket = null;
    private InputStream receiveStream = null;
    private OutputStream sendStreal = null;
    private TReception tReception;
    public final static int CODE_CONNEXION = 0;
    public final static int CODE_RECEPTION = 1;
    public final static int CODE_DECONNEXION = 2;

    @SuppressLint("MissingPermission")
    public Peripherique(BluetoothDevice device, Handler handler)
    {
        if(device != null)
        {
            this.device = device;
            this.nom = device.getName();
            this.adresse = device.getAddress();
            this.handler = handler;
        }
        else
        {
            this.device = device;
            this.nom = "Aucun";
            this.adresse = "";
            this.handler = handler;
        }
        try
        {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            InputStream receiveStream = socket.getInputStream();
            OutputStream sendStream = socket.getOutputStream();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            socket = null;
        }
        if(socket != null)
            tReception = new TReception(handler, receiveStream, sendStreal);
    }
}
