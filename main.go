package main

import (
	"log"
	"net/http"
	"xchess/restapi"
	"xchess/validation"
)


func main() {
	validation.Init()

	http.HandleFunc("/api/games", restapi.HandleGamesRequests)
	http.Handle("/", http.FileServer(http.Dir("./web")))
	log.Fatal(http.ListenAndServe(":8080", nil))
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
