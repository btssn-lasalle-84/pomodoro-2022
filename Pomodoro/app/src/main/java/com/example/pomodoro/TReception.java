package com.example.pomodoro;

/**
 * @file TReception.java
 * @brief Déclaration de la classe TReception
 * @author Teddy ESTABLET
 */

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @class TReception
 * @brief Définit le concept de TReception
 */
public class TReception extends Thread
{
    private static final String TAG = "_TReception";  //!< TAG pour les logs

    Handler handlerUI;
    InputStream receiveStream;
    private boolean fini;

    TReception(Handler h, InputStream receiveStream)
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

    /**public void connecter()
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
                    msg.arg1 = CODE_CONNEXION;
                    handler.sendMessage(msg);

                    tReception.start();

                }
                catch(IOException e)
                {
                    System.out.println("<Socket> error connect");
                    e.printStackTrace();
                }
            }
        }.start();
    }**/
}
