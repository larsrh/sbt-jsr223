package info.hupel.jsr233

import java.nio.charset.Charset
import java.util.Collections
import javax.script.ScriptException

import scala.collection.JavaConverters._
import scala.util.Try
import org.specs2.Specification

class LaunchConfigSpec extends Specification { def is = s2"""

  Python scripts
    can be executed correctly               $launch
    can access classes from the classpath   $cp
    fails correctly                         $fails

  """

  def launchConfig(script: String) =
    LaunchConfig.ofResource("python", getClass.getClassLoader, script, Charset.forName("UTF-8"))

  def launch =
    launchConfig("test_launch.py").launch(Map[String, Object]("y" -> (1: Integer)).asJava).get("x") must beEqualTo(4)

  def cp =
    launchConfig("test_cp.py").launch(Collections.emptyMap()).get("value") must beEqualTo(TestObject.hi)

  def fails =
    Try(launchConfig("test_fail.py").launch()) must beFailedTry[Unit].withThrowable[ScriptException](".*NameError.*")

}