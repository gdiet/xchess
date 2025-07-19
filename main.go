package main

import (
	"fmt"
	"log"
	"math/rand"
	"net/http"

	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool { return true },
}

func generateClientID() string {
	id := rand.Intn(1_000_000) // 0 to 999999
	return fmt.Sprintf("%06d", id)
}

func wsHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Upgrade error:", err)
		return
	}
	defer conn.Close()

	clientID := generateClientID()
	log.Printf("Client %s connected.", clientID)

	for {
		messageType, message, err := conn.ReadMessage()
		if err != nil {
			log.Println("Client", clientID, "read error:", err)
			break
		}

		log.Printf("Client %s received: %s", clientID, message)

		if err := conn.WriteMessage(messageType, message); err != nil {
			log.Println("Client", clientID, "write error:", err)
			break
		}
	}

	log.Printf("Client %s disconnected.", clientID)
}

func main() {
	http.HandleFunc("/ws", wsHandler)
	fmt.Println("Server started at :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
