package com.example.vector.rdd

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

import org.geotools.feature.simple._
import org.opengis.feature.simple._

import geotrellis.spark.util.SparkUtils
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.conf.Configuration
import org.apache.log4j.Logger
import org.apache.log4j.Level

import com.example.vector._

import java.io.Closeable
import scala.util.control.NonFatal
import scala.util.{Failure, Try, Success}

object TryWith {
  def apply[C <: Closeable, R](resource: => C)(f: C => R): Try[R] =
    Try(resource).flatMap(resourceInstance => {
      try {
        val returnValue = f(resourceInstance)
        Try(resourceInstance.close()).map(_ => returnValue)
      }
      catch {
        case NonFatal(exceptionInFunction) =>
          try {
            resourceInstance.close()
            Failure(exceptionInFunction)
          }
          catch {
            case NonFatal(exceptionInClose) =>
              exceptionInFunction.addSuppressed(exceptionInClose)
              Failure(exceptionInFunction)
          }
      }
    })
}

object GdeltRddMain {

  def main(args: Array[String]): Unit = {

    //Logger.getRootLogger().setLevel(Level.WARN)
    

    val zookeeper = Try(args(0)).getOrElse("localhost")
    val instance = Try(args(1)).getOrElse("geowave")
    val username = Try(args(2)).getOrElse("root")
    val password = Try(args(3)).getOrElse("password")
    val namespace = Try(args(4)).getOrElse("gwGDELT")

    val maybeBAO = Try(GdeltIngest.getAccumuloOperationsInstance(zookeeper, instance, username, password, namespace))
    if (maybeBAO.isFailure) {
      println("Could not create Accumulo instance")
      println(maybeBAO)
      System.exit(-1)
    }
    val bao = maybeBAO.get
    val ds = GdeltIngest.getGeowaveDataStore(bao)

    val gf = new jts.GeometryFactory()
    val utah = gf.toGeometry(new jts.Envelope(-114, -109, 37, 42))

    val adapter: FeatureDataAdapter = GdeltIngest.createDataAdapter(GdeltIngest.createGdeltFeatureType)
    val index: PrimaryIndex = GdeltIngest.createSpatialIndex

    val result = Try (ds.query (new QueryOptions(adapter, index), new SpatialQuery(utah))) 

    val len = result match { 
      case Success(results) => {
        println("Success")
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

    implicit val sc = SparkUtils.createSparkContext("gwVectorIngestRDD")
    println("SparkContext created!")

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
    GeoWaveInputFormat.setQueryOptions(config, new QueryOptions(adapter, index))
    val rdd = sc.newAPIHadoopRDD(config,
                                 classOf[GeoWaveInputFormat[SimpleFeature]],
                                 classOf[GeoWaveInputKey],
                                 classOf[SimpleFeature])
 
    println("\tSize of query response from RDD:     " ++ rdd.count.toString)
    println("\tSize of query response from GeoWave: " ++ len.toString)
  }
}
