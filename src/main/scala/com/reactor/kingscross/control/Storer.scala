package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging
import com.reactor.kingscross.config.Config
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.base.patterns.pull.FlowControlArgs
import com.reactor.base.patterns.pull.FlowControlActor

case class StorerArgs(val config:Config) extends FlowControlArgs

abstract class Storer(args:StorerArgs) extends FlowControlActor(args) {

  // Save config
  val config = args.config
  val master = args.master	
  
  // Required to be implemented
  def handleEvent(event: CollectEvent)
  
  val read_channel = config.store_channel
  val write_channel = config.complete_channel
  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! Subscribe(read_channel, master)

  // Ready for work
  ready()
  
  // publish event to bus
  def publish(event:JsonNode) {
	  mediator ! Publish(write_channel, StoreEvent(event))
  }
  
  def receive = {
	  	case event:CollectEvent => handleEvent(event) 
   }  
}