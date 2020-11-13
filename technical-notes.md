# Technical notes

The application is developed using Java for Android, and demo servers are provided with very basic functionalities.


## Geolocation and motion sensors

The application needs to identify information on the user's movements. To improve the original measures that are not very robust, we combine sensors, GPS and network.


* [FSensor](https://github.com/KalebKE/FSensor), Sensor Filter and Fusion
* [Lost from Mapzen](https://github.com/lostzen/lost), a drop-in replacement for Google Play services location APIs for Android


## REST communication between client and server

The application regularly queries a server to obtain messages to broadcast, using a REST API. See [protocol details](protocol-details.md) for a detailed description.

## Application performance

A thread is be dedicated to the message production, another one will be dedicated to the communication with the server, as well as one to handle message queue. The communication between these two threads and the UI thread is implemented using [loops, messages, and threadhandlers](https://blog.mindorks.com/android-core-looper-handler-and-handlerthread-bd54d69fe91a).

In a near future, the application will be split into an application for user interface and a service for all the other processings.

## Reusability

Le pigeon Nelson is implemented as a package to be reusable on other projects. 

