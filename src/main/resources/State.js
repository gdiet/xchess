// @ts-check

import { Clock } from './Clock.js'
import { Board } from './Board.js'
import { Container, Graphics } from './pixi/pixi.mjs'

export class State {
  /** @type {Clock} */
  #clock

  /** @type {number} - maximum column number on board, 7 on standard chess boards */
  #maxCol

  /** @type {number} - maximum row number on board, 7 on standard chess boards */
  #maxRow

  /** @type {Board} */
  #board

  /** @type {Container} */
  #boardContainer

  /**
   * @param {Clock} clock
   * @param {number} maxCol
   * @param {number} maxRow
   * @param {Container} boardContainer
   */
  constructor(clock, maxCol, maxRow, boardContainer) {
    this.#clock = clock
    this.#maxCol = maxCol
    this.#maxRow = maxRow
    this.#board = new Board()
    this.#boardContainer = boardContainer
  }

  get clock() { return this.#clock }
  get maxCol() { return this.#maxCol }
  get maxRow() { return this.#maxRow }
  get board() { return this.#board }
  get boardContainer() { return this.#boardContainer }
}
