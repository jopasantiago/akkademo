import javax.xml.bind.DatatypeConverter._

import Mno.{FullEligibilityCheck, FullEligibilityCheckResponse, UnsupportedMessage}
import PersoScriptGenerator.{PersoScriptRequest, PersoScriptResponse}
import ScriptWrapper.{WrapScriptRequest, WrapScriptResponse}
import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import akka.testkit.TestActorRef


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
  "EXERCISE 1: Mno Actor" should "send a FullEligibilityCheckResponse" in {
    val mno = TestActorRef(Props[Mno])
    mno ! FullEligibilityCheck("48500900500")
    expectMsg(FullEligibilityCheckResponse(true))
  }

  "EXERCISE 1" should "Not not handle invalid message" in {
    val mno = TestActorRef(Props[Mno])
    val stringMessage = "Hello"
    val intMessage = 5
    mno ! stringMessage
    expectMsg(UnsupportedMessage(stringMessage))
    mno ! intMessage
    expectMsg(UnsupportedMessage(intMessage))
  }

  "EXERCISE 2" should "Send a response to the original actors" in {
    val scriptRequestRouter = TestActorRef(Props[ScriptRequestRouter])
    val persoScriptReq = PersoScriptRequest("AABBCCDDEEFF")
    val wrapScriptReq = WrapScriptRequest("1122334455")
      scriptRequestRouter ! persoScriptReq
    expectMsgClass(classOf[PersoScriptResponse])
    scriptRequestRouter ! wrapScriptReq
    expectMsgClass(classOf[WrapScriptResponse])

  }

  "EXERCISE 3" should "Delegate the actions to child actors and gather the results" in {
    val actorRef = TestActorRef[ResultsGatherer]
    val actor = actorRef.underlyingActor

    val persoScriptReq = PersoScriptRequest("AABBCCDDEEFF")
    val wrapScriptReq = WrapScriptRequest("1122334455")
    actorRef ! persoScriptReq

    Thread.sleep(3000)
    assert(actor.results.contains(printBase64Binary(parseHexBinary("AABBCCDDEEFF"))) == true)

    actorRef ! wrapScriptReq
    Thread.sleep(3000)

    assert(actor.results.contains(printBase64Binary(parseHexBinary("AABBCCDDEEFF"))) == true)
    assert(actor.results.contains(printBase64Binary(parseHexBinary("1122334455"))) == true)

  }

}

// Implement the ??? below
// EXERCISE 1
class Mno extends Actor {
  def receive = {
    case msg: FullEligibilityCheck => sender ! FullEligibilityCheckResponse(true)
    case otherMsg => sender ! UnsupportedMessage(otherMsg)
  }
}

object Mno {
  case class FullEligibilityCheck(msisdn: String)
  case class FullEligibilityCheckResponse(eligible: Boolean)
  case class UnsupportedMessage(msg: Any)
}

// EXERCISE 2
// Hint: You must create PersoScriptGenerator and ScriptWrapper using actorOf(Props(new <ActorClass>))
class ScriptRequestRouter extends Actor {
  def receive = {
    case msg: PersoScriptRequest =>
      val persoScriptGeneratorActor = context.actorOf(Props(new PersoScriptGenerator))
      persoScriptGeneratorActor.tell(msg, sender)
    case msg: WrapScriptRequest =>
      val wrapScriptActor = context.actorOf(Props(new ScriptWrapper))
      wrapScriptActor.tell(msg, sender)
  }
}

class PersoScriptGenerator extends Actor {
  def receive = {
    case msg: PersoScriptRequest => sender ! PersoScriptResponse(printBase64Binary(parseHexBinary(msg.persoData)))
  }
}

object PersoScriptGenerator {
  case class PersoScriptRequest(persoData: String)
  case class PersoScriptResponse(script: String)
}

class ScriptWrapper extends Actor {
  def receive = {
    case msg: WrapScriptRequest => sender ! WrapScriptResponse(printBase64Binary(parseHexBinary(msg.script)))
  }
}

object ScriptWrapper {
  case class WrapScriptRequest(script: String)
  case class WrapScriptResponse(wrappedScript: String)
}

// EXERCISE 3
// Hints: You need to create the PersoScriptGenerator and ScriptWrapper Actors
// Handle WrapScriptResponse and PersoScriptResponse by adding their contents to the results var.
class ResultsGatherer extends Actor {
  var results: List[String] = List()

  def receive = {
    case msg: PersoScriptRequest =>
      val persoScriptGeneratorActor = context.actorOf(Props(new PersoScriptGenerator))
      persoScriptGeneratorActor ! msg
    case msg: PersoScriptResponse =>
      results = List(msg.script)
    case msg: WrapScriptRequest =>
      val wrapScriptActor = context.actorOf(Props(new ScriptWrapper))
      wrapScriptActor ! msg
    case msg: WrapScriptResponse =>
      results = results :+ msg.wrappedScript
  }
}