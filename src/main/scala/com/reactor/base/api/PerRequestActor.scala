package com.reactor.base.api

import spray.routing.RequestContext
import scala.concurrent.duration._
import spray.http.StatusCodes._
import spray.http.StatusCode
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Stop
import akka.actor.ActorLogging
import com.fasterxml.jackson.databind.ObjectMapper
import akka.actor.ReceiveTimeout
import com.reactor.base.patterns.monitoring.MonitoredActor
import scala.compat.Platform
import com.reactor.base.patterns.pull.MasterWorkerProtocol._ 
import com.reactor.kingscross.transport.Messages._

class PerRequestActor(startTime: Long, ctx: RequestContext, mapper: ObjectMapper) extends MonitoredActor("per-request-actor") with ActorLogging {
    
    import context._
    
    // Increment count for per request actors
    statsd.count("per-requst-actors", 1)
    
	setReceiveTimeout(5.seconds)
  
	def receive = {
		case ResponseContainer(response) =>
		  complete(OK, response)
		case ReceiveTimeout => 
		  val error = Error("Request timeout")
		  val errString = mapper.writeValueAsString(error)
		  log.error(errString)
		  statsd.histogram("request.timeout", 1)
		  complete(GatewayTimeout, errString)
		  
		case fail: WorkFailed =>
		  log.error("Got a fail message")
		  stop(self)
		case _ => 
		  log.error("Got a message that I've never even heard of!")
		  statsd.histogram("unrecognized.message", 1)
		  stop(self)
	}
	
    // Handle the completing of Responses
    def complete(status: StatusCode, obj: String) = {
    	ctx.complete(status, obj)
    	
    	// Push time to datadog
    	statsd.histogram("response.time", Platform.currentTime - startTime)
    	statsd.count("per-requst-actors", -1)
    	
    	stop(self)
    }
    
    // Supervisor Strategy
    override val supervisorStrategy =
     OneForOneStrategy() {
      case e => {
        log.error(e.getMessage)
        complete(InternalServerError, e.getMessage)
        Stop
      }
    }
}