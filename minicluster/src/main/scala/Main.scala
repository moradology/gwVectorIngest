package com.example.minicluster

import java.io.File
import java.util.concurrent.TimeUnit

import org.apache.accumulo.core.conf.Property;
import org.apache.accumulo.minicluster.impl.MiniAccumuloClusterImpl;
import org.apache.accumulo.minicluster.impl.MiniAccumuloConfigImpl;
import org.apache.accumulo.monitor.Monitor;
import org.apache.hadoop.util.VersionInfo;
import org.apache.hadoop.util.VersionUtil;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

import mil.nga.giat.geowave.datastore.accumulo.minicluster.MiniAccumuloClusterFactory;

object GeoWaveDemoApp {
  def isYarn(): Boolean = {
    return VersionUtil.compareVersions(VersionInfo.getVersion(), "2.2.0") >= 0
  }


  def main(args: Array[String]) = {
    Logger.getRootLogger().setLevel(Level.INFO)

    val interactive =
      if (System.getProperty("interactive") != null)
        java.lang.Boolean.parseBoolean(System.getProperty("interactive"))
      else
        true

    val password =
      if (System.getProperty("password") != null)
        System.getProperty("password")
      else
        "password"

    val tempDir = Files.createTempDir()
    val instanceName =
      if (System.getProperty("instanceName") != null)
        System.getProperty("instanceName")
      else
        "geowave"

    val miniAccumuloConfig = new MiniAccumuloConfigImpl(tempDir, password)
      .setNumTservers(2)
      .setInstanceName(instanceName)
      .setZooKeeperPort(2181)

    miniAccumuloConfig.setProperty(Property.MONITOR_PORT, "50095")

    val accumulo = MiniAccumuloClusterFactory.newAccumuloCluster(
      miniAccumuloConfig,
      classOf[App]
    )

    println("starting up ...")
    accumulo.start()

    println("executing ...")
    accumulo.exec(classOf[Monitor])

    println("cluster running with instance name " + accumulo.getInstanceName() + " and zookeepers " + accumulo.getZooKeepers())

    if (interactive) {
      println("hit Enter to shutdown ..")
      readLine
      println("Shutting down!")
      accumulo.stop
    }
    else {
      Runtime.getRuntime().addShutdownHook(
	      new Thread() {
          new Runnable {
            def run() = {
              try {
                accumulo.stop()
              }
              catch {
                case e: Exception => println("Error shutting down accumulo.")
              }
              println("Shutting down!")
            }
          }
        }
      )

      while (true) {
	      Thread.sleep(TimeUnit.MILLISECONDS.convert(1L,TimeUnit.HOURS))
      }
    }
  }
}
