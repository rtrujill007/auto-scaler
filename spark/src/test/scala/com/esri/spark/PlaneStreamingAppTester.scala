package com.esri.spark

import com.esri.spark.test.Defaults

object PlaneStreamingAppTester extends App {

  // start the streaming app
  val kafkaBrokers = s"localhost:${Defaults.KafkaPort}"
  val topic = "planes"
  val dseHost = "127.0.0.1"
  val parameters = Array("local[8]", "1000", kafkaBrokers, "planes-group1", topic, "1")
  PlaneStreamingApp.main(parameters)

}
