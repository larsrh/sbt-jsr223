package info.hupel.jsr223.sbt

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object JSR223Plugin extends AutoPlugin {

  object autoImport {
    lazy val jsr223Scripts = settingKey[Seq[Script]]("JSR 223 scripts")
    lazy val jsr223Target = settingKey[File]("Code generation target")
    lazy val jsr223AutoDependencies = settingKey[Boolean]("Automatically add known dependencies")

    type Script = info.hupel.jsr223.sbt.Script
    val Script = info.hupel.jsr223.sbt.Script

    type Language = info.hupel.jsr223.sbt.Language
    val Language = info.hupel.jsr223.sbt.Language
  }

  import autoImport._

  override def requires = JvmPlugin
  override def trigger = noTrigger

  private def mkFileName(target: File, script: Script): File = {
    val dir = script.packageName.split('.').foldLeft(target)((file, component) => file / component)
    dir / s"${script.className}.scala"
  }

  def generatorTask(config: Configuration): Def.Initialize[Task[Seq[File]]] = Def.task {
    val scripts = (jsr223Scripts in config).value
    val target = (jsr223Target in config).value
    val log = streams.value.log
    scripts.map { script =>
      val file = mkFileName(target, script)
      log.info(s"Writing JSR 223 main class to $file ...")
      IO.write(file, script.generate)
      file
    }
  }

  def settings(config: Configuration): Seq[Def.Setting[_]] = Seq(
    jsr223AutoDependencies in config := true,
    jsr223Target in config := (sourceManaged in config).value / "sbt-jsr223",
    jsr223Scripts in config := Nil,
    libraryDependencies ++= {
      if ((jsr223AutoDependencies in config).value)
        (jsr223Scripts in config).value.flatMap(_.language.dependencies).flatten.map(_ % config)
      else
        Seq()
    },
    libraryDependencies += BuildInfo.organization % BuildInfo.moduleName % BuildInfo.version % config,
    sourceGenerators in config += generatorTask(config)
  )

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(Compile, Test).flatMap(settings)

}
