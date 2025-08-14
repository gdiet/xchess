// @ts-check
setup()

async function setup() {
  const loc  = window.location
  const name = new URLSearchParams(loc.search).get("name")
  const ches = { color: new URLSearchParams(loc.search).get("color") }
  const tech = { ws: new WebSocket(`${loc.protocol.replace("http","ws")}//${loc.host}/ws/${name}`) }
  tech.ws.onopen  = _ => console.log(`Websocket opened.`)
  tech.ws.onerror = _ => { console.log(`Websocket error.`) } // location.href = "/" }
  tech.ws.onclose = _ => console.log(`Websocket closed.`)
  tech.ws.onmessage = (event) => console.log(event.data)
  console.log(window.location)
}

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
