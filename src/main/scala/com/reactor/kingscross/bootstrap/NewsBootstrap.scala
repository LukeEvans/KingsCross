package com.reactor.kingscross.bootstrap

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.reactor.kingscross.news.News
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.news.custom.Techcrunch
import com.reactor.kingscross.config.Config
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlArgs
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.control.StorerArgs

class NewsBootstrap extends Actor with ActorLogging {
 
  // Init
  storers()
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
	  
	  // Elasticsearch
	  val esFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsESStorer")
	  val esStorer = FlowControlFactory.flowControlledActorFor(context, esFlowConfig, StorerArgs(config=storersConfig, storeType="News"))
	  
	  // Titan
	  val titanFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsTitanStorer")
	  val titanStorer = FlowControlFactory.flowControlledActorFor(context, titanFlowConfig, StorerArgs(config=storersConfig, storeType="News"))      
  }
  
  //================================================================================
  // General news 
  //================================================================================
  def general() {
    val bbc = context.actorOf(Props(classOf[News], new NewsConfig(id="bbc-health", url="www.bbc-health.com", pollTime=1)))
  }
  
  
  //================================================================================
  // Custom News 
  //================================================================================
  def custom() {
	val techcrunch = context.actorOf(Props(classOf[Techcrunch], new NewsConfig(id="techcrunch", url="www.techcrunch.com", emitPlatform="/news/techcrunch", collectPlatform="/news/techcrunch", pollTime=15)))    
  }
  
  // Ignore messages
  def receive = { case _ => }
}
