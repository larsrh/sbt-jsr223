scalaVersion := "2.12.4"

enablePlugins(JSR223Plugin)

jsr223Scripts in Compile += Script(
  "info.hupel.test",
  "Main",
  Language.Python,
  Script.Literal("print 'hi'")
)
