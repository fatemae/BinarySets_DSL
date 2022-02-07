//ThisBuild / version := "0.1.0-SNAPSHOT"
//
//ThisBuild / scalaVersion := "3.1.1"
//
//lazy val root = (project in file("."))
//  .settings(
//    name := "Homework1",
//    idePackagePrefix := Some("CS474")
//  )
version := "0.1.0-SNAPSHOT"

scalaVersion := "3.1.1"
name := "Homework1"
idePackagePrefix := Some("CS474")
val ScalaTestVer = "3.2.9"

libraryDependencies += "org.scalatest" %% "scalatest" % ScalaTestVer % Test
libraryDependencies += "org.scalatest" %% "scalatest-featurespec" % ScalaTestVer % Test
