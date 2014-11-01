package ingest

import java.util.UUID
import java.util.regex.Pattern
import models.Associations
import models.core._
import models.geo._
import play.api.db.slick._
import scala.io.Source
import global.Global
import play.api.Logger
import index.places.IndexedPlace
import scala.Option.option2Iterable

object CSVImporter extends BaseImporter {
  
  private val SEPARATOR = ";"
    
  private val SPLIT_REGEX = "(?<!\\\\)" + Pattern.quote(SEPARATOR)
  
  private def resolvePlaces(uris: Seq[String]): Seq[(IndexedPlace, Int)] = {
    val allReferencedPlaces = uris.distinct
      .map(uri => (uri, Global.index.findPlaceByURI(uri)))
      .filter(_._2.isDefined)
      .map(t => (t._1, t._2.get)).toMap
    
    uris.flatMap(uri => allReferencedPlaces.get(uri))
      .groupBy(_.uri)
      .map(t => (t._2.head, t._2.size))
      .toSeq
  }
  
  // TODO this only works on hierarchical docs now!
  def importRecogitoCSV(source: Source, dataset: Dataset)(implicit s: Session) = {
    val data = source.getLines.toSeq
    val meta = toMap(data.takeWhile(_.startsWith("#")))                   
    val header = data.drop(meta.size).take(1).toSeq.head.split(SEPARATOR, -1).toSeq
    val uuidIdx = header.indexOf("uuid")
    
    val annotations = data.drop(meta.size + 1).map(_.split(SPLIT_REGEX, -1)).map(fields => {
      val uuid = if (uuidIdx > -1) UUID.fromString(fields(uuidIdx)) else UUID.randomUUID 
      val gazetteerURI = fields(header.indexOf("gazetteer_uri"))
      
      // In case annotations are on the root thing, the document part is an empty string!
      val documentPart = fields(header.indexOf("document_part")) 
      (uuid, documentPart, gazetteerURI)     
    }).groupBy(_._2)

    val annotationsOnRoot = annotations.filter(_._1.isEmpty).toSeq.flatMap(_._2.map(t => (t._1, t._3)))
    val annotationsForParts = annotations.filter(!_._1.isEmpty)

    val ingestBatch: Seq[(AnnotatedThing, Seq[Annotation], Seq[(IndexedPlace, Int)])] = {
      // Root thing
      val rootTitle = meta.get("title").get
      val rootThingId = sha256(dataset.id + " " + meta.get("author").getOrElse("") + rootTitle + " " + meta.get("language").getOrElse(""))
      val date = meta.get("date (numeric)").map(_.toInt)
      
      val partIngestBatch = annotationsForParts.map { case (partTitle, tuples) =>
        val partThingId = sha256(rootThingId + " " + partTitle)
        
        val annotations = tuples.map { case (uuid, _, gazetteerURI) =>
          Annotation(uuid, dataset.id, partThingId, gazetteerURI, None, None) }
        
        val places = 
          resolvePlaces(annotations.map(_.gazetteerURI))

        val thing = 
          AnnotatedThing(partThingId, dataset.id, partTitle, None, Some(rootThingId), None, date, date, ConvexHull.fromPlaces(places.map(_._1)))
          
        (thing, annotations, places)
      }.toSeq
     
      // Root thing
      val rootAnnotations = annotationsOnRoot.map(t => Annotation(t._1, dataset.id, rootThingId, t._2, None, None))
      val allPlaces = resolvePlaces(partIngestBatch.flatMap(_._2).map(_.gazetteerURI))
      val rootThing = AnnotatedThing(rootThingId, dataset.id, rootTitle, None, None, None, date, date, ConvexHull.fromPlaces(allPlaces.map(_._1)))
      
      (rootThing, Seq.empty[Annotation], allPlaces) +: partIngestBatch
    }

    // Insert data into DB
    val allThings = ingestBatch.map(_._1)
    AnnotatedThings.insertAll(allThings)

    val allAnnotations = ingestBatch.flatMap(_._2)
    Annotations.insertAll(allAnnotations)

    // Update aggregation table stats
    Associations.insert(ingestBatch.map(t => (t._1, t._3)))

    // Update the parent dataset with new temporal bounds and profile
    val affectedDatasets = Datasets.recomputeSpaceTimeBounds(dataset)

    Logger.info("Updating Index") 
    val parentHierarchy = dataset +: Datasets.getParentHierarchyWithDatasets(dataset)
    Global.index.addAnnotatedThing(allThings.head, ingestBatch.head._3.map(_._1), parentHierarchy)
    Global.index.updateDatasets(affectedDatasets)
    Global.index.refresh()
    
    source.close()
    Logger.info("Import complete")    
  }

  private def toMap(meta: Seq[String]): Map[String, String] = {
    val properties = meta.map(comment => comment.substring(1).split(":"))
    properties.foldLeft(Seq.empty[(String, String)])((map, prop) => {
      map :+ (prop(0).trim , prop(1).trim)
    }).toMap
  }

}