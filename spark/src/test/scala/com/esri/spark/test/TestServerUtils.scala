package com.esri.spark.test

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.curator.test.TestingServer

import scala.collection.JavaConversions.mapAsJavaMap

object TestServerUtils {

  /**
    * Utility method used to get the zookeeper test server's connection string or start a test server
    *
    * @return
    */
  def getOrStartZookeeperTestServer(): String = {
    val name = "StartZooKeeperTestServer"
    if (ProcessUtils.doesProcessAlreadyExists(name, false)) {
      s"localhost:${Defaults.ZKPort}"
    } else {
      val zkServer: TestingServer = new TestingServer(Defaults.ZKPort)
      zkServer.getConnectString
    }
  }


  /**
    * Utility method used to get the Kafka test server's connection string or start a test server
    *
    * @return
    */
  def getOrStartKafkaTestServer(zkConnectionString: String): String = {
    val name = "StartKafkaTestServer"
    if (!ProcessUtils.doesProcessAlreadyExists(name, false)) {
      val properties = mapAsJavaMap(
        Map(
          "zookeeper.connect" -> zkConnectionString,
          "port" -> Defaults.KafkaPort.toString
        )
      )
      //setup kafka server
      val kafkaConfig = KafkaConfig(properties)
      val kafkaServer: KafkaServerStartable = new KafkaServerStartable(kafkaConfig)

      //startup kafka and shutdown hook
      kafkaServer.startup()
      sys.addShutdownHook {
        kafkaServer.shutdown()
      }
    }

    s"localhost:${Defaults.KafkaPort}"
  }

}
