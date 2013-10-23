// Comment to get more information during initialization
logLevel := Level.Warn

// resolvers := Seq("Maven Central" at "http://repo1.maven.org/maven2/",
//                "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
//                "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
//                "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/")

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.0-SNAPSHOT")
