package com.example.vector.rdd

import geotrellis.spark.util.SparkUtils
import geotrellis.spark._
import geotrellis.spark.io.geowave._
import geotrellis.vector._
import geotrellis.geotools._

import com.vividsolutions.jts.{geom => jts}
import mil.nga.giat.geowave.core.geotime.ingest._
import mil.nga.giat.geowave.core.store._
import mil.nga.giat.geowave.core.store.query._
import mil.nga.giat.geowave.core.geotime.store.query.SpatialQuery
import mil.nga.giat.geowave.core.store.index.PrimaryIndex
import mil.nga.giat.geowave.core.geotime.GeometryUtils
import mil.nga.giat.geowave.datastore.accumulo._
import mil.nga.giat.geowave.datastore.accumulo.index.secondary.AccumuloSecondaryIndexDataStore
import mil.nga.giat.geowave.datastore.accumulo.metadata._
import mil.nga.giat.geowave.adapter.vector._
import mil.nga.giat.geowave.mapreduce.input._
import mil.nga.giat.geowave.core.store.operations.remote.options.DataStorePluginOptions
import mil.nga.giat.geowave.datastore.accumulo.operations.config.AccumuloRequiredOptions
import mil.nga.giat.geowave.datastore.accumulo.operations.config.AccumuloOptions
import org.geotools.feature._
import org.geotools.feature.simple._
import org.opengis.feature.simple._
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.conf.Configuration
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.example.vector._
import org.apache.spark.rdd._

import java.io.Closeable
import scala.util.control.NonFatal
import scala.util.{Failure, Try, Success}


object RddWrite {

  val zookeeper = "localhost"
  val instance = "geowave"
  val username = "root"
  val password = "password"
  val namespace = "gwGDELT"
  val gdeltFeatureType = GdeltIngest.createGdeltFeatureType
  val utah = (new jts.GeometryFactory()).toGeometry(new jts.Envelope(-114, -109, 37, 42))

  def writeGT() = {
    // Necessary for going between Feature and SimpleFeature
    implicit def id(x: Map[String, Any]): Seq[(String, Any)] = x.toSeq
    val philly = Point(-75.5859375, 40.713955826286046)

    val builder = new SimpleFeatureTypeBuilder()
    val ab = new AttributeTypeBuilder()
    builder.setName("TestType")
    builder.add(ab.binding(classOf[jts.Point]).nillable(false).buildDescriptor("geometry"))
    val featureType = builder.buildFeatureType()

    val features = Array(Feature(philly, Map[String, Any]()))
    val featureRDD = sc.parallelize(features)

    val gtReadRDD1 = GeoWaveFeatureRDDReader.read(
      zookeeper,
      instance,
      username,
      password,
      "testpoint",
      featureType
    )
    println(s"The first count from GT: ${gtReadRDD1.count()}")

    GeoWaveFeatureRDDWriter.write(
      featureRDD,
      zookeeper,
      instance,
      username,
      password,
      "testpoint",
      featureType
    )

    val gtReadRDD2 = GeoWaveFeatureRDDReader.read(
      zookeeper,
      instance,
      username,
      password,
      "testpoint",
      featureType
    )
    println(s"The second count from GT: ${gtReadRDD2.count()}")
  }

  def readGT() = {
    val gtRDD = GeoWaveFeatureRDDReader.read(
      zookeeper,
      instance,
      username,
      password,
      namespace,
      gdeltFeatureType,
      new SpatialQuery(utah)
    )
    println(s"The count from GT: ${gtRDD.count()}")
  }

  def readGW() = {
    // Writing to our store with GW methods
    val maybeBAO = Try(GdeltIngest.getAccumuloOperationsInstance(zookeeper, instance, username, password, namespace))
    if (maybeBAO.isFailure) {
      println("Could not create Accumulo instance")
      println(maybeBAO)
      System.exit(-1)
    }
    val bao = maybeBAO.get

    val ds = GdeltIngest.getGeowaveDataStore(bao)
    val adapter: FeatureDataAdapter = GdeltIngest.createDataAdapter(gdeltFeatureType)

    val result = Try(ds.query(new QueryOptions(adapter, GdeltIngest.createSpatialIndex), new SpatialQuery(utah)))

    val len = result match {
      case Success(results) => {
        var s = 0
        while (results.hasNext) {
          s += 1
          results.next
        }
        s
      }
      case Failure(_) => {
        println("Failure")
        0
      }
    }

    val options = new AccumuloOptions
    options.setPersistDataStatistics(false)
    //options.setUseAltIndex(true)

    val aro = new AccumuloRequiredOptions
    aro.setZookeeper(zookeeper)
    aro.setInstance(instance)
    aro.setUser(username)
    aro.setPassword(password)
    aro.setGeowaveNamespace(namespace)
    aro.setAdditionalOptions(options)

    val dspo = new DataStorePluginOptions
    dspo.selectPlugin("accumulo")
    dspo.setFactoryOptions(aro)

    val configOptions = dspo.getFactoryOptionsAsMap
    val config = Job.getInstance(sc.hadoopConfiguration).getConfiguration
    GeoWaveInputFormat.setDataStoreName(config, "accumulo")
    GeoWaveInputFormat.setStoreConfigOptions(config, configOptions)
    GeoWaveInputFormat.setQuery(config, new SpatialQuery(utah))
    GeoWaveInputFormat.setQueryOptions(config, new QueryOptions(adapter, GdeltIngest.createSpatialIndex))
    val rdd: RDD[SimpleFeature] = sc.newAPIHadoopRDD(config,
                                 classOf[GeoWaveInputFormat[SimpleFeature]],
                                 classOf[GeoWaveInputKey],
                                 classOf[SimpleFeature]).map({ case (gwIndex, sf) => sf})

    println(s"The count from GW: ${rdd.count()}")
  }


  def main(args: Array[String]): Unit = {
    println("Reading with GeoWave...")
    readGW()
    println("Reading with GeoTrellis...")
    readGT()
    println("Writing with GeoTrellis...")
    writeGT()
  }
}
