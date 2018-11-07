package com.esri.spark

import com.esri.spark.kafka.Producer
import com.esri.spark.test.TestServerUtils
import org.apache.commons.logging.LogFactory

import scala.io.Source

object Simulator extends App {

  private val log = LogFactory.getLog(this.getClass)

  // defaults
  val DEFAULT_TOPIC_NAME = "planes"
  val DEFAULT_NUM_FEATURES = 100
  val DEFAULT_INTERVAL_IN_MS = 1000
  val DEFAULT_DATA_FILE = "/planes-100k.csv"

  val (topicName, numOfFeatures, intervalInMillis) = args.length match {
    case 0 => (DEFAULT_TOPIC_NAME, DEFAULT_NUM_FEATURES, DEFAULT_INTERVAL_IN_MS)
    case 1 => (args(0), DEFAULT_NUM_FEATURES, DEFAULT_INTERVAL_IN_MS)
    case 2 => (args(0), args(1).toInt, DEFAULT_INTERVAL_IN_MS)
    case 3 => (args(0), args(1).toInt, args(2).toInt)
  }
  log.info(s"Starting the Simulator using topic: $topicName, number of features: $numOfFeatures and interval (ms): $intervalInMillis")

  // get or start the tests servers
  val zkConnectionString = TestServerUtils.getOrStartZookeeperTestServer()
  val kafkaConnectionString = TestServerUtils.getOrStartKafkaTestServer(zkConnectionString)

  // create the producer
  val producer = Producer(zkConnectionString, kafkaConnectionString, 1, 1, Array(topicName))

  // load the raw data
  val stream = getClass.getResourceAsStream(DEFAULT_DATA_FILE)
  val data = Source.fromInputStream(stream).getLines.toSeq

  // simulate
  val featuresSize = data.size

  val startTime = System.currentTimeMillis()
  var totalFeaturesSent = 0
  var continue = true
  var index = 0
  while (continue) {

    // send loop
    for (idx <- 1 to numOfFeatures) {
      if (index == featuresSize)
        index = 0
      val line = data(index)
      index += 1
      producer.send(line)
      totalFeaturesSent += 1
      log.debug("Sent line: " + line)
    }

    // sleep
    val totalTime = System.currentTimeMillis() - startTime
    log.warn(s"Sent $totalFeaturesSent in $totalTime ms. Sleeping for $intervalInMillis...")
    Thread.sleep(intervalInMillis)
  }
}
