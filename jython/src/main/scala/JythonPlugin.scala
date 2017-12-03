package info.hupel.jsr223.jython

import info.hupel.jsr223.sbt.Dependencies
import org.python.core.{Options, Py, PySystemState}
import org.python.util.PythonInterpreter
import sbt.Keys._
import sbt.{Def, _}
import sbt.internal.io.Source
import sbt.plugins.JvmPlugin

object JythonPlugin extends AutoPlugin {

  object autoImport {
    lazy val pythonSource = settingKey[File]("Python source directory")
    lazy val pythonSourceDirectories = settingKey[Seq[File]]("Python source directories")
    lazy val pythonCompile = taskKey[Seq[File]]("Python compilation with Jython")
    lazy val pythonTarget = settingKey[File]("Python compilation target")
  }

  import autoImport._

  override def requires = JvmPlugin
  override def trigger = noTrigger

  lazy val Jython = config("jython").extend(Runtime).hide
  val JythonCompiler = Tags.Tag("jython")

  val pythonFilter: FileFilter = "*.py"

  val pythonCompileTask: Def.Initialize[Task[Seq[File]]] = Def.task {
    val log = streams.value.log

    val sources = pythonSourceDirectories.value
    val target = pythonTarget.value

    IO.delete(target)

    val mapping = sources.filter(_.exists()).flatMap { source =>
      ((PathFinder(source) ** pythonFilter) --- source) pair Path.rebase(source, target)
    }

    if (mapping.nonEmpty) {
      log.info(s"Compiling ${mapping.length} Python file(s) ...")

      val interpreter = PythonInterpreter.threadLocalStateInterpreter(null)
      interpreter.exec("import py_compile")

      val py_compile = interpreter.get("py_compile")

      val results =
        for ((s, d) <- mapping) yield {
          IO.createDirectory(d.getParentFile)
          IO.copyFile(s, d)
          val dClass = d.toString.dropRight(3 /* .py */) + "$py.class"
          py_compile.invoke("compile", Py.newString(s.toString), Py.newString(dClass))
          List(s, new File(dClass))
        }

      interpreter.close()

      results.flatten
    }
    else
      Nil
  } tag JythonCompiler // probably over-cautious, but not sure how thread-safe Jython is

  override def projectSettings: Seq[Def.Setting[_]] =
    inConfig(Jython)(Defaults.configSettings) ++
    Seq(
      libraryDependencies += Dependencies.jython,
      mainClass in Jython := Some("org.python.util.jython"),
      unmanagedClasspath in Jython ++= (exportedProducts in Compile).value,
      watchSources ++= pythonSourceDirectories.value.map(base => new Source(base, pythonFilter, NothingFilter)),
      pythonSource := (sourceDirectory in Compile).value / "python",
      pythonSourceDirectories := List(pythonSource.value),
      pythonTarget := target.value / "python_classes",
      productDirectories in Compile += pythonTarget.value,
      console in Jython := Defaults.runTask(
        fullClasspath in Jython,
        mainClass in (Jython, console),
        runner in (Jython, console)
      ).toTask("").value,
      pythonCompile := pythonCompileTask.value,
      resourceGenerators in Compile += pythonCompile
    )

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    concurrentRestrictions += Tags.limit(JythonCompiler, 1)
  )

}
