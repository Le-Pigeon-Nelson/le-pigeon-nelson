# Technical notes

The application will be developed using Java for Android, and a demo server will be provided with very basic functionalities.

## Geolocation

The application needs to identify information on the user's movement: [location and speed](https://developer.android.com/reference/android/location/LocationProvider) (see for example [this article](https://openclassrooms.com/fr/courses/2023346-creez-des-applications-pour-android/2028397-la-localisation-et-les-cartes)), [compas](https://github.com/iutinvg/compass) (see a [description of geolocation an compass](https://www.mobileprocessing.org/geolocation.html)).

To improve the original measures that are not very robust, we can combine sensors, GPS and network:

* [FSensor](https://github.com/KalebKE/FSensor)
* [mad-location-manager](https://github.com/maddevsio/mad-location-manager)

## REST client

The application regularly queries a server to obtain messages to broadcast, using a REST API.

The query contains:

* information on the location and mobility of the phone
* parameters on current usage (application related)
* a description of the request (explicit from the user, or requested by the recurrent mechanism)


## Message description

the server response (`json` or `xml` format) contains a list of messages corresponding to the information provided by the application.

These responses contain a series of information and meta-information:

the server response contains a list of messages corresponding to the information provided by the application. These responses contain a series of information and meta-information:

* conditions for the message to be broadcast (maximum distance to a reference point, maximum angle to a reference orientation, etc)
* conditions for the message to be forgotten (time or location constraint)
* a text to be pronounced with the vocal synthesis, or the link to an mp3 to be played (in streaming, with possibly a duration if it is a stream)
* a broadcast priority to decide if a message can stop a currently broadcasted message (higher priority), has to be queued (same priority), or ignored (smaller priority).

## Playing sound

If the sound comes from a file provided by the server, we can use [MediaPlayer](https://developer.android.com/guide/topics/media/mediaplayer). 

If the text has to be transformed into an audio version, we can use [TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech) API.

See for example the [AudioRenderer](https://github.com/jmtrivial/pictoparle/blob/master/app/src/main/java/com/jmfavreau/pictoparle/interactions/AudioRenderer.java) from Pictoparle that contains both audio renderings in a single class.

## Application performance

A thread will be dedicated to the message production, another one will be dedicated to the cmmunication with the server, and a last one to handle message queue. The communication between these two threads and the UI thread will be implemented using [loops, messages, and threadhandlers](https://blog.mindorks.com/android-core-looper-handler-and-handlerthread-bd54d69fe91a).

## Reusability

Le pigeon Nelson will be implemented as a package to be reusable on other projects. The basic application will be a a demonstration of the possibilities of the tool.

