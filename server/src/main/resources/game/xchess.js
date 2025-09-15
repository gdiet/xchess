// @ts-check
// For release, use the minified library pixi/pixi.min.mjs
// / <reference path="./pixi/pixi.js.d.ts" />  // FIXME needed or not?
import { Application, Assets, Graphics, Sprite } from './pixi/pixi.mjs'

/** @typedef {import("./pixi/pixi.mjs").Application} Application */

/**
 * @typedef {Object} Ches
 * @property {boolean} white
 * @property {{cols: number, rows: number}} size
 * @property {{start: number|null, offset: number|null}} clock
 * If start is set, the clock is running. Indicates the time when the clock started running.
 * If offset is set, the clock is stopped. Indicates the time that has passed in the game.
 * @property {number} freeze
 */

/**
 * @typedef {Object} Tech
 * @property {WebSocket} ws
 * @property {Application} app
 * @property {Map<string, Sprite>} pieces
 */

loadAssets()
setup()

async function loadAssets() {
  "BKNPQR".split("").forEach(async piece => {
    await Assets.load({ alias: piece,               src: `cardinal/w${piece}.svg`, data: { resolution: 2 } });
    await Assets.load({ alias: piece.toLowerCase(), src: `cardinal/b${piece}.svg`, data: { resolution: 2 } });
  });
  console.log(`- assets loaded -`)
}

function setup() {
  const loc  = window.location
  const name = new URLSearchParams(loc.search).get("name") || "test"
  const ches = { 
    white: new URLSearchParams(loc.search).get("color") !== "black",
    size: { cols: -1, rows: -1 },
    clock: { start: null, offset: null },
    freeze: -1
  }
  const tech = {
    ws: new WebSocket(`${loc.protocol.replace("http","ws")}//${loc.host}/ws/${name}`),
    app: new Application(),
    pieces: new Map()
  }
  tech.ws.onopen  = _ => console.log(`- websocket opened -`)
  tech.ws.onerror = _ => { console.log(`- websocket error -`); location.href = "notfound.html" + loc.search }
  tech.ws.onclose = _ => console.log(`- websocket closed -`)
  tech.ws.onmessage = receiveSetupMessages(ches, tech)
}

/**
 * @param {Ches} ches
 * @param {Tech} tech
 * @returns {(event: MessageEvent<string>) => void}
 */
function receiveSetupMessages(ches, tech) { return async event => {
  const [command, arg1, arg2] = event.data.split(" ")
  console.log(`Setup: ${command} ${arg1} ${arg2}`)
  switch (command) {
    case "boardsize": // boardsize: <cols> <rows>
      ches.size.cols = parseInt(arg1)
      ches.size.rows = parseInt(arg2)
      break
    case "clock": // clock: <milliseconds> <stopped|running>
      const running = arg2 === "running"
      ches.clock = {
        start: running ? Date.now() - parseInt(arg1) : null,
        offset: running ? null : parseInt(arg1)
      }
      break
    case "freeze": // freeze: <milliseconds>
      ches.freeze = parseInt(arg1)
      break
    default:
      console.warn(`Unknown setup command: ${command}`)
  }
  if (ches.size.cols > 0 && ches.size.rows > 0 && (ches.clock.offset != null || ches.clock.start != null) && ches.freeze >= 0) {
    console.log(`- setup complete -`)
    console.debug(`Data after setup: ${JSON.stringify(ches)}`)
    await initializeGraphics(ches, tech)
    tech.ws.onmessage = receiveGameMessages(ches, tech)
  }
}}

/**
 * @param {Ches} ches
 * @param {Tech} tech
 * @returns {Promise<void>}
 */
async function initializeGraphics(ches, tech) {
  console.log(`- initialize graphics -`)

  // Create the Pixi Application for the chess board
  const htmlContainer = document.getElementById('gameContainer') || document.body
  await tech.app.init({ background: '#1099bb', resizeTo: htmlContainer })
  htmlContainer.replaceChildren(tech.app.canvas)
 
  // Render the chess board background to make it event sensitive
  const chessBoard = new Graphics({})
  chessBoard.rect(0, 0, ches.size.cols, ches.size.rows)
  chessBoard.fill(0x282020)
  // Add the checkers
  for (var x = 0; x < ches.size.cols; x++)
    for (var y = x%2; y < ches.size.rows; y += 2)
      chessBoard.rect(x, y, 1, 1)
  chessBoard.fill(0xa0a0a0)
  tech.app.stage.addChild(chessBoard)

  resize(ches, tech, chessBoard)

  tech.app.stage.interactive = true
  tech.app.stage.on('pointerdown', (event) => {
    const pos = event.data.getLocalPosition(chessBoard);
    const x = ches.white ? Math.floor(pos.x) : ches.size.cols - Math.ceil(pos.x);
    const y = ches.white ? ches.size.rows - Math.ceil(pos.y) : Math.floor(pos.y);
    console.log(`Pointer down at: ${x}, ${y}`);
  });

  // FIXME demo code, remove soon
  // add board to container, resize container
  // possibly invert coordinats like: container.scale.y = -1
  const mySprite = new Sprite(Assets.get("Q"));
  mySprite.setSize(1, 1);
  mySprite.x = 0
  mySprite.y = 7
  chessBoard.addChild(mySprite);
  chessBoard.scale.x = -1;
}

/**
 * @param {Ches} ches
 * @param {Tech} tech
 * @param {Graphics} chessBoard
 * @returns {void}
 */
function resize(ches, tech, chessBoard) {
  const xBound = tech.app.canvas.width * 0.9
  const yBound = tech.app.canvas.height * 0.9
  const scale = Math.min(xBound / ches.size.cols, yBound / ches.size.rows)
  chessBoard.x = (tech.app.canvas.width / scale - ches.size.cols) / 2
  chessBoard.y = (tech.app.canvas.height / scale - ches.size.rows) / 2
  tech.app.stage.scale.set(scale)
}

/**
 * @param {Ches} ches
 * @param {Tech} tech
 * @returns {(event: MessageEvent<string>) => void}
 */
function receiveGameMessages(ches, tech) { return event => {
  const [command, arg] = event.data.split(/ (.+)/)
  console.debug(`Game: ${command} - ${arg}`)

  switch(command) {
    case "add":
      const [loc, piece, freeze] = arg.split(" ")
      console.log(`Adding piece ${piece} at ${loc} with freeze ${freeze}`)
      break
    default:
      console.warn(`Unknown game command: ${command}`)
  }
}}

// async function init() {
        //   // Create a new application
        //   const app = new PIXI.Application();

        //   // the containing element for the application
        //   const htmlContainer = document.getElementById('gameContainer')

        //   // Initialize the application
        //   await app.init({ background: '#1099bb', resizeTo: htmlContainer });

        //   // Append the application canvas to the document body
        //   htmlContainer.appendChild(app.canvas);

//   // Create and add a container to the stage
//   const container = new PIXI.Container();

//   app.stage.addChild(container);

//   // Load the bunny texture
//   const texture = await PIXI.Assets.load('https://pixijs.com/assets/bunny.png');

//   // Create a 5x5 grid of bunnies in the container
//   for (let i = 0; i < 25; i++) {
//     const bunny = new PIXI.Sprite(texture);

//     bunny.x = (i % 5) * 40;
//     bunny.y = Math.floor(i / 5) * 40;
//     container.addChild(bunny);
//   }

//   // Move the container to the center
//   container.x = app.screen.width / 2;
//   container.y = app.screen.height / 2;

//   // Center the bunny sprites in local container coordinates
//   container.pivot.x = container.width / 2;
//   container.pivot.y = container.height / 2;

//   // Listen for animate update
//   app.ticker.add((time) => {
//     // Continuously rotate the container!
//     // * use delta to create frame-independent transform *
//     container.rotation -= 0.01 * time.deltaTime;
//   });
// }
