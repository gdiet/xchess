// @ts-check
import { Application, Assets, Container, Graphics, Sprite } from './pixi/pixi.mjs' // For release, use pixi/pixi.min.mjs
import { Clock } from './Clock.js'
import { Board } from './Board.js'

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
  await Assets.load({ alias: piece,               src: `pieces/w${piece}.svg`, data: { resolution: 2 } });
  await Assets.load({ alias: piece.toLowerCase(), src: `pieces/b${piece}.svg`, data: { resolution: 2 } });
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
  checkSync(clock, time)
  console.log(`board size is ${size}, freeze time is ${freeze} ticks`)
  const [cols, rows] = squareCoordinates(size).map(x => x + 1)
  const boardContainer = chessBoard(cols, rows)
  pixi.stage.addChild(boardContainer)
  resizeChessBoard(boardContainer, cols, rows)
  // When the window is maximized/restored, the resize event may fire too early.
  // The zero timeout ensures that resizeChessBoard is called after the resize is done.
  window.addEventListener('resize', () => { setTimeout(() => resizeChessBoard(boardContainer, cols, rows), 0); })
  ws.onmessage = receiveGameMessage(clock, new Board(), boardContainer)
} }

// Check whether the client clock is in sync with the server clock
/**
 * @param {Clock} clock
 * @param {string} time
 */
function checkSync(clock, time) {
  if (clock.timeMillis !== Number(time)) console.warn(`clock desync: ${clock.timeMillis} != ${Number(time)}`)
}

/**
 * @param {string} squareString - Chess square (e.g., "A1", "H8", or even "K14")
 * @returns {[number, number]} [column, row] (0-based)
 */
function squareCoordinates(squareString) {
  const col = squareString.charCodeAt(0) - 'A'.charCodeAt(0)
  const row = parseInt(squareString.slice(1)) - 1
  return [col, row]
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
 * @param {Clock} clock
 * @param {Board} board
 * @param {Container} boardContainer
 * @returns {function(MessageEvent<string>): void}
 */
function receiveGameMessage(clock, board, boardContainer) { return event => {
  const [time, command, ...args] = event.data.split(" ")
  checkSync(clock, time)
  switch(command) {
    case "add":
      const [square, piece, freezeUntil] = args
      add(board, boardContainer, square, piece, Number(freezeUntil))
      break
    case "chat":
      console.log(`chat message: ${args.join(" ")}`)
      break
    default:
      console.warn(`unknown command: ${command}`)
  }
} }

/**
 * Add a piece to the board and display it in the container.
 * 
 * @param {Board} board
 * @param {Container} boardContainer
 * @param {string} square - Chess square (e.g., "A1", "H8", or even "K14")
 * @param {string} piece - The piece to place (e.g., "K", "q")
 * @param {number} freezeUntil - Game time until which this square is frozen
 */
function add(board, boardContainer, square, piece, freezeUntil) {
  console.log(`add ${piece} on ${square}, freeze until ${freezeUntil}`)
  const [col, row] = squareCoordinates(square)
  board.set(col, row, piece, freezeUntil)
  const sprite = new Sprite(Assets.get(piece));
  sprite.width = 1
  sprite.height = 1
  if (white) { sprite.x = col; sprite.y = (boardContainer.height - 1) - row }
  else { sprite.x = (boardContainer.width - 1) - col; sprite.y = row }
  boardContainer.addChild(sprite)
}
