// @ts-check

export class GameClock {
  /** @type {number} */
  #millisPerTick
  
  /** @type {number} */
  #offsetToUnixTime
  
  /** @type {number | undefined} */
  #stoppedAt

  /**
   * @param {number} timeMillis
   * @param {number} millisPerTick
   * @param {boolean} stopped
   */
  constructor(timeMillis, millisPerTick, stopped) {
    this.#millisPerTick = millisPerTick
    this.#offsetToUnixTime = Date.now() - timeMillis
    if (stopped) this.#stoppedAt = this.#offsetToUnixTime + timeMillis
  }

  stop() {
    if (this.#stoppedAt === undefined) this.#stoppedAt = Date.now()
  }

  start() {
    if (this.#stoppedAt !== undefined) {
      this.#offsetToUnixTime += Date.now() - this.#stoppedAt
      this.#stoppedAt = undefined
    }
  }

  get millisPerTick() {
    return this.#millisPerTick
  }

  get timeMillis() {
    if (this.#stoppedAt === undefined) return Date.now() - this.#offsetToUnixTime
    else return this.#stoppedAt - this.#offsetToUnixTime
  }

  get time() {
    return Math.floor(this.timeMillis / this.#millisPerTick)
  }

  /**
   * @param {number} gameTime
   * @returns {number} milliseconds until the given game time
   */
  millisUntil(gameTime) {
    return gameTime * this.#millisPerTick - this.timeMillis
  }
}