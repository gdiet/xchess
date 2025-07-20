# xchess

A fast paced bad ass chess game.

## Websocket Communication

The game uses WebSocket for real-time communication between clients and the server. Messages are sent as JSON objects. When the connection is created, the server sends the current game state to the client. Later, the server sends updates to the game state as they occur. Clients send commands to the server. No acknowledgement is sent for commands.

### The Chat Function

The chat function allows players to communicate with each other in real-time during the game. It implements the following protocol:

When the connection is created, the server sends the current chat history, limited to a small number of entries, to the client:

`chat: ["message1", "message2", ...]`

Client messages:

- `{ topic: "chat", data: "[message]" }` - send a new chat message.

Server messages broadcast to all clients:

- `{ topic: "chat", data: "[message]" }` - a chat message was received from a client.

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
