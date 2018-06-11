package com.newrelic.codingchallenge.server;

import com.newrelic.codingchallenge.service.Deduplicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by zahraiman on 6/9/18.
 */
public class RequestHandler extends Thread {
    private Socket socket;
    private Deduplicator deduplicator;
    private static final int numberOfdigits = 9;
    private boolean isStopped = false;
    private static final Object lock = new Object();

    RequestHandler(Socket socket, String logPath) throws IOException {
        this.socket = socket;
        deduplicator = new Deduplicator(logPath);
    }

    public void stopRequestHandler() {
        isStopped = true;
        deduplicator.gracefulShutdown();
    }

    @Override
    public void run()
    {
        try {
            System.out.println("Received a connection");
            // Get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;
            while (!isStopped && (line = in.readLine()) != null) {
                SocketServer.printStatistics();
                if (!checkValidity(line)) {
                    break;
                }

                if (line.equals("terminate")) {
                    SocketServer.shutdownReceived = true;
                    break;
                }

                deduplicator.deduplicateAndWriteLog(Integer.valueOf(line));
            }

            // Close our connection
            in.close();
            socket.close();
            System.out.println("Connection closed");
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally {
            deduplicator.gracefulShutdown();
        }
    }

    private boolean checkValidity(String line) {
        String regex = "\\d{" + numberOfdigits + "}";
        if(!line.matches(regex) && !line.equals("terminate")){
            try {
                socket.close();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}



