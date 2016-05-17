package com.example.ingest.vector

import org.apache.log4j.Logger

import mil.nga.giat.geowave.adapter.vector._
import mil.nga.giat.geowave.datastore.accumulo._
import mil.nga.giat.geowave.datastore.accumulo.metadata._
import mil.nga.giat.geowave.datastore.accumulo.index.secondary._
import mil.nga.giat.geowave.core.store._
import mil.nga.giat.geowave.core.store.index._
import mil.nga.giat.geowave.core.geotime.ingest._
import org.geotools.feature.AttributeTypeBuilder
import org.geotools.feature.simple._
import org.opengis.feature.simple._

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.Point

object GdeltIngest {

  val FEATURE_NAME = "GDELT"

  def getGeowaveDataStore(instance: BasicAccumuloOperations): DataStore = {
    // GeoWave persists both the index and data adapter to the same accumulo
    // namespace as the data. The intent here
    // is that all data is discoverable without configuration/classes stored
    // outside of the accumulo instance.
    return new AccumuloDataStore(
      new AccumuloIndexStore(instance),
      new AccumuloAdapterStore(instance),
      new AccumuloDataStatisticsStore(instance),
      new AccumuloSecondaryIndexDataStore(instance),
      new AccumuloAdapterIndexMappingStore(instance),
      instance);
  }

  def getAccumuloOperationsInstance(
    zookeepers: String,
    accumuloInstance: String,
    accumuloUser: String,
    accumuloPass: String,
    geowaveNamespace: String
  ): BasicAccumuloOperations = {
    return new BasicAccumuloOperations(
      zookeepers,
      accumuloInstance,
      accumuloUser,
      accumuloPass,
      geowaveNamespace)
  }

  def createDataAdapter (sft: SimpleFeatureType): FeatureDataAdapter = {
    return new FeatureDataAdapter(sft)
  }

  def createSpatialIndex(): PrimaryIndex = {
    return (new SpatialDimensionalityTypeProvider).createPrimaryIndex
  }

  /***
   * A simple feature is just a mechanism for defining attributes (a feature
   * is just a collection of attributes + some metadata) We need to describe
   * what our data looks like so the serializer (FeatureDataAdapter for this
   * case) can know how to store it. Features/Attributes are also a general
   * convention of GIS systems in general.
   * 
   * @return Simple Feature definition for our demo point feature
   */
  def createGdeltFeatureType(): SimpleFeatureType = {

    val builder = new SimpleFeatureTypeBuilder()
    val ab = new AttributeTypeBuilder()

    // Names should be unique (at least for a given GeoWave namespace) -
    // think about names in the same sense as a full classname
    // The value you set here will also persist through discovery - so when
    // people are looking at a dataset they will see the
    // type names associated with the data.
    builder.setName(FEATURE_NAME)

    // The data is persisted in a sparse format, so if data is nullable it
    // will not take up any space if no values are persisted.
    // Data which is included in the primary index (in this example
    // lattitude/longtiude) can not be null
    // Calling out latitude an longitude separately is not strictly needed,
    // as the geometry contains that information. But it's
    // convienent in many use cases to get a text representation without
    // having to handle geometries.

    // GlobalEventId
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(0)))
    // Day
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(1)))
    // MonthYear
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(2)))
    // Year
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(3)))
    // FractionDate
    builder.add(ab.binding(classOf[Double]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(4)))
    // Actor1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(5)))
    // Actor1Name
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(6)))
    // Actor1CountryCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(7)))
    // Actor1KnownGroupCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(8)))
    // Actor1EthnicCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(9)))
    // Actor1Religion1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(10)))
    // Actor1Religion2Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(11)))
    // Actor1Type1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(12)))
    // Actor1Type2Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(13)))
    // Actor1Type3Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(14)))
    // Actor2Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(15)))
    // Actor2Name
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(16)))
    // Actor2CountryCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(17)))
    // Actor2KnownGroupCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(18)))
    // Actor2EthnicCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(19)))
    // Actor2Religion1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(20)))
    // Actor2Religion2Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(21)))
    // Actor2Type1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(22)))
    // Actor2Type2Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(23)))
    // Actor2Type3Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(24)))
    // IsRootEvent
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(25)))
    // EventCode
    builder.add(ab.binding(classOf[String]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(26)))
    // EventBaseCode
    builder.add(ab.binding(classOf[String]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(27)))
    // EventRootCode
    builder.add(ab.binding(classOf[String]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(28)))
    // QuadClass
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(29)))
    // GoldsteinScale
    builder.add(ab.binding(classOf[Double]).buildDescriptor(GDeltLine.gdeltCodeNames(30)))
    // NumMentions
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(31)))
    // NumSources
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(32)))
    // NumArticles
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(33)))
    // AvgTone
    builder.add(ab.binding(classOf[Double]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(34)))
    // Actor1Geo_Type
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(35)))
    // Actor1Geo_Fullname
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(36)))
    // Actor1Geo_CountryCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(37)))
    // Actor1Geo_ADM1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(38)))
    // Actor1Geo_Lat
    builder.add(ab.binding(classOf[Double]).buildDescriptor(GDeltLine.gdeltCodeNames(39)))
    // Actor1Geo_Long
    builder.add(ab.binding(classOf[Double]).buildDescriptor(GDeltLine.gdeltCodeNames(40)))
    // Actor1Geo_FeatureID
    builder.add(ab.binding(classOf[Int]).buildDescriptor(GDeltLine.gdeltCodeNames(41)))
    // Actor2Geo_Type
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(42)))
    // Actor2Geo_Fullname
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(43)))
    // Actor2Geo_CountryCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(44)))
    // Actor2Geo_ADM1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(45)))
    // Actor2Geo_Lat
    builder.add(ab.binding(classOf[Double]).buildDescriptor(GDeltLine.gdeltCodeNames(46)))
    // Actor2Geo_Long
    builder.add(ab.binding(classOf[Double]).buildDescriptor(GDeltLine.gdeltCodeNames(47)))
    // Actor2Geo_FeatureID
    builder.add(ab.binding(classOf[Int]).buildDescriptor(GDeltLine.gdeltCodeNames(48)))
    // ActionGeo_Type
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(49)))
    // ActionGeo_Fullname
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(50)))
    // ActionGeo_CountryCode
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(51)))
    // ActionGeo_ADM1Code
    builder.add(ab.binding(classOf[String]).buildDescriptor(GDeltLine.gdeltCodeNames(52)))
    // ActionGeo_Lat
    builder.add(ab.binding(classOf[Double]).buildDescriptor(GDeltLine.gdeltCodeNames(53)))
    // ActionGeo_Long
    builder.add(ab.binding(classOf[Double]).buildDescriptor(GDeltLine.gdeltCodeNames(54)))
    // ActionGeo_FeatureID
    builder.add(ab.binding(classOf[Int]).buildDescriptor(GDeltLine.gdeltCodeNames(55)))
    // DateAdded
    builder.add(ab.binding(classOf[Int]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(56)))
    // SourceURL
    builder.add(ab.binding(classOf[String]).nillable(false).buildDescriptor(GDeltLine.gdeltCodeNames(57)))

    builder.add(ab.binding(classOf[Point]).nillable(false).buildDescriptor("geometry"))

    return builder.buildFeatureType()
  }


}
