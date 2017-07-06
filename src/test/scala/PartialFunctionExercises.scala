import ColorCounter._
import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.testkit.TestActorRef


class PartialFunctionExercises(_system: ActorSystem)
  extends TestKit(_system)
    with ImplicitSender
    with Matchers
    with FlatSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("HelloAkkaSpec"))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  // Implement the receive method in the actors below:
  // EXERCISE 1: Hints:
  // Implement the receive method below using a combination of all the provided partial functions with orElse and andThen
  // Rules:
  // (1) Increment counter for GREEN and RED
  // (2) DO NOT increment counter for YELLOW
  // (3) Handle GetCount and return the counter
  // (4) DO NOT increment the counter when you receive a GetCount message
  "EXERCISE 1: The Color Counter" should "Count Red and Green but not yellow" in {
    val actor = TestActorRef(Props[ColorCounter])
    actor ! ChooseGreen
    expectMsg("GREEN")
    actor ! ChooseYellow
    expectMsg("YELLOW")
    actor ! ChooseGreen
    expectMsg("GREEN")
    actor ! ChooseYellow
    expectMsg("YELLOW")
    actor ! ChooseRed
    expectMsg("RED")
    actor ! GetCount
    expectMsg(3)

  }

}

// Implement the ??? below
// EXERCISE 1
class ColorCounter extends Actor {
  var counter: Int = 0
  def receive = ???
  // Use a combination of orElse and andThen methods with red/green/yellow partial functions to implement the logic.

  def red: PartialFunction[Any, Unit] = {
    case ChooseRed => sender ! "RED"
  }

  def green: PartialFunction[Any, Unit] = {
    case ChooseGreen => sender ! "GREEN"
  }

  def yellow: PartialFunction[Any, Unit] = {
    case ChooseYellow => sender ! "YELLOW"
  }

  def incrementCounter : PartialFunction[Any, Unit] = {
    case _ => counter = counter + 1
  }

  def getCount: PartialFunction[Any, Unit] = {
    case GetCount => sender ! counter
  }
}

object ColorCounter {
  case object ChooseRed
  case object ChooseGreen
  case object ChooseYellow
  case object GetCount
}

