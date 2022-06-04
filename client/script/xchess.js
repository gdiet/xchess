loadTextures()

/* xt.res: texture resources */
function loadTextures() {
  const loader = PIXI.Loader.shared; // semicolon needed before next line
  ["Bishop", "King", "Knight", "Pawn", "Queen", "Rook"].forEach(piece =>
    ["black","white"].forEach(color =>
      loader.add(`${color}${piece}`, `/img/${color}/${piece}.png`)
    )
  )
  loader.add(`ice`,`/img/snowflake.png`)
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
  xt.ws.onerror = _ => { console.log(`Websocket error.`); location.href = "/" }
  xt.ws.onclose = _ => console.log(`Websocket closed.`)
  xt.ws.onmessage = receiveBoardLayout(xt, xc)
}

/* x, y   : 8  (chess) ... use Y(y) to allow for board orientation
   size   : 80 (adapted to window)
   app    : pixi app
   plans  : Map(id -> arrow graphics object)
   pieces : Map(id -> {sprite, color, piece, x, y})
   ids    : Map(x/y -> id ... use setId, hasId, getId, delId
   
   xt: Technical resources might have problems with JSON.stringify
   xc: Logical resources that can be used with JSON.stringify      */
function receiveBoardLayout(xt, xc) { return (event) => {
  const msg = JSON.parse(event.data)
  xc.x = msg.x
  xc.y = msg.y
  console.log(`Board layout ${xc.x} x ${xc.y}.`)
  const maxx = window.innerWidth - 30
  const maxy = window.innerHeight - 60
  if (msg.x/msg.y > maxx/maxy) { xc.size = maxx/msg.x } else { xc.size = maxy/msg.y }
  // Create the Pixi Application for the chess board
  xt.app = new PIXI.Application({width: xc.x*xc.size, height: xc.y*xc.size})
  const checkers = new PIXI.Graphics()
  // Render the chess board background to make it event sensitive
  checkers.beginFill(0x282020)
  checkers.drawRect(0, 0, xc.x * xc.size, xc.y * xc.size)
  checkers.endFill()
  // Add the checkers
  checkers.beginFill(0xa0a0a0)
  for (var x = 0; x < xc.x; x++)
    for (var y = xc.white ? x%2 : (x+1)%2; y < xc.y; y += 2)
      checkers.drawRect(x * xc.size, y * xc.size, xc.size, xc.size)
  checkers.endFill()
  xt.app.stage.addChild(checkers)
  // Hide the mouse when dragging - see also dragStart
  xt.app.renderer.plugins.interaction.cursorStyles.dragging = "none"
  // Add the chess board to the HTML document
  document.body.appendChild(xt.app.view)
  // Initialize board contents maps
  xc.plans = new Map()
  xc.pieces = new Map()
  xc.ids = new Map()
  xc.setId = function(x,y, value){return this.ids.set   (x*100+y, value)}
  xc.hasId = function(x,y       ){return this.ids.has   (x*100+y       )}
  xc.getId = function(x,y       ){return this.ids.get   (x*100+y       )}
  xc.delId = function(x,y       ){return this.ids.delete(x*100+y       )}
  // Set up a y coordinate helper function
  xc.Y = xc.white ? function(y){return this.y-1-y} : function(y){return y}
  // Set up drag-drop handling helper functions
  xc.dragStart = dragStart(xt, xc)
  xc.dragMove  = _ => _
  xc.dragEnd   = _ => _
  // Initialize command handling. On WS error just log, don't go back to the index page.
  xt.ws.onmessage = receiveCommand(xt, xc)
  xt.ws.onerror   = _ => console.log(`Websocket error.`)
  // Set up interaction with the chess board
  xt.app.stage.interactive = true
  xt.app.stage
    .on('pointerdown'     , event => xc.dragStart(event.data.global))
    .on('pointermove'     , event => xc.dragMove (event.data.global))
    .on('pointerup'       , _ => xc.dragEnd())
    .on('pointerupoutside', _ => xc.dragEnd())
}}

function receiveCommand(xt, xc) { return (event) => {
  const msg = JSON.parse(event.data)
  console.debug(JSON.stringify(msg))
  switch (msg.cmd) {
    case "winner": cmdWinner(xt, xc, msg); break
    case "add"   : cmdAdd   (xt, xc, msg); break
    case "move"  : cmdMove  (xt, xc, msg); break
    case "remove": cmdRemove(xt, xc, msg); break
    case "plan"  : cmdPlan  (xt, xc, msg); break
    case "unplan": cmdUnplan(xt, xc, msg); break
    case "keepalive": /* nothing to do */  break
    default: console.warn(`Unknown command ${msg.cmd}.`); break
  }
}}

function addSprite(xt, xc, color, piece, x, y) {
  const sprite  = new PIXI.Sprite(xt.res[`${color}${piece}`].texture)
  sprite.width  = xc.size
  sprite.height = xc.size
  sprite.x      = xc.size * x
  sprite.y      = xc.size * xc.Y(y)
  xt.app.stage.addChild(sprite)
  return sprite
}

function addSpriteWithIce(xt, xc, color, piece, x, y)  {
  const sprite = addSprite(xt, xc, color, piece, x, y)
  const ice = new PIXI.Sprite(xt.res[`ice`].texture)
  ice.width  = 0
  ice.height = 0
  sprite.addChild(ice)
  sprite.iceSprite = ice
  return sprite
}

function showIce(xt, sprite, freeze) {
  const freezeEnd = Date.now() + freeze
  const ice = sprite.iceSprite
  function melt() {
    const remaining = freezeEnd - Date.now()
    if (remaining <= 0) {
      ice.width  = 0
      ice.height = 0
      xt.app.ticker.remove(melt)
    } else {
      const shrinkTo = (remaining / freeze) ** 0.5 * 0.7
      ice.x      = 125 * (1 - shrinkTo)
      ice.y      = 125 * (1 - shrinkTo)
      ice.width  = 250 * shrinkTo
      ice.height = 250 * shrinkTo
    }
  }
  xt.app.ticker.add(melt)
}

/*  If we want to keep the moves on top, do something along the lines of
    https://github.com/pixijs/pixi.js/issues/296:

    stage.children.sort(function(a,b) {
        a.zIndex = a.zIndex || 0;
        b.zIndex = b.zIndex || 0;
        return b.zIndex - a.zIndex
    });
*/
function addMove(xt, xc, from, to) {
  const unit = xc.size / 12
  const length = Math.sqrt((to.x - from.x)**2 + (to.y - from.y)**2) * xc.size
  const arrow = new PIXI.Graphics()
  arrow.beginFill(0xe30dee)
  arrow.drawPolygon(
    0,0, 2*unit,-unit, 1.5*unit,-.3*unit,
    length,-.3*unit, length,.3*unit,
    1.5*unit,.3*unit, 2*unit,unit, 0,0)
  arrow.position.x = xc.size * (.5 + to.x)
  arrow.position.y = xc.size * (.5 + xc.Y(to.y))
  arrow.rotation = Math.atan2(xc.Y(from.y) - xc.Y(to.y), from.x - to.x)
  xt.app.stage.addChild(arrow)
  return arrow
}

function cmdWinner(xt, xc, msg) {
  const gold = new PIXI.Graphics()
  gold.lineStyle(xc.size/20, 0xf0c050)
  if (msg.winner == xc.color) {
    gold.drawRect(xc.size/40, (xc.y - 39/40)* xc.size, (xc.x - 1/20)* xc.size, xc.size*19/20)
  } else {
    gold.drawRect(xc.size/40, xc.size/40, (xc.x - 1/20)* xc.size, xc.size*19/20)
  }
  gold.z = 1
  xt.app.stage.addChild(gold)
}

function cmdAdd(xt, xc, msg) {
  xc.setId(msg.x, msg.y, msg.id)
  const sprite = addSpriteWithIce(xt, xc, msg.color, msg.piece, msg.x, msg.y)
  if (msg.freeze || 0 > 500) showIce(xt, sprite, msg.freeze)
  xc.pieces.set(msg.id,{sprite:sprite, color:msg.color, piece:msg.piece, x:msg.x, y:msg.y})
}

function cmdMove(xt, xc, msg) {
  if (xc.pieces.has(msg.id)) {
    const entry = xc.pieces.get(msg.id)
    let ticks = 20
    const dx = (msg.x - entry.x) * xc.size / ticks
    const dy = (xc.Y(msg.y) - xc.Y(entry.y)) * xc.size / ticks
    function move() {
      ticks = ticks - 1
      if (ticks > 0) {
        entry.sprite.x = entry.sprite.x + dx
        entry.sprite.y = entry.sprite.y + dy
      } else {
        entry.sprite.x = msg.x * xc.size
        entry.sprite.y = xc.Y(msg.y) * xc.size
        xt.app.ticker.remove(move)
        if (entry.piece == "Pawn" && (msg.y == 0 || msg.y == xc.y - 1)) {
          entry.piece = "Queen"
          xt.app.stage.removeChild(entry.sprite)
          const sprite = addSpriteWithIce(xt, xc, entry.color, "Queen", msg.x, msg.y)
          entry.sprite = sprite
          console.log(`Promoted ${entry.color} pawn to queen.`)
        }
        if (msg.freeze || 0 > 500) showIce(xt, entry.sprite, msg.freeze)
      }
    }
    xt.app.ticker.add(move)
    if (!xc.delId(entry.x,entry.y)) console.warn(`Move: ${[entry.x,entry.y]} not found.`)
    xc.setId(msg.x,msg.y, msg.id)
    entry.x = msg.x
    entry.y = msg.y
  } else console.warn(`Move: ID ${msg.id} not found.`)
}

function cmdRemove(xt, xc, msg) {
  if (xc.pieces.has(msg.id)) {
    const entry = xc.pieces.get(msg.id)
    let ticks = 20
    function fade() {
      ticks = ticks - 1
      if (ticks > 0) entry.sprite.alpha = ticks/20
      else { xt.app.stage.removeChild(entry.sprite); xt.app.ticker.remove(fade) }
    }
    xt.app.ticker.add(fade)
    xc.pieces.delete(msg.id)
    if (!xc.delId(entry.x,entry.y)) console.warn(`Remove: ${[entry.x,entry.y]} not found.`)
  } else console.warn(`Remove: ID ${msg.id} not found.`)
}

function cmdPlan(xt, xc, msg) {
  if (msg.color == xc.color) xc.plans.set(msg.id, addMove(xt, xc, msg.from, msg.to))
}

function cmdUnplan(xt, xc, msg) {
  if (xc.plans.has(msg.id)) {
    if (!msg.moved) xt.app.stage.removeChild(xc.plans.get(msg.id))
    else {
      const arrow = xc.plans.get(msg.id)
      let ticks = 20
      function fade() {
        ticks = ticks - 1
        if (ticks > 0) arrow.alpha = ticks/20
        else { xt.app.stage.removeChild(arrow); xt.app.ticker.remove(fade) }
      }
      xt.app.ticker.add(fade)
      xc.plans.delete(msg.id)
    }
  }
}

function x_y(xc, xy) { return {x: xy.x / xc.size | 0, y: xc.Y(xy.y / xc.size | 0)} }

function dragStart(xt, xc) { return xy => {
  const pos = x_y(xc, xy)
  if (xc.hasId(pos.x, pos.y)) {
    const id = xc.getId(pos.x, pos.y)
    if (xc.pieces.has(id)) {
      const entry = xc.pieces.get(id)
      if (entry.color == xc.color) {
        console.debug(`Drag start: ${JSON.stringify(pos)} ${entry.color} ${entry.piece}`)
        const sprite = addSprite(xt, xc, entry.color, entry.piece, pos.x, pos.y)
        sprite.alpha = 0.5
        // configure the sprite as "dragging" so the mouse is hidden (see also above "dragging")
        sprite.interactive = true; sprite.cursor = "dragging"
        xc.dragStart = _ => _
        xc.dragMove  = dragMove(xy.x - sprite.x, xy.y - sprite.y, sprite)
        xc.dragEnd   = dragEnd(xt, xc, id, sprite)
      } else console.debug(`Drag start on opponent's piece ${JSON.stringify(pos)}`)
    } else console.warn(`Drag start: ID ${id} not found.`)
  } else console.debug(`Drag start on empty field ${JSON.stringify(pos)}`)
}}

function dragMove(dx, dy, sprite) { return xy => {
  sprite.x = xy.x - dx
  sprite.y = xy.y - dy
}}

function dragEnd(xt, xc, id, sprite) { return () => {
  const pos = x_y(xc, {x: sprite.x + xc.size / 2, y: sprite.y + xc.size / 2})
  console.debug(`Drag end: ${JSON.stringify(pos)} id ${id}`)
  xt.ws.send(JSON.stringify({id: id, x: pos.x, y: pos.y}))
  xt.app.stage.removeChild(sprite)
  xc.dragStart = dragStart(xt, xc)
  xc.dragMove  = _ => _
  xc.dragEnd   = _ => _
}}
