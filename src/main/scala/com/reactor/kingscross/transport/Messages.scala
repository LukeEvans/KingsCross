package com.reactor.kingscross.transport

import spray.routing.RequestContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

object Messages {

// Stats request and responses
case class RequestContainer(req:String)
case class ResponseContainer(resp:String)

// Error
case class Error(status: String)

// Dispatch messages
case class DispatchRequest(request:RequestContainer, ctx:RequestContext, mapper:ObjectMapper)
case class OverloadedDispatchRequest(message:Any)
  
// HTTP Request
case class HttpObject(uri: String, obj: JsonNode = null, response: JsonNode = null, method: String = "GET") 
case class JsonResponse(node: JsonNode)

// General InitRequest
case object InitRequest

}