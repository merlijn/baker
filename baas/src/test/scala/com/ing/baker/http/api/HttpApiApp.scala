package com.ing.baker.http.api

import akka.actor.ActorSystem
import com.ing.baker.http.KryoUtil
import com.ing.baker.runtime.core.Baker

import scala.concurrent.Await
import scala.concurrent.duration._

object HttpApiApp extends App {

  val host = "localhost"
  val port = 8081
  val actorSystem = ActorSystem("BakerHttp")
  val baker = new Baker()(actorSystem)

  //Startup the BAASAPI
  val baasAPI = new BakerDirectives(baker, host, port)(actorSystem)
  Await.result(baasAPI.start(), 10 seconds)
}
