package com.esri.spark

import org.apache.commons.logging.LogFactory
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

//
// TaxisStreamingApp
//
// NOTE TO SELF: DON'T use the App trait!
// http://www.puroguramingu.com/2016/02/26/spark-dos-donts.html
//
object TaxisStreamingApp {

  private val log = LogFactory.getLog(this.getClass)

  /**
    * Main Method
    */
  def main(args: Array[String]): Unit = {

    if (args.length < 6) {
      System.err.println("Usage: TaxisStreamingApp <sparkMaster> <emitIntervalInMillis>" +
          "<kafkaBrokers> <kafkaConsumerGroup> <kafkaTopics> <kafkaThreads>")
      System.exit(1)
    }

    val Array(sparkMaster, emitInterval, kBrokers, kConsumerGroup, kTopics, kThreads) = args

    // configuration
    val sConf = new SparkConf(true)
        .setAppName(getClass.getSimpleName)

    val sc = new SparkContext(sparkMaster, "taxis-streaming-worker", sConf)

    // the streaming context
    val ssc = new StreamingContext(sc, Milliseconds(emitInterval.toInt))

    // create the kafka stream
    val stream = createKafkaStream(ssc, kBrokers, kConsumerGroup, kTopics, kThreads.toInt)
    stream.foreachRDD {
      (rdd, time) =>
        val count = rdd.count()
        if (count > 0) {
          val msg = "Time %s: reading from kafka (%s total records)".format(time, count)
          log.info(msg)
        }
    }

    log.info("Stream is starting now...")
    //println("Stream is starting now...")

    // start the stream
    ssc.start
    ssc.awaitTermination()
  }

  // create the kafka stream
  private def createKafkaStream(ssc: StreamingContext, brokers: String, consumerGroup: String, topics: String, numOfThreads: Int = 1): DStream[String] = {
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> consumerGroup,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topicMap = topics.split(",")
    val kafkaStreams = (1 to numOfThreads).map { i =>
      KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String, String](topicMap, kafkaParams)).map(_.value())
    }
    val unifiedStream = ssc.union(kafkaStreams)
    unifiedStream
  }
}
