name := "GeowaveVectorQuery"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % Version.spark % "provided",
  "org.apache.hadoop" % "hadoop-client" % Version.hadoop % "provided",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
  )