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
 * @brief Définit le concept de TReception
 */
public class TReception extends Thread
{
    private static final String TAG = "_TReception";  //!< TAG pour les logs

    Handler handlerUI;
    Handler handler;
    Socket socket;
    InputStream receiveStream;
    OutputStream sendStream;
    Thread tReception;
    private boolean fini;

    TReception(Handler h, InputStream receiveStream, OutputStream sendStreal)
    {
        this.handlerUI = h;
        this.receiveStream = receiveStream;
        this.fini = false;
    }

    @Override
    public void run()
    {
        BufferedReader reception = new BufferedReader(new InputStreamReader(receiveStream));
        while(!fini)
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
                //System.out.println("<Socket> error read");
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
    }

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

    public void connecter()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // @todo à modifier
                    socket.connect();

                    Message msg = Message.obtain();
                    msg.arg1 = Peripherique.CODE_CONNEXION;
                    handler.sendMessage(msg);

                    tReception.start();

                }
                catch(IOException e)
                {
                    System.out.println("<Socket> erreur de connection");
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public boolean deconnecter()
    {
        try
        {
            // @todo à modifier
            tReception.arreter();
            socket.close();
            return true;
        }
        catch(IOException e)
        {
            System.out.println("<Socket> erreur de fermeture");
            e.printStackTrace();
            return false;
        }
    }
    public void envoyer(String data)
    {
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
                    System.out.println("<Socket> erreur d'envoi");
                    e.printStackTrace();
                }
            }
        }.start();
    }
    // @todo à retravailler
    public void onClick(View v)
    {
        if(v.getId() == R.id.buttonConnecter)
        {
            peripherique.connecter();
        }

        if(v.getId() == R.id.buttonDeconnecter)
        {
            if(peripherique.deconnecter())
            {
                btnConnecter.setEnabled(true);
                btnDeconnecter.setEnabled(false);
            }
        }

        gererBoutons(v);
    }
}
