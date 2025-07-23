package util

import (
	"fmt"
	"math/rand"
	"sync"
)

type IDManager struct {
    mutex      sync.Mutex
    assigned   map[string]struct{}
}

func NewIDManager() *IDManager {
	m := IDManager{
		assigned: make(map[string]struct{}),
	}
	return &m
}

func (m *IDManager) GetNewID() string {
	m.mutex.Lock()
	defer m.mutex.Unlock()

	for {
		id := fmt.Sprintf("%06d", rand.Intn(1000000))
		if _, exists := m.assigned[id]; !exists {
			m.assigned[id] = struct{}{}
			return id
		}
	}
}

func (m *IDManager) UnregisterID(id string) {
	m.mutex.Lock()
	defer m.mutex.Unlock()

	delete(m.assigned, id)
}
