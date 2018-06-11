package com.newrelic.codingchallenge;


import com.newrelic.codingchallenge.client.SocketClient;
import com.newrelic.codingchallenge.server.SocketServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Main {
    private static final long MEGABYTE = 1024L * 1024L;

    public static void main(String[] args) {

        System.out.println("Starting up server ....");
        int port = 4000;
        SocketServer server = new SocketServer(port, "numbers.log");
        server.startServer();
    }
}