import com.typesafe.sbt.packager.MappingsHelper._
mappings in Universal ++= directory(baseDirectory.value / "public")

name := "siap"

version := "2.1.10"

packageSummary in Windows := "SIAP v2.0"
packageDescription in Windows := "Inventario de Luminarias SIAP"
maintainer := "aldacm2001@gmail.com"

javacOptions ++= Seq("-source", "11", "-target", "11")

scalaVersion := "2.12.11"

scalacOptions += "-feature"
scalacOptions += "-language:postfixOps"
javaOptions in run ++= Seq(
    "-Xms4G", "-Xmx8G", "-XX:MaxPermSize=1024M", "-XX:+UseConcMarkSweepGC")

crossScalaVersions := Seq("2.12.7", "2.12.11")

lazy val `siap` = (project in file(".")).enablePlugins(PlayScala)

// addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.0")

libraryDependencies ++= Seq(
  guice,
  filters,
  jdbc,
  cacheApi,
  ws,
  "org.slf4j" % "log4j-over-slf4j" % "2.0.0-alpha7",
  "org.slf4j" % "slf4j-simple" % "2.0.0-alpha7",  
//  "com.typesafe" %% "jse" % "1.2.4" exclude("org.slf4j", "slf4j-simple"),
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
  "net.sf.jasperreports" % "jasperreports" % "6.16.0",
  "net.sf.jasperreports" % "jasperreports-functions" % "6.16.0",
  "net.sf.jasperreports" % "jasperreports-chart-themes" % "6.16.0",
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
  "commons-io" % "commons-io" % "2.11.0",
  // Docx Files
  "org.docx4j" % "docx4j-core" % "11.3.2",
  "org.docx4j" % "docx4j-JAXB-ReferenceImpl" % "11.3.2",
  "org.docx4j" % "docx4j-JAXB-MOXy" % "11.3.2",
  "org.docx4j" % "docx4j-export-fo" % "11.3.2",
  "org.docx4j" % "docx4j-conversion-via-microsoft-graph" % "11.3.2",
  "org.plutext.graph-convert" % "graph-convert-base" % "1.0.3",
  "org.plutext.graph-convert" % "using-graph-sdk" % "1.0.3",
  "fr.opensagres.xdocreport" % "fr.opensagres.poi.xwpf.converter.pdf" % "2.0.3",  
  "org.apache.poi" % "poi" % "5.2.0",
  "org.apache.poi" % "poi-ooxml" % "5.2.0",
  "com.deepoove" % "poi-tl" % "1.12.0",
  // "org.slf4j" % "slf4j-api" % "2.0.7",
  // "org.slf4j" % "slf4j-simple" % "1.7.36",
  // "ch.qos.logback" % "logback-classic" % "1.4.4",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.9.3-akka-2.6.x",
  "com.lihaoyi" %% "requests" % "0.7.0"
)

// Play framework hooks for development
// PlayKeys.playRunHooks += WebpackServer(file("./front"))

unmanagedResourceDirectories in Test += baseDirectory(
  _ / "target/web/public/test"
).value
/*
resolvers += ("Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/")
  .withAllowInsecureProtocol(true)
resolvers += ("Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/")
  .withAllowInsecureProtocol(true)
*/
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
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
      if (f.isFile) f.delete
    })
  }
}

lazy val frontEndBuild =
  taskKey[Unit]("Execute the npm build command to build the front-end")

/*
frontEndBuild := {
  println(Process(s"cmd /c npm install", file("frontend")).!!)
  println(Process(s"cmd /c npm run build", file("frontend")).!!)
}

frontEndBuild := (frontEndBuild dependsOn cleanFrontEndBuild).value

dist := (dist dependsOn frontEndBuild).value
 */
