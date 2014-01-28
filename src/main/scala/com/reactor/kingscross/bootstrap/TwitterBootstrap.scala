package com.reactor.kingscross.bootstrap

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.reactor.kingscross.config.Config
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlArgs
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.control.StorerArgs
import com.reactor.kingscross.twitter.Twitter
import com.reactor.kingscross.config.TwitterConfig

class TwitterBootstrap extends Actor with ActorLogging {
 
  // Init
  storers()
  general()
  
  //================================================================================
  // Storers
  //================================================================================
  def storers() {
	  val storersConfig = new Config(emitPlatform="/twitter", collectPlatform="/twitter", storePlatform="/twitter")
	  
	  // Mongo
	  val mongoFlowConfig = FlowControlConfig(name="twitterMongoStorer", actorType="com.reactor.kingscross.twitter.TwitterMongoStorer")
	  val mongoStorer = FlowControlFactory.flowControlledActorFor(context, mongoFlowConfig, StorerArgs(config=storersConfig, storeType="Twitter"))
	  
	  // Elasticsearch
	  val esFlowConfig = FlowControlConfig(name="twitterMongoStorer", actorType="com.reactor.kingscross.twitter.TwitterESStorer")
	  val esStorer = FlowControlFactory.flowControlledActorFor(context, esFlowConfig, StorerArgs(config=storersConfig, storeType="Twitter"))
	  
	  // Titan
	  val titanFlowConfig = FlowControlConfig(name="twitterMongoStorer", actorType="com.reactor.kingscross.twitter.TwitterTitanStorer")
	  val titanStorer = FlowControlFactory.flowControlledActorFor(context, titanFlowConfig, StorerArgs(config=storersConfig, storeType="Twitter"))      
  }
  
  //================================================================================
  // General news 
  //================================================================================
  def general() {
    val bbc = context.actorOf(Props(classOf[Twitter], new TwitterConfig(id="bbc-health", handle="bbc-health", pollTime=1)))
  }
  
  // Ignore messages
  def receive = { case _ => }
}
