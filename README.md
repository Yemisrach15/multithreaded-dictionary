# Multi-Threaded Dictionary Server

This is a project for the course distributed system programming. It demonstrates the use of sockets and threads. 

## To run
1. Open a terminal and navigate to the root directory.
2. Execute `java -jar DictionaryServer.jar {port} {dictionary file}`. 
    - Eg. `java -jar DictionaryServer.jar 4444 dictionary.json`
3. Open another terminal and navigate to the root directory.
4. Execute `java -jar DictionaryClient.jar {server address} {server port}`. 
    - Eg. `java -jar DictionaryClient.jar localhost 4444`.


> The port number at step 2 and the server port at step 4 must be the same.
