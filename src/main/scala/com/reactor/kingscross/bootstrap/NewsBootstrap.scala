package com.reactor.kingscross.bootstrap

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.reactor.kingscross.news.News
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.news.custom.Techcrunch
import com.reactor.kingscross.news.NewsStorageBuilder
import com.reactor.kingscross.config.Config

class NewsBootstrap extends Actor with ActorLogging {
	
  // Set up singleton storers
  context.actorOf(Props(classOf[NewsStorageBuilder], new Config(emitChannel="/news", collectChannel="/news", storeChannel="/news")))
  
  // General news
  val bbc = context.actorOf(Props(classOf[News], new NewsConfig(id="bbc-health", url="www.bbc-health.com", emitChannel="/news", collectChannel="/news", storeChannel="/news", pollTime=1)))
  
  // Custom news
  val techcrunch = context.actorOf(Props(classOf[Techcrunch], new NewsConfig(id="techcrunch", url="www.techcrunch.com", emitChannel="/news/techcrunch", collectChannel="/news/techcrunch", storeChannel="/news", pollTime=15)))
  
  
  
  // Ignore messages
  def receive = { case _ => }
}