package games

/*
we need a function returning a channel for the games commands, which handles the commands to update the games registry (create & lookup). this channel is instantiated as singleton in the main.go file.
*/

func GamesRegistry() chan GamesRequest {
	gamesChannel := make(chan GamesRequest)
	// go func() {
	// 	games := make(map[string]PostGamesBody)

	// 	for req := range gamesChannel {
	// 		switch req.Command {
	// 		case Create:
	// 			gameID := fmt.Sprintf("%06d", rand.Intn(1000000))
	// 			games[gameID] = req.Body
	// 			req.ResponseChan <- GamesResponse{Result: gameID, Error: nil}
	// 		case Lookup:
	// 			if body, exists := games[req.GameID]; exists {
	// 				req.ResponseChan <- GamesResponse{Result: body, Error: nil}
	// 			} else {
	// 				req.ResponseChan <- GamesResponse{Error: fmt.Errorf("game not found")}
	// 			}
	// 		}
	// 	}
	// }
	return gamesChannel
}

type GamesCommand string

const (
	Create GamesCommand = "create"
	Lookup GamesCommand = "lookup"
)

type GamesRequest struct {
	Command    		  GamesCommand
	ResponseChan chan GamesResponse
}

type GamesResponse struct {
	Result string
	Error  error
}

type GameCommand string

const (
	Chat    GameCommand = "chat"
	Move    GameCommand = "move"
	Pause   GameCommand = "pause"
	Resume  GameCommand = "resume"
)

type GameRequest struct {
	Command GameCommand
}
