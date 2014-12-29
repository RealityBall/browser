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
    path("years") {
      respondWithMediaType(`application/json`) {
        complete(retrosheetData.years.toJson.toString)
      }
    } ~
    path("teams") {
      parameters('year) { (year) =>
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.teams(year).toJson.toString)
        }
      }
    } ~
    path("players") {
      parameters('team, 'year) { (team, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.players(team, year).toJson.toString)
        }
      }
    } ~
    path("playerSummary") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.playerSummary(player, year).toJson.toString)
        }
      }
    } ~
    path("playerBA") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerBA(player, year)))
        }
      }        
    } ~
    path("playerMovingBA") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerMovingBA(player, year)))
        }
      }        
    } ~
    path("playerVolatilityBA") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerVolatilityBA(player, year)))
        }
      }        
    } ~
    path("playerDailyBA") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerDailyBA(player, year)))
        }
      }        
    } ~
    path("playerFantasy") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerFantasy(player, year)))
        }
      }        
    } ~
    path("playerFantasyMoving") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerFantasyMoving(player, year)))
        }
      }        
    } ~
    path("playerSlugging") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerSlugging(player, year)))
        }
      }        
    } ~
    path("playerOnBase") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerOnBase(player, year)))
        }
      }        
    } ~
    path("playerSluggingMoving") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerSluggingMoving(player, year)))
        }
      }        
    } ~
    path("playerOnBaseMoving") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerOnBaseMoving(player, year)))
        }
      }        
    } ~
    path("playerSluggingVolatility") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerSluggingVolatility(player, year)))
        }
      }        
    } ~
    path("playerOnBaseVolatility") {
      parameters('player, 'year) { (player, year) => 
        respondWithMediaType(`application/json`) {
          complete(retrosheetData.dataTable(retrosheetData.playerOnBaseVolatility(player, year)))
        }
      }        
    } ~
    path("graph") {
      parameters('team.?, 'player.?, 'year.?) { (team, player, year) =>
        val teamStr = team.getOrElse("")              
        val playerStr = player.getOrElse("")   
        val yearStr = year.getOrElse("")   
        respondWithMediaType(`text/html`) {
          if (!teamStr.isEmpty) { 
            complete(html.graph.render("", "").toString)
          } else if (!playerStr.isEmpty && !yearStr.isEmpty) {
            complete(html.graph.render(playerStr, yearStr).toString)
          } else {
            complete(html.graph.render("", "").toString)
          }
        }
      }
    }
  }
}
