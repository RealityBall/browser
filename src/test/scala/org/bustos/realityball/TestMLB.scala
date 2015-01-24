package org.bustos.realityball

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http.StatusCodes._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import scala.concurrent.duration.FiniteDuration

class TestMLB extends Specification with Specs2RouteTest with MLBRoutes {
  
  def actorRefFactory = system
  
  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(5, "second"))
 
  "This service" should {

    "return a greeting for GET requests to the graph" in {
      Get("/graph") ~> mlbRoutes ~> check {
        responseAs[String] must contain("Retrosheet")
      }
    }
    "Identify the Tigers as a team in the league" in {
      Get("/teams") ~> mlbRoutes ~> check {
        responseAs[String] must contain("Tigers")
      }
    }
    "Identify Cabrera as a player on the Tigers" in {
      Get("/players?team=DET") ~> mlbRoutes ~> check {
        responseAs[String] must contain("Cabrera")
      }
    }
  }
}
