name := "GeowaveVectorQueryRDD"

libraryDependencies ++= Seq(
  "com.azavea.geotrellis" %% "geotrellis-geowave" % "1.0.0-SNAPSHOT",
  "com.azavea.geotrellis" %% "geotrellis-geotools" % "1.0.0-SNAPSHOT",
  "org.apache.spark" %% "spark-core" % Version.spark % "provided",
  "org.apache.hadoop" % "hadoop-client" % Version.hadoop % "provided",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
  )
