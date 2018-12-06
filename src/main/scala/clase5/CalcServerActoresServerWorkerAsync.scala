package clase5

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}

import scala.util.Random


object CalcServerActoresServerWorkerAsync extends App {

  case class Calc(x: Int)

  case class InnerCalc(x: Int, respondTo: ActorRef)

  case class Work()

  case class Result(x: Int)

  class Worker extends Actor {
    def receive = {
      case InnerCalc(x, respondTo) =>
        Thread.sleep(new Random().nextInt(3000))
        respondTo ! Result(x * x + 1)
        // our job here is done, kill ourselves
        self ! PoisonPill
    }
  }

  class Server extends Actor {
    def receive = {
      case Calc(x) =>
        println("server received: " + x)
        val child = context.actorOf(Props[Worker], "worker" + x)
        child ! InnerCalc(x, sender())
    }
  }

  class Client(server: ActorRef) extends Actor {

    private var results = List[Int]()

    def receive = {
      case Work() =>
        println("client will send 4")
        server ! Calc(4)

        println("client will send 5")
        server ! Calc(5)
      case Result(x) =>
        println("client received result: " + x)
        results ::= x
        if (results.size == 2) {
          println("final result:" + results.sum)
        }
    }
  }


  val system = ActorSystem("HelloSystem")
  // default Actor constructor
  val server = system.actorOf(Props[Server], name = "server")
  val client = system.actorOf(Props(classOf[Client], server), name = "client")

  client ! Work()
}