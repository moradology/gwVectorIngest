lazy val commonSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.10.6",
  crossScalaVersions := Seq("2.11.5", "2.10.5"),
  organization := "com.example",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-Yinline-warnings",
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:existentials",
    "-feature"),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },

  resolvers ++= Seq(
    "geosolutions" at "http://maven.geo-solutions.it/",
    "osgeo" at "http://download.osgeo.org/webdav/geotools/",
    "boundlessgeo" at "https://boundless.artifactoryonline.com/boundless/main",
    "geowave" at "http://geowave-maven.s3-website-us-east-1.amazonaws.com/snapshot"
  ),
  resolvers += Resolver.sonatypeRepo("releases"),

  libraryDependencies ++= Seq(
    "com.azavea.geotrellis" %% "geotrellis-accumulo" % Version.geotrellis,
    //"org.apache.spark" %% "spark-core" % "1.5.2" % "provided",
    "org.geotools" % "gt-coverage" % Version.geotools,
    "org.geotools" % "gt-epsg-hsql" % Version.geotools,
    "org.geotools" % "gt-geotiff" % Version.geotools,
    "org.geotools" % "gt-main" % Version.geotools,
    "org.geotools" % "gt-referencing" % Version.geotools,
    "mil.nga.giat" % "geowave-adapter-vector" % "0.9.2-SNAPSHOT",
    "mil.nga.giat" % "geowave-datastore-accumulo" % "0.9.2-SNAPSHOT",
    "org.apache.accumulo" % "accumulo-monitor" % "1.7.0",
    compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
  ),

  // When creating fat jar, remote some files with
  // bad signatures and resolve conflicts by taking the first
  // versions of shared packaged types.
  //assemblyMergeStrategy in assembly := {
  //  case "reference.conf" => MergeStrategy.concat
  //  case "application.conf" => MergeStrategy.concat
  //  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  //  case "META-INF\\MANIFEST.MF" => MergeStrategy.discard
  //  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.discard
  //  case "META-INF/ECLIPSEF.SF" => MergeStrategy.discard
  //  case "META-INF/BCKEY.SF" => MergeStrategy.discard
  //  case "META-INF/BCKEY.DSA" => MergeStrategy.discard
  //  case _ => MergeStrategy.first
  //}

  assemblyMergeStrategy in assembly := {
    case "reference.conf" => MergeStrategy.concat
    case "application.conf" => MergeStrategy.concat
    case PathList("META-INF", xs @ _*) =>
      xs match {
        case ("MANIFEST.MF" :: Nil) => MergeStrategy.discard
        // Concatenate everything in the services directory to keep
        // GeoTools happy.
        case ("services" :: _ :: Nil) =>
          MergeStrategy.concat
        // Concatenate these to keep JAI happy.
        case ("javax.media.jai.registryFile.jai" :: Nil) | ("registryFile.jai" :: Nil) | ("registryFile.jaiext" :: Nil) =>
          MergeStrategy.concat
        case (name :: Nil) => {
          // Must exclude META-INF/*.([RD]SA|SF) to avoid "Invalid
          // signature file digest for Manifest main attributes"
          // exception.
          if (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".SF"))
            MergeStrategy.discard
          else
            MergeStrategy.first
        }
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.first
  }
)

lazy val root = Project("gwVectorIngest", file("."))
  .settings(commonSettings: _*)

lazy val base = Project("base", file("base"))
  .settings(commonSettings: _*)

lazy val ingest = Project("ingest", file("ingest"))
  .settings(commonSettings: _*)
  .dependsOn(base)

lazy val rdd = Project("rdd", file("rdd"))
  .settings(commonSettings: _*)
  .dependsOn(base)

lazy val minicluster = Project("minicluster", file("minicluster"))
  .settings(commonSettings: _*)

