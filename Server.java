import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private boolean done;
    private ServerSocket serverSocket;
    private ExecutorService pool;

    Server() {
        done = false;
        connections = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8000);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }

    }

    public void broadcastMessage(String message) {
        for (ConnectionHandler connectionHandler : connections) {
            if (connectionHandler != null) {
                connectionHandler.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        done = true;
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
                for (ConnectionHandler connectionHandler : connections) {
                    connectionHandler.shutdown();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    class ConnectionHandler implements Runnable {

        Socket client;
        BufferedReader input;
        PrintWriter output;
        public String nickname;

        ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output = new PrintWriter(client.getOutputStream(), true);

                output.println("Enter your Nickname :");
                nickname = input.readLine();
                broadcastMessage(nickname + " joined the chat");
                System.out.println(nickname + " Connected");

                String message;
                while ((message = input.readLine()) != null) {
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            System.out.println(nickname + " renamed to " + messageSplit[1]);
                            broadcastMessage(nickname + " renamed to " + messageSplit[1]);
                            nickname = messageSplit[1];
                        } else {
                            System.out.println("No Nickname Provided");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcastMessage(nickname + " leaved the chat");
                        shutdown();
                    } else {
                        broadcastMessage(nickname + " : " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void shutdown() {
            if (!client.isClosed()) {
                try {
                    input.close();
                    output.close();
                    client.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        public void sendMessage(String message) {
            output.println(message);
        }

    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

}
