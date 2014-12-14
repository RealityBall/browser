package org.bustos.mlb

import akka.actor.{ ActorSystem, Actor, Props, ActorLogging, ActorRef, ActorRefFactory }
import akka.io.IO
import akka.pattern.ask
import akka.util.{ Timeout }
import scala.util.{ Success, Failure }
import scala.concurrent._
import scala.concurrent.duration._
import spray.util._
import spray.http._
import spray.routing.HttpServiceActor
import MediaTypes._
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket
import spray.can.websocket.frame.{ BinaryFrame, TextFrame }
import spray.can.websocket.FrameCommandFailed
import spray.routing._
import Directives._
import spray.json._
import DefaultJsonProtocol._ 
import org.slf4j.{Logger, LoggerFactory}

object WebSocketWorker {
  case class Push(msg: String)

  def props(serverConnection: ActorRef) = Props(classOf[WebSocketWorker], serverConnection)
}
  
class WebSocketWorker(val serverConnection: ActorRef) extends HttpServiceActor with websocket.WebSocketServerWorker {
  
  import WebSocketWorker._
  
  override def receive = handshaking orElse businessLogicNoUpgrade orElse closeLogic

  val retrosheetData = new RetrosheetData
  val logger = LoggerFactory.getLogger(getClass)

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
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.teams.toJson.toString)
        }
        
      } ~
      path("players") {
        parameters('team) { (team) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.players(team).toJson.toString)
          }
        }
      } ~
      path("playerBA") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerBA(player)))
          }
        }        
      } ~
      path("playerMovingBA") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerMovingBA(player)))
          }
        }        
      } ~
      path("playerVolatilityBA") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerVolatilityBA(player)))
          }
        }        
      } ~
      path("playerDailyBA") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerDailyBA(player)))
          }
        }        
      } ~
      path("playerFantasy") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerFantasy(player)))
          }
        }        
      } ~
      path("playerFantasyMoving") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerFantasyMoving(player)))
          }
        }        
      } ~
      path("playerSlugging") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerSlugging(player)))
          }
        }        
      } ~
      path("playerOnBase") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerOnBase(player)))
          }
        }        
      } ~
      path("playerSluggingMoving") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerSluggingMoving(player)))
          }
        }        
      } ~
      path("playerOnBaseMoving") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerOnBaseMoving(player)))
          }
        }        
      } ~
      path("playerSluggingVolatility") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerSluggingVolatility(player)))
          }
        }        
      } ~
      path("playerOnBaseVolatility") {
        parameters('player) { (player) => 
          respondWithMediaType(`application/json`) {
            complete(retrosheetData.dataTable(retrosheetData.playerOnBaseVolatility(player)))
          }
        }        
      } ~
      path("graph") {
        parameters('team.?, 'player.?) { (team, player) =>
          val teamStr = team.getOrElse("")              
          val playerStr = player.getOrElse("")   
          respondWithMediaType(`text/html`) {
            if (!teamStr.isEmpty) { 
              complete(html.graph.render("").toString)
            } else if (!playerStr.isEmpty) {
              complete(html.graph.render(playerStr).toString)
            } else {
              complete(html.graph.render("").toString)
            }
          }
        }
      }
    }
  }
}