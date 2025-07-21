package restapi

import (
	"encoding/json"
	"net/http"
	"xchess/games"
	"xchess/validation"
)

func HandleGamesRequests(gamesChan chan games.GamesRequest) func(w http.ResponseWriter, r *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}

		r.Body = http.MaxBytesReader(w, r.Body, 1024*1024)

		var body PostGamesBody
		if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
			http.Error(w, "Invalid JSON: " + err.Error(), http.StatusBadRequest)
			return
		}
		if err := validation.Validator.Struct(body); err != nil {
			http.Error(w, "Validation failed: " + err.Error(), http.StatusBadRequest)
			return
		}

		responseChan := make(chan games.GamesResponse)
		gamesChan <- games.GamesRequest{
			Command:      games.Create,
			ResponseChan: responseChan,
		}
		response := <-responseChan
		if response.Error != nil {
			http.Error(w, "Could not create game: "+response.Error.Error(), http.StatusConflict)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(map[string]string{
			"gameId": response.GameID,
		})
	}
}

type PostGamesBody struct {
	BoardLayout            string `json:"boardLayout" validate:"required,oneof=standard large"`
	// *int, because required does not allow zero values for int
	FreezeTimeMilliseconds *int   `json:"freezeTimeMilliseconds" validate:"required,gte=0,lte=120000"`
}
