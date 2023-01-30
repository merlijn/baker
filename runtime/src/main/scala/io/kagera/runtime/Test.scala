package io.kagera.runtime

import scala.collection.mutable.ArrayBuffer

object Test {
  trait Service[Req, Resp[T]] {
    def ask(req: Req): Resp[Req]
  }

  case class Ping(msg: String)
  case class Pong(msg: String)

  type Request = Ping | Pong

  type Response[Req] = Req match {
    case Ping => Pong
    case Pong => Ping
  }

  trait PingPongService extends Service[Request, Response] {
    override def ask(req: Request): Response[Request] = req match {
      case p: Ping => Pong(p.msg)
      case p: Pong => Ping(p.msg)
    }
  }
}

object Builder {

  class Routes:
    val rows = new ArrayBuffer[Row]
    def add(r: Row): Unit = rows += r
    override def toString = rows.mkString("Table(", ", ", ")")

  class Row:
    val cells = new ArrayBuffer[Cell]
    def add(c: Cell): Unit = cells += c
    override def toString = cells.mkString("Row(", ", ", ")")

  case class Cell(elem: String)

  def route(init: Routes ?=> Unit) =
    given t: Routes = Routes()
    init
    t

  def get(path: String)(init: Row ?=> Unit)(using t: Routes) =
    given r: Row = Row()
    init
    t.add(r)

  def ok(str: String)(using r: Row) =
    r.add(new Cell(str))

  class Record(elems: (String, Any)*) extends Selectable:
    private val fields = elems.toMap
    def selectDynamic(name: String): Any = fields(name)

  type Person = Record { val name: String; val age: Int }

  route {

    get("/api/foo") {
      ok("top left")
    }

    get("/api/bar") {
      ok("bottom left")
    }
  }
}
