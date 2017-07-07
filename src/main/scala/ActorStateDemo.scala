import CoffeeMachine._
import CoffeeVendingMachine.{DoneCreatingCoffee, RequestCoffee, VendoCoffee}
import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Cancellable, Inbox, PoisonPill, Props, Stash}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

object ActorStateDemo extends App {

  // Create the actor system
  val system = ActorSystem("actor_state_demo")

  // Create the 'coffee machine' actor
  val coffeeMachine: ActorRef = system.actorOf(Props[CoffeeMachine], "coffee_machine")

  // Create the 'Logger' actor which just prints to the standard output
  val eventLog: ActorRef = system.actorOf(Props[EventLogger], "printer")

  coffeeMachine.tell(TurnOff, eventLog) // Does nothing. Current State doesn't support the message.
  coffeeMachine.tell(TurnOn, eventLog) // Shows nothing. Internally switches the state to "ready"
  coffeeMachine.tell(MakeCoffee(3, false), eventLog) // At "ready" state, the coffee machine can make coffee"
  coffeeMachine.tell(MakeCoffee(1, false), eventLog) // Coffee machine is currently busy and won't be able to make coffee
  coffeeMachine.tell(TurnOff, eventLog) // If we turn off the machine while the coffee is not yet done, the coffee making won't be done.

  coffeeMachine.tell(TurnOn, eventLog) // Turn on the coffee machine again. Internally switches the state to "ready"
  coffeeMachine.tell(MakeCoffee(5, true), eventLog) // At "ready" state, the coffee machine can make coffee"


  // Create the 'coffee machine' actor
  val vendingMachine: ActorRef = system.actorOf(Props[CoffeeVendingMachine], "coffee_vending_machine")

  vendingMachine.tell(RequestCoffee(2, true), eventLog) // Vending machine can handle coffee requests normally

  // Vending machine can "queue" requests and handle them one at a time automatically
  vendingMachine.tell(RequestCoffee(5, true), eventLog)
  vendingMachine.tell(RequestCoffee(7, false), eventLog)
  vendingMachine.tell(RequestCoffee(1, true), eventLog)

}

class EventLogger extends Actor {
  override def receive: Receive = {
    case anything => println(s"RECEIVED MESSAGE: $anything")
  }
}

class CoffeeMachine extends Actor {
  var currentRequest: Cancellable = null
  var currentRequester: ActorRef = null

  def receive = off

  def off: Receive = {
    case TurnOn => context.become(ready)
    //case TurnOff => sender ! Error("Coffee Machine is already turned off.")
  }

  def ready: Receive = {
    case TurnOff => context.become(off)

    case request: MakeCoffee =>
      currentRequester = sender
      currentRequest = context.system.scheduler.scheduleOnce(FiniteDuration(5,"s"),self, DoneMakingCoffee(Coffee(request.strength, request.milk)))
      context.become(busy)
  }

  def busy: Receive = {
    case done: DoneMakingCoffee =>
      if (currentRequester != null) {
        currentRequester ! done.coffee
      }

    case TurnOff =>
      // cancel current coffee
      if (currentRequest != null) {
        currentRequest.cancel()
      }
      context.become(off)

//    case request: MakeCoffee =>
//      sender ! Error("Coffee machine is busy making coffee.")
  }

}

object CoffeeMachine {
  case object TurnOn
  case object TurnOff
  case class MakeCoffee(strength: Int, milk: Boolean)
  case class Coffee(strength: Int, milk: Boolean)
  private case class DoneMakingCoffee(coffee: Coffee)
  case class Error(message: String)
}

class CoffeeVendingMachine extends Actor with Stash {
  var currentRequest: Cancellable = null
  var currentRequester: ActorRef = null

  def receive = ready

  def ready: Receive = {
    case request: RequestCoffee =>
      currentRequester = sender
      context.system.scheduler.scheduleOnce(FiniteDuration(3,"s"),self, DoneCreatingCoffee(VendoCoffee(request.strength, request.milk)))
      context.become(busy)
  }

  def busy: Receive = {
    case done: DoneCreatingCoffee =>
      if (currentRequester != null) {
        currentRequester ! done.coffee
        context.unbecome()
        unstashAll
      }

    case request: RequestCoffee =>
      stash()

  }

}

object CoffeeVendingMachine {
  case class RequestCoffee(strength: Int, milk: Boolean)
  case class VendoCoffee(strength: Int, milk: Boolean)
  private case class DoneCreatingCoffee(coffee: VendoCoffee)
  case class Error(message: String)
}
