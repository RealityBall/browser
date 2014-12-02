package org.bustos.mlb

import scala.slick.driver.MySQLDriver.simple._

object RetrosheetData {
  case class BattingAverageObservation(date: String, bAvg: Double, lhBAvg: Double, rhBAvg: Double)
  case class Player(id: String, lastName: String, firstName: String, batsWith: String, throwsWith: String, team: String, position: String)
}

class RetrosheetData {

  import org.bustos.mlb.RetrosheetData._
  
  val hitterRawLH: TableQuery[HitterRawLHStatsTable] = TableQuery[HitterRawLHStatsTable]
  val hitterRawRH: TableQuery[HitterRawRHStatsTable] = TableQuery[HitterRawRHStatsTable]
  val hitterStats: TableQuery[HitterStatsTable] = TableQuery[HitterStatsTable]
  val teamsTable: TableQuery[TeamsTable] = TableQuery[TeamsTable]
  val playersTable: TableQuery[PlayersTable] = TableQuery[PlayersTable]
  
  //val db = Database.forURL("jdbc:mysql://localhost:3306/test", driver="com.mysql.jdbc.Driver", user="root", password="")
  val db = Database.forURL("jdbc:mysql://mysql.bustos.org:3306/mlbretrosheet", driver="com.mysql.jdbc.Driver", user="mlbrsheetuser", password="mlbsheetUser")

  def teams: List[String] = {
    db.withSession { implicit session =>
      teamsTable.sortBy(_.mnemonic).list.map(p => p._1)
    }
  }
  
  def players(team: String): List[String] = {
    db.withSession { implicit session =>
      //playersTable.filter(_.team === team).map(p => (p.firstName, p.lastName)).list.map({x => x._1 + " " + x._2})
      //playersTable.filter(_.team === team).list.map({x => x._3 + " " + x._2 + " [" + x._1 + "]"})
      playersTable.filter(_.team === team).list.map({x => x._1})
    }
  }
  
  def playerBA(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStats.filter(_.playerID === playerID).sortBy(_.date).map(p => (p.date, p.battingAverage, p.LHbattingAverage, p.RHbattingAverage)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
}