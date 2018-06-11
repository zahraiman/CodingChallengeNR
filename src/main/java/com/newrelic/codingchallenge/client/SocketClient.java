package com.newrelic.codingchallenge.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;

/**
 * Created by zahraiman on 6/9/18.
 */
public class SocketClient {
    private Socket socket;

    public void setup(String server, int port){
        System.out.println("Client starting...");
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runClient(boolean addTerminate)
    {
        Random rand = new Random();
        try
        {
            // Create input and output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            int startValue = rand.nextInt(999999999);
            int randomIncrementer = rand.nextInt(30);

            while (true) {
                startValue += randomIncrementer;
                if (startValue >= 100000) {
                    startValue = rand.nextInt(999999999);
                    randomIncrementer = rand.nextInt(30);
                }
                String str = String.format("%09d", startValue);
                out.println(str);
                if(addTerminate){
                    out.println("terminate");
                }
            }

        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void sendNonConformingInput(){
        try {
            PrintStream out = new PrintStream( socket.getOutputStream() );
            out.println("gib7e8ish9");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeObviousNumbers(){
        PrintStream out = null;
        try {
            out = new PrintStream( socket.getOutputStream() );
            while(true){
                for(int i = 0; i < 100; i++){
                    String str = String.format("%09d", i);
                    out.println(str);
                }
                out.println("terminate");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
