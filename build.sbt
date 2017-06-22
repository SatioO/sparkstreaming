name := "Simple Project"

version := "1.1"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val sparkV = "2.1.1"
  Seq(
    "org.apache.spark" %% "spark-core" % sparkV % "provided",
    "org.apache.spark" %% "spark-streaming" % sparkV % "provided",
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkV % "provided",
    "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkV % "provided",
    "org.apache.spark" %% "spark-sql" % sparkV % "provided",
    "org.apache.spark" %% "spark-hive" % sparkV % "provided"
  )
}



