package org.bustos.mlb

import scala.slick.driver.MySQLDriver.simple._
import scala.util.Properties.envOrNone

object RetrosheetData {
  case class BattingAverageObservation(date: String, bAvg: Double, lhBAvg: Double, rhBAvg: Double)
  case class Player(id: String, lastName: String, firstName: String, batsWith: String, throwsWith: String, team: String, position: String)
}

class RetrosheetData {

  import RetrosheetData._
  import GoogleTableJsonProtocol._
  
  val hitterRawLH: TableQuery[HitterRawLHStatsTable] = TableQuery[HitterRawLHStatsTable]
  val hitterRawRH: TableQuery[HitterRawRHStatsTable] = TableQuery[HitterRawRHStatsTable]
  val hitterStats: TableQuery[HitterDailyStatsTable] = TableQuery[HitterDailyStatsTable]
  val hitterMovingStats: TableQuery[HitterStatsMovingTable] = TableQuery[HitterStatsMovingTable]
  val hitterVolatilityStats: TableQuery[HitterStatsVolatilityTable] = TableQuery[HitterStatsVolatilityTable]
  val teamsTable: TableQuery[TeamsTable] = TableQuery[TeamsTable]
  val playersTable: TableQuery[PlayersTable] = TableQuery[PlayersTable]
  
  //val db = Database.forURL("jdbc:mysql://localhost:3306/mlbretrosheet", driver="com.mysql.jdbc.Driver", user="root", password="")
  val mysqlURL = envOrNone("MLB_MYSQL_URL").get
  val mysqlUser = envOrNone("MLB_MYSQL_USER").get
  val mysqlPassword = envOrNone("MLB_MYSQL_PASSWORD").get

  val db = Database.forURL(mysqlURL, driver="com.mysql.jdbc.Driver", user=mysqlUser, password=mysqlPassword)

  def teams: List[(String, String, String, String)] = {
    db.withSession { implicit session =>
      teamsTable.sortBy(_.mnemonic).list
    }
  }
  
  def players(team: String): List[(String, String, String, String, String, String, String)] = {
    db.withSession { implicit session =>
      playersTable.filter(_.team === team).list.sortBy(_._2)
    }
  }

  def truePlayerID(playerID: String): String = {
    if (!playerID.contains("[")) {
      playerID      
    } else {
      playerID.split("[")(1).replaceAll("]", "")
    }
  }
  
  def dataTable(data: List[BattingAverageObservation]): String = {
    val columns = List(new GoogleColumn("Date", "Date", "string"), new GoogleColumn("Total", "Total", "number"), new GoogleColumn("Lefties", "Against Lefties", "number"), new GoogleColumn("Righties", "Against Righties", "number"))
    val rows = data.map(ba => GoogleRow(List(new GoogleCell(ba.date), new GoogleCell(ba.bAvg), new GoogleCell(ba.lhBAvg), new GoogleCell(ba.rhBAvg))))
    GoogleTable(columns, rows).toJson.prettyPrint
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

  def playerSlugging(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.sluggingPercentage, p.LHsluggingPercentage, p.RHsluggingPercentage)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerOnBase(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.onBasePercentage, p.LHonBasePercentage, p.RHonBasePercentage)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerSluggingMoving(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.sluggingPercentage25, p.LHsluggingPercentage25, p.RHsluggingPercentage25)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerOnBaseMoving(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.onBasePercentage25, p.LHonBasePercentage25, p.RHonBasePercentage25)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerSluggingVolatility(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterVolatilityStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.sluggingVolatility100, p.LHsluggingVolatility100, p.RHsluggingVolatility100)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
  
  def playerOnBaseVolatility(playerID: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterVolatilityStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date).map(p => (p.date, p.onBaseVolatility100, p.LHonBaseVolatility100, p.RHonBaseVolatility100)).list
      playerStats.map({x => BattingAverageObservation(x._1, x._2, x._3, x._4)})
    }
  }
}