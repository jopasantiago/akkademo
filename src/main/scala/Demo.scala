import akka.actor.{Actor, ActorRef, ActorSystem, Inbox, Props}

case class GreetingRequest(name: String)

// This is an actor
class MessagePrinter extends Actor {
  def receive = {
    case msg: GreetingRequest =>
      println(s"Hello ${msg.name}!")

    case _ => println("ERROR: Received message is not a Greeting Request!")
  }
}


object Demo extends App {

  // Create the 'tsm' actor system
  val system = ActorSystem("demo")

  // Create the 'scriptGenerator' actor
  val messagePrinter = system.actorOf(Props[MessagePrinter], "messagePrinter")

  messagePrinter ! GreetingRequest("Vladimir")
  //messagePrinter ! 3
  //messagePrinter ! "Moana"
  //messagePrinter ! HelloRequest("HELLO")

  val messagePrinterReference = system.actorSelection("/user/messagePrinter")

  messagePrinterReference ! GreetingRequest("Roberto")

}

case class HelloRequest(message: String)
