package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging
import com.reactor.kingscross.config.Config
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._

abstract class Emitter(config:Config) extends Actor with ActorLogging {

    val mediator = DistributedPubSubExtension(context.system).mediator
    
    // Required to be implemented
	def handleEvent(event: Any)
	
	def receive = {
	  	case event:Any => handleEvent(event) 
	}
}