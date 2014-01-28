package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._
import akka.actor.Cancellable
import scala.concurrent.duration._
import scala.util.Random
import com.reactor.kingscross.config.PollingConfig
import com.fasterxml.jackson.databind.ObjectMapper
import scala.collection.mutable.ArrayBuffer

abstract class Emitter(config:PollingConfig) extends Actor with ActorLogging {

    val mediator = DistributedPubSubExtension(context.system).mediator
    val write_platform = config.collect_platform 
    
    // Keys Seen
    var keysSeen = ArrayBuffer[String]()
    val maxBufferSize = 100
    
    val mapper = new ObjectMapper()
    
    // Tick Timer
  	implicit val ec = context.dispatcher
	val cancellable =
		context.system.scheduler.schedule(config.start_delay seconds,
		config.poll_time seconds,
		self,
		FetchEvent)
		
    // Required to be implemented
	def handleEvent()
    
    // publish event to bus
    def publish(event:String, key:String) {
    	if (keysSeen.contains(key)) return
    	
    	val json = mapper.readTree(event)
    	mediator ! Publish(write_platform, EmitEvent(json))
    	
    	keysSeen += key
    	
    	// Clear memory of keys seen so far
    	if (keysSeen.size >= maxBufferSize) {
    	  keysSeen = keysSeen.drop(1)
    	}
    }
	
	def receive = {
	  	case FetchEvent => handleEvent() 
	}
}