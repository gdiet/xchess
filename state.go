package main

type StateMessage struct {
	Type string      // "get" or "set"
	Data string      // For "set", this is the new value
	Resp chan string // Response channel for "get"
}

func StateManager(input chan StateMessage) {
	var state string

	for msg := range input {
		switch msg.Type {
		case "get":
			msg.Resp <- state
		case "set":
			state = msg.Data
		}
	}
}
