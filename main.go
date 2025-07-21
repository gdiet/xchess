package main

import (
	"log"
	"net/http"
	"xchess/games"
	"xchess/restapi"
	"xchess/validation"
)

func main() {
	validation.Init()
	gamesChan := games.GamesRegistry()

	http.HandleFunc("/api/games", restapi.HandleGamesRequests(gamesChan))
	http.Handle("/", http.FileServer(http.Dir("./web")))
	log.Println("Starting server on :7080")
	log.Fatal(http.ListenAndServe(":7080", nil))
}
