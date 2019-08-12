import com.typesafe.sbt.packager.MappingsHelper._
mappings in Universal ++= directory(baseDirectory.value / "public")

name := "siap"

version := "1.0"

packageSummary in Windows := "SIAP v1.0"
packageDescription in Windows := "Inventario de Luminarias SIAP"

scalaVersion := "2.12.7"

scalacOptions += "-feature"
scalacOptions += "-language:postfixOps"

crossScalaVersions := Seq("2.11.12", "2.12.7")

lazy val `siap` = (project in file(".")).enablePlugins(PlayScala)

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
  "com.pauldijou" %% "jwt-play" % "0.16.0",
  "org.apache.commons" % "commons-email" % "1.5",
  "com.jaroop" %% "anorm-relational" % "0.3.0",
  // Jasper Report
  "org.olap4j" % "olap4j" % "1.2.0",
  "com.lowagie" % "itext" % "2.1.7.js6",
  "org.springframework" % "spring-beans" % "5.1.0.RELEASE",
  "net.sf.jasperreports" % "jasperreports" % "6.7.0",
  "net.sf.jasperreports" % "jasperreports-functions" % "6.7.0",
  "net.sf.jasperreports" % "jasperreports-chart-themes" % "6.7.0",
  "com.norbitltd" %% "spoiwo" % "1.4.1",
  "org.eclipse.birt.runtime.3_7_1" % "org.mozilla.javascript" % "1.7.2"
)

// Play framework hooks for development
PlayKeys.playRunHooks += WebpackServer(file("./front"))

unmanagedResourceDirectories in Test +=  baseDirectory ( _ /"target/web/public/test" ).value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "JasperOther" at "https://jaspersoft.artifactoryonline.com/jaspersoft/jr-ce-releases/"
resolvers += "JasperOther2" at "https://jaspersoft.jfrog.io/jaspersoft/jaspersoft-repo/"
resolvers += "Jasper" at "https://jaspersoft.artifactoryonline.com/jaspersoft/repo/"
resolvers += "JasperSoft" at "https://jaspersoft.artifactoryonline.com/jaspersoft/jaspersoft-repo/"
resolvers += "Jasper3rd" at "https://jaspersoft.artifactoryonline.com/jaspersoft/jaspersoft-3rd-party/"
resolvers += "mondrian-repo-cache" at "https://jaspersoft.artifactoryonline.com/jaspersoft/mondrian-repo-cache/"
resolvers += "spring-mil" at "http://repo.spring.io/libs-milestone"
resolvers += "spring-rel" at "http://repo.spring.io/libs-release"
resolvers += "oss" at "https://oss.sonatype.org/content/groups/public/"

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

frontEndBuild := {
  println(Process(s"cmd /c npm install", file("front")).!!)
  println(Process(s"cmd /c npm run build", file("front")).!!)
}

frontEndBuild := (frontEndBuild dependsOn cleanFrontEndBuild).value

dist := (dist dependsOn frontEndBuild).value
