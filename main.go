package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"

	"github.com/gorilla/websocket"
)

func writeText(conn *websocket.Conn, clientID, message string) error {
	err := conn.WriteMessage(websocket.TextMessage, []byte(message))
	if err != nil {
		log.Printf("Write error (%s): %v", clientID, err)
	}
	return err
}

func wsHandler(commandChan chan CommandMessage) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {

		clientID := fmt.Sprintf("%06d", rand.Intn(1_000_000)) // 000000 to 999999

		ws, err := (&websocket.Upgrader{}).Upgrade(w, r, nil)
		if err != nil {
			log.Println("Client", clientID, "websocket upgrade error:", err)
			return
		}
		defer ws.Close()

		log.Println("Client", clientID, "connected.")

		for {
			messageType, message, err := ws.ReadMessage()
			if err != nil {
				log.Println("Client", clientID, "websocket read error:", err)
				break
			}

			if messageType != websocket.TextMessage {
				log.Println("Client", clientID, "sent message type", messageType, "- no operation.")
				break
			}

			command := string(message)
			log.Println("Client", clientID, "sent:", command)

			if command == "get" {
				chatResponseChan := make(chan []string)
				commandChan <- CommandMessage{Type: "get", Resp: chatResponseChan}
				jsonBytes, err := json.Marshal(<-chatResponseChan)
				if err != nil {
					log.Println("Client", clientID, "JSON encoding error:", err)
					break
				}
				err = writeText(ws, clientID, string(jsonBytes))
				if err != nil {
					log.Println("Client", clientID, "websocket write error:", err)
					break
				}
				log.Println("Client", clientID, "response:", string(jsonBytes))

			} else {
				// Treat any non-"get" message as a chat message
				commandChan <- CommandMessage{Type: "chat", Data: command}
				err = writeText(ws, clientID, "Message added to chat")
				if err != nil {
					log.Println("Client", clientID, "websocket write error:", err)
					break
				}
			}
		}

		log.Println("Client", clientID, "disconnected.")
	}
}

func main() {
	chatChan := make(chan CommandMessage)
	go ChatManager(chatChan)

	http.HandleFunc("/ws", wsHandler(chatChan))
	http.Handle("/", http.FileServer(http.Dir("./web")))
	fmt.Println("Server started at :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
