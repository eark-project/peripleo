name := "pelagios-api-v3"

version := "0.0.1"

play.Project.playScalaSettings

libraryDependencies ++= Seq(jdbc, cache)   

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "0.6.0.1",
  "org.xerial" % "sqlite-jdbc" % "3.7.2"
)  

/** Transient dependencies required by Scalagios
  *
  * TODO: remove once Scalagios is included as managed dependency!
  */
libraryDependencies ++= Seq(
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.7.5",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.7.5",
  "com.vividsolutions" % "jts" % "1.13",
  "org.geotools" % "gt-geojson" % "10.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.7.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.7.0"
)   

