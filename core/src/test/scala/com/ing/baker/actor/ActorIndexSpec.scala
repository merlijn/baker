package com.ing.baker.actor

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import io.kagera.akka.actor.PetriNetInstanceProtocol
import io.kagera.akka.actor.PetriNetInstanceProtocol.{AlreadyInitialized, Initialize, Uninitialized}
import io.kagera.api.colored.Marking
import org.mockito
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.scalatest.concurrent.Eventually
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

//noinspection TypeAnnotation
class ActorIndexSpec
  extends WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfter
    with MockitoSugar
    with Eventually {

  implicit val system = ActorSystem("ActorIndexSpec", ConfigFactory.parseString(
    """
      |akka.persistence.journal.plugin = "inmemory-journal"
      |akka.persistence.snapshot-store.plugin = "inmemory-snapshot-store"
    """.stripMargin))

  val recipeMetadataMock = mock[RecipeMetadata]

  before {
    Mockito.reset(recipeMetadataMock)
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "ActorIndex" should {

    "create the PetriNetInstance actor when Initialize message is received" in {
      val initializeMsg = Initialize(Marking.empty)
      val processId = UUID.randomUUID()

      val petriNetActorProbe = TestProbe()
      val actorIndex = createActorIndex(petriNetActorProbe.ref)

      implicit val sender = TestProbe().ref
      actorIndex ! BakerActorMessage(processId, initializeMsg)

      petriNetActorProbe.expectMsg(initializeMsg)
    }

    "not create the PetriNetInstance actor if already created" in {
      val initializeMsg = Initialize(Marking.empty)
      val processId = UUID.randomUUID()

      val senderProbe = TestProbe()

      val petriNetActorProbe = TestProbe()
      val actorIndex = createActorIndex(petriNetActorProbe.ref)

      implicit val sender = senderProbe.ref
      actorIndex ! BakerActorMessage(processId, initializeMsg)
      actorIndex ! BakerActorMessage(processId, initializeMsg)

      petriNetActorProbe.expectMsg(initializeMsg)
      petriNetActorProbe.expectNoMsg()
      senderProbe.expectMsg(AlreadyInitialized)
    }

    "forward messages to the PetriNetInstance actor" in {
      val initializeMsg = Initialize(Marking.empty)
      val otherMsg = mock[PetriNetInstanceProtocol.Command]
      val processId = UUID.randomUUID()

      val petriNetActorProbe = TestProbe()
      val actorIndex = createActorIndex(petriNetActorProbe.ref)

      implicit val sender = TestProbe().ref
      actorIndex ! BakerActorMessage(processId, initializeMsg)
      actorIndex ! BakerActorMessage(processId, otherMsg)

      petriNetActorProbe.expectMsg(initializeMsg)
      petriNetActorProbe.expectMsg(otherMsg)
    }

    "notify ProcessMetadata when a PetriNetInstance actor is created" in {
      val initializeMsg = Initialize(Marking.empty)
      val processId = UUID.randomUUID()

      val actorIndex = createActorIndex(TestProbe().ref)

      implicit val sender = TestProbe().ref
      actorIndex ! BakerActorMessage(processId, initializeMsg)

      implicit val patienceConfig = PatienceConfig()
      eventually {
        verify(recipeMetadataMock)
          .addNewProcessMetadata(
            mockito.Matchers.eq(processId.toString),
            mockito.Matchers.anyLong())
      }

    }

    "not forward messages to uninitialized actors" in {
      val processId = UUID.randomUUID()
      val otherMsg = mock[PetriNetInstanceProtocol.Command]

      val senderProbe = TestProbe()

      val petriNetActorProbe = TestProbe()
      val actorIndex = createActorIndex(petriNetActorProbe.ref)

      implicit val sender = senderProbe.ref
      actorIndex ! BakerActorMessage(processId, otherMsg)

      petriNetActorProbe.expectNoMsg()
      senderProbe.expectMsg(Uninitialized(processId.toString))
    }
  }

  private def createActorIndex(petriNetActorRef: ActorRef) = {
    system.actorOf(Props(new ActorIndex(Props.empty, recipeMetadataMock) {
      override private[actor] def createChildPetriNetActor(id: String) = {
        petriNetActorRef
      }
    }), s"actorIndex-${UUID.randomUUID().toString}")
  }
}
