import sbt._
import Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions}
 
object ReductoBuild extends Build {
  val Organization = "reactor.accio"
  val Version      = "0.1.0"
  val ScalaVersion = "2.10.3"
 
  lazy val ReductoDist = Project(
    id = "reducto-API",
    base = file("."),
    settings = defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      libraryDependencies ++= Dependencies.reductoKernel,
      distJvmOptions in Dist := "-Xms256M -Xmx1024M",
      outputDirectory in Dist := file("target/accio-dist")
      
    )
  )
 
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version      := Version,
    scalaVersion := ScalaVersion,
    crossPaths   := false,
    organizationName := "Reactor Inc."
  )
  
  lazy val defaultSettings = buildSettings ++ Seq(
    // compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
    javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
  )
}
 
object Dependencies {
  import Dependency._
 
  val reductoKernel = Seq(
    akkaKernel, akkaSlf4j, logback
  )
}
 
object Dependency {
  // Versions
  object V {
    val Akka      = "2.2.3"
  }
 
  val akkaKernel = "com.typesafe.akka" %% "akka-kernel" % V.Akka
  val akkaSlf4j  = "com.typesafe.akka" %% "akka-slf4j"  % V.Akka
  val logback    = "ch.qos.logback"    % "logback-classic" % "1.0.0"
}
