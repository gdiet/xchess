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

func writeText(conn *websocket.Conn, clientID, message string) error {
	err := conn.WriteMessage(websocket.TextMessage, []byte(message))
	if err != nil {
		log.Printf("Write error (%s): %v", clientID, err)
	}
	return err
}

func wsHandler(stateChan chan StateMessage) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {

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

			if messageType != websocket.TextMessage {
				log.Println("Client", clientID, "sent message type", messageType, "- no operation.")
				break
			}

			command := string(message)
			log.Printf("Client %s sent: %s", clientID, command)

			if command == "get" {
				respChan := make(chan string)
				stateChan <- StateMessage{Type: "get", Resp: respChan}
				current := <-respChan
				if writeText(conn, clientID, "Current state: "+current) != nil {
					break
				}
			} else {
				stateChan <- StateMessage{Type: "set", Data: command}
				if writeText(conn, clientID, "Updated state to: "+command) != nil {
					break
				}
			}
		}

		log.Printf("Client %s disconnected.", clientID)
	}
}

func main() {
	stateChan := make(chan StateMessage)
	go StateManager(stateChan)

	http.HandleFunc("/ws", wsHandler(stateChan))
	http.Handle("/", http.FileServer(http.Dir("./web")))
	fmt.Println("Server started at :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
