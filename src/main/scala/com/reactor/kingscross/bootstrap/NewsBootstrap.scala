package com.reactor.kingscross.bootstrap

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.reactor.kingscross.news.News
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.config.Config
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlArgs
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.StorerArgs
import com.reactor.kingscross.news.sources.AtlanticNews

class NewsBootstrap extends Actor with ActorLogging {
 
  // Init
  storers()
  collector()
  general()
  custom()
  
  //================================================================================
  // Storers
  //================================================================================
  def storers() {
	  val storersConfig = new Config(emitPlatform="/news", collectPlatform="/news", storePlatform="/news")
	  
	  // Mongo
	  val mongoFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsMongoStorer")
	  val mongoStorer = FlowControlFactory.flowControlledActorFor(context, mongoFlowConfig, StorerArgs(config=storersConfig, storeType="News"))
	  
	  val devStorersConfig = new Config(emitPlatform="/news/dev", collectPlatform="/news", storePlatform="/news/dev")
	  val devMongoStorer = FlowControlFactory.flowControlledActorFor(context, mongoFlowConfig, StorerArgs(config=devStorersConfig, storeType="News-Dev"))
	  
	  // Elasticsearch
	  val esFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsESStorer")
	  val esStorer = FlowControlFactory.flowControlledActorFor(context, esFlowConfig, StorerArgs(config=storersConfig, storeType="News"))
	  
	  // Titan
	  val titanFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsTitanStorer")
	  val titanStorer = FlowControlFactory.flowControlledActorFor(context, titanFlowConfig, StorerArgs(config=storersConfig, storeType="News"))      
  }
  
  //================================================================================
  // Standard news collector
  //================================================================================
  def collector() {
	  // Collector
	  val flowConfig = FlowControlConfig(name="newsCollector", actorType="com.reactor.kingscross.news.NewsCollector")
	  val collectorConfig = new Config(collectPlatform="/news", storePlatform="/news")
	  val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=collectorConfig))    
  }
  
  //================================================================================
  // General news 
  //================================================================================
  def general() {
    val atlantic = context.actorOf(Props(classOf[AtlanticNews], new NewsConfig(id="atlantic",url="http://feeds.feedburner.com/TheAtlantic?format=xml",emitPlatform="/news/atlantic",collectPlatform="/news/atlantic",pollTime=100)))
    //val bbc = context.actorOf(Props(classOf[News], new NewsConfig(id="bbc-health", url="www.bbc-health.com", pollTime=1)))
  }
  
  
  //================================================================================
  // Custom News 
  //================================================================================
  def custom() {
	//val techcrunch = context.actorOf(Props(classOf[Techcrunch], new NewsConfig(id="techcrunch", url="www.techcrunch.com", emitPlatform="/news/techcrunch", collectPlatform="/news/techcrunch", pollTime=15)))    
  }
  
  // Ignore messages
  def receive = { case _ => }
}