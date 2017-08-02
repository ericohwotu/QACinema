name := "QACinema"
 
version := "1.0" 
      
lazy val `qacinema` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( jdbc , cache , ws , specs2 % Test )

libraryDependencies += "com.braintreepayments.gateway" % "braintree-java" % "2.72.1"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )