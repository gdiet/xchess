package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"
)

func main() {
	http.HandleFunc("/api/games", handleGamesRequests)
	http.Handle("/", http.FileServer(http.Dir("./web")))
	log.Fatal(http.ListenAndServe(":8080", nil))
}

func handleGamesRequests(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	r.Body = http.MaxBytesReader(w, r.Body, 1024 * 1024)

	var body PostGamesBody
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		http.Error(w, "Invalid JSON: " + err.Error(), http.StatusBadRequest)
		return
	}

	gameID := fmt.Sprintf("%06d", rand.Intn(1000000))

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{
		"gameId": gameID,
	})
}

type PostGamesBody struct {
	BoardLayout            string `json:"boardLayout"`
	FreezeTimeMilliseconds int    `json:"freezeTimeMilliseconds"`
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
