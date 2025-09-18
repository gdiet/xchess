// @ts-check
// For release, use the minified library pixi/pixi.min.mjs
import { Application, Graphics } from './pixi/pixi.mjs'

// Create the Pixi Application for the chess board
const app = new Application()
const htmlContainer = document.body
await app.init({ background: '#1099bb', resizeTo: htmlContainer })
htmlContainer.replaceChildren(app.canvas)

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
app.stage.addChild(chessBoard)

/**
 * @param {Number} cols 
 * @param {Number} rows 
 */
function resizeChessBoard(cols, rows) {
  const xBound = app.canvas.width * 0.9
  const yBound = app.canvas.height * 0.9
  const scale = Math.min(xBound / cols, yBound / rows)
  chessBoard.x = (app.canvas.width / scale - cols) / 2
  chessBoard.y = (app.canvas.height / scale - rows) / 2
  app.stage.scale.set(scale)
}

resizeChessBoard(cols, rows)

// Call resizeChessBoard whenever the window resizes.
// When the window is maximized/restored, the resize event may fire too early.
// The zero timeout ensures that resizeChessBoard is called after the resize is done.
window.addEventListener('resize', () => { setTimeout(() => resizeChessBoard(cols, rows), 0); });
