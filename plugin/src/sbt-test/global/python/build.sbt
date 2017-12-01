scalaVersion := "2.12.4"

enablePlugins(JSR223Plugin)

jsr223Scripts in Compile += Script(
  "test",
  "Main",
  Language.Python,
  Script.Literal("from test import Test\nprint Test.hi()")
)
