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
		for request := range gamesChan {
			request.Execute(games)
		}
	}()
	return gamesChan
}

type GamesRequest interface {
	Execute(games map[string]chan GameRequest)
}

type CreateGameRequest struct {
	BoardLayout            string
	FreezeTimeMilliseconds int
	ResponseChan           chan CreateGameResponse
}

func (req CreateGameRequest) Execute(games map[string]chan GameRequest) {
	if len(games) >= 10 {
		req.ResponseChan <- CreateGameResponse{Error: fmt.Errorf("Too many games")}
		return
	}
	var gameID string
	for {
		gameID = fmt.Sprintf("%06d", rand.Intn(1000000))
		if _, exists := games[gameID]; !exists {
			break
		}
	}
	games[gameID] = make(chan GameRequest)
	req.ResponseChan <- CreateGameResponse{GameID: gameID}
}

type LookupGameRequest struct {
	GameID      string
	ResponseChan chan LookupGameResponse
}

func (req LookupGameRequest) Execute(games map[string]chan GameRequest) {
	if gameChan, exists := games[req.GameID]; exists {
		req.ResponseChan <- LookupGameResponse{GameChan: gameChan}
	} else {
		req.ResponseChan <- LookupGameResponse{Error: fmt.Errorf("game %s not found", req.GameID)}
	}
}

type CreateGameResponse struct {
	GameID string
	Error  error // too many games
}

type LookupGameResponse struct {
	GameChan chan GameRequest
	Error    error // game not found
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
