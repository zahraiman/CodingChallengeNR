package com.newrelic.codingchallenge.service;

import com.newrelic.codingchallenge.server.SocketServer;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by zahraiman on 6/9/18.
 */
public class Deduplicator {
    private static final int maxCombinationSize = 1000000000;
    private static boolean[] hasDigits = new boolean[maxCombinationSize];
    private static LogWriter logWriter;
    private static final Object lock = new Object();
    private static String logPath;

    public Deduplicator(String filePath) throws IOException {
        logPath = filePath;
        setLogWriter();
    }

    private void setLogWriter() throws IOException {
        synchronized (lock) {
            if(logWriter == null) {
                logWriter = new LogWriter(logPath);
            }
        }
    }

    public void deduplicateAndWriteLog(int digit) {

        synchronized(lock) {
            boolean exists;
            if (!hasDigits[digit]) {
                hasDigits[digit] = true;
                exists = false;
            } else {
                exists = true;
                SocketServer.duplicateCount++;
            }

            if(!exists){
                logWriter.writeLog(digit);
            }
        }
    }

    public void gracefulShutdown() {
        logWriter.closeWriter();
    }
}
