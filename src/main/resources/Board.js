// @ts-check

export class Board {
  /** @type {Map<string, string>} - square -> piece */
  #squares

  constructor() {
    this.#squares = new Map()
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {string | undefined} piece or undefined if empty
   */
  get(square) {
    return this.#squares.get(square)
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @param {string} piece - the piece to place (e.g., "K", "q")
   */
  set(square, piece) {
    this.#squares.set(square, piece)
  }

  /**
   * @param {string} square - chess square (e.g., "A1")
   * @returns {boolean} true if the square existed and was deleted
   */
  delete(square) {
    return this.#squares.delete(square)
  }
}
