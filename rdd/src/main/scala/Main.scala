package com.example.rdd.vector

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

import org.geotools.feature.simple._
import org.opengis.feature.simple._

import com.example.ingest.vector._

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

    val maybeBAO = Try(GdeltIngest.getAccumuloOperationsInstance("leader","instance","root","password","gwGDELT"))
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

    println (len)
  }
}
