package com.ing.baker.runtime.core

import java.time.Duration
import java.util.concurrent.{CompletableFuture, TimeoutException}

import akka.NotUsed
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Broadcast, GraphDSL, Sink, Source}
import akka.stream.{ClosedShape, Materializer}
import com.ing.baker.runtime.actor.process_index.ProcessIndexProtocol
import com.ing.baker.runtime.actor.process_instance.ProcessInstanceProtocol

import scala.compat.java8.FutureConverters
import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{Await, ExecutionContext, Future}

object SensoryEventResponse {

  case class CompletedResponse(sensoryEventStatus: SensoryEventStatus, events: Seq[ProcessEvent])

  private def parseFirstMessage(processId: String, response: Future[Any])(implicit ec: ExecutionContext): Future[SensoryEventStatus] =
    response.map(translateFirstMessage)

  private def translateFirstMessage(msg: Any): SensoryEventStatus = msg match {
    case _: ProcessInstanceProtocol.TransitionFired => SensoryEventStatus.Received
    case _: ProcessInstanceProtocol.TransitionNotEnabled => SensoryEventStatus.FiringLimitMet
    case _: ProcessInstanceProtocol.AlreadyReceived => SensoryEventStatus.AlreadyReceived
    case ProcessIndexProtocol.NoSuchProcess(processId) => throw new NoSuchProcessException(s"No such process: $processId")
    case ProcessIndexProtocol.ReceivePeriodExpired(_) => SensoryEventStatus.ReceivePeriodExpired
    case ProcessIndexProtocol.ProcessDeleted(_) => SensoryEventStatus.ProcessDeleted
    case ProcessIndexProtocol.InvalidEvent(_, invalidEventMessage) => throw new IllegalArgumentException(invalidEventMessage)
    case msg @_ => throw new IllegalStateException(s"Received unexpected message of type: ${msg.getClass}")
  }

  private def parseRemainingMessages(processId: String, response: Future[Seq[Any]])(implicit ec: ExecutionContext): Future[CompletedResponse] =
    response.map { msgs =>

      val sensoryEventStatus = msgs.headOption.map(translateFirstMessage).map {
        case SensoryEventStatus.Received => SensoryEventStatus.Received // If the first message is success, then it means we have all the events completed
        case other => other
      }
        .getOrElse(throw new NoSuchProcessException(s"No such process: $processId"))

      val events: Seq[ProcessEvent] = msgs.flatMap {
        case fired: ProcessInstanceProtocol.TransitionFired => Option(fired.output.asInstanceOf[ProcessEvent])
        case _ => None
      }

      CompletedResponse(sensoryEventStatus, events)
    }

  private def createFlow(processId: String, source: Source[Any, NotUsed])(implicit materializer: Materializer, ec: ExecutionContext):
                                                              (Future[SensoryEventStatus], Future[CompletedResponse]) = {

    val sinkHead = Sink.head[Any]
    val sinkLast = Sink.seq[Any]

    val graph = RunnableGraph.fromGraph(GraphDSL.create(sinkHead, sinkLast)((_, _)) {
      implicit b =>
        (head, last) => {
          import GraphDSL.Implicits._

          val bcast = b.add(Broadcast[Any](2))
          source ~> bcast.in
          bcast.out(0) ~> head.in
          bcast.out(1) ~> last.in
          ClosedShape
        }
    })

    val (firstResponse, allResponses) = graph.run(materializer)

    (parseFirstMessage(processId, firstResponse), parseRemainingMessages(processId, allResponses))
  }
}

class SensoryEventResponse(processId: String, source: Source[Any, NotUsed])(implicit materializer: Materializer, ec: ExecutionContext) {

  val (receivedFuture, completedFuture) = SensoryEventResponse.createFlow(processId, source)

  val defaultWaitTimeout: FiniteDuration = FiniteDuration.apply(10, SECONDS)

  def completedFutureJava: CompletableFuture[SensoryEventResponse.CompletedResponse] =
    FutureConverters.toJava(completedFuture).toCompletableFuture

  def receivedFutureJava: CompletableFuture[SensoryEventStatus] =
    FutureConverters.toJava(receivedFuture).toCompletableFuture

  @throws[TimeoutException]("When the request does not receive a reply within the given deadline")
  def confirmReceived(): SensoryEventStatus = {
    confirmReceived(defaultWaitTimeout)
  }

  @throws[TimeoutException]("When the request does not receive a reply within the given deadline")
  def confirmReceived(duration: Duration): SensoryEventStatus = {
    confirmReceived(duration.toScala)
  }

  @throws[TimeoutException]("When the request does not receive a reply within the given deadline")
  def confirmReceived(implicit timeout: FiniteDuration): SensoryEventStatus = {
    Await.result(receivedFuture, timeout)
  }

  @throws[TimeoutException]("When the request does not receive a reply within the given deadline")
  def confirmCompleted(): SensoryEventStatus = {
    confirmCompleted(defaultWaitTimeout)
  }

  @throws[TimeoutException]("When the request does not receive a reply within the given deadline")
  def confirmCompleted(duration: Duration): SensoryEventStatus = {
    confirmCompleted(duration.toScala)
  }

  @throws[TimeoutException]("When the request does not receive a reply within the given deadline")
  def confirmCompleted(implicit timeout: FiniteDuration): SensoryEventStatus = {
    Await.result(completedFuture, timeout).sensoryEventStatus
  }

  /**
    * Waits for all events that where caused by the sensory event input.
    *
    * !!! Note that this will return an empty list if the sensory event was rejected
    *
    * Therefor you are encouraged to first confirm that the event was properly received before checking this list.
    *
    * Example:
    *
    * {{{
    * val response = baker.processEvent(someEvent);
    *
    * response.confirmReceived() match {
    *   case Received =>
    *
    *     response.confirmAllEvents()
    *
    *   case ReceivePeriodExpired =>
    *   case AlreadyReceived =>
    *   case ProcessDeleted =>
    *   case FiringLimitMet =>
    *   case AlreadyReceived =>
    * }
    * }}}
    *
    * @param timeout The duration to wait for completion
    * @return
    */
  @throws[TimeoutException]("When the request does not receive a reply within the given deadline")
  def confirmAllEvents(implicit timeout: FiniteDuration): Seq[ProcessEvent] = {
    Await.result(completedFuture, timeout).events
  }
}
