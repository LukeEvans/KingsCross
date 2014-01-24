package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging
import com.reactor.kingscross.config.Config
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._
import com.reactor.kingscross.config.NewsConfig

abstract class Collector(config:Config) extends Actor with ActorLogging {

  // Required to be implemented
  def handleEvent(event: Any): Unit
  mediator ! Subscribe(config.read_channel, self)
  
  val nc = new NewsConfig("google.com", 4, "in", "out")
  val mediator = DistributedPubSubExtension(context.system).mediator
  
  val read_channel = config.read_channel
  val write_channel = config.write_channel
  
  def receive = {
    case SubscribeAck(Subscribe(read_channel, `self`)) =>
      context become ready
  }
  
  def ready: Actor.Receive = {
	  	case event:Any => handleEvent(event) 
	}  
}