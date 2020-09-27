<img src="./images/le-pigeon-nelson-logo.svg" width="200px" height="200px" alt="Logo du pigeon Nelson" align="right">

# Le pigeon Nelson : contextual sound broadcasting application

The aim of *le pigeon Nelson* is to provide a sound broadcasting tool according to your location and movement.

<div style="width: 100%; clear: both">
<a href="./images/Screenshot1.png"><img src="./images/Screenshot1.png" width="150px" alt="A list of servers to be played" align="left" margin="1em"></a>
<a href="./images/Screenshot2.png"><img src="./images/Screenshot2.png" width="150px" alt="A playing server" align="left" margin="1em"></a>
<a href="./images/Screenshot3.png"><img src="./images/Screenshot3.png" width="150px" alt="Add a new server (simple)" align="left" margin="1em"></a>
<a href="./images/Screenshot4.png"><img src="./images/Screenshot4.png" width="150px" alt="Add a new server (with details)" align="left" margin="1em"></a>
<a href="./images/Screenshot5.png"><img src="./images/Screenshot5.png" width="150px" alt="Add a new server (with refresh delay)" align="left" margin="1em"></a>

</div>
<div style="clear: both"></div>

## How it works

The application is configurable to choose the reference server from which it will collect the messages to be broadcast, as well as fine-tuning its behavior.

When it is active, the application collects at regular intervals the location, orientation and movement of the user, then consults a reference server with this information (possibly augmented with some information provided by the interface). 

The server then returns a set of broadcastable messages, each of them provided with an associated broadcast guard. These messages are kept by the application, which regularly consults all the broadcastable messages. When it finds that it is in a state that allows the message to be broadcast (location, orientation, expected speed), it broadcasts the message.

This message can be the url of an audio file to be played, or a text that will be said by the application's voice synthesis. 
The application can be set so that the messages can be cut, superimposed, or wait for other messages to be played before being broadcast. Messages can also contain these broadcast constraints.

Servers can be self described, containing their own information (name, description, refresh frequency, etc.).


See [technical notes](technical-notes.md) if you're interested in technical details.

## Examples of use

* Propose an audio-recorded tour of a neighborhood. The user is free to move around, and messages are triggered according to the constraints defined by the designers of the experience, who will have described this information on the server consulted.
* Offer on-demand information on the services available in the area (e.g. [museums](https://github.com/jmtrivial/le-pigeon-nelson/blob/master/servers/museums.php))
* Play selected music according to the location of the listener

## License

This project is [provided under GPL v3](./LICENSE).

## Authors

The initial idea has been discussed on August 2020 between Samuel Braikeh and Jean-Marie Favreau

## About the name of the project

Imagine for a moment a pigeon singing *On the rrrrrroad again* (Willie Nelson). That's it.
