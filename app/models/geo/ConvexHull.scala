package models.geo

import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.algorithm.{ ConvexHull => JTSConvexHull }
import index.places.IndexedPlace
import java.io.StringWriter
import org.geotools.geojson.geom.GeometryJSON
import org.geotools.geometry.jts.JTSFactoryFinder
import play.api.libs.json.Json
import play.api.db.slick.Config.driver.simple._
import scala.collection.JavaConverters._

case class ConvexHull(geometry: Geometry) {
  
  lazy val bounds: BoundingBox = {
    val envelope = geometry.getEnvelopeInternal()
    BoundingBox(envelope.getMinX, envelope.getMaxX, envelope.getMinY, envelope.getMaxY)
  }
  
  lazy val asGeoJSON =
    Json.parse(toString)
  
  override lazy val toString = {
    val writer = new StringWriter()
    new GeometryJSON().write(geometry, writer)
    writer.toString
  }
  
}

object ConvexHull {
  
  /** DB mapper function **/
  implicit val convexHullMapper = MappedColumnType.base[ConvexHull, String](
    { convexHull => convexHull.toString },
    { convexHull => ConvexHull.fromGeoJSON(convexHull) })
  
  def compute(geometries: Seq[Geometry]): Option[ConvexHull] = {
    if (geometries.size > 0) {
      val factory = JTSFactoryFinder.getGeometryFactory()
      val mergedGeometry = factory.buildGeometry(geometries.asJava).union
      val cvGeometry = new JTSConvexHull(mergedGeometry).getConvexHull()
      Some(ConvexHull(cvGeometry))
    } else {
      None
    }
  }
    
  def fromPlaces(places: Seq[IndexedPlace]): Option[ConvexHull] =
    compute(places.flatMap(_.geometry))
  
  def fromGeoJSON(json: String): ConvexHull =
    ConvexHull(new GeometryJSON().read(json.trim))
  
}