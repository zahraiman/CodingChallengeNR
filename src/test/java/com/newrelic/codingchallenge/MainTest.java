package com.newrelic.codingchallenge;

import com.newrelic.codingchallenge.server.RequestHandler;
import com.newrelic.codingchallenge.server.SocketServer;
import com.newrelic.codingchallenge.service.LogWriter;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by zahraiman on 6/10/18.
 */
public class MainTest {
    private static int port = 4000;
    private static String logPath = "src/test/numbers-test.log";
    private static SocketServer serverSocket;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(12);


    public void setupServer() throws IOException {
        serverSocket = new SocketServer(port, logPath);
        serverSocket.startServer();
    }


    public void deduplicatorOnlyPrintsUniqueDigits() throws IOException, InterruptedException {
        Socket clientSocket = connectToServer();
        RequestHandler mockedRh = mockRequestHandler(clientSocket);

        Thread mockedRhThread = new Thread(mockedRh::run);
        Thread digitSender = new Thread(() -> {
            try {
                sendDigits(clientSocket);
                sendShutdown(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.threadPool.execute(mockedRhThread);
        this.threadPool.execute(digitSender);

        while(!SocketServer.shutdownReceived) {
            Thread.sleep(100);
        }

        mockedRh.stopRequestHandler();
        List<Integer> digits = readLogFile(logPath);
        int[] originalDigits = getDigits();
        assertTrue(digits.size() == originalDigits.length - 3);
        for(int d :originalDigits ) {
            assertTrue(digits.contains(d));
        }
    }


    public void terminateCausesGracefulShutdown() throws Exception {
        Socket clientSocket1 = connectToServer();
        RequestHandler mockedRh1 = mockRequestHandler(clientSocket1);
        RequestHandler mockedRh2 = mockRequestHandler(clientSocket1);
        RequestHandler mockedRh3 = mockRequestHandler(clientSocket1);
        Thread mockedRhThread1 = new Thread(mockedRh1::run);
        Thread mockedRhThread2 = new Thread(mockedRh2::run);
        Thread mockedRhThread3 = new Thread(mockedRh3::run);
        serverSocket.addConnection(mockedRh1);
        serverSocket.addConnection(mockedRh2);
        serverSocket.addConnection(mockedRh3);

        Thread digitSender1 = new Thread(() -> {
            try {
                sendDigits(new int[] { 1, 2, 3, 4, 5}, clientSocket1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread digitSender3 = new Thread(() -> {
            try {
                sendDigits(new int[] { 21, 22, 23, 24, 25}, clientSocket1);
                sendShutdown(clientSocket1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread digitSender2 = new Thread(() -> {
            try {
                sendDigits(new int[]{11, 12, 13, 14, 15}, clientSocket1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.threadPool.execute(mockedRhThread1);
        this.threadPool.execute(mockedRhThread2);
        this.threadPool.execute(mockedRhThread3);
        this.threadPool.execute(digitSender1);
        this.threadPool.execute(digitSender2);
        this.threadPool.execute(digitSender3);
        while(!SocketServer.shutdownReceived) {
            Thread.sleep(100);
        }
        Whitebox.invokeMethod(serverSocket, "hasShutdowned");


        verify(mockedRh1, times(1)).stopRequestHandler();
        verify(mockedRh2, times(1)).stopRequestHandler();
        verify(mockedRh3, times(1)).stopRequestHandler();
    }

    private List<Integer> readLogFile(String logPath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(new File(logPath)));
        String line;
        List<Integer> digits = new ArrayList<>();
        while((line = in.readLine()) != null){
            digits.add(Integer.valueOf(line));
        }
        return digits;
    }

    public int[] getDigits(){
        //12 unique, 3 duplicate
        return new int[]{123, 245, 678, 123456789, 234567891, 345678912, 456789123, 567891234, 678912345, 789123456, 891234567, 912345678, 234567891, 345678912, 123};
    }

    private Socket connectToServer() throws IOException {
        return new Socket("localhost", port);
    }


    private void sendDigits(Socket clientSocket) throws IOException {
        InputStream stream = mock( InputStream.class );
        PrintStream out = new PrintStream( clientSocket.getOutputStream() );
        for(int d : getDigits()){
            String str = String.format("%09d", d);
            out.println(str);
        }
    }

    private void sendDigits(int[] digits, Socket clientSocket) throws IOException {
        PrintStream out = new PrintStream( clientSocket.getOutputStream() );
        for(int d : digits){
            String str = String.format("%09d", d);
            out.println(str);
        }
    }

    private void sendShutdown(Socket clientSocket) throws IOException {

        PrintStream out = new PrintStream( clientSocket.getOutputStream() );
        out.println("terminate");
    }

    private RequestHandler mockRequestHandler(Socket clientSocket) throws IOException {
        (new FileWriter(new File(logPath).getAbsoluteFile(),false)).close();
        RequestHandler rh = mock(RequestHandler.class);
        Whitebox.setInternalState(rh, "socket", clientSocket);
        return rh;
    }
}
