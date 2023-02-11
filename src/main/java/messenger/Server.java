package messenger;
import messenger.objects.*;
import messenger.objects.helper.API;
import messenger.objects.helper.APIException;
import messenger.objects.request.CreateAccountRequest;
import messenger.objects.request.DeleteAccountRequest;
import messenger.objects.request.Request;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Multithreaded server implementation, loosely based on
 * https://www.geeksforgeeks.org/multithreaded-servers-in-java/
 *
 * For each connection, the server creates a ClientHandler instance in a separate
 * thread.
 */
public class Server {
    public static void main(String[] args){
        ServerCore server = new ServerCore();
        ServerSocket serverSocket = null;
        try {
            System.out.println("Starting server...");
            serverSocket=new ServerSocket(6666);
            serverSocket.setReuseAddress(true);
            System.out.println("Waiting for connection...");

            while(true) {
                Socket s=serverSocket.accept();//establishes connection
                System.out.println("New connection established " + s.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(s, server);
                new Thread(clientHandler).start();
            }
        } catch(Exception e) {
            System.out.println(e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final ServerCore server;

        // Constructor
        public ClientHandler(Socket socket, ServerCore server)
        {
            this.clientSocket = socket;
            this.server = server;
        }

        private Request parseRequestString(DataInputStream stream) throws IOException {
            // Read method identifier
            List<String> args = new ArrayList<>();
            int method = stream.readInt();
            // All methods currently expect one argument
            args.add(stream.readUTF());
            return new Request(method, args);
        }

        public void run() {
            DataOutputStream out = null;
            DataInputStream in = null;
            try {
                // get the outputstream of client
                out = new DataOutputStream(clientSocket.getOutputStream());
                // get the inputstream of client
                in = new DataInputStream(clientSocket.getInputStream());
  
                while (true) {
                    /*
                    1. Read input as a string
                    2. Convert string input into a Request
                    3. Depending on first field of Request, enter appropriate IF statement
                    IF:
                        4. request = [method]Request.parseGenericRequest(request);
                        5. [Method]Response = [method](request)
                    6. String rawResponse = encResponse([Method]Response.getGenericResponse());
                    7. sendToClient(rawResponse)
                    */

                    try {
                        // Form request
                        Request request = parseRequestString(in);

                        // Parse integer into the corresponding API call
                        System.out.println(request.getMethodId());
                        API calledMethod = API.fromInt(request.getMethodId());
                        if (calledMethod == API.CREATE_ACCOUNT) {
                            // Client wants to create a new user
                            CreateAccountRequest createAccountRequest = new CreateAccountRequest(request);
                            Status status = server.createAccount(createAccountRequest);
                            System.out.println(status.getMessage());
                        } else if (calledMethod == API.DELETE_ACCOUNT) {
                            DeleteAccountRequest deleteAccountRequest = new DeleteAccountRequest(request);
                            Status status = server.deleteAccount(deleteAccountRequest);
                            System.out.println(status.getMessage());
                        }
                    } catch (EOFException e) {
                        System.out.println("Connection closed");
                        break;
                    } catch (APIException e) {
                        e.printStackTrace();
                        //out.writeUTF(e.toString());
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}