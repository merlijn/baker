package io.kagera.runtime

import akka.actor.typed.{ActorRef, Behavior, Scheduler}
import akka.persistence.typed.{PersistenceId, RecoveryCompleted}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.Scheduler
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import cats.effect.IO
import org.slf4j.LoggerFactory
import akka.util.Timeout

import concurrent.duration.DurationInt
import java.util.UUID
import scala.concurrent.ExecutionContext
import cats.effect.unsafe.implicits.global

import scala.reflect.ClassTag
import scala.util.{ Success, Failure}

object ProcessInstance {

  val logger = LoggerFactory.getLogger("ProcessInstance")

  def behavior[S, E, Cmd[X], Resp](
    processId: String,
    initialState: S,
    runtime: ProcessRuntime[S, E, Cmd]): Behavior[Cmd[Resp]] = {

    Behaviors.setup[Either[JobCompleted[E], Cmd[Resp]]] { context =>

      given scheduler: Scheduler = context.system.scheduler
      val jobExecutor: ActorRef[ExecuteJob[E]] = context.spawn(jobHandler[E](), "job-executor")

      EventSourcedBehavior.apply[Either[JobCompleted[E], Cmd[Resp]], E, S](
        persistenceId  = PersistenceId.ofUniqueId(processId),
        emptyState     = initialState,
        commandHandler = processHandler(runtime, jobExecutor, context),
        eventHandler   = runtime.eventSource,
      ).receiveSignal {
        case (state, RecoveryCompleted) =>
          logger.info("recovered")
      }
    }.transformMessages[Cmd[Resp]](cmd => Right(cmd))
  }

  sealed trait ControlMsg
  case class Ack(id: String) extends ControlMsg

  def processHandler[S, E, Cmd[X], Resp](
     runtime: ProcessRuntime[S, E, Cmd],
     jobExecutor: ActorRef[ExecuteJob[E]],
     context: ActorContext[Either[JobCompleted[E], Cmd[Resp]]])(using ec: Scheduler): (S, Either[JobCompleted[E], Cmd[Resp]]) => Effect[E, S] = (state, msg) => {

    given timeout: Timeout = Timeout(5.seconds)

    val receiveJobCompleted = context.messageAdapter[JobCompleted[E]](Left(_))

    msg match {
      case Left(completed) =>
        logger.info(s"persisting event: ${completed.result}")
        completed.result match {
          case None        => Effect.none
          case Some(event) => Effect.persist[E, S](event)
        }

      case Right(cmd) =>
        val job = runtime.receive[Resp](state, cmd)

        logger.info(s"received command: $cmd")

        jobExecutor.tell(ExecuteJob(job, receiveJobCompleted))

        Effect.none
    }
  }


  // --- Job executor

  case class ExecuteJob[E](job: Job[E], sender: ActorRef[JobCompleted[E]])
  case class JobCompleted[E](result: Option[E])

  def jobHandler[E](): Behaviors.Receive[ExecuteJob[E]] =
    Behaviors.receive[ExecuteJob[E]] { (context, msg) =>

      val io = msg.job.execute()
      val e = io.unsafeRunSync()

      msg.sender.tell(JobCompleted(e))

      Behaviors.same
    }
}
