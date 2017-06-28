import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Inbox, PoisonPill, Props}

case class GreetingRequest(name: String)

// This is an actor
class MessagePrinter extends Actor {
  def receive = {
    case msg: GreetingRequest =>
      println(s"Hello ${msg.name}!")

    case "Moana" => println("hahahaha")
    //case _ => println("ERROR: Received message is not a Greeting Request!")
  }
}


object Demo extends App {

  // Create the 'tsm' actor system
  val system = ActorSystem("demo")

  // Create the 'scriptGenerator' actor
  val messagePrinter: ActorRef = system.actorOf(Props[MessagePrinter], "messagePrinter")

//  messagePrinter ! PoisonPill
//  messagePrinter ! GreetingRequest("Vladimir")
//  messagePrinter ! 3
//  messagePrinter ! "Moana"
//  messagePrinter ! HelloRequest("HELLO")

  val messagePrinterReference: ActorSelection = system.actorSelection("/user/messagePrinterssss")

  messagePrinterReference ! GreetingRequest("Roberto")

}

case class HelloRequest(message: String)
