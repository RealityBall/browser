package org.bustos.mlb

import scala.slick.driver.MySQLDriver.simple._

object RetrosheetData {
  case class BattingAverageObservation(date: String, bAvg: Double, lhBAvg: Double, rhBAvg: Double)
  case class Player(id: String, lastName: String, firstName: String, batsWith: String, throwsWith: String, team: String, position: String)
}

class RetrosheetData {

  import RetrosheetData._
  
  val hitterRawLH: TableQuery[HitterRawLHStatsTable] = TableQuery[HitterRawLHStatsTable]
  val hitterRawRH: TableQuery[HitterRawRHStatsTable] = TableQuery[HitterRawRHStatsTable]
  val hitterStats: TableQuery[HitterDailyStatsTable] = TableQuery[HitterDailyStatsTable]
  val hitterMovingStats: TableQuery[HitterStatsMovingTable] = TableQuery[HitterStatsMovingTable]
  val hitterVolatilityStats: TableQuery[HitterStatsVolatilityTable] = TableQuery[HitterStatsVolatilityTable]
  val teamsTable: TableQuery[TeamsTable] = TableQuery[TeamsTable]
  val playersTable: TableQuery[PlayersTable] = TableQuery[PlayersTable]
  
  val db = Database.forURL("jdbc:mysql://localhost:3306/mlbretrosheet", driver="com.mysql.jdbc.Driver", user="root", password="")
  //val db = Database.forURL("jdbc:mysql://mysql.bustos.org:3306/mlbretrosheet", driver="com.mysql.jdbc.Driver", user="mlbrsheetuser", password="mlbsheetUser")

  def teams: List[String] = {
    db.withSession { implicit session =>
      teamsTable.sortBy(_.mnemonic).list.map(p => p._1)
    }
  }
  
  def players(team: String): List[(String, String)] = {
    db.withSession { implicit session =>
      //playersTable.filter(_.team === team).map(p => (p.firstName, p.lastName)).list.map({x => x._1 + " " + x._2})
      playersTable.filter(_.team === team).list.map({x => (x._3 + " " + x._2, x._1)})
      //playersTable.filter(_.team === team).list.map({x => x._1})
    }
  }

  def truePlayerID(playerID: String): String = {
    if (!playerID.contains("[")) {
      playerID      
    } else {
      playerID.split("[")(1).replaceAll("]", "")
    }
  }
  
  def playerBA(playerID: String): List[BattingAverageObservation] = {
    db.withSession { implicit session =>
      val playerStats = hitterStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.battingAverage, p.LHbattingAverage, p.RHbattingAverage)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerMovingBA(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.battingAverage25, p.LHbattingAverage25, p.RHbattingAverage25)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerVolatilityBA(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterVolatilityStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.battingVolatility100, p.LHbattingVolatility100, p.RHbattingVolatility100)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerDailyBA(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.dailyBattingAverage, p.LHdailyBattingAverage, p.RHdailyBattingAverage)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerFantasy(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.fantasyScore, p.LHfantasyScore, p.RHfantasyScore)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerFantasyMoving(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.fantasyScore25, p.LHfantasyScore25, p.RHfantasyScore25)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
}