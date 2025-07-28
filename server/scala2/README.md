# Xchess Server Protocol

## POST /games

` curl http://localhost:8080/games -X POST -d '{"name":"hallo","gameType":"chess","initialFreeze":60,"freeze":5}' -H "Content-Type: application/json"`

## Websocket /ws/{game name}

All WS messages are JSON text messages.

### Server To Client

When the WS is established, a single message is sent defining the board size:

`{"x":8,"y":8}`

The server sends the initial board contents as a number of `add` and possibly `plan` messages, possibly with one `winner` message. For game progress the server additionally uses `move`, `remove` and `unplan` messages. To avoid websockets closing due to inactivity, the server periodically sends `keepalive` messages.

`id` in messages refer to piece IDs that are defined when a piece is added to the board. `freeze` is the remaining freeze time in milliseconds.

Message examples:

    {"id":1,"x":4,"y":6,"color":"black","piece":"Rook","freeze":29040,"cmd":"add"}`
    {"id":1,"color":"black","from":{"x":0,"y":6},"to":{"x":0,"y":4},"cmd":"plan"}
    {"winner":"black"} // TODO not yet implemented
    {"cmd":"keepalive"}

* `move { id: 1, x: 3, y: 1, freeze: 100 }`
* `remove { id: 1 }`
* `unplan { id: 1, moved: true }`
* `winner { winner: black }`
* `keepalive`

### Client To Server

The client only sends messages of the type `move { id: 1, x: 3, y: 1 }`. These messages can be

* valid move commands,
* valid plan commands (the piece is currently frozen),
* valid unplan commands when moving a piece to its current position, 
* or invalid commands.

Valid commands trigger at lease one server message. Invalid commands are silently ignored.
