import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientEx {

    public static void main(String[] args) {
        String serverIp = "localhost"; // default IP
        int port = 1234; // default port
        String configFileName = "server_info.dat";

        try (BufferedReader fileReader = new BufferedReader(new FileReader(configFileName))) {
            // Try reading configuration file
            System.out.println("Reading server information from " + configFileName);
            serverIp = fileReader.readLine(); // 1st line: IP address
            port = Integer.parseInt(fileReader.readLine()); // 2nd line: port number

            if (serverIp == null) serverIp = "localhost"; // In case file exists but IP line is empty

        } catch (FileNotFoundException e) {
            // File not found → use default values
            System.out.println(configFileName + " not found.");
            System.out.println("Attempting to connect with default configuration (" + serverIp + ":" + port + ").");
        } catch (IOException e) {
            // Error while reading file
            System.out.println("Error occurred while reading " + configFileName + ": " + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            // Port number is not numeric
            System.out.println("Invalid port number in " + configFileName + ". Using default port (1234).");
            port = 1234;
        }

        System.out.println("Trying to connect to server: " + serverIp + ":" + port);

        // Socket and I/O stream setup
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in); // For user keyboard input

        try {
            // Connect to the server
            socket = new Socket(serverIp, port);
            System.out.println("Connected to the server.");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Start communication loop
            while (true) {
                System.out.print("Enter expression (e.g., ADD 10 20) >> ");
                String outputMessage = scanner.nextLine(); // Read one line from user

                // Send message to server
                out.write(outputMessage + "\n");
                out.flush();

                // Terminate if user types "bye"
                if (outputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("Closing connection...");
                    break;
                }

                // Wait for server response
                String inputMessage = in.readLine();
                if (inputMessage == null) {
                    System.out.println("Server connection closed.");
                    break;
                }

                // Parse and display server response
                processResponse(inputMessage);
            }

        } catch (UnknownHostException e) {
            System.out.println("Cannot find server IP (" + serverIp + ")");
        } catch (IOException e) {
            System.out.println("I/O error occurred during communication: " + e.getMessage());
        } finally {
            // Release all resources
            try {
                scanner.close();
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Error while closing resources: " + e.getMessage());
            }
        }
    }

    // Handle and display the response from the server
    private static void processResponse(String response) {
        String[] tokens = response.split(" ", 2); // Split response into [status code] and [data]
        String statusCode = tokens[0];

        if (statusCode.equals("200")) {
            // Success
            System.out.println("Answer: " + tokens[1]);
        } else {
            // Failure (400, 401, etc.)
            System.out.println("Error: " + tokens[1]);
        }
    }
}
