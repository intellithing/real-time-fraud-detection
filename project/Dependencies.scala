import sbt._

object Dependencies {

  object Versions {
    val flink = "1.4.0"
    val json4s = "3.5.3"
  }

  val flink = Seq(
    "org.apache.flink" %% "flink-scala" % Versions.flink,
    "org.apache.flink" %% "flink-streaming-scala" % Versions.flink
  )

  val json4s = Seq(
    "org.json4s" %% "json4s-native" % Versions.json4s,
    "org.json4s" %% "json4s-jackson" % Versions.json4s
  )
}
