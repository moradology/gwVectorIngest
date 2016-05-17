package com.example.ingest.vector

import scala.util.Try

object GDeltLine {

  val gdeltCodeNames = List("GlobalEventId",                // 0
                            "Day",                          // 1
                            "MonthYear",                    // 2
                            "Year",                         // 3
                            "FractionDate",                 // 4
                            "Actor1Code",                   // 5
                            "Actor1Name",                   // 6
                            "Actor1CountryCode",            // 7
                            "Actor1KnownGroupCode",         // 8
                            "Actor1EthnicCode",             // 9
                            "Actor1Religion1Code",          // 10
                            "Actor1Religion2Code",          // 11
                            "Actor1Type1Code",              // 12
                            "Actor1Type2Code",              // 13
                            "Actor1Type3Code",              // 14
                            "Actor2Code",                   // 15
                            "Actor2Name",                   // 16
                            "Actor2CountryCode",            // 17
                            "Actor2KnownGroupCode",         // 18
                            "Actor2EthnicCode",             // 19
                            "Actor2Religion1Code",          // 20
                            "Actor2Religion2Code",          // 21
                            "Actor2Type1Code",              // 22
                            "Actor2Type2Code",              // 23
                            "Actor2Type3Code",              // 24
                            "IsRootEvent",                  // 25
                            "EventCode",                    // 26
                            "EventBaseCode",                // 27
                            "EventRootCode",                // 28
                            "QuadClass",                    // 29
                            "GoldsteinScale",               // 30
                            "NumMentions",                  // 31
                            "NumSources",                   // 32
                            "NumArticles",                  // 33
                            "AvgTone",                      // 34
                            "Actor1Geo_Type",               // 35
                            "Actor1Geo_Fullname",           // 36
                            "Actor1Geo_CountryCode",        // 37
                            "Actor1Geo_ADM1Code",           // 38
                            "Actor1Geo_Lat",                // 39
                            "Actor1Geo_Long",               // 40
                            "Actor1Geo_FeatureID",          // 41
                            "Actor2Geo_Type",               // 42
                            "Actor2Geo_Fullname",           // 43
                            "Actor2Geo_CountryCode",        // 44
                            "Actor2Geo_ADM1Code",           // 45
                            "Actor2Geo_Lat",                // 46
                            "Actor2Geo_Long",               // 47
                            "Actor2Geo_FeatureID",          // 48
                            "ActionGeo_Type",               // 49
                            "ActionGeo_Fullname",           // 50
                            "ActionGeo_CountryCode",        // 51
                            "ActionGeo_ADM1Code",           // 52
                            "ActionGeo_Lat",                // 53
                            "ActionGeo_Long",               // 54
                            "ActionGeo_FeatureID",          // 55
                            "DateAdded",                    // 56
                            "SourceURL")                    // 57

  def apply(arr: Array[String]): GDeltLine = {
    def grabI(s: String): Int = Try(s.toInt).getOrElse(0)
    def grabD(s: String): Double = Try(s.toDouble).getOrElse(0.0)

    new GDeltLine (grabI(arr(0)),  grabI(arr(1)),  grabI(arr(2)),  grabI(arr(3)),  grabD(arr(4)),
                   arr(5),         arr(6),         arr(7),         arr(8),         arr(9),
                   arr(10),        arr(11),        arr(12),        arr(13),        arr(14),
                   arr(15),        arr(16),        arr(17),        arr(18),        arr(19),
                   arr(20),        arr(21),        arr(22),        arr(23),        arr(24),
                   grabI(arr(25)), arr(26),        arr(27),        arr(28),        grabI(arr(29)),
                   grabD(arr(30)), grabI(arr(31)), grabI(arr(32)), grabI(arr(33)), grabD(arr(34)),
                   grabI(arr(35)), arr(36),        arr(37),        arr(38),        grabD(arr(39)),
                   grabD(arr(40)), grabI(arr(41)), grabI(arr(42)), arr(43),        arr(44),
                   arr(45),        grabD(arr(46)), grabD(arr(47)), grabI(arr(48)), grabI(arr(49)),
                   arr(50),        arr(51),        arr(52),        grabD(arr(53)), grabD(arr(54)),
                   grabI(arr(55)), grabI(arr(56)), arr(57))
  }
}

class GDeltLine (val globalEventId: Int,           val day: Int,
                 val monthYear: Int,               val year: Int,
                 val fractionDate: Double,         val actor1Code: String,
                 val actor1Name: String,           val actor1CountryCode: String,
                 val actor1KnownGroupCode: String, val actor1EthnicCode: String,
                 val actor1Religion1Code: String,  val actor1Religion2Code: String,
                 val actor1Type1Code: String,      val actor1Type2Code: String,
                 val actor1Type3Code: String,      val actor2Code: String,
                 val actor2Name: String,           val actor2CountryCode: String,
                 val actor2KnownGroupCode: String, val actor2EthnicCode: String,
                 val actor2Religion1Code: String,  val actor2Religion2Code: String,
                 val actor2Type1Code: String,      val actor2Type2Code: String,
                 val actor2Type3Code: String,      val isRootEvent: Int,
                 val eventCode: String,            val eventBaseCode: String,
                 val eventRootCode: String,        val quadClass: Int,
                 val goldsteinScale: Double,       val numMentions: Int,
                 val numSources: Int,              val numArticles: Int,
                 val avgTone: Double,              val actor1Geo_Type: Int,
                 val actor1Geo_Fullname: String,   val actor1Geo_CountryCode: String,
                 val actor1Geo_ADM1Code: String,   val actor1Geo_Lat: Double,
                 val actor1Geo_Long: Double,       val actor1Geo_FeatureID: Int,
                 val actor2Geo_Type: Int,          val actor2Geo_Fullname: String,
                 val actor2Geo_CountryCode: String,val actor2Geo_ADM1Code: String,
                 val actor2Geo_Lat: Double,        val actor2Geo_Long: Double,
                 val actor2Geo_FeatureID: Int,     val actionGeo_Type: Int,
                 val actionGeo_Fullname: String,   val actionGeo_CountryCode: String,
                 val actionGeo_ADM1Code: String,   val actionGeo_Lat: Double,
                 val actionGeo_Long: Double,       val actionGeo_FeatureID: Int,
                 val dateAdded: Int,               val sourceURL: String)
  

