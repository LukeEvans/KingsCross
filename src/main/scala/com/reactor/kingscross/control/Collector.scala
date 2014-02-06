package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator._
import com.reactor.kingscross.config.NewsConfig
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.base.patterns.pull._
import akka.actor.ActorRef
import com.reactor.kingscross.config.Config

case class CollectorArgs(config:Config) extends FlowControlArgs

abstract class Collector(args:CollectorArgs) extends FlowControlActor(args) {

  // Save config
  val config = args.config
  val master = args.master
  
  // Required to be implemented
  def handleEvent(event: EmitEvent): Unit
  
  val mediator = DistributedPubSubExtension(context.system).mediator

  val read_platform = config.collect_platform
  var write_platform = config.store_platform

  mediator ! Subscribe(read_platform, master)
  
  // Ready for work
  ready()
  
  // publish event back to bus
  def publish(event:JsonNode) {
     mediator ! Publish(write_platform, CollectEvent(event))
  }
  
  def receive = {
    case event:EmitEvent => handleEvent(event) 
  }
}