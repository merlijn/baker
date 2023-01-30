package io.kagera.runtime

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import akka.util.Timeout

import scala.reflect.ClassTag

object Main {

  type Id[X] = X

  def processBehavior[S, E, Cmd[X]](processId: String, initialState: S, runtime: ProcessRuntime[S, E, Cmd]): Behavior[Cmd[_]] =

    Behaviors.setup[Cmd[_]] { context =>
      implicit val mat = Materializer(context)

      ProcessInstance.behavior(processId, initialState, runtime)
    }

  def main(args: Array[String]) = {

//    given timeout: Timeout = Timeout(5.seconds)
    type Arg = [A] =>> A

    val b = processBehavior("test-process-2", 0, CounterProcess)

    val system = ActorSystem(b, "kagera")

    system.tell(Inc(5))
  }
}
