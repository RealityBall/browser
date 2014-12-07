package org.bustos.mlb

import akka.actor.{ ActorSystem, Actor, Props, ActorLogging, ActorRef, ActorRefFactory }
import akka.io.IO
import spray.util._
import spray.http._
import MediaTypes._
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket
import spray.can.websocket.frame.{ BinaryFrame, TextFrame }
import spray.http.HttpRequest
import spray.can.websocket.FrameCommandFailed
import spray.routing.HttpServiceActor
import spray.json._
import DefaultJsonProtocol._ 
import org.bustos.mlb._

object MlbServer extends App with MySslConfiguration {

  final case class Push(msg: String)

  object WebSocketServer {
    def props() = Props(classOf[WebSocketServer])
  }
  class WebSocketServer extends Actor with ActorLogging {
    def receive = {
      // when a new connection comes in we register a WebSocketConnection actor as the per connection handler
      case Http.Connected(remoteAddress, localAddress) =>
        println("Connected, Remote: " + remoteAddress + ", Local: " + localAddress)
        val serverConnection = sender()
        val conn = context.actorOf(WebSocketWorker.props(serverConnection))
        serverConnection ! Http.Register(conn)
    }
  }

  object WebSocketWorker {
    def props(serverConnection: ActorRef) = Props(classOf[WebSocketWorker], serverConnection)
  }
  
  class WebSocketWorker(val serverConnection: ActorRef) extends HttpServiceActor with websocket.WebSocketServerWorker {
    override def receive = handshaking orElse businessLogicNoUpgrade orElse closeLogic

    val retrosheetData = new RetrosheetData
    
    def businessLogic: Receive = {
      // just bounce frames back for Autobahn testsuite
      case x @ (_: BinaryFrame | _: TextFrame) =>
        println("Frame: " + x)
        sender() ! x

      case Push(msg) =>
        println("Push: " + msg)
        send(TextFrame(msg))

      case x: FrameCommandFailed =>
        log.error("frame command failed", x)

      case x: HttpRequest => println("BL: " + x) // do something
    }

    def businessLogicNoUpgrade: Receive = {
      implicit val refFactory: ActorRefFactory = context
      runRoute {
        getFromResourceDirectory("webapp") ~
        path("teams") {
          respondWithMediaType(`text/plain`) {
            complete(retrosheetData.teams.toJson.toString)
          }
          
        } ~
        path("players") {
          parameters('team) { (team) => 
            respondWithMediaType(`text/plain`) {
              complete(retrosheetData.players(team).toJson.toString)
            }
          }
        } ~
        path("graph") {
          parameters('team.?, 'player.?) { (team, player) =>
            val teamStr = team.getOrElse("")              
            val playerStr = player.getOrElse("")   
            respondWithMediaType(`text/html`) {
              if (!teamStr.isEmpty) { 
                complete(html.graph.render("", retrosheetData.teams, retrosheetData.players(teamStr), List(), List(), List()).toString)
              } else if (!playerStr.isEmpty) {
                complete(html.graph.render(playerStr, retrosheetData.teams, retrosheetData.players(teamStr), retrosheetData.playerBA(playerStr), retrosheetData.playerMovingBA(playerStr), retrosheetData.playerVolatilityBA(playerStr)).toString)
              } else {
                complete(html.graph.render("", retrosheetData.teams, List(), List(), List(), List()).toString)
              }
            }
          }
        }
      }
    }
  }

  def doMain() {
    implicit val system = ActorSystem()
    import system.dispatcher
    import akka.util.Timeout
    import scala.concurrent.duration._
    import akka.pattern.{ ask, pipe }
    implicit val timeout = Timeout(DurationInt(5).seconds)

    val server = system.actorOf(WebSocketServer.props(), "websocket")

    //IO(UHttp) ? Http.Bind(server, "localhost", 8110)
    IO(UHttp) ? Http.Bind(server, "0.0.0.0", args(0).toInt)

  }

  // because otherwise we get an ambiguous implicit if doMain is inlined
  doMain()
}
