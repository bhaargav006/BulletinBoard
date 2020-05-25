# BulletinBoard
This project was done as a part of Distributed Systems course at The University of Minnesota. 
The aim of the project was to develop a bulletin board wherein a client can post/reply/read/choose articles. 
Our implementation consists of three packages: Client, Server and Coordinator.
The behaviour of the system changes based on the consistency option chosen by the user when running the coordinator. 

##
### Client
The client side functionality is implemented in the package named, **main**.
* Client.java has the functionality for the client to connect with the Server to carry out several functionalities that include, Post (A new article), Read (List of existing articles), Choose (An article to reply to an article), Reply (To an existing article).
* ClientHelper.java has helper functions to facilitate processing of the message sent from client, the connection to the server (Sending and receiving messages), and displaying the list of articles when the client chooses to read the articles

##
### Server
The server side functionality is implemented in the **Server** package, with three core files, **Server.java, ClientResponder.java, and ServerHelper.java**.
* This package helps in connecting the Client to Server and Server to Coordinator (primary server).
* Whenever a new client comes to a server, it creates a thread for its functionality using the ClientResponder class, so that it does not block the I/O and queues further requests from either Client or Coordinator.
* **The first message received by the server is from the Coordinator, which sends the type of consistency to be used for the run of the program.**

##
### Coordinator
The coordinator is essentially a server which acts as a primary server. It is a part of the **Coordinator** package which includes, **Coordinator.java, ServerResponder.java, CoordinateHelper.java.**
* Coordinator.java is the point of entry for the primary server and it asks the user about the consistency that is to be used for the particular run of the program. Once the consistency is established by the user, it sends the type of consistency to all the servers as well and starts the ServerResponder, which has a thread for each of the servers so that it does not block the I/O. 

##
### How to run
This project is built to run in localhost with multiple ports

* Run the make command to compile java files
* Now, first we run the coordinator,using ***java Coordinator***
* Run the servers using, ***java Server < port >***
* Ports can be found in serverList.properties, where server is running

- Note: We would suggest you to start the servers in the same order as present in the serverList.properties. 
Run the clients using  java Client <port>, here the port is where the server is running and client can connect to that server.
Run the TestClient to see the functionality of the operations that can be performed by the clients. 

##
### About the consistencies
* **Sequenctial**:
1. For sequential consistency, we are using the primary-backup protocol. 
2. The updates(post/reply) are propogated to all the servers before the client moves on to the next operation. 

* **Quorum**:
1. We have to choose the number of read and write servers from a given pool of servers. 
2. It should adhere to the 2 conditions to satisfy the number of read and number of write servers, the sum of read and write servers should exceed the total number of servers and the number of write servers should be greater than the half of total number of servers.
3. The updates are propagated to Nw(Number of write server) servers. It is important to note that it is any randomly selected Nw number of servers and not the same servers again. 
4. The reads are fetched from Nr(Number of read servers) servers. Again these are randomly chosen. 

* **Read your write**:
1. The read your write consistency is implemented using the local protocol, wherein if a server writes in one of the servers and then connects to another server, it can see the changes made to the bulletin board.
2. It doesn't wait for all the servers to get upodated unlike sequetial, whereas it believes that it will eventually get updated. 

##
### Group Members
* Bhaargav Sriraman (srira048)
* Soumya Agrawal (agraw184)
* Garima Mehta (mehta250)
