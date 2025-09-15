package xchess

import sourcecode.{File, Line, Text}

object ServerLogger extends cask.Logger:
  override def exception(t: Throwable): Unit = t.printStackTrace()
  override def debug(t: Text[Any])(implicit f: File, line: Line): Unit = {}
