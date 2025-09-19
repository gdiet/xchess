// @ts-check

import { Application, Assets, Container, Graphics, Sprite } from './pixi/pixi.mjs' // For release, use pixi/pixi.min.mjs
import { GameClock } from './GameClock.js'

const game = new URLSearchParams(window.location.search).get('game') || 'test'
const white = new URLSearchParams(window.location.search).get('color') != 'black'
const color = white ? 'white' : 'black'
console.log(`xChess game '${game}' as ${color}`)

const pixi = new Application()
await pixi.init({ background: '#1099bb', resizeTo: document.body })
document.body.replaceChildren(pixi.canvas)

/** @type {WebSocket} */
const ws = new WebSocket(`${window.location.protocol.replace("http","ws")}//${window.location.host}/ws/${game}/${color}`)
ws.onopen  = _ => console.log(`websocket opened`)
ws.onerror = _ => console.log(`websocket error`) // maybe add: location.href = "notfound.html" + window.location.search
ws.onclose = _ => console.log(`websocket closed`)

ws.onmessage = receiveClockInitialization

/** @param {MessageEvent<string>} event */
function receiveClockInitialization(event) {
  const [time, _clock, millisPerTick, _millis, stopped] = event.data.split(" ")
  const clock = new GameClock(Number(time), Number(millisPerTick), stopped === "stopped")
  console.log(`clock at ${clock.timeMillis} millis ${stopped}, tick is ${clock.millisPerTick} millis`)
  ws.onmessage = receiveBoardSize(clock)
}

/**
 * @param {GameClock} clock
 * @returns {function(MessageEvent<string>): void}
 */
function receiveBoardSize(clock) { return event => {
  const [time, _board, _size, size, _freeze, freeze] = event.data.split(" ")
  checkSync(clock, time)
  console.log(`board size is ${size}, freeze is ${freeze}`)
  ws.onmessage = _ => {} // FIXME continue
} }

/**
 * @param {GameClock} clock
 * @param {string} time
 */
function checkSync(clock, time) {
  if (clock.timeMillis !== Number(time)) console.warn(`clock desync: ${clock.timeMillis} != ${Number(time)}`)
}

const cols = 10
const rows = 8

const chessBoard = new Graphics({})
chessBoard.rect(0, 0, cols, rows)
chessBoard.fill(0x282020)
// Add the checkers
for (var x = 0; x < cols; x++)
for (var y = x%2; y < rows; y += 2)
  chessBoard.rect(x, y, 1, 1)
chessBoard.fill(0xa0a0a0)

const chessBoardContainer = new Container()
chessBoardContainer.addChild(chessBoard)

pixi.stage.addChild(chessBoardContainer)

await loadImages()

// FIXME demo code, remove soon
// possibly invert coordinats like: container.scale.y = -1 => probably too much trouble, position sprites correctly instead
const mySprite = new Sprite(Assets.get("P"));
mySprite.setSize(1, 1);
mySprite.x = 0
mySprite.y = 7
chessBoardContainer.addChild(mySprite);

async function loadImages() {
  await Promise.all("BKNPQR".split("").map(async piece => {
    await Assets.load({ alias: piece,               src: `cardinal/w${piece}.svg`, data: { resolution: 2 } });
    await Assets.load({ alias: piece.toLowerCase(), src: `cardinal/b${piece}.svg`, data: { resolution: 2 } });
  }));
  console.log(`images loaded`)
}

/**
 * @param {Number} cols 
 * @param {Number} rows 
 */
function resizeChessBoard(cols, rows) {
  const xBound = pixi.canvas.width * 0.9
  const yBound = pixi.canvas.height * 0.9
  const scale = Math.min(xBound / cols, yBound / rows)
  chessBoardContainer.x = (pixi.canvas.width / scale - cols) / 2
  chessBoardContainer.y = (pixi.canvas.height / scale - rows) / 2
  pixi.stage.scale.set(scale)
}

resizeChessBoard(cols, rows)

// Call resizeChessBoard whenever the window resizes.
// When the window is maximized/restored, the resize event may fire too early.
// The zero timeout ensures that resizeChessBoard is called after the resize is done.
window.addEventListener('resize', () => { setTimeout(() => resizeChessBoard(cols, rows), 0); });
