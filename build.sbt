name := "QACinema"
 
version := "1.0"

lazy val `qacinema` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( jdbc , cache , ws , specs2 % Test)

libraryDependencies += "com.braintreepayments.gateway" % "braintree-java" % "2.72.1"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.5-play25"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)




libraryDependencies += "com.typesafe.play" %% "play-mailer" % "6.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"

