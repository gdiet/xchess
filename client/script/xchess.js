export function setup() {
  const websocket = new WebSocket("ws://localhost:8080/ws")
  websocket.onopen = function(event) {
    console.log(`Websocket opened.`)
    websocket.send("hallo")
  }
  websocket.onmessage = function(event) {
    console.log(`Websocket message: ${event.data}`)
  }
  websocket.onerror = function(event) {
    console.log(`Websocket error.`)
  }
  websocket.onclose = function(event) {
    console.log(`Websocket close.`)
  }
}

setup()
