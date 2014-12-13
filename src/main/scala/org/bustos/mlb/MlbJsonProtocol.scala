package org.bustos.mlb

import spray.json._
import org.bustos.mlb.RetrosheetData._

object MlbJsonProtocol extends DefaultJsonProtocol {
  implicit val playerFormat = jsonFormat7(Player)
}
