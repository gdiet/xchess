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

/* rows, cols: 8  (chess)
   size      : 80 (adapted to window)
   app       : pixi app               */
function receiveBoardLayout(xc) { return (event) => {
  const msg = JSON.parse(event.data)
  xc.rows = msg.rows
  xc.cols = msg.cols
  console.log(`Board layout: ${xc.cols} x ${xc.rows} - ${JSON.stringify(xc)}`)
  let maxx = window.innerWidth - 30
  let maxy = window.innerHeight - 30
  if (msg.cols/msg.rows > maxx/maxy) { xc.size = maxx/msg.cols } else { xc.size = maxy/msg.rows }
  // Create the Pixi Application for the chess board
  xc.app = new PIXI.Application({width: xc.cols*xc.size, height: xc.rows*xc.size})
  // Set the chess board background
  xc.app.renderer.backgroundColor = 0x282020
  // Add checkers to chess board
  let checkers = new PIXI.Graphics()
  checkers.beginFill(0xa0a0a0)
  for (var x = 0; x < xc.cols; x++)
    for (var y = (x + xc.side) % 2; y < xc.rows; y += 2)
      checkers.drawRect(x * xc.size, y * xc.size, xc.size, xc.size);
  checkers.endFill()
  xc.app.stage.addChild(checkers);
  // Add the chess board to the HTML document
  document.body.appendChild(xc.app.view)
}}
