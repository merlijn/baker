package io.kagera.runtime

import cats.effect.IO
import com.ing.baker.graph.api.Graph

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class ProcessTask[E](delay: Option[FiniteDuration], work: IO[Option[E]])

case class RetryWithDelay(delay: FiniteDuration)
case class Proceed[E](output: Option[E])
case object HaltProcess

case class Ack(id: String)

trait Job[E] {
  
  def execute(): IO[Option[E]]

  def failureHandler(e: Exception, failureCount: Int): RetryWithDelay | Proceed[E] | HaltProcess.type
}

object Job {

  def apply[E](value: Option[E]) = JobImpl(IO.pure(value))
}

case class JobImpl[E](io: IO[Option[E]]) extends Job[E] {
  override def execute(): IO[Option[E]] = io
  override def failureHandler(e: Exception, failureCount: Int) = HaltProcess
}

trait ProcessRuntime[S, E, Cmd[Resp]] {

  def eventSource(state: S, event: E): S

  def step(state: S): Seq[Job[E]]

  // must do nothing else then compute the job deterministically 
  def receive[Resp](state: S, msg: Cmd[Resp]): Job[E]
}

trait ProcessRef[S, E, Cmd[Resp]] {

  def getState: Future[S]

  def sendCommand[Resp](cmd: Cmd[Resp]): Unit

  def sendCommand[Resp](cmd: Cmd[Resp], id: String): Unit
}

