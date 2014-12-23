package org.bustos.mlb

import spray.can.Http
import spray.can.server.UHttp
import spray.routing._
import spray.json._
import spray.http._
import MediaTypes._
import DefaultJsonProtocol._ 
import org.slf4j.{Logger, LoggerFactory}

trait MLBRoutes extends HttpService {
  
  import RetrosheetJsonProtocol._
  
  val retrosheetData = new RetrosheetData
  val logger = LoggerFactory.getLogger(getClass)

  val mlbRoutes = {
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
    path("playerSummary") {
      parameters('player) { (player) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.playerSummary(player).toJson.toString)
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
