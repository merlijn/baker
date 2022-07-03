package io.kagera.runtime

import cats.effect.IO

sealed trait CounterEvent
case class Incremented(amount: Int) extends CounterEvent
case class Decremented(amount: Int) extends CounterEvent

sealed trait CounterCmd
case class Inc(amount: Int) extends CounterCmd
case class Dec(amount: Int) extends CounterCmd

object CounterProcess extends ProcessRuntime[Int, CounterEvent, CounterCmd] {

  override def eventSource(state: Int, event: CounterEvent): Int = event match {
    case Incremented(n) => state + n
    case Decremented(n) => state - n
  }

  override def step(state: Int): Seq[Job[CounterEvent]] = Seq.empty

  override def receive(state: Int, msg: CounterCmd): Job[CounterEvent] = msg match {
    case Inc(n) => Job(Some(Incremented(n)))
    case Dec(n) => Job(Some(Decremented(n)))
  }
}
