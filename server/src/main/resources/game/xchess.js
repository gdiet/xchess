// @ts-check
// For release, use the minified library pixi/pixi.min.mjs
import { Application, Assets, Container, Sprite } from './pixi/pixi.mjs';

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
 */

setup()

function setup() {
  const loc  = window.location
  const name = new URLSearchParams(loc.search).get("name") || "test"
  const ches = { 
    white: new URLSearchParams(loc.search).get("color") !== "black",
    size: { cols: -1, rows: -1 },
    clock: { start: null, offset: null },
    freeze: -1
  }
  const tech = { ws: new WebSocket(`${loc.protocol.replace("http","ws")}//${loc.host}/ws/${name}`) }
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
function receiveSetupMessages(ches, tech) { return event => {
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
    console.debug(`Setup data: ${JSON.stringify(ches)}`)
    initializeGraphics(ches, tech)
    tech.ws.onmessage = receiveGameMessages(ches, tech)
  }
}}

/**
 * @param {Ches} ches
 * @param {Tech} tech
 * @returns {void}
 */
function initializeGraphics(ches, tech) {
  new Application({width: 100, height: 100})
  // // Create the Pixi Application for the chess board
  // tech.app = new PIXI.Application({width: ches.size.cols * 100, height: ches.size.rows * 100})
  // const checkers = new PIXI.Graphics()
  // // Render the chess board background to make it event sensitive
  // checkers.beginFill(0x282020)
  // checkers.drawRect(0, 0, ches.size.cols * 100, ches.size.rows * 100)
  // checkers.endFill()
  // // Add the checkers
  // checkers.beginFill(0xa0a0a0)
  // for (var x = 0; x < ches.size.cols; x++)
  //   for (var y = ches.white ? x%2 : (x+1)%2; y < ches.size.rows; y += 2)
  //     checkers.drawRect(x * 100, y * 100, 100, 100)
  // checkers.endFill()
  // tech.app.stage.addChild(checkers)
  console.log(`- initialize graphics -`)
}

/**
 * @param {Ches} ches
 * @param {Tech} tech
 * @returns {(event: MessageEvent<string>) => void}
 */
function receiveGameMessages(ches, tech) { return event => {
  console.debug(`Game: ${event.data}`)
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
