// @ts-check
import { Application, Assets, Container, Graphics, Sprite } from './pixi/pixi.mjs' // For release, use pixi/pixi.min.mjs
import { Clock } from './Clock.js'
import { State } from './State.js'

// Get game and color from URL parameters
const game = new URLSearchParams(window.location.search).get('game') || 'test'
const white = new URLSearchParams(window.location.search).get('color') != 'black'
const color = white ? 'white' : 'black'
console.log(`xChess game '${game}' as ${color}`)

// Initialize PixiJS
const pixi = new Application()
await pixi.init({ background: '#1099bb', resizeTo: document.body })
document.body.replaceChildren(pixi.canvas)

// Load piece images
await Promise.all("BKNPQR".split("").map(async piece => {
  const aliasWhite = piece === 'P' ? ['P', 'M'] : [piece] // Moved pawn is 'M'/'m'
  const aliasBlack = aliasWhite.map(p => p.toLowerCase())
  await Assets.load({ alias: aliasWhite, src: `pieces/w${piece}.svg`, data: { resolution: 2 } });
  await Assets.load({ alias: aliasBlack, src: `pieces/b${piece}.svg`, data: { resolution: 2 } });
}));
console.log(`images loaded`)

// Set up WebSocket connection
/** @type {WebSocket} */
const ws = new WebSocket(`${window.location.protocol.replace("http","ws")}//${window.location.host}/ws/${game}/${color}`)
ws.onopen  = _ => console.log(`websocket opened`)
ws.onerror = _ => console.log(`websocket error`) // maybe add: location.href = "notfound.html" + window.location.search
ws.onclose = _ => console.log(`websocket closed`)
ws.onmessage = receiveClockInitialization

// Receive clock initialization message and initialize the game clock
/** @param {MessageEvent<string>} event */
function receiveClockInitialization(event) {
  const [time, _clock, millisPerTick, _millis, stopped] = event.data.split(" ")
  const clock = new Clock(Number(time), Number(millisPerTick), stopped === "stopped")
  console.log(`clock at ${clock.timeMillis} millis ${stopped} with ${clock.millisPerTick} millis per tick`)
  ws.onmessage = receiveBoardSize(clock)
}

// Receive board initialization message and set up the chess board
/**
 * @param {Clock} clock
 * @returns {function(MessageEvent<string>): void}
 */
function receiveBoardSize(clock) { return event => {
  const [time, _board, _size, size, _freeze, freeze] = event.data.split(" ")
  clock.checkSync(time)
  console.log(`board size is ${size}, freeze time is ${freeze} ticks`)
  const [maxCol, maxRow] = parseSquare(size)
  const [cols, rows] = [maxCol + 1, maxRow + 1]
  const boardContainer = chessBoard(cols, rows)
  pixi.stage.addChild(boardContainer)
  resizeChessBoard(boardContainer, cols, rows)
  // When the window is maximized/restored, the resize event may fire too early.
  // The zero timeout ensures that resizeChessBoard is called after the resize is done.
  window.addEventListener('resize', () => { setTimeout(() => resizeChessBoard(boardContainer, cols, rows), 0); })
  const state = new State(clock, maxCol, maxRow, boardContainer)
  ws.onmessage = receiveGameMessage(state)
} }

/**
 * @param {string} squareString - Chess square (e.g., "A1", "H8", or even "K14")
 * @returns {[number, number]} [column, row] (0-based)
 */
function parseSquare(squareString) {
  const col = squareString.charCodeAt(0) - 'A'.charCodeAt(0)
  const row = parseInt(squareString.slice(1)) - 1
  return [col, row]
}

/**
 * @param {State} state
 * @param {string} squareString - Chess square (e.g., "A1", "H8", or even "K14")
 * @returns {[number, number]} graphics coordinates [x, y] of the square
 */
function coordinates(state, squareString) {
  const [col, row] = parseSquare(squareString)
  if (white) return [col, state.maxRow - row]
  else return [state.maxCol - col, row]
}

/**
 * @param {number} cols - The number of columns.
 * @param {number} rows - The number of rows.
 * @returns {Container} The chess board graphics container.
 */
function chessBoard(cols, rows) {
  const chessBoard = new Graphics({})
  chessBoard.rect(0, 0, cols, rows)
  chessBoard.fill(0x282020)
  // Add the checkers
  for (var x = 0; x < cols; x++)
    for (var y = x%2; y < rows; y += 2)
      chessBoard.rect(x, y, 1, 1)
  chessBoard.fill(0xa0a0a0)
  const container = new Container()
  container.addChild(chessBoard)
  return container
}

/**
 * @param {Container} chessBoardContainer
 * @param {Number} cols 
 * @param {Number} rows 
 */
function resizeChessBoard(chessBoardContainer,cols, rows) {
  const xBound = pixi.canvas.width * 0.9
  const yBound = pixi.canvas.height * 0.9
  const scale = Math.min(xBound / cols, yBound / rows)
  chessBoardContainer.x = (pixi.canvas.width / scale - cols) / 2
  chessBoardContainer.y = (pixi.canvas.height / scale - rows) / 2
  pixi.stage.scale.set(scale)
}

// Main message handler for game updates
/**
 * @param {State} state
 * @returns {function(MessageEvent<string>): void}
 */
function receiveGameMessage(state) { return event => {
  const [time, command, ...args] = event.data.split(" ")
  state.clock.checkSync(time)
  switch(command) {
    case "add":
      const [square, piece, freezeUntil] = args
      add(state, square, piece, Number(freezeUntil))
      break
    case "chat":
      console.log(`chat message: ${args.join(" ")}`)
      break
    case "plan":
      const [planFrom, planTo] = args
      plan(state, planFrom, planTo)
      break
    case "unplan":
      const [unplanFrom] = args
      unplan(state, unplanFrom)
      break
    case "start":
      console.log(`start`)
      state.clock.start()
      break
    case "stop":
      console.log(`stop`)
      state.clock.stop()
      break
    default:
      console.warn(`unknown command ${command}: ${event.data}`)
  }
} }

/**
 * Add a piece to the board and display it in the container.
 *
 * @param {State} state
 * @param {string} square - Chess square (e.g., "A1", "H8", or even "K14")
 * @param {string} piece - The piece to place (e.g., "K", "q")
 * @param {number} freezeUntil - Game time until which this square is frozen
 */
function add(state, square, piece, freezeUntil) {
  console.log(`add ${piece} on ${square}, freeze until ${freezeUntil}`)
  state.board.set(square, piece, freezeUntil)
  const sprite = new Sprite(Assets.get(piece))
  sprite.setSize(1, 1)
  sprite.position.set(...coordinates(state, square))
  state.boardContainer.addChild(sprite)
}

/**
 * @param {State} state
 * @param {string} from - e.g. "D7"
 * @param {string} to - e.g. "D5"
 */
function plan(state, from, to) {
  console.log(`plan from ${from} to ${to}`)
  const [fromCol, fromRow] = coordinates(state, from)
  const [toCol, toRow] = coordinates(state, to)
  const length = Math.sqrt((toRow - fromRow)**2 + (toCol - fromCol)**2)
  const arrow = new Graphics({})
  arrow.poly([0,0, 0.35,-.2, 0.25,-.06, length,-.06, length,.06, 0.25,.06, 0.35,.2, 0,0])
  arrow.fill(0xe30dee)
  arrow.position.x = .5 + toCol
  arrow.position.y = .5 + toRow
  arrow.rotation = Math.atan2(fromRow - toRow, fromCol - toCol)
  state.boardContainer.addChild(arrow)
  state.plans.set(from, arrow)
}

/**
 * @param {State} state
 * @param {string} from
 */
function unplan(state, from) {
  console.log(`unplan from ${from}`)
  const arrow = state.plans.get(from)
  if (arrow) {
    state.boardContainer.removeChild(arrow)
    state.plans.delete(from)
  } else console.warn(`no plan from ${from} found`)
}
