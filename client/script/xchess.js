export function setup() {
  const loc = window.location
  const game = new URLSearchParams(loc.search).get("name")
  const wsUrl = `${loc.protocol.replace("http","ws")}//${loc.host}/ws/${game}`
  const websocket = new WebSocket(wsUrl)
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
