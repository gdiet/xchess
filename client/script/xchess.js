setup()

/* color: white     | black
   side : 0 (white) | 1 (black)
   name : chess or large or ...
   ws   : websocket             */
export function setup() {
  const loc = window.location
  const name = new URLSearchParams(loc.search).get("name")
  const xc = {
    color: new URLSearchParams(loc.search).get("color"),
    name: name,
    ws: new WebSocket(`${loc.protocol.replace("http","ws")}//${loc.host}/ws/${name}`)
  }
  xc.side = xc.color == "white" ? 0 : 1
  console.log(`side: ${xc.side}`)
  xc.ws.onopen  = _ => console.log(`Websocket opened.`)
  xc.ws.onerror = _ => console.log(`Websocket error.`)
  xc.ws.onclose = _ => console.log(`Websocket closed.`)
  xc.ws.onmessage = receiveBoardLayout(xc)
}

/* x, y: 8  (chess)
   size: 80 (adapted to window)
   app : pixi app               */
function receiveBoardLayout(xc) { return (event) => {
  const msg = JSON.parse(event.data)
  xc.x = msg.x
  xc.y = msg.y
  console.log(`Board layout: ${xc.x} x ${xc.y}`)
  let maxx = window.innerWidth - 30
  let maxy = window.innerHeight - 30
  if (msg.x/msg.y > maxx/maxy) { xc.size = maxx/msg.x } else { xc.size = maxy/msg.y }
  // Create the Pixi Application for the chess board
  console.log(`Chess board: ${JSON.stringify(xc)}`)
  xc.app = new PIXI.Application({width: xc.x*xc.size, height: xc.y*xc.size})
  let checkers = new PIXI.Graphics()
  // Render the chess board background to make it event sensitive
  checkers.beginFill(0x282020)
  checkers.drawRect(0, 0, xc.x * xc.size, xc.y * xc.size)
  checkers.endFill()
  // Add the checkers
  checkers.beginFill(0xa0a0a0)
  for (var x = 0; x < xc.x; x++)
    for (var y = (x + xc.side) % 2; y < xc.y; y += 2)
      checkers.drawRect(x * xc.size, y * xc.size, xc.size, xc.size);
  checkers.endFill()
  xc.app.stage.addChild(checkers);
  // Add the chess board to the HTML document
  document.body.appendChild(xc.app.view)
  xc.ws.onmessage = receiveCommand(xc)
  // Set up interaction with the chess board
  xc.app.stage.interactive = true
  xc.app.stage.on('pointerdown', (event) => {
    console.log(`pointerdown ${JSON.stringify(event.data.global)}`)
 })
}}

function receiveCommand(xc) { return (event) => {
  const msg = JSON.parse(event.data)
  switch (msg.cmd) {
    case "add": cmdAdd(xc, msg); break
    default: console.warn(`Unknown command ${msg.cmd}.`)
  }
}}

function cmdAdd(xc, msg) {
  console.log(`add ${JSON.stringify(msg)}`)
}
