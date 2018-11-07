package com.esri.spark.test

import org.apache.commons.logging.LogFactory
import org.apache.curator.test.TestingServer

import scala.util.Try

object StartZooKeeperTestServer extends App {

  private val log = LogFactory.getLog(this.getClass)

  // name of teh process to search for
  val Name = StartZooKeeperTestServer.getClass.getSimpleName.replace("$", "")

  // make sure we have not started the server already
  if (ProcessUtils.doesProcessAlreadyExists(Name)) {
    log.info(s"Zk Server is already started. Exiting....")
    System.exit(0)
  }

  // parse the port out of the args. Default to $Port
  val port = {
    if (args.length > 0)
      Try(args(0).toInt).toOption.getOrElse(Defaults.ZKPort)
    else
      Defaults.ZKPort
  }

  // start the ZK Server
  val zkServer: TestingServer = new TestingServer(port)
  val zkConnectionString = zkServer.getConnectString

  log.info(s"Zk Server started ($zkConnectionString)")

  // add the shut down hook
  sys.addShutdownHook {
    log.info(s"Zk Server stopped ($zkConnectionString)")
    zkServer.stop()
    zkServer.close()
  }
}
