package com.reactor.base.utilities

import akka.actor._
import akka.pattern.ask
import com.reactor.kingscross.transport.Messages._
import com.fasterxml.jackson.databind.JsonNode
import spray.http._
import scala.concurrent.Future
import akka.io.IO
import spray.can.Http
import spray.http.HttpMethods._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import com.fasterxml.jackson.databind.ObjectMapper

class HttpRequestActor extends Actor {

	val mapper = new ObjectMapper()
	
  	def receive = {
		case req:HttpObject =>
		  val origin = sender;
		  processRequest(req, origin);
	}
  	
  	def processRequest(req: HttpObject, origin: ActorRef): JsonNode = {
  		req.method.toLowerCase() match {
  		  case "get" => return processGETRequest(req.uri, origin)
  		  case "post" => return processPOSTRequest(req.uri, req.obj, origin)
  		}
  	}
  	
  	def processGETRequest(uri:String, origin: ActorRef): JsonNode = {
  		implicit val system = context.system;
  		implicit val timeout = Timeout(5 seconds);
  		
  		val futureResponse: Future[HttpResponse] = (IO(Http) ? HttpRequest(GET, Uri(uri))).mapTo[HttpResponse]
  		val response = Await.result(futureResponse, timeout.duration);
  		val resp = mapper.readTree(response.entity.asString);
  		
  		if (origin != null) origin ! HttpObject(uri, null, resp)
  		resp;
  	}
  	
  	def processPOSTRequest(uri:String, obj:JsonNode, origin: ActorRef): JsonNode = {
  		println("noooo!!!")
  		null;
  	}
  	
}