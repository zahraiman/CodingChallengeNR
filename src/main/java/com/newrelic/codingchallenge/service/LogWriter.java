package com.newrelic.codingchallenge.service;

import com.newrelic.codingchallenge.server.SocketServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by zahraiman on 6/10/18.
 */
public class LogWriter {
    private long lastTime = System.nanoTime();
    private final PrintWriter logWriter;
    private static final Object lock = new Object();

    public LogWriter(String logPath) throws IOException {
        logWriter = new PrintWriter(new FileWriter(new File(logPath).getAbsoluteFile(),true));
    }

    public void writeLog(int digit) {
        synchronized (lock) {
            long currTime = System.nanoTime();
            long elapsedTime = (currTime - lastTime) / 1000000;
            if (elapsedTime >= 200) {
                lastTime = currTime;
                logWriter.flush();
            }
        }
        SocketServer.uniqueCount++;
        logWriter.println(digit);
    }


    public void closeWriter(){
        synchronized (logWriter) {
            logWriter.flush();
            logWriter.close();
        }
    }
}
