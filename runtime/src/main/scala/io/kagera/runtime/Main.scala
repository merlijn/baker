package io.kagera.runtime

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.util.Timeout

import scala.reflect.ClassTag

object Main {

  def processBehavior[S, E, Cmd : ClassTag](processId: String, initialState: S, runtime: ProcessRuntime[S, E, Cmd]): Behavior[Cmd] =
    Behaviors.setup[Cmd] { context =>
      implicit val mat = Materializer(context)

      ProcessInstance.behavior(processId, initialState, runtime)
    }

  def main(args: Array[String]) = {

//    given timeout: Timeout = Timeout(5.seconds)

    val system = ActorSystem(processBehavior("test-process-2", 0, CounterProcess), "kagera")

    system.tell(Inc(5))
  }
}
