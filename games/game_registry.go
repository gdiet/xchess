package games

func GameRegistry(boardLayout string, freezeTimeMilliseconds int) chan GameRequest {
	gameChan := make(chan GameRequest)
	state := GameState {
		FreezeTimeMilliseconds: freezeTimeMilliseconds,
		Pause:                  true,
		Chat:                   make([]string, 0),
	}
	go func() {
		for req := range gameChan {
			req.Execute(state)
		}
	}()
	return gameChan
}

type GameState struct {
	FreezeTimeMilliseconds int
	Pause                  bool
	Chat                   []string
	Subscribers            map[chan interface{}]struct{}
}
	
type GameRequest interface {
	Execute(game GameState)
}

type SubscribeGameRequest struct {
	UpdatesChan chan interface{}
}

func (req SubscribeGameRequest) Execute(game GameState) {
	game.Subscribers[req.UpdatesChan] = struct{}{}
}

type UnsubscribeGameRequest struct {
	UpdatesChan chan interface{}
}

func (req UnsubscribeGameRequest) Execute(game GameState) {
	delete(game.Subscribers, req.UpdatesChan)
}

type ChatGameRequest struct {
	Message       string
}

type PlanGameRequest struct {
	Plan          string
}

type PauseGameRequest struct {
}

func (req PauseGameRequest) Execute(game GameState) {
	game.Pause = true
}

type ResumeGameRequest struct {
}
