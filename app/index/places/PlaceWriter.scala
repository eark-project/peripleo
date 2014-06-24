package index.places

import index.{ Index, IndexFields }
import java.io.InputStream
import org.apache.lucene.document.{ Field, StringField }
import org.apache.lucene.index.{ IndexWriter, Term }
import org.apache.lucene.search.{ BooleanQuery, BooleanClause, TermQuery, TopScoreDocCollector }
import org.pelagios.api.gazetteer.Place
import play.api.Logger
import scala.collection.mutable.Set
import org.pelagios.Scalagios
import org.openrdf.rio.RDFFormat

trait PlaceWriter extends PlaceReader {
  
  def addPlaces(places: Iterator[Place], sourceGazetteer: String): (Int, Seq[String]) =  { 
    val writer = newPlaceWriter()
    
    val uriPrefixes = Set.empty[String]
    val distinctNewPlaces = places.foldLeft(0)((distinctNewPlaces, place) => {
      val isDistinct = addPlace(place, sourceGazetteer, uriPrefixes, writer)
      if (isDistinct)
        distinctNewPlaces + 1 
      else
        distinctNewPlaces
    })

    writer.close()
    (distinctNewPlaces, uriPrefixes.toSeq)
  }
  
  def addPlaceStream(is: InputStream, filename: String, sourceGazetteer: String): (Int, Int, Seq[String]) = {
    val writer = newPlaceWriter()
    
    val uriPrefixes = Set.empty[String]
    var totalPlaces = 0
    var distinctNewPlaces = 0
    def placeHandler(place: Place): Unit = {
      val isDistinct = addPlace(place, sourceGazetteer, uriPrefixes, writer)
      totalPlaces += 1
      if (isDistinct)
        distinctNewPlaces += 1
    }
    
    Scalagios.streamPlaces(is, filename, placeHandler)
    writer.close()
    (totalPlaces, distinctNewPlaces, uriPrefixes.toSeq)
  }
  
  private def addPlace(place: Place, sourceGazetteer: String, uriPrefixes: Set[String], writer: IndexWriter): Boolean = {
      val normalizedUri = Index.normalizeURI(place.uri)
      
      // Enforce uniqueness
      if (findPlaceByURI(normalizedUri).isDefined) {
        Logger.warn("Place '" + place.uri + "' already in index!")
        false // No new distinct place
      } else {
        // Record URI prefix
        uriPrefixes.add(normalizedUri.substring(0, normalizedUri.indexOf('/', 8)))
            
        // First, we query our index for all closeMatches our new place has 
        val closeMatches = place.closeMatches.map(uri => {
          val normalized = Index.normalizeURI(uri)
          (normalized, findPlaceByURI(normalized))
        })
        
        // These are the closeMatches we already have in our index        
        val indexedCloseMatchesOut = closeMatches.filter(_._2.isDefined).map(_._2.get)

        // Next, we query our index for places which list our new places as their closeMatch
        val indexedCloseMatchesIn = findPlaceByCloseMatch(normalizedUri)
        
        val indexedCloseMatches = indexedCloseMatchesOut ++ indexedCloseMatchesIn
        
        // These are closeMatch URIs we don't have in our index (yet)...
        val unrecordedCloseMatchesOut = closeMatches.filter(_._2.isEmpty).map(_._1)

        // ...but we can still use them to extend our network through indirect connections
        val indirectlyConnectedPlaces = // expandNetwork(unrecordedCloseMatchesOut)
          unrecordedCloseMatchesOut.flatMap(uri => findPlaceByCloseMatch(uri))
          .filter(!indexedCloseMatches.contains(_)) // We filter out places that are already connected directly

        if (indirectlyConnectedPlaces.size > 0) {
          Logger.info("Connecting " + indirectlyConnectedPlaces.size + " places through indirect closeMatches")
          indirectlyConnectedPlaces.foreach(p => Logger.info("  " + p.title))
        }

        val allCloseMatches = indexedCloseMatches ++ indirectlyConnectedPlaces
        
        // All closeMatches need to share the same seed URI
        val seedURI =
          if (allCloseMatches.size > 0) 
            allCloseMatches(0).seedURI
          else
            normalizedUri
 
        // Update seed URIs where necessary
        updateSeedURI(allCloseMatches.filter(!_.seedURI.equals(seedURI)), seedURI, writer)
        
        // Add new document to index
        val differentSeedURI = if (normalizedUri == seedURI) None else Some(seedURI)
        writer.addDocument(IndexedPlace.toDoc(place, sourceGazetteer, Some(seedURI)))
        
        // If this place didn't have any closeMatches in the index, it's a new distinct contribution
        closeMatches.size == 0
      }      
  }
  
  private def updateSeedURI(places: Seq[IndexedPlace], seedURI: String, writer: IndexWriter) = {
    places.foreach(place => {
      // Delete doc from index
      writer.deleteDocuments(new Term(IndexFields.PLACE_URI, place.uri))
      
      // Update seed URI and re-add
      val doc = place.doc
      doc.removeField(IndexFields.PLACE_SEED_URI)
      doc.add(new StringField(IndexFields.PLACE_SEED_URI, seedURI, Field.Store.YES))
      writer.addDocument(doc)
    })
  }

}