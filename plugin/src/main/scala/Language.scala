package info.hupel.jsr223.sbt

import sbt._
import sbt.librarymanagement.ModuleID

sealed trait Language {
  def name: String
  def dependencies: Option[List[ModuleID]]
}

object Language {

  val Python: Language = new Language {
    val name = "python"
    val dependencies = Some(List("org.python" % "jython-standalone" % "2.7.1"))
  }

  def ofName(name0: String): Language = new Language {
    val name = name0
    val dependencies = None
  }

}