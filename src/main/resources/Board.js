// @ts-check

export class Board {
  /** @type {Map<string, [string, number]>} */ // FIXME consider removing the freezeUntil, it's not needed?
  #squares

  constructor() {
    this.#squares = new Map()
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {[string, number] | undefined} [piece, freezeUntil] or undefined if empty
   */
  get(square) {
    return this.#squares.get(square)
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @param {string} piece - the piece to place (e.g., "K", "q")
   * @param {number} freezeUntil - game time until which this square is frozen
   */
  set(square, piece, freezeUntil) {
    this.#squares.set(square, [piece, freezeUntil])
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {boolean} true if the square existed and was deleted
   */
  delete(square) {
    return this.#squares.delete(square)
  }
}
