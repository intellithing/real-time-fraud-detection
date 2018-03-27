
lazy val flink = (project in file("flink")).settings(
  inThisBuild(List(
    organization := "com.intellithing",
    scalaVersion := "2.11.11"
  )),
  name := "rules-engine",
  version := "1.0.0",
  mainClass in Compile := Some("com.intellithing.FlinkRulesEngine"),
  libraryDependencies ++= Dependencies.flink,
  libraryDependencies ++= Dependencies.json4s
)

