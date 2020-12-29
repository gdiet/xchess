loadTextures()

/* xt.res: texture resources */
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
   xt.ws: websocket             */
function setup(xt) {
  const loc = window.location
  const name = new URLSearchParams(loc.search).get("name")
  const xc = { color: new URLSearchParams(loc.search).get("color") }
  xc.name = name
  xc.white = xc.color == "white"
  console.log(`Setup OK for ${xc.white ? 'white':'black'}.`)
  xt.ws = new WebSocket(`${loc.protocol.replace("http","ws")}//${loc.host}/ws/${name}`)
  xt.ws.onopen  = _ => console.log(`Websocket opened.`)
  xt.ws.onerror = _ => console.log(`Websocket error.`)
  xt.ws.onclose = _ => console.log(`Websocket closed.`)
  xt.ws.onmessage = receiveBoardLayout(xt, xc)
}

/* x, y   : 8  (chess) ... use Y(y) to allow for board orientation
   size   : 80 (adapted to window)
   app    : pixi app
   pieces : Map(id -> {sprite, color, piece, x, y})
   ids    : Map(x/y -> id ... use setId, hasId, getId, delId
   
   xt: Technical resources might have problems with JSON.stringify
   xc: Logical resources that can be used with JSON.stringify      */
function receiveBoardLayout(xt, xc) { return (event) => {
  const msg = JSON.parse(event.data)
  xc.x = msg.x
  xc.y = msg.y
  console.log(`Board layout ${xc.x} x ${xc.y}.`)
  let maxx = window.innerWidth - 30
  let maxy = window.innerHeight - 30
  if (msg.x/msg.y > maxx/maxy) { xc.size = maxx/msg.x } else { xc.size = maxy/msg.y }
  // Create the Pixi Application for the chess board
  xt.app = new PIXI.Application({width: xc.x*xc.size, height: xc.y*xc.size})
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
  xt.app.stage.addChild(checkers);
  // Add the chess board to the HTML document
  document.body.appendChild(xt.app.view)
  // Initialize board contents maps
  xc.pieces = new Map()
  xc.ids = new Map()
  xc.setId = function(x,y, value){return this.ids.set   (x*100+y, value)}
  xc.hasId = function(x,y       ){return this.ids.has   (x*100+y       )}
  xc.getId = function(x,y       ){return this.ids.get   (x*100+y       )}
  xc.delId = function(x,y       ){return this.ids.delete(x*100+y       )}
  // Set up a y coordinate helper function
  xc.Y = xc.white ? function(y){return this.y-1-y} : function(y){return y}
  // Initialize command handling
  xt.ws.onmessage = receiveCommand(xt, xc)
  // Set up interaction with the chess board
  xt.app.stage.interactive = true
  xt.app.stage.on('pointerdown', (event) => {
    console.log(`pointerdown ${JSON.stringify(event.data.global)}`)
 })
}}

function receiveCommand(xt, xc) { return (event) => {
  const msg = JSON.parse(event.data)
  console.debug(JSON.stringify(msg))
  switch (msg.cmd) {
    case "add"   : cmdAdd   (xt, xc, msg); break
    case "move"  : cmdMove  (xt, xc, msg); break
    case "remove": cmdRemove(xt, xc, msg); break
    case "keepalive": /* nothing to do */  break
    default: console.warn(`Unknown command ${msg.cmd}.`); break
  }
}}

function cmdAdd(xt, xc, msg) {
  xc.setId(msg.x, msg.y, msg.id)
  const sprite = new PIXI.Sprite(xt.res[`${msg.color}${msg.piece}`].texture)
  sprite.width = xc.size
  sprite.height = xc.size
  sprite.x = msg.x * xc.size
  sprite.y = xc.Y(msg.y) * xc.size
  xc.pieces.set(msg.id,{sprite:sprite, color:msg.color, piece:msg.piece, x:msg.x, y:msg.y})
  xt.app.stage.addChild(sprite)
}

function cmdMove(xt, xc, msg) {
  if (xc.pieces.has(msg.id)) {
    const entry = xc.pieces.get(msg.id)
    let ticks = 20
    const dx = (msg.x - entry.x) * xc.size / ticks
    const dy = (xc.Y(msg.y) - xc.Y(entry.y)) * xc.size / ticks
    function move(delta) {
      ticks = ticks - 1
      if (ticks > 0) {
        entry.sprite.x = entry.sprite.x + delta * dx
        entry.sprite.y = entry.sprite.y + delta * dy
      } else {
        entry.sprite.x = msg.x * xc.size
        entry.sprite.y = xc.Y(msg.y) * xc.size
        xt.app.ticker.remove(move)
      }
    }
    xt.app.ticker.add(move)
    if (!xc.delId(entry.x,entry.y)) console.warn(`Move: ${[entry.x,entry.y]} not found.`)
    xc.setId(entry.x,entry.y, msg.id)
  } else console.warn(`Move: ID ${msg.id} not found.`)
}

function cmdRemove(xt, xc, msg) {
  if (xc.pieces.has(msg.id)) {
    const entry = xc.pieces.get(msg.id)
    xt.app.stage.removeChild(entry.sprite)
    xc.pieces.delete(msg.id)
    if (!xc.delId(entry.x,entry.y)) console.warn(`Remove: ${[entry.x,entry.y]} not found.`)
  } else console.warn(`Remove: ID ${msg.id} not found.`)
}
