package com.ing.baker.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.ing.baker.http.KryoUtil
import com.ing.baker.http.KryoUtil.defaultKryoPool
import com.ing.baker.http.client.ClientUtils._
import com.ing.baker.recipe.javadsl.Recipe
import com.ing.baker.runtime.core.{ProcessEvent, ProcessState, SensoryEventStatus}
import com.ing.baker.types.Value
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class RemoteBaker(val host: String, val port: Int)(implicit val actorSystem: ActorSystem) {

  val baseUri = s"http://$host:$port"

  implicit val materializer = ActorMaterializer()

  def logEntity = (entity: ResponseEntity) =>
    entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
      log.info("Got response body: " + body.utf8String)
    }

  val log = LoggerFactory.getLogger(classOf[RemoteBaker])
  implicit val requestTimeout: FiniteDuration = 30 seconds

  def addRecipe(recipe: Recipe) : String = {

    val serializedRecipe = KryoUtil.serialize(recipe)

    val httpRequest = HttpRequest(
        uri = baseUri +  "/recipe",
        method = akka.http.scaladsl.model.HttpMethods.POST,
        entity = ByteString.fromArray(serializedRecipe))

    doRequestAndParseResponse[String](httpRequest)
  }

  def fireEvent(requestId: String, event: Any): SensoryEventStatus = {

    //Create request to give to Baker
    log.info("Creating runtime event to fire")
    val processEvent = ProcessEvent.of(event)

    val request = HttpRequest(
        uri =  s"$baseUri/$requestId/fire-event?confirm=completed",
        method = POST,
        entity = ByteString.fromArray(defaultKryoPool.toBytesWithClass(processEvent)))

    doRequestAndParseResponse[SensoryEventStatus](request)
  }

  def createProcessInstance(recipeId: String, requestId: String): Unit = {

    val request = HttpRequest(
        uri = s"$baseUri/$requestId/$recipeId/create-process",
        method = POST)

    doRequestAndParseResponse[String](request)
  }

  def getState(requestId: String): ProcessState = {

    val request = HttpRequest(
        uri = s"$baseUri/$requestId/state",
        method = GET)

    doRequestAndParseResponse[ProcessState](request)
  }

  def getIngredients(requestId: String): Map[String, Value] = getState(requestId).ingredients

  def getVisualState(requestId: String): String = {

    val request = HttpRequest(
      uri = s"$baseUri/$requestId/visual_state",
      method = GET)

    doRequestAndParseResponse[String](request)
  }

  def getEvents(requestId: String): List[ProcessEvent] = {

    val request = HttpRequest(
      uri = s"$baseUri/$requestId/events",
      method = GET)

    doRequestAndParseResponse[List[ProcessEvent]](request)
  }
}
