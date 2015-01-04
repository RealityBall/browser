package org.bustos.mlb

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.lifted.{ProvenShape, ForeignKeyQuery}
import spray.json._
import DefaultJsonProtocol._

object RetrosheetRecords {
  import scala.collection.mutable.Queue

  case class Statistic(var total: Double, var rh: Double, var lh: Double)
  case class StatisticInputs(var totalNumer: Int, var totalDenom: Int, var rhNumer: Int, var rhDenom: Int, var lhNumer: Int, var lhDenom: Int)
  case class RunningData(ba: Queue[StatisticInputs], obp: Queue[StatisticInputs], slugging: Queue[StatisticInputs], fantasy: Map[String, Queue[Statistic]])
  case class RunningVolatilityData(ba: Queue[StatisticInputs], obp: Queue[StatisticInputs], slugging: Queue[StatisticInputs], fantasy: Map[String, Queue[Statistic]])
  case class Team(year: String, mnemonic: String, league: String, city: String, name: String)

  case class BattingAverageObservation(date: String, bAvg: Double, lhBAvg: Double, rhBAvg: Double)
  
  case class Player(id: String, year: String, lastName: String, firstName: String, batsWith: String, throwsWith: String, team: String, position: String)
  case class PlayerSummary(id: String, RHatBats: Int, LHatBats: Int, games: Int)
  case class PitcherSummary(id: String, wins: Int, losses: Int, saves: Int, games: Int)
  case class PlayerData(meta: Player, appearances: PlayerSummary)
  case class PitcherData(meta: Player, appearances: PitcherSummary)
  
  case class PitcherDaily(id: String, date: String, var win: Boolean, var loss: Boolean, var save: Boolean,
                          var hits: Int, var walks: Int, var hitByPitch: Int, var strikeOuts: Int, var earnedRuns: Int, var outs: Int,
                          var shutout: Boolean, var noHitter: Boolean, var pitches: Int, var balls: Int)
  
  case class Game(var id: String, var homeTeam: String, var visitingTeam: String, var site: String, var date: String, var number: Int)
  case class GameConditions(var id: String, var startTime: String, var  daynight: String, var usedh: Boolean, 
      var temp: Int, var winddir: String, var windspeed: Int, var fieldcond: String, var precip: String, var sky: String)
  case class GameScoring(var id: String, var umphome: String, var ump1b: String, var ump2b: String, var ump3b: String, var howscored: String,
      var timeofgame: Int, var attendance: Int, var wp: String, var lp: String, var save: String)
}

import RetrosheetRecords._
object RetrosheetJsonProtocol extends DefaultJsonProtocol {
  import RetrosheetRecords._
  implicit val playerFormat = jsonFormat8(Player)
  implicit val playerSummaryFormat = jsonFormat4(PlayerSummary)
  implicit val pitcherSummaryFormat = jsonFormat5(PitcherSummary)
  implicit val playerDataFormat = jsonFormat2(PlayerData)
  implicit val pitcherDataFormat = jsonFormat2(PitcherData)
}

class TeamsTable(tag: Tag)
  extends Table[Team](tag, "teams") {

  def year: Column[String] = column[String]("year")
  def mnemonic: Column[String] = column[String]("mnemonic")
  def league: Column[String] = column[String]("league")
  def city: Column[String] = column[String]("city")
  def name: Column[String] = column[String]("name")

  def * = (year, mnemonic, league, city, name) <> (Team.tupled, Team.unapply)
}

class GamesTable(tag: Tag)
  extends Table[Game](tag, "games") {

  def id: Column[String] = column[String]("id", O.PrimaryKey)
  def homeTeam: Column[String] = column[String]("homeTeam")
  def visitingTeam: Column[String] = column[String]("visitingTeam")
  def site: Column[String] = column[String]("site")
  def date: Column[String] = column[String]("date")
  def number: Column[Int] = column[Int]("number")

  def * = (id, homeTeam, visitingTeam, site, date, number) <> (Game.tupled, Game.unapply)
}

class GameConditionsTable(tag: Tag)
  extends Table[GameConditions](tag, "gameConditions") {

  def id: Column[String] = column[String]("id", O.PrimaryKey)
  def startTime: Column[String] = column[String]("startTime")
  def daynight: Column[String] = column[String]("daynight")
  def usedh: Column[Boolean] = column[Boolean]("usedh")
  def temp: Column[Int] = column[Int]("temp")
  def winddir: Column[String] = column[String]("winddir")
  def windspeed: Column[Int] = column[Int]("windspeed")
  def fieldcond: Column[String] = column[String]("fieldcond")
  def precip: Column[String] = column[String]("precip")
  def sky: Column[String] = column[String]("sky")
  
  def * = (id, startTime, daynight, usedh, temp, winddir, windspeed, fieldcond, precip, sky) <> (GameConditions.tupled, GameConditions.unapply)
}

class GameScoringTable(tag: Tag)
  extends Table[GameScoring](tag, "gameScoring") {

  def id: Column[String] = column[String]("id", O.PrimaryKey)
  def umphome: Column[String] = column[String]("umphome")
  def ump1b: Column[String] = column[String]("ump1b")
  def ump2b: Column[String] = column[String]("ump2b")
  def ump3b: Column[String] = column[String]("ump3b")
  def howscored: Column[String] = column[String]("howscored")
  def timeofgame: Column[Int] = column[Int]("timeofgame")
  def attendance: Column[Int] = column[Int]("attendarnce")
  def wp: Column[String] = column[String]("wp")
  def lp: Column[String] = column[String]("lp")
  def save: Column[String] = column[String]("save")
  
  def * = (id, umphome, ump1b, ump2b, ump3b, howscored, timeofgame, attendance, wp, lp, save) <> (GameScoring.tupled, GameScoring.unapply)
}

class PitcherDailyTable(tag: Tag)
  extends Table[PitcherDaily](tag, "pitcherDaily") {

  def id: Column[String] = column[String]("id")
  def date: Column[String] = column[String]("date")
  def win: Column[Boolean] = column[Boolean]("win")
  def loss: Column[Boolean] = column[Boolean]("loss")
  def save: Column[Boolean] = column[Boolean]("save")
  
  def hits: Column[Int] = column[Int]("hits")
  def walks: Column[Int] = column[Int]("walks")
  def hitByPitch: Column[Int] = column[Int]("hitByPitch")
  def strikeOuts: Column[Int] = column[Int]("strikeOuts")
  def earnedRuns: Column[Int] = column[Int]("earnedRuns")
  def outs: Column[Int] = column[Int]("outs")

  def shutout: Column[Boolean] = column[Boolean]("shutout")
  def noHitter: Column[Boolean] = column[Boolean]("noHitter")

  def pitches: Column[Int] = column[Int]("pitches")
  def balls: Column[Int] = column[Int]("balls")

  def pk = index("pk_id_date", (id, date)) // Duplicate issue with Joaquin Benoit on 20100910
      
  def * = (id, date, win, loss, save, hits, walks, hitByPitch, strikeOuts, earnedRuns, outs, shutout, noHitter, pitches, balls) <> (PitcherDaily.tupled, PitcherDaily.unapply)
}

class PlayersTable(tag: Tag)
  extends Table[Player](tag, "players") {

  def id: Column[String] = column[String]("id")
  def year: Column[String] = column[String]("year")
  def lastName: Column[String] = column[String]("lastName")
  def firstName: Column[String] = column[String]("firstName")
  def batsWith: Column[String] = column[String]("batsWith")
  def throwsWith: Column[String] = column[String]("throwsWith")
  def team: Column[String] = column[String]("team")
  def position: Column[String] = column[String]("position")

  def pk = primaryKey("pk_id_date", (id, year))

  def * = (id, year, lastName, firstName, batsWith, throwsWith, team, position) <> (Player.tupled, Player.unapply)
}

class HitterRawLHStatsTable(tag: Tag)
  extends Table[(String, String, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)](tag, "hitterRawLHstats") {

  def date: Column[String] = column[String]("date")
  def id: Column[String] = column[String]("id")
  def LHatBat: Column[Int] = column[Int]("LHatBat")
  def LHsingle: Column[Int] = column[Int]("LHsingle")
  def LHdouble: Column[Int] = column[Int]("LHdouble")
  def LHtriple: Column[Int] = column[Int]("LHtriple")
  def LHhomeRun: Column[Int] = column[Int]("LHhomeRun")
  def LHRBI: Column[Int] = column[Int]("LHRBI")
  def LHbaseOnBalls: Column[Int] = column[Int]("LHbaseOnBalls")
  def LHhitByPitch: Column[Int] = column[Int]("LHhitByPitch")
  def LHsacFly: Column[Int] = column[Int]("LHsacFly")
  def LHsacHit: Column[Int] = column[Int]("LHsacHit")

  def pk = index("pk_id_date", (id, date)) // First game of double headers are ignored for now
      
  def * : ProvenShape[(String, String, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)] =
    (date, id, LHatBat, LHsingle, LHdouble, LHtriple, LHhomeRun, LHRBI, LHbaseOnBalls, LHhitByPitch, LHsacFly, LHsacHit)
}

class HitterRawRHStatsTable(tag: Tag)
  extends Table[(String, String, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)](tag, "hitterRawRHstats") {

  def date: Column[String] = column[String]("date")
  def id: Column[String] = column[String]("id")
  def RHatBat: Column[Int] = column[Int]("RHatBat")
  def RHsingle: Column[Int] = column[Int]("RHsingle")
  def RHdouble: Column[Int] = column[Int]("RHdouble")
  def RHtriple: Column[Int] = column[Int]("RHtriple")
  def RHhomeRun: Column[Int] = column[Int]("RHhomeRun")
  def RHRBI: Column[Int] = column[Int]("RHRBI")
  def RHbaseOnBalls: Column[Int] = column[Int]("RHbaseOnBalls")
  def RHhitByPitch: Column[Int] = column[Int]("RHhitByPitch")
  def RHsacFly: Column[Int] = column[Int]("RHsacFly")
  def RHsacHit: Column[Int] = column[Int]("RHsacHit")

  def pk = index("pk_id_date", (id, date)) // First game of double headers are ignored for now
      
  def * : ProvenShape[(String, String, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)] =
    (date, id, RHatBat, RHsingle, RHdouble, RHtriple, RHhomeRun, RHRBI, RHbaseOnBalls, RHhitByPitch, RHsacFly, RHsacHit)
}

class HitterDailyStatsTable(tag: Tag)
  extends Table[(String, String, 
      Option[Double], Option[Double], Option[Double], 
      Option[Double], Option[Double], Option[Double], 
      Option[Double], Option[Double], Option[Double], 
      Option[Double], Option[Double], Option[Double])](tag, "hitterDailyStats") {

  def date: Column[String] = column[String]("date")
  def id: Column[String] = column[String]("id")
  def RHdailyBattingAverage = column[Option[Double]]("RHdailyBattingAverage")
  def LHdailyBattingAverage = column[Option[Double]]("LHdailyBattingAverage")
  def dailyBattingAverage = column[Option[Double]]("dailyBattingAverage")
  def RHbattingAverage = column[Option[Double]]("RHbattingAverage")
  def LHbattingAverage = column[Option[Double]]("LHbattingAverage")
  def battingAverage = column[Option[Double]]("battingAverage")
  def RHonBasePercentage = column[Option[Double]]("RHonBasePercentage")
  def LHonBasePercentage = column[Option[Double]]("LHonBasePercentage")
  def onBasePercentage = column[Option[Double]]("onBasePercentage")
  def RHsluggingPercentage = column[Option[Double]]("RHsluggingPercentage")
  def LHsluggingPercentage = column[Option[Double]]("LHsluggingPercentage")
  def sluggingPercentage = column[Option[Double]]("sluggingPercentage")

  def pk = index("pk_id_date", (id, date)) // First game of double headers are ignored for now
      
  def * = (date, id, 
           RHdailyBattingAverage, LHdailyBattingAverage, dailyBattingAverage,  
           RHbattingAverage, LHbattingAverage, battingAverage,  
           RHonBasePercentage, LHonBasePercentage, onBasePercentage, 
           RHsluggingPercentage, LHsluggingPercentage, sluggingPercentage)
}

class HitterStatsMovingTable(tag: Tag)
  extends Table[(String, String, 
      Option[Double], Option[Double], Option[Double], 
      Option[Double], Option[Double], Option[Double], 
      Option[Double], Option[Double], Option[Double])](tag, "hitterMovingStats") {

  def date: Column[String] = column[String]("date")
  def id: Column[String] = column[String]("id")
  def RHbattingAverage25 = column[Option[Double]]("RHbattingAverage25")
  def LHbattingAverage25 = column[Option[Double]]("LHbattingAverage25")
  def battingAverage25 = column[Option[Double]]("battingAverage25")
  def RHonBasePercentage25 = column[Option[Double]]("RHonBasePercentage25")
  def LHonBasePercentage25 = column[Option[Double]]("LHonBasePercentage25")
  def onBasePercentage25 = column[Option[Double]]("onBasePercentage25")
  def RHsluggingPercentage25 = column[Option[Double]]("RHsluggingPercentage25")
  def LHsluggingPercentage25 = column[Option[Double]]("LHsluggingPercentage25")
  def sluggingPercentage25 = column[Option[Double]]("sluggingPercentage25")

  def pk = index("pk_id_date", (id, date))
      
  def * = (date, id, 
           RHbattingAverage25, LHbattingAverage25, battingAverage25,  
           RHonBasePercentage25, LHonBasePercentage25, onBasePercentage25, 
           RHsluggingPercentage25, LHsluggingPercentage25, sluggingPercentage25)
}

class HitterFantasyTable(tag: Tag)
  extends Table[(String, String, 
                Option[Double], Option[Double], Option[Double], 
                Option[Double], Option[Double], Option[Double], 
                Option[Double], Option[Double], Option[Double])](tag, "hitterFantasyStats") {

  def date: Column[String] = column[String]("date")
  def id: Column[String] = column[String]("id")
  def RHfanDuel = column[Option[Double]]("RHfanDuel")
  def LHfanDuel = column[Option[Double]]("LHfanDuel")
  def fanDuel = column[Option[Double]]("fanDuel")
  def RHdraftKings = column[Option[Double]]("RHdraftKings")
  def LHdraftKings = column[Option[Double]]("LHdraftKings")
  def draftKings = column[Option[Double]]("draftKings")
  def RHdraftster = column[Option[Double]]("RHdraftster")
  def LHdraftster = column[Option[Double]]("LHdraftster")
  def draftster = column[Option[Double]]("draftster")

  def pk = index("pk_id_date", (id, date))
      
  def * = (date, id, 
           RHfanDuel, LHfanDuel, fanDuel,
           RHdraftKings, LHdraftKings, draftKings,
           RHdraftster, LHdraftster, draftster)
}

class HitterFantasyMovingTable(tag: Tag)
  extends Table[(String, String, 
                Option[Double], Option[Double], Option[Double], 
                Option[Double], Option[Double], Option[Double], 
                Option[Double], Option[Double], Option[Double])](tag, "hitterFantasyMovingStats") {

  def date: Column[String] = column[String]("date")
  def id: Column[String] = column[String]("id")
  def RHfanDuel25 = column[Option[Double]]("RHfanDuel25")
  def LHfanDuel25 = column[Option[Double]]("LHfanDuel25")
  def fanDuel25 = column[Option[Double]]("fanDuel25")
  def RHdraftKings25 = column[Option[Double]]("RHdraftKings25")
  def LHdraftKings25 = column[Option[Double]]("LHdraftKings25")
  def draftKings25 = column[Option[Double]]("draftKings25")
  def RHdraftster25 = column[Option[Double]]("RHdraftster25")
  def LHdraftster25 = column[Option[Double]]("LHdraftster25")
  def draftster25 = column[Option[Double]]("draftster25")

  def pk = index("pk_id_date", (id, date))
      
  def * = (date, id, 
           RHfanDuel25, LHfanDuel25, fanDuel25,
           RHdraftKings25, LHdraftKings25, draftKings25,
           RHdraftster25, LHdraftster25, draftster25)
}

class HitterStatsVolatilityTable(tag: Tag)
  extends Table[(String, String, 
                 Option[Double], Option[Double], Option[Double], 
                 Option[Double], Option[Double], Option[Double], 
                 Option[Double], Option[Double], Option[Double])](tag, "hitterVolatilityStats") {

  def date: Column[String] = column[String]("date")
  def id: Column[String] = column[String]("id")
  def RHbattingVolatility100 = column[Option[Double]]("RHbattingVolatility100")
  def LHbattingVolatility100 = column[Option[Double]]("LHbattingVolatility100")
  def battingVolatility100 = column[Option[Double]]("battingVolatility100")
  def RHonBaseVolatility100 = column[Option[Double]]("RHonBaseVolatility100")
  def LHonBaseVolatility100 = column[Option[Double]]("LHonBaseVolatility100")
  def onBaseVolatility100 = column[Option[Double]]("onBaseVolatility100")
  def RHsluggingVolatility100 = column[Option[Double]]("RHsluggingVolatility100")
  def LHsluggingVolatility100 = column[Option[Double]]("LHsluggingVolatility100")
  def sluggingVolatility100 = column[Option[Double]]("sluggingVolatility100")

  def pk = index("pk_id_date", (id, date))
      
  def * = (date, id, 
           RHbattingVolatility100, LHbattingVolatility100, battingVolatility100,  
           RHonBaseVolatility100, LHonBaseVolatility100, onBaseVolatility100, 
           RHsluggingVolatility100, LHsluggingVolatility100, sluggingVolatility100)
}

