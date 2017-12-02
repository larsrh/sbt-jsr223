package info.hupel.jsr223.sbt

import sbt._
import sbt.librarymanagement.ModuleID

object Dependencies {

  val jython: ModuleID = "org.python" % "jython-standalone" % BuildInfo.jythonVersion

}
