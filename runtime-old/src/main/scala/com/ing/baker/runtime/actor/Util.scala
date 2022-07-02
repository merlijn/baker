package com.ing.baker.runtime.actor

import akka.event.DiagnosticLoggingAdapter
import akka.event.Logging.LogLevel

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.JavaConverters.*
import scala.concurrent.*
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.{Failure, Success}

object Util {

  private val sequenceTimeoutExtra = 10 seconds

  def collectFuturesWithin[T, M[X] <: scala.Iterable[X]](futures: M[Future[T]], timeout: FiniteDuration, scheduler: akka.actor.Scheduler)(implicit ec: ExecutionContext): Seq[T] = {

    val size = futures.iterator.size
    val queue = new LinkedBlockingQueue[T](size)
    val counter = new AtomicInteger(0)
    val promise = Promise[List[T]]()

    def completePromise() = promise.trySuccess(queue.iterator().asScala.toList)

    futures.foreach { _.onComplete {
        case Success(result) =>
          queue.put(result)
          if (counter.incrementAndGet() == size)
            completePromise()
        case Failure(_) =>
          if (counter.incrementAndGet() == size)
            completePromise()
      }
    }

    scheduler.scheduleOnce(timeout) {
      completePromise()
    }

    Await.result(promise.future, timeout + sequenceTimeoutExtra)
  }

  object logging {

    implicit class DiagnosticLoggingAdapterFns(log: DiagnosticLoggingAdapter) {

      def errorWithMDC(msg: String, mdc: Map[String, Any], cause: Throwable) = {
        try {
          log.setMDC(mdc.asJava)
          log.error(cause, msg)
        } finally {
          log.clearMDC()
        }
      }

      def logWithMDC(level: LogLevel, msg: String, mdc: Map[String, Any]) = {
        try {
          log.setMDC(mdc.asJava)
          log.log(level, msg)
        } finally {
          log.clearMDC()
        }
      }
    }

  }
}