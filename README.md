# Distributed Shared Whiteboard

The aim of the problem is to implement a shared whiteboard that allows multiple users to concurrently draw on canvas over a network, with a wide range of functionalities like freehand, shapes and text. Group chat services and Menu options are the other two important features of the shared whiteboard apart from the Graphical User Interface. Through the chat service, active users will be able to interact with other users with messages. Menu options include ‘New’, ‘Open’, ‘Save’, and ‘Save as’ and ‘Close’.

## Overview

The DistributedWhiteBoard folder contains the Client implementation while the Server folder contains the Server implementation of the System. The communication between these two divisions is a multithreaded TCP connection.

### Graphical User Interface:
Swing and JavaFX were considered as two primary options for the GUI framework. On exploring the frameworks, JavaFX was chosen as the primary framework as it has rich GUI options, libraries and eminence documentation available to support our development. However, all the requests, inputs and notification pop up dialog boxes are implemented in Java Swing.

### Network Technology
Sockets have exclusively been used over TCP to perform communication between the Server and respective client(s) in the project.

## Implementation

The project is a JavaFX Project with Model View Controller (MVC) architecture. The project also integrates certain aspects of Java Swing. The crux of the project GUI is JavaFX and all canvas operations are handled by the controller class in the application. All the requests, inputs and notification pop up dialog boxes are implemented in Java Swing. To connect all the remote components of the system, we use sockets which establishes a TCP connection. The secured connection between the clients and the server handles the chat and canvas broadcasting. The connection architecture between each user and the server uses a thread per request model. In this architecture, for every connection request to the server, a thread is allocated to the client which serves all the requests of the client. At the client application end, we use multi-threading by implementing two threads. One thread serves the canvas of the application while the other thread serves chat side.
