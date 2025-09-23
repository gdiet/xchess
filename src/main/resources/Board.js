// @ts-check

import { Graphics, Sprite } from "./pixi/pixi.mjs"

export class Field {
  /**
   * @param {Sprite} sprite
   * @param {boolean} isPawn - true if the piece is a pawn
   */
  constructor(sprite, isPawn) {
    this.sprite = sprite
    this.isPawn = isPawn
  }

  /** @type {Sprite} */
  sprite

  /** @type {boolean} */
  isPawn

  /** @type {Graphics | undefined} */
  plan
}

export class Board {
  /** @type {Map<string, Field>} - square -> Field */
  #squares

  constructor() {
    this.#squares = new Map()
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {Field | undefined}
   */
  get(square) {
    return this.#squares.get(square)
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {Field}
   */
  at(square) {
    return this.#squares.get(square) || (() => { throw new Error(`no piece at ${square}`) })()
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @param {Field} field
   */
  set(square, field) {
    this.#squares.set(square, field)
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {boolean} true if the square existed and was deleted
   */
  delete(square) {
    return this.#squares.delete(square)
  }
}
