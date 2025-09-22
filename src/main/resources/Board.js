// @ts-check

import { Sprite } from "./pixi/pixi.mjs"

export class Board {
  /** @type {Map<string, [Sprite, boolean]>} - square -> [sprite, isPawn] */
  #squares

  constructor() {
    this.#squares = new Map()
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {[Sprite, boolean] | undefined} [sprite, isPawn] or undefined if empty
   */
  get(square) {
    return this.#squares.get(square)
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @param {Sprite} sprite
   * @param {boolean} isPawn - true if the piece is a pawn
   */
  set(square, sprite, isPawn) {
    this.#squares.set(square, [sprite, isPawn])
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {boolean} true if the square existed and was deleted
   */
  delete(square) {
    return this.#squares.delete(square)
  }
}
