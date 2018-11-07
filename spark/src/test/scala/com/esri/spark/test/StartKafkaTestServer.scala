package com.esri.spark.test

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.commons.logging.LogFactory

import scala.collection.JavaConversions._

object StartKafkaTestServer extends App {

  private val log = LogFactory.getLog(this.getClass)

  // name of the process to search for
  val zkName = StartZooKeeperTestServer.getClass.getSimpleName dropRight 1
  val kafkaName = StartKafkaTestServer.getClass.getSimpleName dropRight 1

  //make sure that the test Zookeper server is already running
  if (!ProcessUtils.doesProcessAlreadyExists(zkName, includeSelf = false)) {
    log.info(s"Could not find an instance of a running Zookeper Server. Exiting...")
    System.exit(0)
  }
  // make sure we have not started the server already
  else if (ProcessUtils.doesProcessAlreadyExists(kafkaName)) {
    log.info(s"Kafka Server is already started. Exiting...")
    System.exit(0)
  }

  //setup kafka server
  val properties = mapAsJavaMap(
    Map(
      "zookeeper.connect" -> s"localhost:${Defaults.ZKPort}",
      "port" -> Defaults.KafkaPort.toString
    )
  )
  val kafkaConfig = KafkaConfig(properties)
  val kafkaServer = new KafkaServerStartable(kafkaConfig)

  log.info(s"Kafka Server is starting up (port: ${Defaults.KafkaPort})")
  kafkaServer.startup()
  sys.addShutdownHook {
    cleanup()
  }

  def cleanup() = {
    kafkaServer.shutdown()
    log.info(s"Kafka Server stopped (port: ${Defaults.KafkaPort})")

    //Are there any further cleanup steps to be performed?
  }

}
