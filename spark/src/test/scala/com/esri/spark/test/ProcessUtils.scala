package com.esri.spark.test

import scala.sys.process._

object ProcessUtils {

  /**
    * Utility function used to call external proc "jps" to gather all jvm processes running. Then it searches for the
    * jvm process which matches the name provided.
    *
    * @param processName the name of the process to fetch its id
    * @return
    */
  def getProcessId(processName: String): Option[String] = {
    // look up the available java processes - find the process by name
    val processes =  "jps".!!
    val process = processes.split("\n").find(_.contains(processName))
    process.flatMap(_.split(" ").headOption)
  }

  /**
    * Utility function used to call external proc "jps" to gather all jvm processes running. If more than one processes
    * are running (we must count ourselfs as one) then there is already a process running.
    *
    * @param processName the name of the process to check if it exists
    * @param includeSelf if true, will return true for 1 or more counts of process with processName
    *                    if false, will return true for 0 or more counts of process with processName
    * @return
    */
  def doesProcessAlreadyExists(processName: String, includeSelf: Boolean = true): Boolean = {
    // look up the available java processes - find the process by name
    val processes = "jps".!!
    val totalCount = if (includeSelf) 1 else 0
    processes.split("\n").count(_.contains(processName)) > totalCount
  }

  /**
    * Starts the cmd using external java proc in the background
    * @param cmd the cmd staring to start
    * @return
    */
  def startBackgroundProcess(cmd: String): Unit = {
    cmd.run()
  }

  /**
    * Utility function used to kill a jvm process by pid. This utility function does a check for os. If the os
    * is Windows, it will exec external proc "taskkill /pid ${pid} /f" otherwise it will try to execute "kill -9 ${pid}".
    *
    * @param pid the process id to kill
    * @return
    */
  def killProcess(pid: String): Int = {
    // check if we are running on windows or linux/mac os
    val isWindows = System.getProperty("os.name").toLowerCase.contains("win")

    // kill the process
    var success = -1
    if (isWindows) {
      val killCmd = s"taskkill /pid $pid /f"
      success = killCmd.!
    } else {
      val killCmd = s"kill -9 $pid"
      success = killCmd.!
    }
    success
  }
}
