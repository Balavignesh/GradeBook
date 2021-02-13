/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.balavignesh.gradebook.connection;

/**
 *
 * @author avr73
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.TimerTask;

class TelnetCheck extends TimerTask
{
    public static void main(String args[])
    {
            int port=8080;
            int totalclients = 4;
            //String ip[]={"35.224.65.85","34.68.29.81","104.197.5.217","35.202.183.42"};
            String ip[]={"34.72.167.47"};
            int k =ip.length;
	    TimerTask con  = new TelnetCheck();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(con,1,1000);
            StringBuilder stringBuilder = new StringBuilder();
              
        for(int i=0;i<totalclients;i++){
        try
        {
            Socket s=new Socket(ip[i],port);
            InputStream is=s.getInputStream();
            DataInputStream dis=new DataInputStream(is);
            if(dis!=null)
            {
                System.out.println("Connected with ip:"+ ip[i] +" and port "+port);
            }
            else
            {
                System.out.println("Connection invalid for " + ip[i]);
            }
            dis.close();
            s.close();   
        }
        catch(Exception e)
        {
            System.out.println(e + " for " + ip[i]);   
        }        
    }}
 
    @Override
    public void run() {
    }
}