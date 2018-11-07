package com.esri.spark.kafka

import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

case class Producer(zookeeperQuorum:String,
                    brokers: String,
                    numPartitions: Int,
                    replicationFactor: Int,
                    topics:Array[String]) {


  private val configs = new java.util.Properties() {
    put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,  brokers)
    put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    put(ProducerConfig.BATCH_SIZE_CONFIG, "163840")
    put(ProducerConfig.ACKS_CONFIG, "1")
    put(ProducerConfig.LINGER_MS_CONFIG, "10") // time in ms to wait before sending a batch just in case more records come in

  }
  @transient lazy val defaultProducer = new KafkaProducer[String,String](configs)

  // create topic
  @transient private val zkUtils = ZkUtils.apply(zookeeperQuorum, 3000, 3000, isZkSecurityEnabled = false)
  topics.foreach{
    topic => {
      if (!AdminUtils.topicExists(zkUtils, topic))
        AdminUtils.createTopic(zkUtils, topic, numPartitions, replicationFactor, new java.util.Properties())
    }
  }

  zkUtils.close()

  def send(line: String): Unit = {

    topics.foreach {
      topic =>
        defaultProducer.send(new ProducerRecord[String, String](topic, line))
    }
  }

  def close(): Unit = {
    defaultProducer.close()
  }

  override def toString: String = "Kafka Producer"
}
