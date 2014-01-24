package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging
import com.reactor.kingscross.config.Config
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._
import com.fasterxml.jackson.databind.JsonNode

abstract class Storer(config:Config) extends Actor with ActorLogging {
	
  
  // Required to be implemented
  def handleEvent(event: CollectEvent)
  
  val read_channel = config.store_channel
  val write_channel = config.complete_channel
  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! Subscribe(read_channel, self)

  // publish event to bus
  def publish(event:JsonNode) {
	  mediator ! Publish(write_channel, StoreEvent(event))
  }
  
  def receive = {
	  	case event:CollectEvent => handleEvent(event) 
   }  
}