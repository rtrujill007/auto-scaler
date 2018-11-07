package com.esri.spark

import com.esri.spark.test.Defaults

object TaxisStreamingAppTester extends App {

  // start the streaming app
  val kafkaBrokers = s"localhost:${Defaults.KafkaPort}"
  val topic = "planes"
  val dseHost = "127.0.0.1"
  val parameters = Array("local[8]", "1000", kafkaBrokers, "taxis-group1", topic, "1")
  TaxisStreamingApp.main(parameters)

}
