package main

type CommandMessage struct {
	Type string        // "get" or "chat"
	Data string        // For "chat", this is the message to add
	Resp chan []string // Response channel for "get"
}

func ChatManager(input chan CommandMessage) {
	chat := []string{}

	for msg := range input {
		switch msg.Type {
		case "get":
			msg.Resp <- chat
		case "chat":
			// Add new message to chat
			chat = append(chat, msg.Data)
			
			// Keep only the last 20 messages
			if len(chat) > 20 {
				chat = chat[1:] // Remove the oldest message
			}
		}
	}
}
