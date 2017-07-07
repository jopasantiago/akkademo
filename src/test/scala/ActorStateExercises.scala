import ColorCounter._
import OverloadController.{InstallServiceRequest, InstallationDone, Overloaded}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.testkit.TestActorRef
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.FiniteDuration


class ActorStateExercises(_system: ActorSystem)
  extends TestKit(_system)
    with ImplicitSender
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("ActorStateExercises"))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  // Implement the ready() and full() partial functions in the actors below:
  // Goal: Handle up to 5 parallel service installation requests.
  // Reject all incoming requests if 5 service installations are already running.
  // Hint: ServiceInstaller will return InstallationDone after 5 seconds. So each service installation will take 5 seconds.
  // The ! (tell) method should be almost instantaneous. You may assume it takes 0 second to execute them.
  "EXERCISE 1: The Overload" should "Handle up to 5 parallel service installation requests" in {
    val actorRef = TestActorRef[OverloadController]
    val actor = actorRef.underlyingActor

    assert(actor.currentRequests == 0, "Requests should be zero")

    actorRef ! InstallServiceRequest("0001")
    actorRef ! InstallServiceRequest("0002")

    assert(actor.currentRequests == 2, "Requests should be incremented twice")

    actorRef ! InstallServiceRequest("0003")
    actorRef ! InstallServiceRequest("0004")
    actorRef ! InstallServiceRequest("0005")
    actorRef ! InstallServiceRequest("0006")

    assert(actor.currentRequests == 5, "Requests should be 5. Do not handle other requests.")
    expectMsg(Overloaded)// expect to Overload for more than 5 parallel requests.

    Thread.sleep(6000) // Wait for 6 seconds. All requests should be done now.

    actorRef ! InstallServiceRequest("0007")
    actorRef ! InstallServiceRequest("0008")
    actorRef ! InstallServiceRequest("0009")
    actorRef ! InstallServiceRequest("0010")
    actorRef ! InstallServiceRequest("0011")

    // ASSERTION
    // Should be able to handle 5 requests. No overload message should be sent.
    expectNoMsg
  }

}

// Implement the ??? below
// EXERCISE 1
// use context.become/unbecome
// increment/decrement counter
// send an Overloaded message back if there are already 5 currentRequests
class OverloadController extends Actor {
  val serviceInstaller = context.actorOf(Props[ServiceInstaller])

  var currentRequests: Int = 0
  def receive = ready

  def ready: Receive = {
    case msg: InstallServiceRequest =>
      serviceInstaller ! msg // serviceInstaller will send back an "InstallationDone" message after 5 seconds.
      // Implement the rest of the functionality below:
      ???

    case msg: InstallationDone =>
      ???
  }

  def full: Receive = {
    case msg: InstallServiceRequest =>
      ???

    case msg: InstallationDone =>
      ???
  }

}

object OverloadController {
  case class InstallServiceRequest(transactionId: String)
  case class InstallationDone(transactionId: String)
  case object Overloaded
}

class ServiceInstaller extends Actor {
  def receive = {
    case msg: InstallServiceRequest =>
      context.system.scheduler.scheduleOnce(FiniteDuration(5,"s"), sender, InstallationDone(msg.transactionId))
  }
}