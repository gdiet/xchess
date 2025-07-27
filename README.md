# xchess

A fast paced bad ass chess game.

## Rules (implementation view)

- The board is a rectangle of square fields, usually 8x8, but can be larger, in which case initially there are more of the standard pieces on the board
- There are two players, white and black
- Players can issue commands at any time
- At turn end, all commands issued so far are evaluated sequentially
  - alternating between the players, on odd turns white first, on even turns black first
  - by player in the order they were received
- Movement of queen (q), bishop (b) and knight (n) are standard chess
- Movement of king (k) and rook (r) are standard chess except that there is no castling and that king _can_ move into check
- Movement of pawn (p) is standard chess except there is no _en passant_ capturing, and it is always promoted to queen

There are two main play modes:
- Turn based
  - turns end either after a specific duration (e.g. one second or 60 seconds), but players can issue "pause" and "continue" commands
  - or when both players signal they have finished the turn 
- Continually (which can be implemented "turn based with turn duration 0")
  - whenever the server receives a command or event, it is immediately evaluated
  - players can issue "pause" and "continue" commands

## Websocket Communication

The game uses WebSocket for real-time communication between clients and the server. When the connection is created, the server sends the current game state to the client. Later, the server sends updates to the game state as they occur. Clients send commands to the server. No acknowledgement is sent for commands.

### Client To Server

```
chat [message]         chat hello all!
player [white|black]   player white
move [from] [to]       move a4 a6
pause                  pause
resume                 resume
```

### The Chat Function

The chat function allows players to communicate with each other in real-time during the game. It implements the following protocol:

When the connection is created, the server sends the current chat history, limited to a small number of entries, to the client:

`chat: ["message1", "message2", ...]`

Client messages:

- `{ topic: "chat", data: "[message]" }` - send a new chat message.

Server messages broadcast to all clients:

- `{ topic: "chat", data: "[message]" }` - a chat message was received from a client.

## REST API

`POST /api/games`

Body:
```json
{
   boardLayout: "standard",
   freezeTimeMillis: 10000
}
```

Response:
```json
{
   gameId: "002572"
}
```

## Development Setup

### Prerequisites

- Go 1.24.5 or later
- Git

### Setting up Live Reloading with Air

1. **Install Air** (Go live reload tool):

   ```bash
   go install github.com/air-verse/air@latest
   ```

2. **Add Go bin to your PATH** (optional but recommended):

   ```bash
   echo 'export PATH=$PATH:$(go env GOPATH)/bin' >> ~/.profile
   source ~/.profile
   ```

3. **Run the application with live reloading**:

   ```bash
   air
   ```

4. **Access the application**:
   - Open your browser and go to `http://localhost:8080`
   - The server will automatically restart when you make changes to any `.go` files

## How Air Works

Air watches your Go files for changes and automatically:

- Rebuilds your application
- Restarts the server
- Preserves your terminal output

This means you can edit your Go code and see changes immediately without manually stopping and restarting the server.

### Manual Build & Run

If you prefer not to use air:

```bash
go run .
```
