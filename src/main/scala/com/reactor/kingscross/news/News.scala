package com.reactor.kingscross.news

import akka.actor.Actor
import com.reactor.kingscross.control.Emitter
import akka.actor.ActorLogging
import com.reactor.kingscross.control.Collector
import com.reactor.kingscross.config.Config

class NewsEmitter(config:Config) extends Emitter(config) {
  
  def handleEvent(event:Any) {
	  
  }
}

class NewsCollector(config:Config) extends Collector(config) {
  
  def handleEvent(event:Any) {
	  
  }  
}
