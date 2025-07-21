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
