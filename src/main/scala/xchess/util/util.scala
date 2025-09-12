package xchess.util

def interleave[A](a: Iterable[A], b: Iterable[A]): Iterator[A] =
  interleave(a.iterator, b.iterator)

def interleave[A](a: Iterator[A], b: Iterator[A]): Iterator[A] =
  var aFirst = true
  new Iterator[A]:
    def hasNext: Boolean = a.hasNext || b.hasNext
    def next(): A =
      aFirst = !aFirst
      if (aFirst && b.hasNext) b.next()
      else if (a.hasNext) a.next()
      else if (!aFirst && b.hasNext) b.next()
      else throw new NoSuchElementException("No more elements in interleaved iterator")
