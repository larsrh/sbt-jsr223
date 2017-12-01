# sbt-jsr223

| Service                   | Status |
| ------------------------- | ------ |
| Travis (Linux CI)         | [![Build Status](https://travis-ci.org/larsrh/sbt-jsr223.svg?branch=master)](https://travis-ci.org/larsrh/sbt-jsr223) |

Java tool &amp; sbt plugin for launching JSR 223 scripts

## Goal

[JSR 223](https://www.jcp.org/en/jsr/detail?id=223) specifies an interface for scripting languages in Java.
This plugin allows you to treat snippets written in a compatible language to be treated as regular `main` classes.
You can configure a list of script files (or literal text) in your build definition, and depending on the scripting language, all dependencies get added automatically.

Currently, the following languages have automatic dependencies:
* Python (Jython)

## Usage

Add the following line to your `project/plugins.sbt`:

```scala
addSbtPlugin("info.hupel" % "sbt-jsr223" % "0.1.0")
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
