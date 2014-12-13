package org.bustos.mlb

import spray.json._
import DefaultJsonProtocol._ 

class GoogleCell(val v: Any) {}

object GoogleCellJsonProtocol extends DefaultJsonProtocol {
  import RetrosheetData._ 
  implicit object GoogleCellFormat extends RootJsonFormat[GoogleCell] {
    def write(c: GoogleCell) = c.v match {
        case x: String => JsObject("v" -> JsString(x))
        case x: Double => JsObject("v" -> JsNumber(x))
    }
    def read(value: JsValue) = value match {
      case _ => deserializationError("Undefined Read")
    }
  }
}

object GoogleTableJsonProtocol extends DefaultJsonProtocol {
  import RetrosheetData._
  import GoogleCellJsonProtocol._
  
  case class GoogleRow(c: List[GoogleCell])
  case class GoogleColumn(id: String, label: String, typeName: String)
  case class GoogleTable(cols: List[GoogleColumn], rows: List[GoogleRow])

  implicit val googleRowJSON = jsonFormat1(GoogleRow)
  implicit val googleColumnJSON = jsonFormat3(GoogleColumn)
  implicit val googleTableJSON = jsonFormat2(GoogleTable)
}