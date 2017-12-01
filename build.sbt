name := "cspom"

organization := "fr.univ-valenciennes"

version := "2.19-SNAPSHOT"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "fr.univ-valenciennes" %% "bitvectors" % "2.0",
  "fr.univ-valenciennes" %% "mdd" % "1.5.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "org.apache.commons" % "commons-compress" % "1.15",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.storm-enroute" %% "scalameter-core" % "0.8.2",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
  "org.tukaani" % "xz" % "1.6",
  "com.lihaoyi" %% "fastparse" % "1.0.0"
)

scalacOptions ++= Seq(
  "-Xdisable-assertions"
  , "-deprecation"
  //	"-unchecked"
  , "-Xlint"
  //
  //	"-feature"
  //	"-Yinline-warnings"
)

//wartremoverWarnings ++= Warts.all


publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", s"${(target in Test).value / "test-reports"}")

// EclipseKeys.withBundledScalaContainers := false

val scalaMeterFramework = new TestFramework("org.scalameter.ScalaMeterFramework")

testFrameworks += scalaMeterFramework

parallelExecution in Test := false

testOptions += Tests.Argument(scalaMeterFramework, "-silent")

licenses := Seq("LGPL 3.0" -> url("https://www.gnu.org/licenses/lgpl-3.0.txt"))

homepage := Some(url("https://github.com/concrete-cp/cspom"))

publishMavenStyle := true

pomExtra in Global := {
  <scm>
    <connection>scm:git:github.com/concrete-cp/cspom.git</connection>
    <url>github.com/concrete-cp/cspom.git</url>
  </scm>

    <developers>
      <developer>
        <id>scand1sk</id>
        <name>Julien Vion</name>
        <url>http://vion.free.fr/perso</url>
      </developer>
    </developers>
}
