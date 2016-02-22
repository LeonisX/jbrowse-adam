package md.fusionworks.adam.jbrowse.service

import akka.actor.Actor
import md.fusionworks.adam.jbrowse.model.JsonProtocol._
import spray.httpx.SprayJsonSupport._
import spray.routing._


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ServiceActor extends Actor with ServiceRoutes {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)
}

// this trait defines our service behavior independently from the service actor
trait ServiceRoutes extends HttpService {

  val route = compressResponse() {
    get {
      path("data" / "trackList.json") {
        complete {
          JBrowseService.getTrackList
        }
      }
    } ~
      get {
        path("data" / "seq" / "refSeqs.json") {
          complete {
            JBrowseService.getRefSeqs
          }
        }
      } ~
      get {
        path("data" / Segment / "stats" / "global") { trackId: String =>
          complete {
            JBrowseService.getGlobal
          }
        }
      } ~
      path("data" / Segment / "features" / Segment) { (trackId: String, contigName: String) =>
        parameters('start, 'end,'reference_sequences_only.as[Boolean]?) {
          (start, end, reference_sequences_only) =>
            complete(JBrowseService.getFeatures(start.toLong, end.toLong, contigName, trackId))
        }
      } ~
      path("") {
        getFromResource("jbrowse/index.html")
      } ~ {
      getFromResourceDirectory("jbrowse/")
    }
  }
}