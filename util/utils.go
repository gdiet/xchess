package util

import (
	"net/http"

	"github.com/gorilla/mux"
)

type ParamCallback func(r *http.Request) string

func RequestParam(key string) ParamCallback {
	return func(r *http.Request) string {
		return mux.Vars(r)[key]
	}
}
