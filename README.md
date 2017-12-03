# sbt-jsr223

| Service                   | Status |
| ------------------------- | ------ |
| Travis (Linux/macOS CI)   | [![Build Status](https://travis-ci.org/larsrh/sbt-jsr223.svg?branch=master)](https://travis-ci.org/larsrh/sbt-jsr223) |
| AppVeyor (Windows CI)     | [![Build status](https://ci.appveyor.com/api/projects/status/077e3uqkss5bmgx5/branch/master?svg=true)](https://ci.appveyor.com/project/larsrh/sbt-jsr223/branch/master) |

Java tool &amp; sbt plugin for launching JSR 223 scripts

## Goal

[JSR 223](https://www.jcp.org/en/jsr/detail?id=223) specifies an interface for scripting languages in Java.
This plugin allows you to treat snippets written in a compatible language to be treated as regular `main` classes.
You can configure a list of script files (or literal text) in your build definition, and depending on the scripting language, all dependencies get added automatically.

Currently, the following languages have automatic dependencies:
* Python (Jython 2.7.1)

## Usage

Add the following line to your `project/plugins.sbt`:

```scala
addSbtPlugin("info.hupel" % "sbt-jsr223" % "0.1.1")
```

You can configure the plugin in your `build.sbt` as follows:

```scala
enablePlugins(JSR223Plugin)

jsr223Scripts in Compile += Script(
  "test",
  "Main",
  Language.Python,
  Script.Literal("from test import Test\nprint Test.hi()")
)
```

This will create a main class `test.Main` that, upon running, will execute the embedded Python script.

Besides specifiying literal scripts, it is also possible to use `Static` (file content gets read at build time) or `Dynamic` scripts (file content gets read at run time).
In both cases, you have to pass a `java.io.File`, but there are no checks whether or not the file exists.

Scripts will have access to the regular classpath, so if the language supports importing from Java packages, calling into Java (or Scala) code, including library dependencies, is possible.

## Special Python support

For special Python support, add the following line to your `project/plugins.sbt`:

```scala
addSbtPlugin("info.hupel" % "sbt-jython" % "0.1.1")
```

This will allow you to drop Python code into `src/main/python` that gets compiled and added to the build products.

To enable, add `enablePlugins(JythonPlugin)` to your `build.sbt`.
You can open a Jython console by running `jython:console` in the sbt shell.
In that console, all Java/Scala and Python dependencies are present.

## Known Issues

Jython does not play well with JRuby on the same classpath.
If you use an sbt plugin that relies on JRuby, e.g. [sbt-site](https://github.com/sbt/sbt-site) via Asciidoctor, you should exclude the JRuby dependency:

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.0" exclude("org.jruby", "jruby-complete"))
```
