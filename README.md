# xchess

A fast paced bad ass chess game.

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
   echo 'export PATH=$PATH:$(go env GOPATH)/bin' >> ~/.bashrc
   source ~/.bashrc
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
