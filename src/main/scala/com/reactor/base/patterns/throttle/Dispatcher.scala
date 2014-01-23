package com.reactor.base.patterns.throttle
import scala.compat.Platform
import akka.actor.ActorRef
import akka.actor.Props
import spray.routing.RequestContext
import spray.routing.RequestContext
import com.reactor.base.api.PerRequestActor
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.routing.RequestContext
import com.reactor.base.patterns.monitoring.MonitoredActor
import com.reactor.base.api.PerRequestActor
import com.reactor.kingscross.transport.Messages._

class Dispatcher(reductoRouter:ActorRef) extends MonitoredActor("reducto-dispatcher"){

  def receive = {
    case DispatchRequest(request, ctx, mapper) => 
         val start = Platform.currentTime
         val tempActor = context.actorOf(Props(classOf[PerRequestActor], start, ctx, mapper))
        	
        reductoRouter.tell(request, tempActor)
        log.info("Handling request")
    
    case OverloadedDispatchRequest(message) =>
        message match {
          case req:DispatchRequest =>
          	val err = req.mapper.writeValueAsString(Error("Rate limit exceeded"))
          	completeOverload(req.ctx, ServiceUnavailable, err)    
          	log.error(err)
          	
          case _ => log.info("Unrecognized overload message")
        }
        
    case _ => log.warning("Unknown Request")

  }
  
  // Handle the completing of Responses
  def completeOverload(ctx: RequestContext, status: StatusCode, obj: String) = {
   	ctx.complete(status, obj)
  }
}