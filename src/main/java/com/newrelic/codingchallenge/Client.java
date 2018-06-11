package com.newrelic.codingchallenge;

import com.newrelic.codingchallenge.client.SocketClient;

import java.util.ArrayList;
import java.util.List;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(5);
        // This works
        threads.add(new Thread(() -> {
                SocketClient client = new SocketClient();
                client.setup("localhost", 4000);
                client.runClient(false);
            }));
        threads.add(new Thread(() -> {
            SocketClient client = new SocketClient();
            client.setup("localhost", 4000);
            client.runClient(false);
        }));
        threads.add(new Thread(() -> {
            SocketClient client = new SocketClient();
            client.setup("localhost", 4000);
            client.runClient(false);
        }));
        threads.add(new Thread(() -> {
            SocketClient client = new SocketClient();
            client.setup("localhost", 4000);
            client.runClient(false);
        }));
        threads.add(new Thread(() -> {
            SocketClient client = new SocketClient();
            client.setup("localhost", 4000);
            client.runClient(false);
        }));

        threads.forEach(Thread::start);


        while (true) {
            try {
                Thread.sleep(500);
            }
            catch(Exception ignored){

            }
        }
    }
}
