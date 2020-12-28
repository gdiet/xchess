loadTextures()

/* res: texture resources */
function loadTextures() {
  const loader = PIXI.Loader.shared; // semicolon needed before next line
  ["Bishop", "King", "Knight", "Pawn", "Queen", "Rook"].forEach(piece =>
    ["black","white"].forEach(color =>
      loader.add(`${color}${piece}`, `/img/${color}/${piece}.png`)
    )
  )
  loader.onError.add(e => console.error(`Error ${e}.`))
  loader.load((_, resources) => {
    console.log(`Piece images loaded.`)
    setup({res: resources})
  })
}

/* color: white | black
   white: true  | false
   name : chess or large or ...
   ws   : websocket             */
function setup(xc) {
  const loc = window.location
  const name = new URLSearchParams(loc.search).get("name")
  xc.color = new URLSearchParams(loc.search).get("color")
  xc.name = name
  xc.ws = new WebSocket(`${loc.protocol.replace("http","ws")}//${loc.host}/ws/${name}`)
  xc.white = xc.color == "white"
  console.log(`Setup OK for ${xc.white ? 'white':'black'}.`)
  xc.ws.onopen  = _ => console.log(`Websocket opened.`)
  xc.ws.onerror = _ => console.log(`Websocket error.`)
  xc.ws.onclose = _ => console.log(`Websocket closed.`)
  xc.ws.onmessage = receiveBoardLayout(xc)
}

/* x, y   : 8  (chess)
   size   : 80 (adapted to window)
   app    : pixi app
   sprites: Map(id -> sprite)      */
function receiveBoardLayout(xc) { return (event) => {
  const msg = JSON.parse(event.data)
  xc.x = msg.x
  xc.y = msg.y
  console.log(`Board layout ${xc.x} x ${xc.y}.`)
  let maxx = window.innerWidth - 30
  let maxy = window.innerHeight - 30
  if (msg.x/msg.y > maxx/maxy) { xc.size = maxx/msg.x } else { xc.size = maxy/msg.y }
  // Create the Pixi Application for the chess board
  xc.app = new PIXI.Application({width: xc.x*xc.size, height: xc.y*xc.size})
  let checkers = new PIXI.Graphics()
  // Render the chess board background to make it event sensitive
  checkers.beginFill(0x282020)
  checkers.drawRect(0, 0, xc.x * xc.size, xc.y * xc.size)
  checkers.endFill()
  // Add the checkers
  checkers.beginFill(0xa0a0a0)
  for (var x = 0; x < xc.x; x++)
    for (var y = xc.white ? x%2 : (x+1)%2; y < xc.y; y += 2)
      checkers.drawRect(x * xc.size, y * xc.size, xc.size, xc.size);
  checkers.endFill()
  xc.app.stage.addChild(checkers);
  // Add the chess board to the HTML document
  document.body.appendChild(xc.app.view)
  // Initialize sprites map
  xc.sprites = new Map()
  // Initialize command handling
  xc.ws.onmessage = receiveCommand(xc)
  // Set up interaction with the chess board
  xc.app.stage.interactive = true
  xc.app.stage.on('pointerdown', (event) => {
    console.log(`pointerdown ${JSON.stringify(event.data.global)}`)
 })
}}

function receiveCommand(xc) { return (event) => {
  const msg = JSON.parse(event.data)
  console.debug(JSON.stringify(msg))
  switch (msg.cmd) {
    case "add": cmdAdd(xc, msg); break
    case "remove": cmdRemove(xc, msg); break
    case "keepalive": /* nothing to do */ break
    default: console.warn(`Unknown command ${msg.cmd}.`); break
  }
}}

function cmdAdd(xc, msg) {
  const sprite = new PIXI.Sprite(xc.res[`${msg.color}${msg.piece}`].texture)
  sprite.width = xc.size
  sprite.height = xc.size
  sprite.x = msg.x * xc.size
  sprite.y = xc.white ? (xc.y - 1 - msg.y) * xc.size : msg.y * xc.size
  xc.sprites.set(msg.id, sprite)
  xc.app.stage.addChild(sprite)
}

function cmdRemove(xc, msg) {
  if (xc.sprites.has(msg.id)) {
    const sprite = xc.sprites.get(msg.id)
    xc.app.stage.removeChild(sprite)
    xc.sprites.delete(msg.id)
  } else log.warn(`Remove: ID ${msg.id} not found.`)
}
