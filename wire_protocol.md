# Wire Protocol
## Request
The request specifies the method the client is calling, as well as any
arguments required by the method.
```
Request {
    (4)int method,
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
	(4)int success,
	If success = 0
	    UTF error message:
	        (2)int length of error message,
	        (variable)string error message
	Else
	    UTF response
}
```
