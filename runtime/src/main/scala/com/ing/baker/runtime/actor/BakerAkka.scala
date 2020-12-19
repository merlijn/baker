package com.ing.baker.runtime.actor

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.stream.Materializer
import com.ing.baker.runtime.actor.process_index.ProcessIndex.ActorMetadata
import com.ing.baker.runtime.actor.serialization.Encryption
import com.ing.baker.runtime.actor.serialization.Encryption.NoEncryption
import com.ing.baker.runtime.core.internal.InteractionManager
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration


trait BakerAkka extends Extension {

  val interactionManager: InteractionManager = new InteractionManager()

  val configuredEncryption: Encryption

  /**
    * The recipe manager actor.
    */
  val recipeManagerActor: ActorRef

  /**
    * The process index actor.
    */
  val processIndexActor: ActorRef

  def getIndex(implicit timeout: FiniteDuration): Seq[ActorMetadata]
}

object BakerActorApiExtension
  extends ExtensionId[BakerAkka] with ExtensionIdProvider {
  //The lookup method is required by ExtensionIdProvider,
  // so we return ourselves here, this allows us
  // to configure our extension to be loaded when
  // the ActorSystem starts up
  override def lookup = BakerActorApiExtension

  //This method will be called by Akka
  // to instantiate our Extension
  override def createExtension(extendedSystem: ExtendedActorSystem): BakerAkka = {

    val config = extendedSystem.settings.config

    implicit val actorSystem: ActorSystem = extendedSystem

    val configuredEncryption: Encryption = {
      if (config.as[Boolean]("baker.encryption.enabled")) {
        new Encryption.AESEncryption(config.getString("baker.encryption.secret"))
      } else {
        NoEncryption
      }
    }

    config.as[Option[String]]("akka.actor.provider") match {
      case Some("local" | "akka.actor.LocalActorRefProvider")       => new BakerAkkaLocal(config, configuredEncryption)
      case Some("cluster" | "akka.cluster.ClusterActorRefProvider") => new BakerAkkaCluster(config, configuredEncryption)
      case other                                                    => throw new IllegalArgumentException(s"Unsupported actor provider: $other")
    }
  }

  /**
    * Java API: retrieve the Count extension for the given system.
    */
  override def get(system: ActorSystem): BakerAkka = super.get(system)
}