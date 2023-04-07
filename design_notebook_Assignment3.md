## Implementation Notes
### Challenges
- Previously, our implementation was agnostic of the status of the servers (e.g., the clients
   had no way of knowing whether the server was still available). As such, we had to implement
   a separate thread to check the server's status. Interestingly, GRPC connections are only 
   set to `READY` once *some* RPC call has been made. This required us to implement a handshake
   RPC call to initiate the connection.
- Other important decisions were
   - Where fault tolerance should be handled (client or server?).  
   - How to handle primary vs. back up servers
- On the persistence side, one challenge was deciding how to persist the server's status. Initially
   the persistence required a good deal of custom code, but the Gson package made this process a lot
   easier, and it also handled the conversion back from text to Java objects very well.

### Performance
Overall, the performance seems to be comparable, except that up to three messages will need to be
sent for every one message sent previously.

# March 22

## 2-Fault Tolerance
Idea:
1. Maintain 3 servers, with known IP addresses / ports.
    - All Clients should have the servers configured the same way.
    - By external configuration, an ordering is imposed on the servers; the lowest server
      in the ordering is elected the primary.
2. On network requests to the primary server processes the request as normal, except in 
   the following cases,
    - On `CREATE_USER`, the request is forwarded to the other 2 servers
    - On `SEND_MESSAGE`, *if* the message is undelivered, forward the request
3. If a client detects a server is down, a connection is made to the next server in the
   ordering.

To implement the above, we'll need the following changes.
1. Client should have a separate thread which checks the status of the server.
2. The servers should act as a client to the other two servers, in order to forward requests.

>**Bug**
>When attempting to check server status using `getState`, the status doesn't move from `IDLE` to
>`READY` until *some* request has been sent.

# March 23
The client was slightly restructured to allow it to properly terminate in response to a server
failure. Now, the first that happens after a server IP is entered, is that the user is prompted
to login or create an account. Afterwards, the grpc connection is established, and a disconnect
can be detected by checking the channel's status is not `READY`.

>**Update**
> Rather than the `CREATE_USER` request being used to signal a connection, we've implemented a 
> separate `HandshakeRequest` which establishes the connection and allows the heartbeat checks
> to return the correct value.

To implement redundancy, the primary server (which is the server initialized with offset 0)
forwards *state altering* requests it receives to the other servers. Non-state altering requests
are
- GetAccounts
- SendMessage **to logged in user**

## Persistence

We started to think about how to modify our system to incorporate persistence. We made the design decision to incorporate persistence into the list of all users and the map of undelivered messages. We made the design decision to not make the list of logged in users persistent, meaning that if the server crashes then all of the logged-in users are logged out. We believe this is a fair design decision, and it does fit in the specifications.

We will have the server keep track of two files: ``all_users.txt`` and ``undelivered_messages.txt``. These will be created if the don't yet exist. Upon starting up, the server reads these two files and sees if there is content. The server has two data structures ``private final Set<String> allAccounts`` and ``private final Map<String, List<Message>> undeliveredMessages`` that it uses to keep track of and modify the set of all accounts and map of undelivered messages. If there are any changes to either of these two made by the server, the server always updates the two files accordingly. This implementation facilitates the manipulation and use of the set of all accounts and map of undelivered messages, and works well with the way we store these in the files.

The formatting of the files uses JSON and GSON. GSON provides a nice way to convert data structures and classes into JSON strings, which we then write to the file. When either of the files are read, GSON also provides a nice way to convert the file contents back into the data structure or class. Using GSON and JSON is simple and understandable in the code, and ensures that we don't encounter bugs and errors related to how the data is stored in the files.

# March 24

## Persistence
Today, we worked on writing up the logic involved with starting up the server. In the constructor method for the ServerCore, we have to add the following logic to create the initialization of the all users and undelivered messages files:

Try the following (and catch IOException when relevant):
- Instantiate the data structures: ``private final Set<String> allAccounts`` and ``private final Map<String, List<Message>> undeliveredMessages``.
- Create the `all_users.txt` file if it doesn't exist. Create the `undelivered_messages.txt` file if it doesn't exist.
- Create `BufferedReader` instances for each of the two files `all_users.txt` and `undelivered_messages.txt`.
- Read the first line of  `all_users.txt` and `undelivered_messages.txt`. These first lines will contain all of the content for each file (since each is just one line long by the design). Call the first line of `all_users.txt`: `String usersList`. Call the first line of `undelivered_messages.txt`: `String undeliveredMsgList`.
- If `userList` is null, then convert `allAccounts` to JSON using GSON and write this to `all_users.txt`. Else, add the existing accounts in `all_users.txt` to `allAccounts` by reading from `all_users.txt` and using GSON to convert this back into a `HashSet<String>`. Add all elements of this HashSet to `allAccounts`.
- Do the exact same thing as in the previous step but with `undeliveredMessages` and `undelivered_messages.txt`. Here, convert the contents of the text file back into a `HashMap<String, List<Message>>` and put all elements of this HashMap in `undeliveredMessages`.


# March 25

## Persistence
We implemented the methods for adding and deleting users and adding and deleting undelivered messages. For both of these, in the code we add/delete from `allAccounts` or `undeliveredMessages` correspondingly. We then overwrite the corresponding text files with the updated version of the data structure.

# March 26
>**Decision**
>Rather than the servers forwarding request to other servers, the client will be tasked with
>forwarding the request. The reason for this is that the server already needs to maintain a
>heartbeat with the other servers, in order to check whether they are live. Furthermore, this
>keeps the servers independent of each other.

>**Bug**
> New clients need to know what the "primary" server is, in order to all agree on the same
> server. As such, the server will keep a `isPrimary` variable, and this variable will be returned
> as part of the handshake reply.