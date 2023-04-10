# DistributedWireProtocols
Project 3 (formerly Project 1) for Distributed Systems

## How To Run

1. Run `git clone https://github.com/hwangy/DistributedWireProtocols.git`
2. `cd` into the directory `DistributedWireProtocols`
3. Run `./gradlew build`
   * If you are on a Windows system, use `gradlew.bat build` instead, and similarly
     for the below commands.
4. On the server `./gradlew runServerGRPC --console=plain`
5. On the client:
   * Check the IP address of the server
   * Run: `./gradlew runClientGRPC --console=plain`
   * Enter the IP address when prompted.

>**Unit Tests**
>Note, unit tests will run when the project is built. The passing status of the tests
>is indicated by `BUILD SUCCESSFUL` in green.

### Notes:
* Exiting the client with `ctrl-c` *will not* log out the client. As a result,
    this will prevent you from logging in as that user in the future. Therefore,
    always exit using option `0`.
* The latest code for the project is in the `main` branch (not `grpc`). By default
    the `git clone` command will pull from `main`, as desired. Similarly, the latest
    design notebook is also present in `main`.

## Design Overview
The **server** acts as the server for the Messenger service, but as the client for the
MessageReceiver service. This allows the server to dispatch received messages to the
appropriate clients asynchronously and without delay.

The **client** acts as the client for the Messenger service (and thus issue requests like
`createAccount` or `sendMessage`), but as the server for the MessageReceiver service
and is constantly listening for incoming messages.

### The Server
On startup, `ServerGRPC` launches a MessageServer on port `Constants.API_PORT`
(which defaults to `7777`). When a `CREATE ACCOUNT` or `LOGIN` request is received, a
new Thread is created which runs the `MessageHandler`. The server also assigns a
**unique** address/port to the client on which the client is expected to start the
MessageReceiver service.

The message handler constantly looks for messages to send to that particular logged-in
user and sends messages using the MessageReceiver service.

The server keeps track of the following state,
1. All currently existing users
2. All logged in users
3. All logged in user's connection information (IP address and port used for the 
    MessageReceiver service)
4. Undelivered and delivered messages.

### The Client
On startup, `ClientGRPC` asks the user for the Server IP address, then connects on
port `Constants.API_PORT`. After a `CREATE ACCOUNT` or `LOGIN` request is sent and a
response received, the client takes the port assigned by the server and launches a
MessageReceiver service on which to receive incoming messages.
   
## An Example Scenario to Test

Below we describe an example scenario you may want to try out to test our system. This will involve 
two people, on devices called Device 1 and Device 2.

1. Create 3 server instances, two on the Device 1 and the third on Device 2. The primary server instance should be on Device 1.
2. Create 2 client instances, one on each device.
3. Log in a user Alice on Device 1.
4. Log in a user Bob on Device 2.
5. Send the message "Hello" from Alice to Bob. (This should output "Hello" on Bob's side.)
6. Log out Bob.
7. Send the message "Hi again" from Alice to Bob. (This message will be added to the persistent list of undelivered messages.)
8. Control-C on the primary server instance on Device 1.
9. Observe that the primary server role is granted to a different server instance.
10. Reconnect the server that was disconnected.
11. Reconnect the client instances on each device as promted on the terminal screen.
12. Log in the user Alice on Device 1. Observe that because the username Alice was created already and added to the persistent list of accounts, you need to utilize the "log into an existing account" option, which will successfully log in the user Alice.
13. Log in the user Bob on Device 2.
14. Call the "get undelivered messages" feature on Bob's account on Device 2. Observe that the message "Hi again" is sent from Alice to Bob.

This scenario demonstrates both persistence and 2-fault tolerance.
