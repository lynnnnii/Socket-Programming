import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerEx {

    public static void main(String[] args) {
        ServerSocket listener = null;
        int nPort = 1234;

        // Create a thread pool that can handle up to 10 concurrent clients
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        try {
            // Create a server socket listening on port 1234
            listener = new ServerSocket(nPort);
            System.out.println("Multi-threaded server is waiting on port " + nPort + "...");

            // Keep the server running and continuously accept new clients
            while (true) {
                Socket socket = listener.accept(); // Wait for a client to connect
                threadPool.submit(new ClientHandler(socket)); // Assign the client to a thread in the pool
            }
        } catch (IOException e) {
            System.out.println("Main server error: " + e.getMessage());
        } finally {
            System.out.println("Server shutting down...");

            try {
                if (listener != null) listener.close();
                if (threadPool != null) threadPool.shutdown();
            } catch (IOException e) {
                System.out.println("Error while closing server resources: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // A class responsible for handling communication with one connected client
    static class ClientHandler implements Runnable {

        private Socket socket; // Socket for the connected client
        private BufferedReader in = null;
        private BufferedWriter out = null;

        // Constructor receives the socket accepted by the main server
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        // Automatically executed when submitted to the thread pool
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + ": Handling client connection from " +
                               socket.getInetAddress() + "...");

            try {
                // Set up input/output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // Communication loop
                while (true) {
                    String inputMessage = in.readLine();

                    // Handle disconnection or "bye" command
                    if (inputMessage == null || inputMessage.equalsIgnoreCase("bye")) {
                        System.out.println(Thread.currentThread().getName() +
                                           ": Client disconnected (" + socket.getInetAddress() + ")");
                        break;
                    }

                    System.out.println(Thread.currentThread().getName() +
                                       " received: " + inputMessage);

                    String res = processRequest(inputMessage);

                    // Send response back to the client
                    out.write(res + "\n");
                    out.flush();
                    System.out.println(Thread.currentThread().getName() +
                                       " sent: " + res);
                }

            } catch (IOException e) {
                System.out.println(Thread.currentThread().getName() +
                                   ": Communication error - " + e.getMessage());
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Process and evaluate client requests (e.g., ADD 10 20)
        private String processRequest(String request) {
            try {
                String[] tokens = request.trim().split(" ");
                if (tokens.length != 3) {
                    return "401 TOO_MANY_ARGS";
                }

                String command = tokens[0].toUpperCase();
                int NUM1 = Integer.parseInt(tokens[1]);
                int NUM2 = Integer.parseInt(tokens[2]);

                switch (command) {
                    case "ADD": return "200 " + (NUM1 + NUM2);
                    case "SUB": return "200 " + (NUM1 - NUM2);
                    case "MUL": return "200 " + (NUM1 * NUM2); // Fixed: multiplication
                    case "DIV":
                        if (NUM2 == 0) {
                            return "400 Bad Request (Divide by Zero)";
                        } else {
                            return "200 " + (NUM1 / NUM2);
                        }
                    default:
                        return "401 Bad Request (Invalid Command)";
                }
            } catch (NumberFormatException e) {
                return "401 Bad Request (Invalid Format)";
            } catch (Exception e) {
                System.out.println("Processing Error: " + e.getMessage());
                return "500 Internal Server Error";
            }
        }
    }
}
