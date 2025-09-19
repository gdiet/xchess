// @ts-check

export class Board {
  /** @type {Map<string, [string, number]>} */
  #squares

  constructor() {
    this.#squares = new Map()
  }

  /**
   * @param {number} col - column (0-based)
   * @param {number} row - row (0-based)
   * @returns {[string, number] | undefined} [piece, freezeUntil] or undefined if empty
   */
  get(col, row) {
    return this.#squares.get(`${col},${row}`)
  }

  /**
   * @param {number} col - column (0-based)
   * @param {number} row - row (0-based)
   * @param {string} piece - the piece to place (e.g., "K", "q")
   * @param {number} freezeUntil - game time until which this square is frozen
   */
  set(col, row, piece, freezeUntil) {
    this.#squares.set(`${col},${row}`, [piece, freezeUntil])
  }

  /**
   * @param {number} col - column (0-based)
   * @param {number} row - row (0-based)
   * @returns {boolean} true if the square existed and was deleted
   */
  delete(col, row) {
    return this.#squares.delete(`${col},${row}`)
  }
}
