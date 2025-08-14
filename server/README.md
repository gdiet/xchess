# xChess technical documentation

## REST API

    POST /api/games
    {
      "id": "game_id"   // optional, if not provided a random id is generated
    }
    => 201 Created  => { "id": "game_id" }
    => 409 Conflict => ID conflict or too many games

## Websocket communication

    ws://[server:port]/ws/[game_id]
    => Websocket connection
    => 404 Not Found => Game not found

Messages on connection:

    freeze: 3000            // milliseconds
    clock: 0 stopped        // milliseconds, or running
    cols: 8                 // board size
    rows: 8                 // board size

    board: A1 R 3000        // square, piece, freeze time
    plan: A2 A4             // from, to
    chat: Hello chess       // chat message
    winner: white           // game winner, when the first king has already been captured

Server to client:

    freeze: 3000            // milliseconds
    clock: 0 stopped        // milliseconds, or running
    plan: A2 A4             // from, to
    move: A2 A4             // from, to
    chat: Hello chess       // chat message
    advance: 3000           // game time in milliseconds
    winner: white           // game winner, when the king is captured

Client to server:

    chat: Hello chess       // chat message
    plan: A1 B2             // plan move from A1 to B2
    stop                    // stop the game clock
    start                   // start the game clock
    advance: white          // signal that the move is finished early
