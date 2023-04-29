import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 8000);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);

            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String message;
            while ((message = input.readLine()) != null) {
                System.out.println(message);
            }

        } catch (Exception e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            if (!client.isClosed()) {
                input.close();
                output.close();
                client.close();
            }
        } catch (Exception e) {
            // Ignores
        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            while (!done) {
                try {
                    String message = inputReader.readLine();
                    if (message.equals("/quit")) {
                        inputReader.close();
                        shutdown();
                    } else {
                        output.println(message);
                    }
                } catch (IOException e) {
                    shutdown();
                }

            }
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

}
