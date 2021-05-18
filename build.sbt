import com.typesafe.sbt.packager.MappingsHelper._
mappings in Universal ++= directory(baseDirectory.value / "public")

name := "siap"

version := "1.3"

packageSummary in Windows := "SIAP v1.3"
packageDescription in Windows := "Inventario de Luminarias SIAP"
maintainer := "aldacm2001@gmail.com"

scalaVersion := "2.12.11"

scalacOptions += "-feature"
scalacOptions += "-language:postfixOps"

crossScalaVersions := Seq("2.12.7", "2.12.11")

lazy val `siap` = (project in file(".")).enablePlugins(PlayScala)

// addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.0")

libraryDependencies ++= Seq(
  guice, 
  filters, 
  jdbc, 
  cacheApi,
  ws, 
  specs2 % Test,
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "org.postgresql" % "postgresql" % "42.2.2",
  "com.typesafe.play" %% "play-json" % "2.6.10",
  "com.typesafe.play" %% "play-json-joda" % "2.6.10",
  "com.pauldijou" %% "jwt-play" % "4.2.0",
  "org.apache.commons" % "commons-email" % "1.5",
  "com.jaroop" %% "anorm-relational" % "0.3.0",
  // Jasper Report
  "org.olap4j" % "olap4j" % "1.2.0",
  "com.lowagie" % "itext" % "2.1.7.js7",
  "org.springframework" % "spring-beans" % "5.1.0.RELEASE",
  "net.sf.jasperreports" % "jasperreports" % "6.13.0",
  "net.sf.jasperreports" % "jasperreports-functions" % "6.13.0",
  "net.sf.jasperreports" % "jasperreports-chart-themes" % "6.13.0",
  "com.norbitltd" %% "spoiwo" % "1.7.0",
  "org.mozilla" % "rhino" % "1.7R3",
  // JSON
  "net.liftweb" %% "lift-json" % "3.4.1",
  "net.liftweb" %% "lift-json-ext" % "3.4.1",
  // CSV parser
  "org.apache.poi" % "poi" % "3.17",
  "org.apache.poi" % "poi-ooxml" % "3.17",
  "com.univocity" % "univocity-parsers" % "2.4.1",
  //
  "org.apache.httpcomponents" % "httpcore" % "4.4.13",
  //
  "com.beachape" %% "enumeratum" % "1.6.1",
  // Pdf Generator
  "com.hhandoko" %% "play28-scala-pdf" % "4.3.0",
  //
  "net.codingwell" %% "scala-guice" % "4.2.11",
  "commons-io" % "commons-io" % "2.4"
)

// Play framework hooks for development
// PlayKeys.playRunHooks += WebpackServer(file("./front"))

unmanagedResourceDirectories in Test +=  baseDirectory ( _ /"target/web/public/test" ).value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Jasper" at "https://jaspersoft.artifactoryonline.com/jaspersoft/jr-ce-releases/"
resolvers += "Jasper2" at "https://jaspersoft.jfrog.io/jaspersoft/jaspersoft-repo/"
resolvers += "Jasper3" at "https://jaspersoft.artifactoryonline.com/jaspersoft/repo/"
resolvers += "Jasper4" at "https://mvnrepository.com/artifact/net.sf.jasperreports/jasperreports"
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
// Production front-end build
lazy val cleanFrontEndBuild = taskKey[Unit]("Remove the old front-end build")

cleanFrontEndBuild := {
  val d = file("public/bundle")
  if (d.exists()) {
    d.listFiles.foreach(f => {
      if(f.isFile) f.delete
    })
  }
}

lazy val frontEndBuild = taskKey[Unit]("Execute the npm build command to build the front-end")

/*
frontEndBuild := {
  println(Process(s"cmd /c npm install", file("frontend")).!!)
  println(Process(s"cmd /c npm run build", file("frontend")).!!)
}

frontEndBuild := (frontEndBuild dependsOn cleanFrontEndBuild).value

dist := (dist dependsOn frontEndBuild).value
*/