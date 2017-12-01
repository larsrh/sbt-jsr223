package info.hupel.jsr223.sbt

import java.io.File
import java.nio.charset.Charset

import org.apache.commons.text.StringEscapeUtils
import sbt.io.IO

final case class Script(packageName: String, className: String, language: Language, mode: Script.Mode) {

  private def escape(str: String) = '"' + StringEscapeUtils.escapeJava(str) + '"'

  def generate: String = {
    val config = mode match {
      case Script.Dynamic(file) =>
        s"""
           |_root_.info.hupel.jsr223.LaunchConfig.ofPath(
           |  ${escape(language.name)},
           |  _root_.java.nio.file.Paths.get(${escape(file.toString)}),
           |  _root_.java.nio.charset.Charset.forName("UTF-8"))
         """.stripMargin
      case Script.Static(file) =>
        val script = IO.read(file, Charset.forName("UTF-8"))
        s"""
           |new _root_.info.hupel.jsr223.LaunchConfig(
           |  ${escape(language.name)},
           |  ${escape(script)}
           |)
         """.stripMargin
      case Script.Literal(literal) =>
        s"""
           |new _root_.info.hupel.jsr223.LaunchConfig(
           |  ${escape(language.name)},
           |  ${escape(literal)}
           |)
         """.stripMargin
    }

    s"""
       |package $packageName
       |
       |object $className extends App {
       |
       |  val config = $config
       |
       |  config.launch()
       |
       |}
     """.stripMargin
  }
}

object Script {
  sealed trait Mode
  case class Dynamic(file: File) extends Mode
  case class Static(file: File) extends Mode
  case class Literal(string: String) extends Mode
}
