import ScriptGenerator.{GenerateInitUpdate, GenerateInitUpdateResponse, GetPasscodeResetCommand, GetPasscodeResetCommandResponse}
import akka.actor.{Actor, ActorRef, ActorSystem, Inbox, Props}


// This is an actor
class ScriptGenerator extends Actor {
  def receive = {
    case msg @ GenerateInitUpdate(k) =>
    case GetPasscodeResetCommand =>
      sender ! GetPasscodeResetCommandResponse("14FFFFFFFFFFFFFF")
  }

}

// Companion object
case object ScriptGenerator {
  case class GenerateInitUpdate(kvn: String)
  case class GenerateInitUpdateResponse(script: String)
  case object GetPasscodeResetCommand
  case class GetPasscodeResetCommandResponse(script: String)
}

class MessageHandler extends Actor {
  override def receive: Receive = {
    case msg: GenerateInitUpdate =>
      val scriptGenerator = context.actorOf(Props[ScriptGenerator])
      println(s"Sender: $sender")
      scriptGenerator.tell(msg, self)

    case GetPasscodeResetCommand =>
      val scriptGenerator = context.actorOf(Props[ScriptGenerator])
      println(s"Sender: $sender")
      scriptGenerator ! GetPasscodeResetCommand

    case anything => println("Received Message: " +  anything)
      println(s"Sender: $sender")
  }
}

object Demo2 extends App {

  // Create the 'tsm' actor system
  val system = ActorSystem("tsm")

  // Create the 'scriptGenerator' actor
  val messageHandler = system.actorOf(Props[MessageHandler], "messageHandler")


  messageHandler ! GetPasscodeResetCommand
  messageHandler ! GenerateInitUpdate("23")

}

