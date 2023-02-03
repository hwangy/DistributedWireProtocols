The following tracks our design decisions for the wire protocols project.

## February 3rd

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