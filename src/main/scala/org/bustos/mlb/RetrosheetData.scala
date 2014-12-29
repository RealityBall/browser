package org.bustos.mlb

import scala.slick.driver.MySQLDriver.simple._
import scala.util.Properties.envOrNone
import spray.json._
import DefaultJsonProtocol._ 
import scala.slick.jdbc.{GetResult, StaticQuery => Q}

class RetrosheetData {

  import RetrosheetRecords._
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

  def teams(year: String): List[(String, String, String, String, String)] = {
    db.withSession { implicit session =>
      if (year == "All") {
        Q.queryNA[(String, String, String, String, String)]("select distinct '2014', mnemonic, league, city, name from teams order by mnemonic").list
      } else {
        teamsTable.filter(_.year === year).sortBy(_.mnemonic).list
      }
    }
  }
  
  def players(team: String, year: String): List[Player] = {
    db.withSession { implicit session =>
      if (year == "All") {
        val groups = playersTable.filter(_.team === team).list.groupBy(_.id)
        val players = groups.mapValues(_.head).values
        val partitions = players.partition(_.position != "P")
        partitions._1.toList.sortBy(_.lastName) ++ partitions._2.toList.sortBy(_.lastName)
      } else {
        val partitions = playersTable.filter({x => x.team === team && x.year === year}).list.partition(_.position != "P")
        partitions._1.sortBy(_.lastName) ++ partitions._2.sortBy(_.lastName)        
      } 
    }
  }

  def playerSummary(playerID: String, year: String): PlayerData = {
    db.withSession { implicit session =>
      val playerMnemonic = truePlayerID(playerID)
      if (year == "All") {
        val player = playersTable.filter(_.mnemonic === playerMnemonic).list.head
        val RHatBats = hitterRawRH.filter(_.playerID === playerMnemonic).map(_.RHatBat).sum.run.get
        val LHatBats = hitterRawLH.filter(_.playerID === playerMnemonic).map(_.LHatBat).sum.run.get
        val gamesQuery = for {
                           r <- hitterRawLH if r.playerID === playerMnemonic;
                           l <- hitterRawRH if (l.playerID === playerMnemonic && l.date === r.date)
                         } yield (1)
        val summary = PlayerSummary(playerMnemonic, RHatBats, LHatBats, gamesQuery.length.run)
        PlayerData(player, summary)
      } else {
        val player = playersTable.filter({x => x.mnemonic === playerMnemonic && x.year.startsWith(year)}).list.head
        val RHatBats = hitterRawRH.filter({x => x.playerID === playerMnemonic && x.date.startsWith(year)}).map(_.RHatBat).sum.run.get
        val LHatBats = hitterRawLH.filter({x => x.playerID === playerMnemonic && x.date.startsWith(year)}).map(_.LHatBat).sum.run.get
        val gamesQuery = for {
                           r <- hitterRawLH if r.playerID === playerMnemonic && r.date.startsWith(year);
                           l <- hitterRawRH if (l.playerID === playerMnemonic && l.date === r.date)
                         } yield (1)
        val summary = PlayerSummary(playerMnemonic, RHatBats, LHatBats, gamesQuery.length.run)
        PlayerData(player, summary)        
      }
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
  
  def displayDouble(x: Option[Double]): Double = {
    x match {
      case None => Double.NaN
      case _ => x.get
    }
  }
  
  def years: List[String] = {
    db.withSession { implicit session =>
      "All" :: (Q.queryNA[String]("select distinct(substring(date, 1, 4)) as year from games order by year").list)
    }
  }
  
  def hitterStatsQuery(playerID: String, year: String): Query[HitterDailyStatsTable, HitterDailyStatsTable#TableElementType, Seq] = {
    if (year == "All") hitterStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date)
    else hitterStats.filter({x => x.playerID === truePlayerID(playerID) && x.date.startsWith(year)}).sortBy(_.date)
  }
  
  def hitterMovingStatsQuery(playerID: String, year: String): Query[HitterStatsMovingTable, HitterStatsMovingTable#TableElementType, Seq] = {
    if (year == "All") hitterMovingStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date)
    else hitterMovingStats.filter({x => x.playerID === truePlayerID(playerID) && x.date.startsWith(year)}).sortBy(_.date)
  }
  
  def hitterVolatilityStatsQuery(playerID: String, year: String): Query[HitterStatsVolatilityTable, HitterStatsVolatilityTable#TableElementType, Seq] = {
    if (year == "All") hitterVolatilityStats.filter(_.playerID === truePlayerID(playerID)).sortBy(_.date)
    else hitterVolatilityStats.filter({x => x.playerID === truePlayerID(playerID) && x.date.startsWith(year)}).sortBy(_.date)
  }
  
  def playerBA(playerID: String, year: String): List[BattingAverageObservation] = {
    db.withSession { implicit session =>
      val playerStats = hitterStatsQuery(playerID, year).map(p => (p.date, p.battingAverage, p.LHbattingAverage, p.RHbattingAverage)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerMovingBA(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStatsQuery(playerID, year).map(p => (p.date, p.battingAverage25, p.LHbattingAverage25, p.RHbattingAverage25)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerVolatilityBA(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterVolatilityStatsQuery(playerID, year).map(p => (p.date, p.battingVolatility100, p.LHbattingVolatility100, p.RHbattingVolatility100)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerDailyBA(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStatsQuery(playerID, year).map(p => (p.date, p.dailyBattingAverage, p.LHdailyBattingAverage, p.RHdailyBattingAverage)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerFantasy(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStatsQuery(playerID, year).map(p => (p.date, p.fantasyScore, p.LHfantasyScore, p.RHfantasyScore)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerFantasyMoving(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStatsQuery(playerID, year).map(p => (p.date, p.fantasyScore25, p.LHfantasyScore25, p.RHfantasyScore25)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }

  def playerSlugging(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStatsQuery(playerID, year).map(p => (p.date, p.sluggingPercentage, p.LHsluggingPercentage, p.RHsluggingPercentage)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerOnBase(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterStatsQuery(playerID, year).map(p => (p.date, p.onBasePercentage, p.LHonBasePercentage, p.RHonBasePercentage)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerSluggingMoving(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStatsQuery(playerID, year).map(p => (p.date, p.sluggingPercentage25, p.LHsluggingPercentage25, p.RHsluggingPercentage25)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerOnBaseMoving(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterMovingStatsQuery(playerID, year).map(p => (p.date, p.onBasePercentage25, p.LHonBasePercentage25, p.RHonBasePercentage25)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerSluggingVolatility(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterVolatilityStatsQuery(playerID, year).sortBy(_.date).map(p => (p.date, p.sluggingVolatility100, p.LHsluggingVolatility100, p.RHsluggingVolatility100)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
  
  def playerOnBaseVolatility(playerID: String, year: String): List[BattingAverageObservation] = {        
    db.withSession { implicit session =>
      val playerStats = hitterVolatilityStatsQuery(playerID, year).sortBy(_.date).map(p => (p.date, p.onBaseVolatility100, p.LHonBaseVolatility100, p.RHonBaseVolatility100)).list
      playerStats.map({x => BattingAverageObservation(x._1, displayDouble(x._2), displayDouble(x._3), displayDouble(x._4))})
    }
  }
}