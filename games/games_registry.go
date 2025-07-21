package games

import (
	"fmt"
	"math/rand"
)

/*
we need a function returning a channel for the games commands, which handles the commands to update the games registry (create & lookup). this channel is instantiated as singleton in the main.go file.
*/

func GamesRegistry() chan GamesRequest {
	gamesChan := make(chan GamesRequest)
	games := make(map[string]chan GameRequest)

	go func() {
		for req := range gamesChan {
			switch req.Command {

			case Create:
				var gameID string
				for {
					gameID = fmt.Sprintf("%06d", rand.Intn(1000000))
					if _, exists := games[gameID]; !exists {
						break
					}
				}
				games[gameID] = make(chan GameRequest)
				req.ResponseChan <- GamesResponse{GameID: gameID}

			case Lookup:
				if gameChan, exists := games[req.GameID]; exists {
					req.ResponseChan <- GamesResponse{GameID: req.GameID, GameChan: gameChan}
				} else {
					req.ResponseChan <- GamesResponse{Error: fmt.Errorf("game not found")}
				}
			}
		}
	}()
	return gamesChan
}

type GamesCommand string

const (
	Create GamesCommand = "create"
	Lookup GamesCommand = "lookup"
)

type GamesRequest struct {
	Command                GamesCommand
	BoardLayout            string // only for create command
	FreezeTimeMilliseconds int    // only for create command
	GameID                 string // only for lookup command
	ResponseChan           chan GamesResponse
}

type GamesResponse struct { // FIXME split into Create and Lookup response
	GameID   string           // only for create command
	GameChan chan GameRequest // only for lookup command
	Error    error
}

type GameCommand string

const (
	Subscribe GameCommand = "subscribe"
	Chat      GameCommand = "chat"
	Plan      GameCommand = "plan"
	Pause     GameCommand = "pause"
	Resume    GameCommand = "resume"
)

type GameRequest struct {
	Command  GameCommand
	BackChan chan GameUpdate
}

type GameTopic string
const (
	ChatTopic   GameTopic = "chat"
	PlanTopic   GameTopic = "plan"
	MoveTopic   GameTopic = "move"
	PauseTopic  GameTopic = "pause"
	ResumeTopic GameTopic = "resume"
)

type GameUpdate struct {
	Topic GameTopic
	Data  interface{}
}
