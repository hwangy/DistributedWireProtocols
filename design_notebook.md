The following tracks our design decisions for the wire protocols project.

# February 3rd

### Design of Wire Protocol
The two core objects are the Request and Response. 
>**Decision** Currently, there are no internal objects representing the 
> request and response. This can probably be created on-the-fly.

#### Request
The request specifies the method the client is calling, as well as any 
arguments required by the method.
```
Request {
    (4)int method,
    (4)int first argument length,
    (variable)string first argument,
    ...
}
```
Each server method is associated with a fixed number of arguments and
the client should provide an integer and string for each argument,
specifying the length of the argument, as well as the argument itself.

#### Response
```
Response {
	(4)int success,
	If success = 0
		(4)int length of error message,
		(variable)string error message
	Else
		(4)int length of response,
		(variable)string response
}
```
The response includes a integer encoding the success or failure of
the request. If the request failed, the response includes a non-zero
integer encoding the length of the error message, followed by the 
error message itself. Otherwise, there is an integer for the length
of the response, followed by the response itself.

> **Decision**. The request and responses will be *agnostic* of the 
> the data structure underlying the request and response, generically
> representing everything as a string. The associated data structures
> (e.g. for a message) will have accompanying ``toString`` and ``fromString``
> methods to perform the encoding and decoding.

### Higher Level Data Structures

#### Network Requests
An object for representing whether a request was successful or not. 
**This object does not encode network errors** which are instead 
represented at the request / response level.
```
src.main.java.Messenger.objects
Status {
	1. Boolean success
	// if not successful
	2. String errorMessage
}
```

An object for a single message.
```
src.main.java.Messenger.objects
Message {
	1. int timestamp
	// -1 if undelivered
	2. int deliveredTimestamp
	3. String sender
	4. String receiver
	5. String message
}
```
>**Decision** This object will initialize delivered_timestamp to -1 (e.g. if
> the message is queued for delivery) and only set to a value > 0 once it's
> been sent. *We're currently not planning on verifying receipt by the client
> and assuming a message is delivered if it's sent*.

#### Local Data Structures

Client structures:
1. ```Map<String, Array<Message>> receivedMessages```

Server
1. ```Map<String, Array<Message>> sentMessages```
2. ```Map<String, Array<Message>> queuedMessages```
3. ```Set<String> loggedInUsers```

The first two data structures are HashMaps, mapping String usernames
to the messages addressed to that user. Upon receiving a message, if 
the destination user is in ``loggedInUsers``, then the message is sent
and added to ``sentMessages.`` Otherwise, it is added to ``queuedMessages.``

Messages will be delivered when the corresponding user next logs in.

### API
>**Decision** The client and server will both have access to the *same* 
> methods. Essentially, the difference is that the server maintains the
> "source of truth" of messages, and clients can call methods to update
> the server and synchronize their local store of messages with the 
> server (e.g. by calling ``getMessages`` and ``sendMessage``).

>**Decision** As noted in the comment for the object ``Status``, the
> status returned by the API calls will not include network errors. Instead,
> these return values represent assumption violations (e.g. creating a user
> which already exists).

1. ```void createUser(String username)```
	* Registers a user with the server.
2. ```Array<String> getAccounts(optional String regex)```
    * Gets accounts with an optional regex argument.
3. ```Status sendMessage(Message msg)```
    * Sends a message to specified user (which is a field of msg).
    > **Decision** ``sendMessage`` doesn't indicate whether a message was
	> delivered or merely queued.
4. ```Array<Message> getUndeliveredMessages(String username)```
    * Gets all undelivered messages for a provided username.
5. ```Status deleteAccount(String username)```
    * Deletes an account (but not any associated messages).

### Concurrency
Idea:
- The server creates a new thread for each connection and maintains a list
  of logged in users.

# February 4th
>**Decision** When a user is created, it will be added to both `allUsers`
> as well as `loggedInUsers`. As a consequence of this decision, create
> `allUsers` Set for the server.

## Design of Client and Server protocols

Today, we discussed the specifics of the Client and Server protocols. Our goal was to create a design that is
both modular and easily testable. We also kept in consideration the fact that we will be altering the application we 
build using gRPC for Part 2, which further necessitates that we create a modular design.

### Client

In the `while(true)` loop of the Client, we choose to include the following.
1. Ask for choice of method
2. Get arguments for method
3. Create an IF statement for each method (from 1 to 5). Inside each IF statement:

	1. ethod]Request req = gen[Method]Request(String arguments...)`
	2. `String rawRequest = encodeRequest(req.getGenericRequest());`
	3. `sendToServer(rawRequest());`
	4. `String response = getFromServer();`
	5. `[Method]Response response = [Method]Response.parseResponse(Response.fromString(response));`
	6. Take some action based on the server's response
		(e.g. adding messages to hashmap, telling user there was a failure)


### Server
In the `while(true)` loop of the Server, we choose to include the following.

1. Read input as a string
2. Convert string input into a `Request`
3. Depending on first field of `Request`, enter appropriate IF statement
4. Create an IF statement for each method (from 1 to 5). Inside each IF statement:

    1. `request = [method]Request.parseGenericRequest(request);`
    2. `[Method]Response = [method](request)`
    3. `String rawResponse = encResponse([Method]Response.getGenericResponse());`
    4. `sendToClient(rawResponse)`

## Request and response objects

> **Decision** We have also made the decision to create interfaces and implementation classes that allow both the Client
> and Server to encode and decode the Request and Response objects.

For example, for the Client to send a request to the Server, it first will want to encode its request as a Request 
object, then convert it to a String to be sent across the network to the Server. The Server will then encode this String
back into a Request object. This ensures that the Server can properly determine the components of the message being 
sent. The same concept applies for the Response object.

We have created the interfaces `MethodRequestInterface` and `MethodResponseInterface`. Implementation classes will be 
created for each of these interfaces corresponding to each of the five method options.

We have also made changes to the Request and Response objects initially described in the February 3rd section.

### Request objects

We have made the design decision to modify the Request object to remove the `(4)int first argument length` component. 
The reason is that the UTF methods handle reading the length of the String. When one writes a UTF (`writeUTF`), Java 
encodes the length of the String being sent in the first two bytes, then sends the rest of the string. When one reads a
UTF (`readsUTF`), the length is first read and then the rest of the String is read. Therefore, we can remove the 
argument length component of Request:

```
Request {
    (4)int method,
    (variable)string first argument,
    ...
}
```

The `MethodRequestInterface` and its implementation classes will contain a method `genGenericRequest` which takes in a
String and returns a Request object.

### Response objects

The same reasoning as for the Request object led us to modify the Response object to the following:

```
Response {
	(4)int success,
	If success = 0
		(variable)string error message
	Else
		(variable)string response
}
``` 

The `MethodResponseInterface` and its implementation classes will contain a method `genGenericResponse` which takes in a
String and returns a Request object.

## Storing messages on the Client side

> **Decision** On February 3rd, we made the decision to keep a data structure on the Client side called `Map<String, 
> Array<Message>> receivedMessages`. We are modifying this decision. Instead, once a message has been delivered to the 
> Client from the Server, the Server deletes it and the Client does not store it. This is for simplicity of design and 
> falls within the requirements of the project. Due to the modularity of our design, it would be possible to add in a 
> data structure to store received messages later on, if we choose to.

# February 5th

> **Decision** The non-network components of the Server class have been refactored out to a separate class
> `serverCore` to allow for more modular design and easier testing of server functionality.

# February 11
> **Decision** We've integrated the Mockito and JUnit testing frameworks using Maven to help with unit testing.

While implementing the API, we realized that when reading the response from the server, we'll need to support receiving
multiple responses, e.g. a list of strings which encode a list of messages. Similarly, we should encode the number of
arguments to be sent to the server rather than just sending some number of arguments.

This is important because the connection between the server and client is *persistent*; therefore, there's no way for
server to know how many arguments it'll be receiving without being told.

> **Decision** Add to the wire protocol support for receiving a variable number of responses and requests, by first
> sending the number of strings to be sent, followed by a corresponding number of strings.

> **Decision** Add a helper class `Connection` which holds the DataInputStream and DataOutputstream.
> This should make testing using Mockito easier.

## February 14th
We've realized that the username needs to be set for many of the methods to work, on the client side.
As a result, we have added the `username` field to many of the requests, and the client will only
allow methods other than `CREATE_USER` to be called if `username` is set.

We also realized we need to treat messages and API calls separately; we don't want the client to
mistake a message as a response to a request. We considered several options; one of which is having
the wire protocol start with an int indicating whether it is a message or a response. But this 
seemed complicated to design, so we went with the following approach.
> **Decision** The client and server will maintain a message-specific thread. The client, once the
> user has logged in, will launch a MessageReceiver thread, listening for incoming messages. The
> server, also when a user has logged in, will launch a MessageDispatcher thread, which checks 
> queued messages for those addressed to that specific user, then sends it.

### Login and Logout

We have implemented log-in and log-out methods and objects. On the client side, this helps in keeping track of the username/account that is logged in, and identify when a user is logged in on the client side or not. On the server side, the status of being logged in or not is tracked for each user. 

More specifically, we have added `LoginRequest`, `LogoutRequest`, `LoginResponse`, `LogoutResponse` objects. These simply extend the `SingleArgumentRequestWithUsername` (for the requests) and `StatusMessageResponse` objects  (for the responses).

We have also updated the `ClientCore` object to include a `loginAPI` and `logoutAPI` method. These will help the Client keep track of if a user logged in or out successfully, and will update the username that the Client stores accordingly. If a user is logged in, the Client stores their username. Else the username the Client stores is set to ``null``.

The following are the `loginAPI` and `logoutAPI` methods:

```
public Boolean loginAPI(LoginRequest request, StatusMessageResponse response) {
        Boolean success = response.isSuccessful();
        if (success) {
            this.username = request.getUsername();
        } else {
            Logging.logService("Failed to log in.");
        }
        return success;
    }
```

``` 
public Boolean logoutAPI(StatusMessageResponse response) {
        Boolean success = response.isSuccessful();
        if(success) {
            this.username = null;
        } else {
            Logging.logService("Failed to log out.");
        }
        return success;
    }
```

We have made the decision to only allow certain actions if the user is logged in. Namely, the user must either create an
account or log into an existing account to call any of the other methods. From our perspective, implementing the log-in 
and log-out functionality makes logical and practical sense when considering the methods that we implement. With log-in 
and log-out, sending messages to another account is always done from a logged-in user. Also, delivering undelivered 
messages to a particular user was interpreted by us to mean that undelivered messages can be delivered to a user who has
logged in. Deleting an account should also be done by someone who is logged in.

## February 15th
We've begun the conversion into GRPC. We've created a separate branch in our git repo and are building
a separate `ServerGRPC` and `ClientGRPC` class to try to keep things distinct. One immediate change
we're making is using a single StatusReply object for each of the API calls which only return a simple
response.

Additionally, we've made a tentative decision to have a separate service in which the client acts as a 
server so that it can receive messages sent from the client.
> **Decision** Implement a new `MessageReceiver` service through which the server can call 
> `SendMessage` to send a message to a client.

### Unique Identifiers
In order to keep track of sessions, we're adding identifiers to each of the requests made by the client.
Currently, the plan is for the server to issue an identifier to each client, then the client should present
this identifier at each API call.

These identifiers will be issued on `CreateAccount` and `Login`.

### Initial Thoughts on gRPC
Overall, the complexity to port over to GRPC is fairly small; after we figured out how to set up gRPC with 
Gradle and Java, it was not too difficult to convert the core API methods in `ServerCore` and `ClientCore` over
to their gRPC counterparts.

For example, rather than using our own `SendMessageRequest` object, we can now use the one generated by gRPC. Thus,
we merely had to change the signature for most of our methods without too much other modification necesary.

One point of difference is the secondary service required to receive messages on the client. However, this was quite
easy to set up. The only part which was not completely straightfoward was creating the client on the Message Server.
A new client needed to be created for each logged-in user; as such, whenever a login request or create account request
is received, a new thread is started which acts as a message dispatcher for that specific user.

## February 16th

> **Bug** Address Already in Use Exception
> This occurs on the client side when I attempt to connect a second client to the server, with both
> clients on localhost.

This bug occurs when two clients on the *same* IP address start the MessageReceiver. Since the port
was fixed, they can't both listen on the same port. I fixed this bug by maintain a list of connected 
IP addresses, and incrementing the port number for each connection on the same IP address. The server
maintains this list, and the port the client should use is passed back as part of the `LoginReply`

> **Bug** Starting a new MessageReceiver after deleting account
> There is a bug where if a client deletes their account (and thereby not exiting the client entirely),
> the old MessageReceiver has not been shut down, and a new one causes the `Address Already in Use`
> exception.

This was fixed by holding onto a handle of the server, and calling shutdown on deleting or logging out.

> **Enhancement** Messages addressed to non-existent users should fail
> Previously, you are able to send messages to non-existent users. This has been fixed.

> **Bug** We encountered an issue where we saw an error `HTTP/2 client preface string missing or corrupt.`.

From searching on Google, this seems to be often caused by an issue where one side of the connection is
using TLS and the other is just plain text. However, this was not fixed by setting `.usePlainText()`
on the connection side. The way we were initializing our server seemed exactly the same as the example on
the gRPC website which was confusing.

In the end, I was able to fix it by using a slightly different constructor for the server, which uses a
`ServerBuilder` not the `Grpc.newServerBuilderForPort`.

## February 18th: Reflections on using gRPC

We are now wrapping up our implementation of the design exercise with gRPC, and are ready to compare the complexity of the code (our first implementation vs gRPC implementation), any performance differences, and the size of the buffers being sent back and forth between the client and the server. 

### The size of the buffers being sent back and forth between the client and the server

The size of the buffers being sent back and forth between the client and the server was rather similar between the two implementations (previous implementation vs gRPC). This was because the way we originally implemented the code shared several similarities with the way that gRPC automatically sets up request and reply objects.

When we implemented the code without gRPC, we set up request and response objects for each of the 5 methods we had to implement, plus login and logout request and response objects. When gRPC was used, gRPC automatically generated request and reply objects, which we equipped with the same type of functionality as in our original implementation. The number of request and reply objects, their methods, and their stored information were all very comparable. 

For this reason, we believe that the size of the buffers will end up being similar between the two implementations. Besides comparing the size of the objects being converted and sent across the network, it is tough to compare the size of the buffers more explicitly as gRPC keeps the this information (about the size of the buffer being sent and the details of how sending across the network works) rather hidden. Therefore, we are considering the size of the objects being sent as a proxy for the size of the buffers being sent back and forth between the client and the server.

### The complexity of the code

Using our reasoning about the size of the buffers above, we now speculate waht this means for the compleixty of the code. Since the size of the buffers is rather comparable, the code (without gRPC and with gRPC) will have a similar complexity ultimately.

However, it was a lot easier to set up the request and response/reply objects with gRPC than without it. In Part 1 (the implementation without gRPC) we had to manually define request and response objects for each of the 5 methods and for logging in and logging out. This was a rather repetitive task that took time. However, with gRPC, it took less time to set up these objects and gRPC created them for us and created appropriate methods, once we specified the structure of these objects we wanted in ``messageService.proto`` for gRPC.

### Performance differences

- similar performance overall when run. Same messages sent back and forth, same behavior.