# Xchess Server Protocol

## POST /games

` curl http://localhost:8080/games -X POST -d '{"name":"hallo","gameType":"chess","initialFreeze":60,"freeze":5}' -H "Content-Type: application/json"`

## Websocket /ws/{game name}

All WS messages are JSON text messages.

### Server To Client

1) When the WS is established, a single message is sent:

`x: 8, y: 8`

2) The initial board contents are sent as a number of `add` and possibly `plan` messages, possibly with one `winner` message.

3) The game progress is sent like this:

* `add { id: 1, x: 1, y: 1, color: white, piece: Rook, freeze: 100 }`
* `move { id: 1, x: 3, y: 1, freeze: 100 }`
* `remove { id: 1 }`
* `plan { id: 1, from: {x:1,y:1}, to: {x:3,y:1}, color: white }`
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
