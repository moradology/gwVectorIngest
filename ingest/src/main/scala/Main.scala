package com.example.ingest.vector

import mil.nga.giat.geowave.core.geotime.ingest._
import mil.nga.giat.geowave.core.store._
import mil.nga.giat.geowave.core.store.index.PrimaryIndex
import mil.nga.giat.geowave.core.geotime.GeometryUtils
import mil.nga.giat.geowave.datastore.accumulo._
import mil.nga.giat.geowave.datastore.accumulo.index.secondary.AccumuloSecondaryIndexDataStore
import mil.nga.giat.geowave.datastore.accumulo.metadata._

import org.geotools.feature.simple._
import org.opengis.feature.simple._

import com.vividsolutions.jts.geom.Coordinate

import scala.util.Try

import java.util.zip._
import scala.collection.JavaConversions._

object GdeltIngestMain {

  def gdeltToSimpleFeature(gd: GDeltLine, build: SimpleFeatureBuilder): SimpleFeature = {
    build.set("GlobalEventId", gd.globalEventId)
    build.set("Day", gd.day)
    build.set("MonthYear", gd.monthYear)
    build.set("Year", gd.year)
    build.set("FractionDate", gd.fractionDate)
    build.set("Actor1Code", gd.actor1Code)
    build.set("Actor1Name", gd.actor1Name)
    build.set("Actor1CountryCode", gd.actor1CountryCode)
    build.set("Actor1KnownGroupCode", gd.actor1KnownGroupCode)
    build.set("Actor1EthnicCode", gd.actor1EthnicCode)
    build.set("Actor1Religion1Code", gd.actor1Religion1Code)
    build.set("Actor1Religion2Code", gd.actor1Religion2Code)
    build.set("Actor1Type1Code", gd.actor1Type1Code)
    build.set("Actor1Type2Code", gd.actor1Type2Code)
    build.set("Actor1Type3Code", gd.actor1Type3Code)
    build.set("Actor2Code", gd.actor2Code)
    build.set("Actor2Name", gd.actor2Name)
    build.set("Actor2CountryCode", gd.actor2CountryCode)
    build.set("Actor2KnownGroupCode", gd.actor2KnownGroupCode)
    build.set("Actor2EthnicCode", gd.actor2EthnicCode)
    build.set("Actor2Religion1Code", gd.actor2Religion1Code)
    build.set("Actor2Religion2Code", gd.actor2Religion2Code)
    build.set("Actor2Type1Code", gd.actor2Type1Code)
    build.set("Actor2Type2Code", gd.actor2Type2Code)
    build.set("Actor2Type3Code", gd.actor2Type3Code)
    build.set("IsRootEvent", gd.isRootEvent)
    build.set("EventCode", gd.eventCode)
    build.set("EventBaseCode", gd.eventBaseCode)
    build.set("EventRootCode", gd.eventRootCode)
    build.set("QuadClass", gd.quadClass)
    build.set("GoldsteinScale", gd.goldsteinScale)
    build.set("NumMentions", gd.numMentions)
    build.set("NumSources", gd.numSources)
    build.set("NumArticles", gd.numArticles)
    build.set("AvgTone", gd.avgTone)
    build.set("Actor1Geo_Type", gd.actor1Geo_Type)
    build.set("Actor1Geo_Fullname", gd.actor1Geo_Fullname)
    build.set("Actor1Geo_CountryCode", gd.actor1Geo_CountryCode)
    build.set("Actor1Geo_ADM1Code", gd.actor1Geo_ADM1Code)
    build.set("Actor1Geo_Lat", gd.actor1Geo_Lat)
    build.set("Actor1Geo_Long", gd.actor1Geo_Long)
    build.set("Actor1Geo_FeatureID", gd.actor1Geo_FeatureID)
    build.set("Actor2Geo_Type", gd.actor2Geo_Type)
    build.set("Actor2Geo_Fullname", gd.actor2Geo_Fullname)
    build.set("Actor2Geo_CountryCode", gd.actor2Geo_CountryCode)
    build.set("Actor2Geo_ADM1Code", gd.actor2Geo_ADM1Code)
    build.set("Actor2Geo_Lat", gd.actor2Geo_Lat)
    build.set("Actor2Geo_Long", gd.actor2Geo_Long)
    build.set("Actor2Geo_FeatureID", gd.actor2Geo_FeatureID)
    build.set("ActionGeo_Type", gd.actionGeo_Type)
    build.set("ActionGeo_Fullname", gd.actionGeo_Fullname)
    build.set("ActionGeo_CountryCode", gd.actionGeo_CountryCode)
    build.set("ActionGeo_ADM1Code", gd.actionGeo_ADM1Code)
    build.set("ActionGeo_Lat", gd.actionGeo_Lat)
    build.set("ActionGeo_Long", gd.actionGeo_Long)
    build.set("ActionGeo_FeatureID", gd.actionGeo_FeatureID)
    build.set("DateAdded", gd.dateAdded)
    build.set("SourceURL", gd.sourceURL)

    build.set("geometry", GeometryUtils.GEOMETRY_FACTORY.createPoint(new Coordinate(gd.actor1Geo_Long, gd.actor1Geo_Lat)))

    build.buildFeature(gd.globalEventId.toString)
  }

  def ingestFiles(iters: Array[Iterator[String]], ds: DataStore): Unit = {
    val record = GdeltIngest.createGdeltFeatureType
    val recBuilder = new SimpleFeatureBuilder(record)

    val adapter = GdeltIngest.createDataAdapter(record)
    val index = GdeltIngest.createSpatialIndex

    val indexWriter = ds.createWriter(adapter, index).asInstanceOf[IndexWriter[SimpleFeature]]
    

    iters foreach { iter =>
      iter foreach { s =>
        val arr = s.split("\t")
        val gdl = GDeltLine(arr)

        val gdsf = gdeltToSimpleFeature(gdl, recBuilder)
        indexWriter.write(gdsf)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Too few arguments: expecting <file1> [<file2> ...]")
      System.exit(-1)
    }

    val inputs = (args flatMap { 
      filename => 
        val zf = Try(new ZipFile(filename))
        if (zf.isSuccess)
          zf.get.entries.map { e => scala.io.Source.fromInputStream(zf.get.getInputStream(e)) }
        else
          List(scala.io.Source.fromFile(filename))
    }).map(_.getLines)

    val maybeBAO = Try(GdeltIngest.getAccumuloOperationsInstance("leader","instance","root","password","gwGDELT"))
    if (maybeBAO.isFailure) {
      println("Could not create Accumulo instance")
      println(maybeBAO)
      System.exit(-1)
    }
    val bao = maybeBAO.get
    val ds = GdeltIngest.getGeowaveDataStore(bao)

    ingestFiles(inputs, ds);

  }
}
