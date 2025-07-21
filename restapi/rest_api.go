package restapi

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"net/http"
	v "xchess/validation"
)

func HandleGamesRequests(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	r.Body = http.MaxBytesReader(w, r.Body, 1024*1024)

	var body PostGamesBody
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		http.Error(w, "Invalid JSON: "+err.Error(), http.StatusBadRequest)
		return
	}
	if err := v.Validator.Struct(body); err != nil {
		http.Error(w, "Validation failed: "+err.Error(), http.StatusBadRequest)
		return
	}

	gameID := fmt.Sprintf("%06d", rand.Intn(1000000))

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{
		"gameId": gameID,
	})
}

type PostGamesBody struct {
	BoardLayout            string `json:"boardLayout" validate:"required,oneof=standard large"`
	FreezeTimeMilliseconds int    `json:"freezeTimeMilliseconds" validate:"required,gte=0,lte=120000"`
}
