package com.reactor.kingscross.bootstrap

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.reactor.kingscross.news.News
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.news.custom.Techcrunch
import com.reactor.kingscross.news.NewsStorageBuilder
import com.reactor.kingscross.config.Config
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlArgs
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.config.NewsConfig

class NewsBootstrap extends Actor with ActorLogging {
	
  // Set up singleton storers
  context.actorOf(Props(classOf[NewsStorageBuilder], new Config(emitPlatform="/news", collectPlatform="/news", storePlatform="/news")))
  
  // General news
  val bbc = context.actorOf(Props(classOf[News], new NewsConfig(id="bbc-health", url="www.bbc-health.com", pollTime=1)))
  
  // Custom news
  val techcrunch = context.actorOf(Props(classOf[Techcrunch], new NewsConfig(id="techcrunch", url="www.techcrunch.com", emitPlatform="/news/techcrunch", collectPlatform="/news/techcrunch", pollTime=15)))
  
  // Ignore messages
  def receive = { case _ => }
}