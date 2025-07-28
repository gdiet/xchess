package xchess

object Server extends cask.MainRoutes:

  override def port = 7070

  @staticFilesWithIndex("/")
  def staticFiles() = "src/main/resources"

  initialize()
  println(s"xChess server started at port $port")
