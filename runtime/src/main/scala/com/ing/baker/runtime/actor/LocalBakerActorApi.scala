package com.ing.baker.runtime.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import com.ing.baker.runtime.actor.process_index.ProcessIndex
import com.ing.baker.runtime.actor.process_index.ProcessIndex.{ActorMetadata, CheckForProcessesToBeDeleted}
import com.ing.baker.runtime.actor.process_index.ProcessIndexProtocol.{GetIndex, Index}
import com.ing.baker.runtime.actor.recipe_manager.RecipeManager
import com.ing.baker.runtime.actor.serialization.Encryption
import com.ing.baker.runtime.core.internal.InteractionManager
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Await
import scala.concurrent.duration._

class LocalBakerActorApi(config: Config, override val configuredEncryption: Encryption)(implicit actorSystem: ActorSystem, materializer: Materializer) extends BakerActorApi {

  private val retentionCheckInterval = config.as[FiniteDuration]("baker.actor.retention-check-interval")
  val actorIdleTimeout: Option[FiniteDuration] = config.as[Option[FiniteDuration]]("baker.actor.idle-timeout")

  override val recipeManagerActor: ActorRef = {
    actorSystem.actorOf(RecipeManager.props())
  }

  override val processIndexActor: ActorRef = {
    actorSystem.actorOf(
    ProcessIndex.props(actorIdleTimeout, Some(retentionCheckInterval), configuredEncryption, interactionManager, recipeManagerActor))
  }

  override def getIndex(implicit timeout: FiniteDuration): Seq[ActorMetadata] = {

    import akka.pattern.ask
    import actorSystem.dispatcher
    implicit val akkaTimeout: akka.util.Timeout = timeout

    val future = processIndexActor.ask(GetIndex).mapTo[Index].map(_.entries)

    Await.result(future, timeout)
  }
}

