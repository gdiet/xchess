# xChess technical documentation

## REST API

    POST /api/games
    {
      "id": "game_id"   // optional, if not provided a random id is generated
    }
    => 201 Created  => { "id": "game_id" }
    => 409 Conflict => ID conflict or too many games

## Websocket communication

`ws://[server:port]/ws/[game_id]`
