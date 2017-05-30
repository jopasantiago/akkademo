import Mno.{FullEligibilityCheck, FullEligibilityCheckResponse}
import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class AkkaExercises(_system: ActorSystem)
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
  "EXERCISE 1" should "Send a response to the requesting actor" in {
    val mno = TestActorRef(Props[Mno])
    mno ! FullEligibilityCheck("123456789")
    expectMsg(FullEligibilityCheckResponse(true))
  }

  "EXERCISE 2" should "Send a response to the original actors" in {

  }

  "EXERCISE 3" should "Delegate the actions to child actors and compile the results" in {

  }

}

// EXERCISE 1
class Mno extends Actor {
  def receive = {
    ???
  }
}

object Mno {
  case class FullEligibilityCheck(msisdn: String)
  case class FullEligibilityCheckResponse(eligible: Boolean)
}

// EXERCISE 2: Uncomment and Implement

//class ScriptRouter extends Actor {
//
//}
//
//class ScriptGenerator extends Actor {
//
//}

// EXERCISE 3: Uncomment and Implement

