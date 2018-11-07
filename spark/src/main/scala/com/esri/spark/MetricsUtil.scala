package com.esri.spark

import java.net.{DatagramPacket, DatagramSocket, InetSocketAddress}

import org.apache.commons.logging.LogFactory

object MetricsUtil {

  val METRICS_ENVVAR_HOST           = "STATSD_UDP_HOST"
  val METRICS_ENVVAR_PORT           = "STATSD_UDP_PORT"

  def getStatsdHost: Option[String] = {
    getEnvironmentVariable(METRICS_ENVVAR_HOST)
  }

  def getStatsdPort: Option[Int] = {
    getEnvironmentVariable(METRICS_ENVVAR_PORT) match {
      case Some(StringToIntConverter(port)) =>
        Some(port)
      case Some(nonInt: String) =>
        println(s"Invalid port for environment variable $METRICS_ENVVAR_PORT ($nonInt)")
        None
      case _ =>
        None
    }
  }

  def apply(host: String, port: Int): MetricsUtil = new MetricsUtil(host, port)

  private def getEnvironmentVariable(envVar: String): Option[String] = {
    System.getProperty(envVar) match {
      case value: String if value.nonEmpty => Some(value)
      case _ =>
        sys.env.getOrElse(envVar, "") match {
          case value: String if value.nonEmpty => Some(value)
          case _ => None
        }
    }
  }


  object StringToIntConverter {
    def unapply(s: String): Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _: NumberFormatException => None
    }
  }
}
/**
  * Utility object used to send metrics info to statsd
  */
class MetricsUtil(statsDHost: String, statsDPort: Int) extends Serializable {

  private val LOGGER = LogFactory.getLog(this.getClass)

  val METRICS_LOGGING_NAME_MAP_KEY = "logging.name"
  private val BASE_A4IOT_METRICS_NAME = "a4iot."

  @transient
  private lazy val udpSocket = new DatagramSocket()
  @transient
  private lazy val metricsAddress: InetSocketAddress = new InetSocketAddress(statsDHost, statsDPort)

  /**
    * Main report method
    */
  def reportEventGenerationRate(bucket: String, numEvents: Long, durationInMs: Long): Unit = {
    LOGGER.info(s"bucket=$bucket, numEvents=$numEvents, durationInMs=$durationInMs")
    val averageEventsPerSecond = numEvents.toDouble / (durationInMs / 1000.0)
    sendMessage(s"$BASE_A4IOT_METRICS_NAME$bucket:$averageEventsPerSecond|s", metricsAddress)
  }

  /**
    * Main report method
    */
  def incrementBucketCount(bucket: String, countIncrementValue: Long):Unit = {
    sendMessage(s"$BASE_A4IOT_METRICS_NAME$bucket:$countIncrementValue|g", metricsAddress)
  }

  //
  // When sending a message, make sure to encode it using statsd format
  //   https://github.com/etsy/statsd/blob/master/docs/metric_types.md
  //
  private def sendMessage(message: String, address: InetSocketAddress): Unit = {
    try {
      //if (LOGGER.isDebugEnabled)
      LOGGER.info(s"Broadcasting to metrics-dcos($statsDHost:$statsDPort): $message")

      LOGGER.info(s"UDP Socket: $udpSocket")
      val bytes = message.getBytes
      val packet = new DatagramPacket(bytes, 0, bytes.length, address)
      udpSocket.send(packet)
    } catch {
      case error: Throwable =>
        LOGGER.error(s"Failed to send metrics to $statsDHost:$statsDPort.", error)
    }
  }
}
