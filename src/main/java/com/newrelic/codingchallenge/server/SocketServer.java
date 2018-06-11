package com.newrelic.codingchallenge.server;

import com.newrelic.codingchallenge.service.Deduplicator;
import com.newrelic.codingchallenge.service.LogWriter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.*;

public class SocketServer extends Thread
{
    public ServerSocket serverSocket;
    private int port;
    private boolean running = false;
    private Thread shutdownHook = null;
    protected Thread mainThread;
    public static boolean shutdownReceived = false;
    private static final int maxNumberOfConnections = 5;
    private int numberOfConnections = 0;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(maxNumberOfConnections*2);
    private final static Collection<RequestHandler> activeSocketRequestHandlers = new ConcurrentLinkedQueue<>();
    public static int duplicateCount = 0;
    private static int lastUniqueCount = 0;
    private static int lastDuplicateCount = 0;
    public static int uniqueCount = 0;
    private static long lastTime = System.nanoTime();
    private static final Object lock = new Object();
    private String logPath;
    private long startTime;

    public SocketServer(int port, String logPath)
    {
        this.port = port;
        this.logPath = logPath;
    }

    public SocketServer()
    {
        this.port = 4000;
        this.logPath = "numbers.log";
    }

    public void startServer()
    {
        try {
            if (shutdownHook == null) {
                shutdownHook = new ShutdownHook();
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
            cleanLogFile();
            synchronized (this) {
                this.mainThread = Thread.currentThread();
            }
            openServerSocket();
            this.start();
            startTime = System.nanoTime();
        }finally {
            mainThread = null;
        }
    }

    private void cleanLogFile() {
        try {
            (new FileWriter(new File(logPath).getAbsoluteFile(),false)).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopServer()
    {
        running = false;
        printStatistics();
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Listening for connections...");

        while (isRunning()) {
            if(hasShutdowned())
                break;

            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if (!isRunning()) {
                    System.out.println("Server has been shutdown.");
                    break;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }

            try {

                RequestHandler rh = new RequestHandler(clientSocket, logPath);
                boolean addedConnection = addConnection(rh);
                if(addedConnection) {
                    this.threadPool.execute(rh);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.");

    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            if (running) {
                shutdownHandlersGracefully();
                stopServer();
            } else {
                System.out.println("Shutdown already called.");
            }
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + port, e);
        }
    }

    private synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized boolean addConnection(RequestHandler requestHandler) {
        if(this.numberOfConnections < maxNumberOfConnections) {
            synchronized (activeSocketRequestHandlers) {
                activeSocketRequestHandlers.add(requestHandler);
            }
            this.numberOfConnections++;
            return true;
        }
        return false;
    }

    public void shutdownHandlersGracefully(){
        activeSocketRequestHandlers.forEach(RequestHandler::stopRequestHandler);
    }

    private void outputStatistics(){
        long stopTime = System.nanoTime();
        long elapsedTime = (stopTime - startTime ) / 1000000000;
        System.out.println("Run time: " + elapsedTime);
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is megabytes: " + memory / (1024L * 1024L));
    }

    private boolean hasShutdowned(){
        if (shutdownReceived){
            shutdownHandlersGracefully();
            stopServer();
            return true;
        }
        return false;
    }

    public static void printStatistics() {

        long currTime = System.nanoTime();
        long elapsedTime = (currTime - lastTime) / 1000000000;
        if (elapsedTime >= 10) {
            synchronized (lock) {
                lastTime = currTime;
                System.out.println("**** Received  " + (uniqueCount - lastUniqueCount) + " unique numbers, " + (duplicateCount - lastDuplicateCount) + " duplicate numbers. Unique total: " + uniqueCount + " in " + elapsedTime + "(s).");
                lastDuplicateCount = duplicateCount;
                lastUniqueCount = uniqueCount;
            }
        }
    }
}


