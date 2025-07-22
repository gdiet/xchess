package main

import (
	"log"
	"net/http"
	"xchess/games"
	"xchess/restapi"
	"xchess/util"
	"xchess/validation"
	"xchess/websockets"

	"github.com/gorilla/mux"
)

func main() {
	validation.Init()

	router    := mux.NewRouter()
	gamesChan := games.GamesRegistry()

	router.HandleFunc("/api/games", restapi.HandleGamesRequests(gamesChan))
	router.HandleFunc("/ws/{gameId}", websockets.WsHandler(gamesChan, util.RequestParam("gameId")))
	router.PathPrefix("/").Handler(http.FileServer(http.Dir("./web")))

	http.Handle("/", router)
	log.Println("Starting server on :7080")
	log.Fatal(http.ListenAndServe(":7080", router))
}
