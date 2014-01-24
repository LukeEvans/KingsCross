package com.reactor.kingscross.news.custom

import com.reactor.kingscross.control.EmitEvent
import com.reactor.kingscross.config.Config
import com.reactor.kingscross.control.Collector
import akka.actor.Actor
import akka.actor.Props
import com.reactor.kingscross.config.PollingConfig
import com.reactor.kingscross.news.NewsEmitter

class Techcrunch(config:PollingConfig) extends Actor {
	val emmitter = context.actorOf(Props(classOf[NewsEmitter], config))

	// Collector
	val collector = context.actorOf(Props(classOf[TechCrunchCollector], config))
  
	// Ignore messages
	def receive = { case _ => }	
}

// Collect News
class TechCrunchCollector(config:Config) extends Collector(config) {
  
  def handleEvent(event:EmitEvent) {
	  println("TC Collector: Collecting story - " + event.data)
      publish(event.data)
      	
      Thread.sleep(5000)
  }  
}