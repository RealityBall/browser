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

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class TestMLB extends WordSpec with Matchers with ScalatestRouteTest with MLBRoutes {
  
  def actorRefFactory = system

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5 seconds)
 
  "This service" should {

    "Identify the Tigers as a team in the league" in {
      Get("/teams?year=2018") ~> routes ~> check {
        responseAs[String] should include("Tigers")
      }
    }
    "Identify Cabrera as a player on the Tigers" in {
      Get("/players?team=DET&year=2018") ~> routes ~> check {
        responseAs[String] should include("Cabrera")
      }
    }
  }
}
