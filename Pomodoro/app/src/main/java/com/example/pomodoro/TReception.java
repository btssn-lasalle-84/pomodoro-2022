package com.example.pomodoro;

/**
 * @file TReception.java
 * @brief Déclaration de la classe TReception
 * @author Teddy ESTABLET
 */

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @class TReception
 * @brief Permet d'assurer la réception des trames dans un thread
 */
public class TReception extends Thread
{
    private static final String TAG = "_TReception";  //!< TAG pour les logs

    Handler handlerUI;
    InputStream receiveStream;
    private boolean fini;

    /**
     * @brief Constructeur
     */
    TReception(Handler h, InputStream receiveStream)
    {
        this.handlerUI = h;
        this.receiveStream = receiveStream;
        this.fini = false;
    }

    /**
     * @brief Le thread
     */
    @Override
    public void run()
    {
        BufferedReader reception = new BufferedReader(new InputStreamReader(receiveStream));
        while(!fini)
        {
            recevoir(reception);
        }
    }

    /**
     * @brief réceptionne les trames et les transmet à l'IHM via un handler
     */
    private void recevoir(BufferedReader reception)
    {
        try
        {
            String trame = "";
            if(reception.ready())
            {
                trame = reception.readLine();
            }
            if(trame.length() > 0)
            {
                Log.d(TAG, "run() trame : " + trame);
                Message msg = Message.obtain();
                msg.what = Peripherique.CODE_RECEPTION;
                msg.obj = trame;
                handlerUI.sendMessage(msg);
            }
        }
        catch(IOException e)
        {
            System.out.println("<Socket> error read");
            e.printStackTrace();
        }
        try
        {
            Thread.sleep(250);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @brief Arrête le thread
     */
    public void arreter()
    {
        if(fini == false)
        {
            fini = true;
        }
        try
        {
            Thread.sleep(250);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
