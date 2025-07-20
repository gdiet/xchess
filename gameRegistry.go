package main

import (
	"fmt"
	"math/rand"
)

type Game struct {
	ID               string
	BoardLayout      string
	FreezeTimeMillis int
}

type CreateGameRequest struct {
	BoardLayout      string
	FreezeTimeMillis int
	ResponseChan     chan CreateGameResponse
}

type CreateGameResponse struct {
	GameID string
	Error  error
}

type GameCommand struct {
	Create *CreateGameRequest
	// Add other commands later (e.g., Get, Delete)
}

func GameRegistry(commandChan <-chan GameCommand) {
	games := make(map[string]*Game)

	for cmd := range commandChan {
		if cmd.Create != nil {
			id := fmt.Sprintf("%06d", rand.Intn(1_000_000))
			game := &Game{
				ID:               id,
				BoardLayout:      cmd.Create.BoardLayout,
				FreezeTimeMillis: cmd.Create.FreezeTimeMillis,
			}
			games[id] = game
			cmd.Create.ResponseChan <- CreateGameResponse{GameID: id}
		}
	}
}
