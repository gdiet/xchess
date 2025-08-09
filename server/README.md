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

Initial messages:

    freeze: 3000            // milliseconds
    clock: 0 stopped        // milliseconds, or running
    cols: 8                 // board size
    rows: 8                 // board size
    board: A1 R 3000        // square, piece, freeze time
    chat: Hello chess       // chat message
