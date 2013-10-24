sbtPlugin := true

organization := "me.ssmoot.cloudant"

name := "cloudant-plugin"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

resolvers += "Spray Releases" at "http://repo.spray.io/"

libraryDependencies ++= Seq("net.databinder.dispatch" %% "dispatch-core" % "0.10.1",
                            "io.spray" %% "spray-json" % "1.2.5")