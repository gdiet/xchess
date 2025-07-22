package websockets

import (
	"fmt"
	"log"
	"math/rand"
	"net/http"
	"xchess/games"

	"github.com/gorilla/websocket"
)

func WsHandler(gamesChan chan games.GamesRequest) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		clientID := fmt.Sprintf("%06d", rand.Intn(1_000_000)) // 000000 to 999999

		ws, err := (&websocket.Upgrader{}).Upgrade(w, r, nil)
		if err != nil {
			log.Println("Client", clientID, "websocket upgrade error:", err)
			return
		}
		log.Println("Client", clientID, "connected.")
		defer log.Println("Client", clientID, "disconnected.")
		defer ws.Close()

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
			// FIXME continue
			log.Println("Client", clientID, "sent message:", command)
		}

	}
}