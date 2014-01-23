name := "Akka Accio"

version := "0.1"

scalaVersion := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "spray repo" at "http://repo.spray.io/"

resolvers += "jlangdetect-googlecode" at "https://jlangdetect.googlecode.com/svn/repo"

atmosSettings

traceAkka("2.2.3")

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-kernel_2.10" % "2.2.3"

libraryDependencies += "com.typesafe.akka" % "akka-cluster_2.10" % "2.2.3"

libraryDependencies += "com.typesafe" % "config" % "1.0.2"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.2.0" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")) 

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.3"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.2.3"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.3"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.5"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.0-alpha4"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.5"

libraryDependencies += "commons-lang" % "commons-lang" % "2.1"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.5"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.5"

libraryDependencies += "play" % "play_2.10" % "2.1.0"

libraryDependencies += "org.elasticsearch" % "elasticsearch" % "0.90.0"

libraryDependencies += "org.apache.opennlp" % "opennlp-tools" % "1.5.3"

libraryDependencies += "com.indeed" % "java-dogstatsd-client" % "2.0.7"

libraryDependencies +=  "redis.clients" % "jedis" % "2.2.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

libraryDependencies += "ch.qos.logback" % "logback-core"  % "1.0.13"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.10" % "2.2.3"

libraryDependencies += "me.champeau.jlangdetect" % "jlangdetect-extra" % "0.3" excludeAll(
        ExclusionRule(organization = "javax.jms"),
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx")
)

libraryDependencies += "me.champeau.jlangdetect" % "jlangdetect-europarl" % "0.3" excludeAll(
        ExclusionRule(organization = "javax.jms"),
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx")
)

libraryDependencies += "me.champeau.jlangdetect" % "jlangdetect" % "0.3" excludeAll(
        ExclusionRule(organization = "javax.jms"),
        ExclusionRule(organization = "com.sun.jdmk"),
        ExclusionRule(organization = "com.sun.jmx")
)

libraryDependencies ++= {
  val akkaV = "2.2.3"
  val sprayV = "1.2-RC4"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV,
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV,
    "org.specs2"          %%  "specs2"        % "2.2.3" % "test"
  )
}

seq(Revolver.settings: _*)

