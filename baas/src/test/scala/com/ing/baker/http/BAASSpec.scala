package com.ing.baker.http

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.ing.baker.http.api.BakerDirectives
import com.ing.baker.http.client.RemoteBaker
import com.ing.baker.recipe.TestRecipe.InitialEvent
import com.ing.baker.runtime.core.{Baker, ProcessState, SensoryEventStatus}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class BAASSpec extends TestKit(ActorSystem("BAASSpec")) with WordSpecLike with Matchers with BeforeAndAfterAll {
  def actorSystemName: String = "BAASSpec"

  val host = "localhost"
  val port = 8081

//  Startup a empty BAAS cluster
  val baker = new Baker()(system)
  val baasAPI: BakerDirectives = new BakerDirectives(baker, host, port)(system)
  Await.result(baasAPI.start(), 10 seconds)

  //Start a BAAS API
  val baasClient: RemoteBaker = new RemoteBaker(host, port)

  "Happy flow simple recipe BAAS" ignore {

    val recipeName = "simpleRecipe" + UUID.randomUUID().toString

//    val recipe = ???

//    val recipeId = baasClient.addRecipe(recipe)

    val requestId = UUID.randomUUID().toString

//    baasClient.createProcessInstance(recipeId, requestId)

    val sensoryEventStatusResponse: SensoryEventStatus =
      baasClient.fireEvent(requestId, InitialEvent("initialIngredient"))

    sensoryEventStatusResponse shouldBe SensoryEventStatus.OK

    val processState: ProcessState = baasClient.getState(requestId)

    processState.ingredients.keys should contain("initialIngredient")
    processState.ingredients.keys should contain("interactionOneIngredient")

    val events = baasClient.getEvents(requestId)

    println(s"events: $events")

//    println(s"procesState : ${requestState.processState}")
//    println(s"visualState : ${requestState.visualState}")
  }

  override def afterAll() = shutdown(system)
}
