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

To implement redundancy, the primary server (which is the server initialized with offset 0)
forwards *state altering* requests it receives to the other servers. Non-state altering requests
are
- GetAccounts
- SendMessage **to logged in user**