// @ts-check

import { Application, Assets, Container, Graphics, Sprite } from './pixi/pixi.mjs' // For release, use pixi/pixi.min.mjs

const game = new URLSearchParams(window.location.search).get('game') || 'test'
const white = new URLSearchParams(window.location.search).get('color') != 'black'
console.log(`xChess game '${game}' as ${white ? 'white' : 'black'}`)

const pixi = new Application()
await pixi.init({ background: '#1099bb', resizeTo: document.body })
document.body.replaceChildren(pixi.canvas)

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
// add board to container, resize container
// possibly invert coordinats like: container.scale.y = -1
const mySprite = new Sprite(Assets.get("P"));
mySprite.setSize(1, 1);
mySprite.x = 0
mySprite.y = 7
chessBoard.addChild(mySprite);

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
