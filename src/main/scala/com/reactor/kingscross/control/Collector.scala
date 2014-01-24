package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging
import com.reactor.kingscross.config.Config
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._
import com.reactor.kingscross.config.NewsConfig
import com.fasterxml.jackson.databind.JsonNode

abstract class Collector(config:Config) extends Actor with ActorLogging {

  // Required to be implemented
  def handleEvent(event: EmitEvent): Unit
  
  val mediator = DistributedPubSubExtension(context.system).mediator

  val read_channel = config.collect_channel
  val write_channel = config.store_channel

  mediator ! Subscribe(read_channel, self)
  
  // publish event back to bus
  def publish(event:JsonNode) {
     mediator ! Publish(write_channel, CollectEvent(event))
  }
  
  def receive = {
    case SubscribeAck(Subscribe(read_channel, `self`)) =>
      context become ready
  }
  
  def ready: Actor.Receive = {
	  case event:EmitEvent => handleEvent(event) 
  }  
}