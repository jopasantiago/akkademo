import Divider.Divide
import LimitedRequestCounter.End
import RequestCounter.{GetCount, Request}
import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Inbox, PoisonPill, Props}

object PartialFunctionDemo extends App {

  // total function
  def multiply(multiplicand: Int, multiplier: Int) = {
    multiplicand * multiplier
  }

  println("2 multiplied by 3 is " + multiply(2, 3))
  println("37 multiplied by 7 is " + multiply(37, 7))

  // function as val
  val add = (a: Int, b: Int) => a + b

  println("1 plus 1 equals " + add(1, 1))
  println("3478 plus 12659 equals " + add(3478, 12659))

  // Partial Function Example 1
  val getMobileSubscriptionIdentifierType: PartialFunction[Int, String] = {
    case 1 => "MSISDN"
    case 2 => "ALIAS"
    case 3 => "IDTECH"
  }

  println("MSID Type 1 is " + getMobileSubscriptionIdentifierType(1))
  println("MSID Type 2 is " + getMobileSubscriptionIdentifierType(2))
  println("MSID Type 3 is " + getMobileSubscriptionIdentifierType(3))
  println("Is type 2 defined? " + getMobileSubscriptionIdentifierType.isDefinedAt(2))
  println("Is type 4 defined? " + getMobileSubscriptionIdentifierType.isDefinedAt(4))
  // println("MSID Type 4 is " + getMobileSubscriptionIdentifierType(4)) // What will happen?

  // Partial Function Example 2
  val divide: PartialFunction[(Int, Int), Int] = {
    case (dividend: Int, divisor: Int) if divisor != 0 => dividend / divisor
  }

  println("Divide 55 by 11 = " + divide(55, 11))
  println("Divide 0 by 33 = " + divide(0, 33))
  println(divide.isDefinedAt(0, 0))
  println(divide.isDefinedAt(111, 0))
  println(divide.isDefinedAt(0, 178))
  // println("10 / 0 = " + divide(10, 0)) // You know what will happen.

  val divideByZero: PartialFunction[(Int, Int), Int] = {
    case (dividend: Int, divisor: Int) if divisor == 0 => 0
  }

  val safeDivide = divide orElse divideByZero
  println("Safely divide 33 by 0 = " + safeDivide(33, 0))


  val addOne: Int => Int = _ + 1

  val divideThenAddOne = divide andThen addOne

  println("Divide(66 by 6) Then Add One = " + divideThenAddOne(66, 6))

  // See Actor.Receive type
  // See Actor.receive method

  // Create the actor system
  val system = ActorSystem("partial_function_demo")

  // Create the 'requestCounter' actor
  val divider: ActorRef = system.actorOf(Props[Divider], "divider")

  divider ! Divide(100, 5)
  divider ! Divide(33, 0)

  // Create the 'requestCounter' actor
  val requestCounter: ActorRef = system.actorOf(Props[RequestCounter], "requestCounter")

  requestCounter ! GetCount

  requestCounter ! Request(1)
  requestCounter ! Request(2)

  requestCounter ! GetCount

  requestCounter ! Request(3)
  requestCounter ! Request(4)
  requestCounter ! Request(5)

  requestCounter ! GetCount

  // Compare with requestCounter
  val limitedRequestCounter: ActorRef = system.actorOf(Props[LimitedRequestCounter], "limitedRequestCounter")

  limitedRequestCounter ! GetCount

  limitedRequestCounter ! Request(1)
  limitedRequestCounter ! Request(2)

  limitedRequestCounter ! End

  // Uncomment the code below. What happens if we execute?
//  limitedRequestCounter ! Request(3)
//  limitedRequestCounter ! GetCount

  // SHOW the ExecutionTrail class

}

class Divider extends Actor {

  def receive = nonZero orElse zero

  def nonZero: PartialFunction[Any, Unit] = {
    case x @ Divide(a, b) if b != 0 => println(s"$a/$b = ${a/b}")
  }

  def zero: PartialFunction[Any, Unit] = {
    case x @ Divide(a, b) if b == 0 => println(s"$a/$b = INFINITY")
  }

}

object Divider {
  case class Divide(dividend: Int, divisor: Int)
}

class RequestCounter extends Actor {
  var counter = 0

  def receive = {
    case r: Request => counter = counter + 1
    case GetCount => println("Request Count: " + counter)
  }

}

object RequestCounter {
  case class Request(transactionId: Int)
  case object GetCount
}

class LimitedRequestCounter extends Actor {
  var counter = 0

  // andThen sample usage: for storing in the DB
  def receive = (receiveRequest andThen incrementCounter) orElse receiveGetCounter

  def receiveRequest: PartialFunction[Any, Unit] = {
    case r: Request => println("Received Request " + r)
  }

  def receiveGetCounter: PartialFunction[Any, Unit] = {
    case GetCount => println("Current Request Count: " + counter)
  }

  def incrementCounter: PartialFunction[Any, Unit] = {
    case _ => counter = counter + 1
      println("Incremented Counter = " + counter)
  }

}

object LimitedRequestCounter {
  case class Request(transactionId: Int)
  case object GetCount
  case object End
}


