lazy val standardSettings = Seq(
  organization := "info.hupel",
  scalaVersion := "2.12.4",
  javacOptions += "-Xlint:unchecked",
  homepage := Some(url("https://github.com/larsrh/sbt-jsr223/")),
  licenses := Seq(
    "Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")
  ),
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  resolvers += Resolver.sonatypeRepo("releases"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <developers>
      <developer>
        <id>larsrh</id>
        <name>Lars Hupel</name>
        <url>http://lars.hupel.info</url>
      </developer>
    </developers>
    <scm>
      <connection>scm:git:github.com/larsrh/sbt-jsr223.git</connection>
      <developerConnection>scm:git:git@github.com:larsrh/sbt-jsr223.git</developerConnection>
      <url>https://github.com/larsrh/sbt-jsr223</url>
    </scm>
  ),
  credentials += Credentials(
    Option(System.getProperty("build.publish.credentials")) map (new File(_)) getOrElse (Path.userHome / ".ivy2" / ".credentials")
  )
)

lazy val noPublishSettings = Seq(
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

lazy val scriptedSettings = Seq(
  scriptedLaunchOpts += s"-Dproject.version=${version.value}",
  scriptedBufferLog := false
)

lazy val jythonVersion = settingKey[String]("Jython version")

jythonVersion in ThisBuild := "2.7.1"

lazy val root = project.in(file("."))
  .settings(standardSettings)
  .settings(noPublishSettings)
  .aggregate(launcher, plugin, jython)

lazy val launcher = project.in(file("launcher"))
  .settings(standardSettings)
  .settings(
    moduleName := "jsr223-launcher",
    autoScalaLibrary := false,
    crossPaths := false,
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "4.0.2" % Test,
      "org.python" % "jython-standalone" % jythonVersion.value % Test
    )
  )

lazy val plugin = project.in(file("plugin"))
  .enablePlugins(BuildInfoPlugin)
  .settings(standardSettings)
  .settings(scriptedSettings)
  .settings(
    moduleName := "sbt-jsr223",
    sbtPlugin := true,
    buildInfoKeys := Seq[BuildInfoKey](
      version,
      moduleName in launcher,
      organization,
      jythonVersion
    ),
    buildInfoPackage := "info.hupel.jsr223.sbt",
    libraryDependencies += "org.apache.commons" % "commons-text" % "1.1",
    scripted := {
      // we need to publish launcher first, because the plugin pulls it in as
      // a dependency
      (publishLocal in launcher).value
      scripted.evaluated
    }
  )

lazy val jython = project.in(file("jython"))
  .dependsOn(plugin)
  .settings(standardSettings)
  .settings(scriptedSettings)
  .settings(
    moduleName := "sbt-jython",
    sbtPlugin := true,
    libraryDependencies += "org.python" % "jython-standalone" % jythonVersion.value
  )


// Release stuff

import ReleaseTransformations._

releaseVcsSign := true
releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeRelease")
)


// Miscellaneous

cancelable in Global := true
