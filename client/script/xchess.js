setup()

export function setup() {
  const loc = window.location
  const name = new URLSearchParams(loc.search).get("name")
  const xc = {
    color: new URLSearchParams(loc.search).get("color"),
    name: name,
    ws: new WebSocket(`${loc.protocol.replace("http","ws")}//${loc.host}/ws/${name}`)
  }
  xc.ws.onopen  = _ => console.log(`Websocket opened.`)
  xc.ws.onerror = _ => console.log(`Websocket error.`)
  xc.ws.onclose = _ => console.log(`Websocket closed.`)
  xc.ws.onmessage = receiveBoardLayout(xc)
}

function receiveBoardLayout(xc) { return (event) => {
  const msg = JSON.parse(event.data)
  const rows = msg.rows
  const cols = msg.cols
  console.log(`Board layout: ${cols} x ${rows}`)
}}

