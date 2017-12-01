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
  pomExtra :=
    <developers>
      <developer>
        <id>larsrh</id>
        <name>Lars Hupel</name>
        <url>http://lars.hupel.info</url>
      </developer>
    </developers>,
  credentials += Credentials(
    Option(System.getProperty("build.publish.credentials")) map (new File(_)) getOrElse (Path.userHome / ".ivy2" / ".credentials")
  )
)

lazy val noPublishSettings = Seq(
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

lazy val root = project.in(file("."))
  .settings(standardSettings)
  .settings(noPublishSettings)
  .aggregate(launcher)

lazy val launcher = project.in(file("launcher"))
  .settings(standardSettings)
  .settings(
    moduleName := "jsr223-launcher",
    autoScalaLibrary := false,
    crossPaths := false,
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "4.0.2" % Test,
      "org.python" % "jython-standalone" % "2.7.1" % Test
    )
  )
