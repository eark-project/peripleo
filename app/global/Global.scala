package global

import java.util.zip.GZIPInputStream
import java.io.{ File, FileInputStream }
import models._
import org.openrdf.rio.RDFFormat
import org.pelagios.Scalagios
import org.pelagios.gazetteer.PlaceIndex
import play.api.Play
import play.api.Play.current
import play.api.{ Application, GlobalSettings, Logger }
import play.api.db.slick._
import scala.slick.jdbc.meta.MTable
import global.index.ObjectIndex

object Global extends GlobalSettings {
    
  private val GAZETTER_DATA_DIR = "gazetteer"
    
  private val INDEX_DIR = "index"
    
  lazy val DEFAULT_PAGE_SIZE = Play.current.configuration.getInt("default.page.size").getOrElse(20)
  
  /** Initializes the gazetteer index **/
  lazy val gazetteer = {
    val idx = PlaceIndex.open(INDEX_DIR + "/gazetteer")
    if (idx.isEmpty) {
      Logger.info("Building new index")
      
      val dumps = Play.current.configuration.getString("gazetteer.files")
        .map(_.split(",").toSeq).getOrElse(Seq.empty[String]).map(_.trim)
        
      dumps.foreach(f => {
        Logger.info("Loading gazetteer dump: " + f)
        val is = if (f.endsWith(".gz"))
            new GZIPInputStream(new FileInputStream(new File(GAZETTER_DATA_DIR, f)))
          else
            new FileInputStream(new File(GAZETTER_DATA_DIR, f))
        
        val places = Scalagios.readPlaces(is, "http://pelagios.org/", RDFFormat.TURTLE).toSeq
        val names = places.flatMap(_.names)
        Logger.info("Inserting " + places.size + " places with " + names.size + " names into index")
        idx.addPlaces(places)
      })
      
      Logger.info("Index complete")      
    }
    idx
  }
  
  /** Initializes the object index **/
  lazy val index = ObjectIndex.open(INDEX_DIR + "/objects")

  override def onStart(app: Application): Unit = {
    // Initializes the database schema
    DB.withSession { implicit session: Session =>
      if (MTable.getTables("annotated_things").list().isEmpty) {
        Logger.info("DB table 'annotated_things' does not exist - creating")
        AnnotatedThings.create
      }
       
      if (MTable.getTables("annotations").list().isEmpty) {
        Logger.info("DB table 'annotations' does not exist - creating")
        Annotations.create
      }
      
      if (MTable.getTables("datasets").list().isEmpty) {
        Logger.info("DB table datasets does not exist - creating")
        Datasets.create
      }
      
      if (MTable.getTables("places_by_dataset").list().isEmpty && MTable.getTables("places_by_annotated_thing").list().isEmpty) {
        Logger.info("Places index tables do not exist - creating")
        Places.create
      }
    }
  }  

}