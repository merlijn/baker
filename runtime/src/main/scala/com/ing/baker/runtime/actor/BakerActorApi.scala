package com.ing.baker.runtime.actor

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.stream.ActorMaterializer
import com.ing.baker.runtime.actor.process_index.ProcessIndex.ActorMetadata
import com.ing.baker.runtime.actor.serialization.Encryption
import com.ing.baker.runtime.actor.serialization.Encryption.NoEncryption
import com.ing.baker.runtime.core.internal.InteractionManager
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration


trait BakerActorApi extends Extension {

  val interactionManager: InteractionManager = new InteractionManager()

  val configuredEncryption: Encryption

  val recipeManagerActor: ActorRef

  val processIndexActor: ActorRef

  def getIndex(implicit timeout: FiniteDuration): Seq[ActorMetadata]
}

object BakerActorApiExtension
  extends ExtensionId[BakerActorApi] with ExtensionIdProvider {
  //The lookup method is required by ExtensionIdProvider,
  // so we return ourselves here, this allows us
  // to configure our extension to be loaded when
  // the ActorSystem starts up
  override def lookup = BakerActorApiExtension

  //This method will be called by Akka
  // to instantiate our Extension
  override def createExtension(extendedSystem: ExtendedActorSystem): BakerActorApi = {

    val config = extendedSystem.settings.config

    implicit val actorSystem: ActorSystem = extendedSystem
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val configuredEncryption: Encryption = {
      if (config.as[Boolean]("baker.encryption.enabled")) {
        new Encryption.AESEncryption(config.getString("baker.encryption.secret"))
      } else {
        NoEncryption
      }
    }

    config.as[Option[String]]("akka.actor.provider") match {
      case Some("local" | "akka.actor.LocalActorRefProvider")       => new LocalBakerActorApi(config, configuredEncryption)
      case Some("cluster" | "akka.cluster.ClusterActorRefProvider") => new ClusterBakerActorApi(config, configuredEncryption)
      case other                                                    => throw new IllegalArgumentException(s"Unsupported actor provider: $other")
    }
  }

  /**
    * Java API: retrieve the Count extension for the given system.
    */
  override def get(system: ActorSystem): BakerActorApi = super.get(system)
}