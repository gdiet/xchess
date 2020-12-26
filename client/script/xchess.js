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
  xc.rows = msg.rows
  xc.cols = msg.cols
  console.log(`Board layout: ${xc.cols} x ${xc.rows} - ${JSON.stringify(xc)}`)
  // Create the Pixi Application for the chess board
  xc.app = new PIXI.Application({width: 640, height: 640})
  // Set the chess board background
  xc.app.renderer.backgroundColor = 0x282020
  // Add the chess board to the HTML document
  document.body.appendChild(xc.app.view)
}}
