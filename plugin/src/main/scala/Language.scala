package info.hupel.jsr223.sbt

import sbt.librarymanagement.ModuleID

sealed trait Language {
  def name: String
  def dependencies: Option[List[ModuleID]]
}

object Language {

  val Python: Language = new Language {
    val name = "python"
    val dependencies = Some(List(Dependencies.jython))
  }

  def ofName(name0: String): Language = new Language {
    val name = name0
    val dependencies = None
  }

}
