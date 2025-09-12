package xchess.util

class InterleaveTest extends munit.FunSuite:
  
  test("interleave two equal-length iterables") {
    val a = List(1, 3, 5)
    val b = List(2, 4, 6)
    val result = interleave(a, b).toList
    assertEquals(result, List(1, 2, 3, 4, 5, 6))
  }

  test("interleave first iterable longer") {
    val a = List(1, 3, 5, 7)
    val b = List(2, 4)
    val result = interleave(a, b).toList
    assertEquals(result, List(1, 2, 3, 4, 5, 7))
  }

  test("interleave second iterable longer") {
    val a = List(1, 3)
    val b = List(2, 4, 6, 8)
    val result = interleave(a, b).toList
    assertEquals(result, List(1, 2, 3, 4, 6, 8))
  }

  test("interleave with empty first iterable") {
    val a = List()
    val b = List(2, 4, 6)
    val result = interleave(a, b).toList
    assertEquals(result, List(2, 4, 6))
  }

  test("interleave with empty second iterable") {
    val a = List(1, 3, 5)
    val b = List()
    val result = interleave(a, b).toList
    assertEquals(result, List(1, 3, 5))
  }

  test("interleave two empty iterables") {
    val a = List()
    val b = List()
    val result = interleave(a, b).toList
    assertEquals(result, List())
  }
