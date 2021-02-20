# Protocol details

A standard use of the pigeon Nelson application consists in adding a new server. In the following description, we use `https://example.com/` to illustrate how it works.

## Server description

The first request from the application to the server is dedicated to the server description: `https://example.com/?self-description`.

The resulting document is a json with the following content:

```json
[ 
    { 
        "name": "...", 
        "description": "...",
        "encoding": "...",
        "defaultPeriod": "..." 
    }
]

```

*Name* and *description* will be shown in the user interface to identify the server. *Encoding* can be `utf-8` or equivalent. *default period* (in seconds) corresponds to the delay between two requests for messages. If this value is equal to `0`, the server is considered as a **single-message server**, and not a **broadcast server** (see below).

## Request for messages

Only once if the server is a single-message server, or periodically if the server is a broadcast server, a request is sent from the application with the following address: `https://example.com/?uid=...&lat=...&lng=...&azimuth=...&pitch=...&roll=...`

with the following values:

* `uid`: a (quasi) unique device ID 
* `lat` and `lng`: last known coordinates
* `azimuth`, `pitch` and `roll`: last known compass orientation wrt to the world coordinates (`azimuth=0` corresponding to a North orientation).

The resulting document is a json with a list of messages described as following:

```json
[
    {
        "txt": "...",
        "lang": "...",
        "priority": ...,
        "requiredConditions": [
            {
                "reference": "...", 
                "comparison": "...", 
                "parameter": "..." 
            },
            {
                "reference": "...", 
                "comparison": "...", 
                "parameter": "..." 
            }

        ],
        "forgettingConditions": [
            {
                "reference": "...", 
                "comparison": "...", 
                "parameter": "..." 
            },
            {
                "reference": "...", 
                "comparison": "...", 
                "parameter": "..." 
            }
        ]
        
    }, 
    
    {
        "audioURL": "...",
        "priority": ...,
        "requiredConditions": [],
        "forgettingConditions": []
    }
]
```

Each message is described by its content (`audioURL` for an audio file, or `txt` and `lang` if a synthetized voice message), a priority (used in the message queue), and two series of constraints: one used as guards for broadcasting, and the other used as condition to forget the message.

Each condition is described by 3 parameters:

* a `reference` selected in the following list: `timeFromReception`, `distanceTo(..., ...)` (with a reference lattitude and a reference longitude), `azimuthDeviation(...)` (with a reference azimuth), `pitchDeviation(...)` (with a reference pitch) or `rollDeviation(...)` (with a reference roll). 
* a `comparison` selected in the following list: '<', '<=', '=', '=>' or '>'.
* a `parameter` filled by a scalar value (example: `0.12`).

A message is considered *playable* if all the *required conditions* are satisfied. A message will be forgotten if one of the *forgetting conditions* is satisfied.

One good practice consists in adding a forgetting condition `timeFromReception>=duration` where given duration corresponds to the default period of the server, if this default period is not equal to zero.

## Message queue

All received messages are then inserted into the message queue, sorted by priority. 

At regular intervals, when location or sensor properties have significantly changed, or when the application has just finished playing a message, the message queue is processed, starting with the messages with the highest priority. If a message is playable, and if the application is not playing a message or it's playing a message with a lower priority, this message becomes the new played message (it can therefore interrupt a message being played back).



