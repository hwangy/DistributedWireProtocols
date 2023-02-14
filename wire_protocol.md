# Wire Protocol

Requests are always sent from the client to the server, and responses
or messages. For the responses and messages, since they travel in the
same direction, the *first* integer of the response / message will
identify whether this is a response or message. 0 corresponds to an API
request, and 1 corresponds to a message.

## Request
The request specifies the method the client is calling, as well as any
arguments required by the method.
```
Request {
    (4)int method,
    (4)int number of arguments,
    [optional] UTF first argument:
        (2)int first argument length,
        (variable)string first argument,
    [optional] UTF second argument
    ...
}
```
Each server method is associated with a fixed number of arguments and
the client should provide an integer and string for each argument,
specifying the length of the argument, as well as the argument itself.
In our implementation, Java provides a UTF data structure which 
encodes the length of the underlying string using 2 bytes, followed by 
the string itself. This eliminates the need to explicitly include the
length parameter in the protocol.

## Response
```
Response {
    // First field is always 1 for Responses
    (4)int 1,
	(4)int success,
	(4)int number of responses,
	If success = 0
	    UTF error message:
	        (2)int length of error message,
	        (variable)string error message
	Else
	    [optional] UTF first response,
	    [optional] UTF second response,
	    ...
}
```

```
Message {
    // First field is always 0 for Messages
    (4)int 0
    UTF sent timestamp,
    UTF sender,
    UTF message
}
```
