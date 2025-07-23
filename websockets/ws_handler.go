package websockets

import (
	"log"
	"net/http"
	"xchess/games"
	"xchess/util"

	"github.com/gorilla/websocket"
)

func WsHandler(gamesChan chan games.GamesRequest, getGameID util.ParamCallback) http.HandlerFunc {
	clientIDs := util.NewIDManager()

	return func(w http.ResponseWriter, r *http.Request) {
		gameId       := getGameID(r)
		responseChan := make(chan games.LookupGameResponse)
		gamesChan <- games.LookupGameRequest{
			GameID:      gameId,
			ResponseChan: responseChan,
		}
		response := <-responseChan
		if response.Error != nil {
			http.Error(w, response.Error.Error(), http.StatusNotFound)
			return
		}
		gameChan := response.GameChan

		clientID := clientIDs.GetNewID()

		ws, err := (&websocket.Upgrader{}).Upgrade(w, r, nil)
		if err != nil {
			log.Println("Client", clientID, "websocket upgrade error:", err)
			return
		}
		log.Println("Client", clientID, "connected to game:", gameId)

		updatesChan := make(chan interface{})
		gameChan <- games.SubscribeGameRequest{
			UpdatesChan: updatesChan,
		}

		defer clientIDs.UnregisterID(clientID)
		defer log.Println("Client", clientID, "disconnected from game:", gameId)
		defer func() { gameChan <- games.UnsubscribeGameRequest{ UpdatesChan: updatesChan } }()
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