logLevel := Level.Debug

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
//Resolver.typesafeRepo("releases")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.2")