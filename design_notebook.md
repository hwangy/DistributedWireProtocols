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

Today, we discussed the specifics of the Client and Server protocols. Our goal was to create a design that is both modular and easily testable. We also kept in consideration the fact that we will be altering the application we build using gRPC for Part 2, which further necessitates that we create a modular design.

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

> **Decision** We have also made the decision to create interfaces and implementation classes that allow both the Client and Server to encode and decode the Request and Response objects.

For example, for the Client to send a request to the Server, it first will want to encode its request as a Request object, then convert it to a String to be sent across the network to the Server. The Server will then encode this String back into a Request object. This ensures that the Server can properly determine the components of the message being sent. The same concept applies for the Response object.

We have created the interfaces `MethodRequestInterface` and `MethodResponseInterface`. Implementation classes will be created for each of these interfaces corresponding to each of the five method options.

We have also made changes to the Request and Response objects initially described in the February 3rd section.

### Request objects

We have made the design decision to modify the Request object to remove the `(4)int first argument length` component. The reason is that the UTF methods handle reading the length of the String. When one writes a UTF (`writeUTF`), Java encodes the length of the String being sent in the first two bytes, then sends the rest of the string. When one reads a UTF (`readsUTF`), the length is first read and then the rest of the String is read. Therefore, we can remove the argument length component of Request:

```
Request {
    (4)int method,
    (variable)string first argument,
    ...
}
```

The `MethodRequestInterface` and its implementation classes will contain a method `genGenericRequest` which takes in a String and returns a Request object.

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

The `MethodResponseInterface` and its implementation classes will contain a method `genGenericResponse` which takes in a String and returns a Request object.

## Storing messages on the Client side

> **Decision** On February 3rd, we made the decision to keep a data structure on the Client side called `Map<String, Array<Message>> receivedMessages`. We are modifying this decision. Instead, once a message has been delivered to the Client from the Server, the Server deletes it and the Client does not store it. This is for simplicity of design and falls within the requirements of the project. Due to the modularity of our design, it would be possible to add in a data structure to store received messages later on, if we choose to.