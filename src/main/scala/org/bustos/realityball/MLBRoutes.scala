/*

    Copyright (C) 2016 Mauricio Bustos (m@bustos.org)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.bustos.realityball

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import org.bustos.realityball.common.RealityballConfig._
import org.bustos.realityball.common.{RealityballData, RealityballJsonProtocol}
import org.slf4j.LoggerFactory

trait MLBRoutes extends SprayJsonSupport with RealityballJsonProtocol {

  val realityballData = new RealityballData
  val logger = LoggerFactory.getLogger("browser")

  val routes = {
    path("years") {
      complete(realityballData.years)
    } ~
      path("teams") {
        parameters('year) { (year) =>
          complete(realityballData.teams(year))
        }
      } ~
      path("players") {
        parameters('team, 'year) { (team, year) =>
          complete(realityballData.players(team, year))
        }
      } ~
      pathPrefix("predictions") {
        path("dates") {
          complete(realityballData.availablePredictionDates)
        } ~
          path(IntNumber / """.*""".r ~ Slash.?) { (dateInteger, position) =>
            val predictions = realityballData.predictions(CcyymmddFormatter.parseDateTime(dateInteger.toString), position, "Fanduel")
            complete(realityballData.dataNumericTable2(predictions._1, List("Predicted", "Actual"), predictions._2))
          } ~
          path(IntNumber / """.*""".r / """.*""".r) { (dateInteger, position, platform) =>
            val predictions = realityballData.predictions(CcyymmddFormatter.parseDateTime(dateInteger.toString), position, platform)
            complete(realityballData.dataNumericTable2(predictions._1, List("Predicted", "Actual"), predictions._2))
          } ~ {
          getFromResource("index.html")
        }
      } ~
      pathPrefix("team") {
        path("injuries") {
          parameters('team) { (team) =>
            complete(realityballData.injuries(team))
          }
        } ~
          path("fantasy") {
            parameters('team, 'year) { (team, year) =>
              complete(realityballData.dataNumericTable2(realityballData.teamFantasy(team, year), List("Total", TeamMovingAverageWindow.toString + " Day"), Nil))
            }
          } ~
          path("ballparkBA") {
            parameters('team, 'year) { (team, year) =>
              complete(realityballData.dataTable(realityballData.ballparkBA(team, year)))
            }
          } ~
          path("ballparkAttendance") {
            parameters('team, 'year) { (team, year) =>
              complete(realityballData.dataNumericTable(realityballData.ballparkAttendance(team, year), "Total"))
            }
          } ~
          path("ballparkConditions") {
            parameters('team, 'year) { (team, year) =>
              complete(realityballData.dataNumericTable2(realityballData.ballparkConditions(team, year), List("Temp (F)", "Precip (%)"), Nil))
            }
          } ~
          path("schedule") {
            parameters('team, 'year) { (team, year) =>
              complete(realityballData.schedule(team, year))
            }
          }
      } ~
      pathPrefix("pitcher") {
        path("summary") {
          parameters('player, 'year) { (player, year) =>
            complete(realityballData.pitcherSummary(player, year))
          }
        } ~
          path("outs") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataNumericTable3(realityballData.outs(player, year), List("Strike Outs", "Fly Outs", "Ground Outs"), Nil))
            }
          } ~
          path("outsTypes") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataNumericPieChart(realityballData.outsTypeCount(player, year), "Type", "Outs"))
            }
          } ~
          path("strikeRatio") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataNumericTable(realityballData.strikeRatio(player, year), "Strike / Total"))
            }
          }
      } ~
      path("fantasy") {
        parameters('player, 'year, 'gameName) { (player, year, gameName) =>
          complete(realityballData.dataNumericTable(realityballData.pitcherFantasy(player, year, gameName), "Score"))
        }
      } ~
      path("fantasyMoving") {
        parameters('player, 'year, 'gameName) { (player, year, gameName) =>
          complete(realityballData.dataNumericTable(realityballData.pitcherFantasyMoving(player, year, gameName), "Score Moving"))
        }
      } ~
      pathPrefix("batter") {
        path("summary") {
          parameters('player, 'year) { (player, year) =>
            complete(realityballData.batterSummary(player, year))
          }
        } ~
          path("BA") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.BA(player, year)))
            }
          } ~
          path("movingBA") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.movingBA(player, year)))
            }
          } ~
          path("volatilityBA") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.volatilityBA(player, year)))
            }
          } ~
          path("dailyBA") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.dailyBA(player, year)))
            }
          } ~
          path("fantasy") {
            parameters('player, 'year, 'gameName) { (player, year, gameName) =>
              complete(realityballData.dataTable(realityballData.fantasy(player, year, gameName)))
            }
          } ~
          path("fantasyMoving") {
            parameters('player, 'year, 'gameName) { (player, year, gameName) =>
              complete(realityballData.dataTable(realityballData.fantasyMoving(player, year, gameName)))
            }
          } ~
          path("slugging") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.slugging(player, year)))
            }
          } ~
          path("onBase") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.onBase(player, year)))
            }
          } ~
          path("sluggingMoving") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.sluggingMoving(player, year)))
            }
          } ~
          path("onBaseMoving") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.onBaseMoving(player, year)))
            }
          } ~
          path("sluggingVolatility") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.sluggingVolatility(player, year)))
            }
          } ~
          path("onBaseVolatility") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataTable(realityballData.onBaseVolatility(player, year)))
            }
          } ~
          path("style") {
            parameters('player, 'year) { (player, year) =>
              complete(realityballData.dataNumericPieChart(realityballData.batterStyleCounts(player, year), "Type", "At Bat Result"))
            }
          }
      } ~ get {
      getFromResourceDirectory("webapp")
    } ~ getFromResource("webapp/index.html")

  }
}
