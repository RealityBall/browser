package org.bustos.realityball

import spray.can.Http
import spray.can.server.UHttp
import spray.routing._
import spray.json._
import spray.http._
import MediaTypes._
import DefaultJsonProtocol._
import _root_.org.slf4j.{ Logger, LoggerFactory }

trait MLBRoutes extends HttpService {

  import RealityballJsonProtocol._

  val realityballData = new RealityballData
  val logger = LoggerFactory.getLogger(getClass)

  val mlbRoutes = {
    getFromResourceDirectory("webapp") ~
      path("years") {
        respondWithMediaType(`application/json`) {
          complete(realityballData.years.toJson.toString)
        }
      } ~
      path("teams") {
        parameters('year) { (year) =>
          respondWithMediaType(`application/json`) {
            complete(realityballData.teams(year).toJson.toString)
          }
        }
      } ~
      path("players") {
        parameters('team, 'year) { (team, year) =>
          respondWithMediaType(`application/json`) {
            complete(realityballData.players(team, year).toJson.toString)
          }
        }
      } ~
      pathPrefix("team") {
        path("fantasy") {
          parameters('team, 'year) { (team, year) =>
            respondWithMediaType(`application/json`) {
              complete(realityballData.dataNumericTable2(realityballData.teamFantasy(team, year), List("Total", "25 Day")))
            }
          }
        } ~
          path("ballparkBA") {
            parameters('team, 'year) { (team, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.ballparkBA(team, year)))
              }
            }
          } ~
          path("ballparkAttendance") {
            parameters('team, 'year) { (team, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataNumericTable(realityballData.ballparkAttendance(team, year), "Total"))
              }
            }
          } ~
          path("ballparkTemp") {
            parameters('team, 'year) { (team, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataNumericTable(realityballData.ballparkTemp(team, year), "Temp (F)"))
              }
            }
          } ~
          path("schedule") {
            parameters('team, 'year) { (team, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.schedule(team, year).toJson.toString)
              }
            }
          }
      } ~
      pathPrefix("pitcher") {
        path("summary") {
          parameters('player, 'year) { (player, year) =>
            respondWithMediaType(`application/json`) {
              complete(realityballData.pitcherSummary(player, year).toJson.toString)
            }
          }
        } ~
          path("outs") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataNumericTable3(realityballData.outs(player, year), List("Strike Outs", "Fly Outs", "Ground Outs")))
              }
            }
          } ~
          path("outsTypes") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataNumericPieChart(realityballData.outsTypeCount(player, year), "Type", "Outs"))
              }
            }
          } ~
          path("strikeRatio") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataNumericTable(realityballData.strikeRatio(player, year), "Strike / Total"))
              }
            }
          } ~
          path("fantasy") {
            parameters('player, 'year, 'gameName) { (player, year, gameName) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataNumericTable(realityballData.pitcherFantasy(player, year, gameName), "Score"))
              }
            }
          } ~
          path("fantasyMoving") {
            parameters('player, 'year, 'gameName) { (player, year, gameName) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataNumericTable(realityballData.pitcherFantasyMoving(player, year, gameName), "Score Moving"))
              }
            }
          }
      } ~
      pathPrefix("batter") {
        path("summary") {
          parameters('player, 'year) { (player, year) =>
            respondWithMediaType(`application/json`) {
              complete(realityballData.batterSummary(player, year).toJson.toString)
            }
          }
        } ~
          path("BA") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.BA(player, year)))
              }
            }
          } ~
          path("movingBA") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.movingBA(player, year)))
              }
            }
          } ~
          path("volatilityBA") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.volatilityBA(player, year)))
              }
            }
          } ~
          path("dailyBA") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.dailyBA(player, year)))
              }
            }
          } ~
          path("fantasy") {
            parameters('player, 'year, 'gameName) { (player, year, gameName) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.fantasy(player, year, gameName)))
              }
            }
          } ~
          path("fantasyMoving") {
            parameters('player, 'year, 'gameName) { (player, year, gameName) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.fantasyMoving(player, year, gameName)))
              }
            }
          } ~
          path("slugging") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.slugging(player, year)))
              }
            }
          } ~
          path("onBase") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.onBase(player, year)))
              }
            }
          } ~
          path("sluggingMoving") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.sluggingMoving(player, year)))
              }
            }
          } ~
          path("onBaseMoving") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.onBaseMoving(player, year)))
              }
            }
          } ~
          path("sluggingVolatility") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.sluggingVolatility(player, year)))
              }
            }
          } ~
          path("onBaseVolatility") {
            parameters('player, 'year) { (player, year) =>
              respondWithMediaType(`application/json`) {
                complete(realityballData.dataTable(realityballData.onBaseVolatility(player, year)))
              }
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
