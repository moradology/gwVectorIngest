name := "geowave-vector-ingest"

version := "0.1.0"
scalaVersion := "2.10.5"
crossScalaVersions := Seq("2.11.5", "2.10.5")
organization := "com.azavea"
licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-Yinline-warnings",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:existentials",
  "-feature")
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := { _ => false }

resolvers += Resolver.bintrayRepo("azavea", "geotrellis")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.5.2",
  "org.scalatest"       %%  "scalatest"      % "2.2.0" % "test",
  "org.typelevel" %% "cats" % "0.5.0"
)

resolvers ++= Seq(
  "geosolutions" at "http://maven.geo-solutions.it/",
  "osgeo" at "http://download.osgeo.org/webdav/geotools/",
  "boundlessgeo" at "https://boundless.artifactoryonline.com/boundless/main",
  "geowave" at "http://geowave-maven.s3-website-us-east-1.amazonaws.com/release"
)
resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "com.azavea.geotrellis" %% "geotrellis-accumulo" % Version.geotrellis,
  "com.azavea.geotrellis" %% "geotrellis-spark" % Version.geotrellis,
  "org.apache.spark" %% "spark-core" % "1.5.2" % "provided",
  "org.geotools" % "gt-coverage" % Version.geotools,
  "org.geotools" % "gt-epsg-hsql" % Version.geotools,
  "org.geotools" % "gt-geotiff" % Version.geotools,
  "org.geotools" % "gt-main" % Version.geotools,
  "org.geotools" % "gt-referencing" % Version.geotools,
  "mil.nga.giat" % "geowave-adapter-vector" % "0.9.1",
  "mil.nga.giat" % "geowave-datastore-accumulo" % "0.9.1",
  "com.github.melrief" %% "purecsv" % "0.0.6",
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
)

// When creating fat jar, remote some files with
// bad signatures and resolve conflicts by taking the first
// versions of shared packaged types.
assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF\\MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.discard
  case "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
  case _ => MergeStrategy.first
}

