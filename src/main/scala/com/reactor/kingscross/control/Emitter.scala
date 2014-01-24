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

abstract class Emitter(config:PollingConfig) extends Actor with ActorLogging {

    val mediator = DistributedPubSubExtension(context.system).mediator
    val write_channel = config.collect_channel 
    
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
    def publish(event:String) {
    	val json = mapper.readTree(event)
    	mediator ! Publish(write_channel, EmitEvent(json))
    }
	
	def receive = {
	  	case FetchEvent => handleEvent() 
	}
}