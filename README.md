*Roumpoutsos Nikolaos nikosr@kth.se
*Sapountzis Ioannis ioanniss@kth.se

README file
The MailServer is an application designed to serve HTTP requests from users and provide users
also a form to send their emails and an administrative status page for their messages.

Delivered files:
----------------
README.txt - Instructions how to compile and run the server

Roumpoutsos-Sapountzis.pdf - Project report

Java files:
----------------
-package Mailserver

--Server.java :
Class implementing the main method, creating one thread for every client.

--mail.java :
class mail, representing a mail class and methods to get and set mail properties and also check the validity of the mail

--Handler.java
Class extending thread to serve client's request (GET,POST)

--FutureSend.java
implementig methods for future send and notification to sender

--MailHandler.java
Class including the Message List containing all the future mails that have to be delivered,
implementing also methods to send an email instantly, handle the message list and print the message list to html

-package Mailutils

--CharEncode.java
Class implementing the method to encode characters to any encode Map ("UTF-8" is defined in this case) by Quoted-Printable method

--MXSearch.java
Class implementing the method to search for MX records of a domain


Instructions

The project was developped in NetBeans 7.0.1 and it was tested on Ubuntu 10.04

– How to compile
ant jar
– How to run 
java -jar dist/Mailserver.jar
or 
ant jar dist/Mailserver.jar
(assuming that working directory is the same with the project folder)
