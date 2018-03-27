package com.intellithing

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.apache.flink.streaming.api.scala._
import org.apache.flink.api.java.utils.ParameterTool
import scala.collection.mutable.HashSet
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.api.common.functions.AggregateFunction
import scala.collection.mutable.ListBuffer


object FlinkRulesEngine {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val text = env.fromElements("hello world", "hello intellithing", "intellithing is a great place to work")

    val windowCounts = text
      .flatMap { w => w.split("\\s") }
      .map { w => WordWithCount(w, 1) }
      .keyBy("word")
      .sum("count")
    windowCounts.print().setParallelism(1)

    // config port to read stream from
    val port: Int = try {
        ParameterTool.fromArgs(args).getInt("port")
    } catch {
        case e: Exception => {
           System.err.println("No port specified. Please run 'FlinkRulesEngine --port <port>'")
        return
      }
    }

    // get input data by connecting to the socket
    val socketText = env.socketTextStream("localhost", port, '\n')

    // split stream according to data type
    val recordStream = socketText.map{accountRecord => {
      var jsonMap: Map[String, Any] = try {
        parse(accountRecord).values.asInstanceOf[Map[String, Any]]
      } catch {
        case e: Exception => {
          Map("data_type" -> "malformat")
        }
      }
      val dataType = jsonMap("data_type")
      val record: Record = dataType match {
        case "account" => Record("account", jsonMap("id").toString, jsonMap("created_from_ip").toString)
        case "ticket" => Record("ticket", jsonMap("account_id").toString, jsonMap("content").toString)
        case "user" => Record("user", jsonMap("account_id").toString, jsonMap("id").toString)
        case _ => Record("malformat", null, accountRecord.toString)
      }
      record
    }}

    val splitted: SplitStream[Record] = recordStream
      .split(r => Seq (r.typeName))

    // catch accounts created by bad ips
    val accountStream = splitted.select("account")
    val aggBase:DataStream[AccountAggregation] = accountStream
      .filter{ accountRecord => isBadIp(accountRecord.data) }
      .map( record => AccountAggregation(record.accountId,record.data,1) )
    val keyedStream = aggBase
                      .keyBy(_.id)
    val ipReasonDataStream: DataStream[(String, (String, String))] =
        keyedStream.map { riskyIP => (riskyIP.id, ("ip", riskyIP.ip)) }
    ipReasonDataStream.print().setParallelism(1)

    // ticket record handling
    val ticketRecordDataStream = splitted.select("ticket")
    val ticketTupleDataStream = ticketRecordDataStream
                                .flatMap { tr => {
                                            val words = tr.data.split("\\s")
                                            words.map(word => TicketTuple(tr.accountId, word))
                                                } }
    val riskyTicketDataStream = ticketTupleDataStream
                                .filter {
                                        ticketTuple => isRiskyWord(ticketTuple.word) }
                                .keyBy(_.accountId)
                                .timeWindow(Time.seconds(1))
                                .aggregate(new WordAggregate)

    // convert to (accountId, ("word", word)) format
    val wordReasonDataStream: DataStream[(String, (String, String))] =
      riskyTicketDataStream.flatMap { riskyTicket => {
                                        val words = riskyTicket._2.toArray
                                        words.map(word => (riskyTicket._1, ("word", word)))
                                }}
    
    // user record handling
    val userNumberLimit = 5
    val userStream: DataStream[Record] = splitted.select("user")
    val userCountsDataStream = userStream
      .map{userRecord => (userRecord.accountId,1)}
      .keyBy(0)
      .countWindow(userNumberLimit)
      .sum(1)

    val userReasonDataStream: DataStream[(String, (String, String))] =
        userCountsDataStream.map { userCount => (userCount._1, ("user", userNumberLimit.toString)) }
    
    // aggregate all red flags
    val allReasonsStreams: DataStream[(String, (String, String))] =
        wordReasonDataStream.union(ipReasonDataStream).union(userReasonDataStream);
    allReasonsStreams.keyBy(_._1)
        .timeWindow(Time.seconds(10))
        .aggregate(new ReasonAggregateFunction)
        .print()
        .setParallelism(1)

    // malformatted record handling
    val malStream: DataStream[Record] = splitted.select("malformat")
    malStream.print().setParallelism(1)

    env.execute("FlinkRulesEngine")
  }

  case class ReasonsRecord(
    accountId: String
    , reasons: ListBuffer[(String, String)])
  {
    override def toString: String = {
      var msg: String = "Account " + accountId + " is risky because"
      msg = reasons.foldLeft(msg) {(z,f) =>
        val reason = f._1 match {
          case "ip" => "\n- It is created from a known bad ip: " + f._2
          case "word" => "\n- It created a spammy ticket with '" + f._2 + "' word in it"
          case "user" => "\n- Number of users is over threshold"
        }
        z.concat(reason)
      }
      msg
    }
  }

  class ReasonAggregateFunction extends AggregateFunction[(String, (String, String)), ReasonsRecord, ReasonsRecord] {
    override def createAccumulator() = (ReasonsRecord("", ListBuffer()))

    override def add(value: (String, (String, String)), accumulator: ReasonsRecord) =
    ReasonsRecord(value._1, accumulator.reasons += value._2)

    override def getResult(accumulator: ReasonsRecord) = accumulator

    override def merge(a: ReasonsRecord, b: ReasonsRecord) = ReasonsRecord(a.accountId, a.reasons ++ b.reasons)
  }

  class WordAggregate extends AggregateFunction[TicketTuple, (String, HashSet[String]), (String, HashSet[String])] {
    override def createAccumulator() = ("", HashSet[String]())

    override def add(value: TicketTuple, accumulator: (String, HashSet[String])) =
    (value.accountId, accumulator._2 += value.word)

    override def getResult(accumulator: (String, HashSet[String])) = accumulator

    override def merge(a: (String, HashSet[String]), b: (String, HashSet[String])) = (a._1, a._2 ++ b._2)
  }

  val rickyWords: HashSet[String] = HashSet("Apple","password", "Hello")
  def isRiskyWord(word: String) : Boolean = {
    val isWordRisky = rickyWords.contains(word)
    return isWordRisky
  }

  case class TicketTuple (
    accountId: String
    , word: String
  )

  case class Record(
    typeName: String
    , accountId: String
    , data: String
  )

  def isBadIp(ip: String) : Boolean = {
    val badIPs: HashSet[String] = HashSet("192.30.71.11","202.62.86.10")
    return badIPs(ip)
  }

  case class Account(
    id: String
    , ip: String
  )

  case class AccountAggregation(
    id:String
   , ip:String
   , total: Integer
 ) {
     override def toString: String = "Account " + id + " is risky because\n" + "- It is created from a known bad ip: " + ip;
   }

  case class WordWithCount(word: String, count: Long)

}
