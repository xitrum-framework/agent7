organization := "tv.cntt"

name         := "agent7"

version      := "1.0-SNAPSHOT"

scalaVersion := "2.11.4"

autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

javacOptions ++= Seq("-Xlint:deprecation")

// File watch API in Java 7+ is used
scalacOptions += "-target:jvm-1.7"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

// Add tools.jar to classpath
// https://blogs.oracle.com/CoreJavaTechTips/entry/the_attach_api
unmanagedJars in Compile := (file(System.getProperty("java.home")) / ".." / "lib" * "tools.jar").classpath

packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
  "Agent-Class"          -> "agent7.Agent",
  "Premain-Class"        -> "agent7.Agent",
  "Can-Redefine-Classes" -> "true"
)
