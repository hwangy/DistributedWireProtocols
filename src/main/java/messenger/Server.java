package messenger;
import messenger.objects.*;

import java.io.*;
import java.net.*;  
import java.util.*;

public class Server {

    private Map<String, ArrayList<Message>> sentMessages;
    private Map<String, ArrayList<Message>> queuedMessages;
    private Set<String> loggedInUsers;
    private Set<String> allUsers;

    private void initializeObjects() {
        sentMessages = new HashMap<>();
        queuedMessages = new HashMap<>();
        loggedInUsers = new HashSet<>();
        allUsers = new HashSet<>();
    }

    /**
     * Creates a user with a given username. If the user exists,
     * returns an unsuccessful Status object. Otherwise, the user
     * is added to ``allUsers`` as well as ``loggedInUsers``.
     *
     * @param username  Username of new user.
     * @return          A status object indicating whether the operation
     *                  succeeded or failed.
     */
    private Status createUser(String username) {
        if (allUsers.contains(username)) {
            return Status.genFailure("User " + username + " already exists.");
        }
        this.allUsers.add(username);
        return Status.genSuccess();
    }
    public static void main(String[] args){

        try{
            System.out.println("Starting server...");
            ServerSocket ss=new ServerSocket(6666);
            System.out.println("Waiting for connection...");
            Socket s=ss.accept();//establishes connection

            String options = "Pick a method:\n" +
            "1. Create an account. You must supply a unique user name.\n" +
            "2. List accounts (or a subset of the accounts, by text wildcard)\n" +
            "3. Send a message to a recipient.\n" +
            "4. Deliver undelivered messages to a particular user.\n" +
            "5. Delete an account. If you attempt to delete an account that contains undelivered message, (ADD HERE)";

            while(true) {
                System.out.println(options);

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
                break;
            }

            DataInputStream dis=new DataInputStream(s.getInputStream());
            String  str=(String)dis.readUTF();
            System.out.println("message= "+str);
            ss.close();

        }catch(Exception e) {
            System.out.println(e);
        }
    }

}  