import ApduGenMock.{PackageLoadScriptRequest, PackageLoadScriptResponse}
import CpsMock.{PersoScriptRequest, PersoScriptResponse}
import ScriptCreator._
import ScriptManager.Start
import akka.actor.{Actor, ActorRef, ActorSystem, Inbox, Props}

class CpsMock extends Actor {
  def receive = {
    case req @ PersoScriptRequest(data) =>
      val persoScript = "83FF" + data + "0000000000C00000000"
      sender ! PersoScriptResponse(persoScript)
  }
}

object CpsMock {
  case class PersoScriptRequest(persoData: String)
  case class PersoScriptResponse(script: String)
}


class ApduGenMock extends Actor {
  def receive = {
    case req @ PackageLoadScriptRequest(data) =>
      val persoScript = "8888" + data + "55558888" + data + "0000000000C00000000"
        sender ! PackageLoadScriptResponse(persoScript)
  }
}

object ApduGenMock {
  case class PackageLoadScriptRequest(packageData: String)
  case class PackageLoadScriptResponse(script: String)
}


// Actor
class ScriptCreator extends Actor {
  val cps = context.actorOf(Props[CpsMock])
  val apduGen = context.actorOf(Props[ApduGenMock])

  def receive = {
    case msg @ RequestWrapper(PersoScriptRequest(data)) =>
      cps forward PersoScriptRequest(data)

    case msg @ RequestWrapper(PackageLoadScriptRequest(data)) =>
      apduGen.tell(PackageLoadScriptRequest(data), sender)

  }
}
// Companion object
case object ScriptCreator {
  case class RequestWrapper(req: Any)
}

class ScriptManager extends Actor {
  var scripts: List[String] = List()

  def receive = {
    case Start =>
      val scriptGenerator = context.actorSelection("/user/scriptCreator")
      scriptGenerator ! RequestWrapper(PersoScriptRequest("1111"))
      scriptGenerator ! RequestWrapper(PackageLoadScriptRequest("55005500550055005500"))

    case msg: PackageLoadScriptResponse =>
      println("Received message: " + msg)
      println("Updating script list: " + scripts)
      scripts = scripts :+ msg.script
      println("Script list is now updated " + scripts)

    case msg: PersoScriptResponse =>
      println("Received message: " + msg)
      println("Updating script list: " + scripts)
      scripts = scripts :+ msg.script
      println("Script list is now updated " + scripts)
  }
}

object ScriptManager {
  case object Start
}

object Demo3 extends App {

  // Create the 'tsm' actor system
  val system = ActorSystem("tsm")

  // Create the 'scriptManager' actor
  val scriptManager = system.actorOf(Props[ScriptManager], "scriptManager")

  // Create the 'scriptManager' actor
  val scriptGenerator = system.actorOf(Props[ScriptCreator], "scriptCreator")

  scriptManager ! Start




}

