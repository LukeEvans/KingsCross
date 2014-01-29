package com.reactor.kingscross.news

import akka.actor.Actor
import com.reactor.kingscross.control.Emitter
import akka.actor.ActorLogging
import com.reactor.kingscross.control.Collector
import scala.util.Random
import com.reactor.kingscross.config._
import akka.actor.Props
import com.reactor.kingscross.control._
import com.reactor.base.patterns.pull._
import com.reactor.kingscross.store.MongoStore
import com.reactor.kingscross.store.TitanStore
import com.reactor.kingscross.store.ElasticsearchStore

class News(config:PollingConfig) extends Actor {

  // Emitter
  val emmitter = context.actorOf(Props(classOf[NewsEmitter], config))
  
  // Ignore messages
  def receive = { case _ => }    
}

// Fetch news from RSS
class NewsEmitter(config:PollingConfig) extends Emitter(config) {
  
  def handleEvent() {
      // Fetch RSS
        
      // Publish each story as String representation RSS object
	  // publish(event=rss_string, key=url+headline)
  }  
}

// Collect News
class NewsCollector(args:CollectorArgs) extends Collector(args) {
  
  def handleEvent(event:EmitEvent) {
    
	  // Take event.data (story in rss format as a String) and build a NewsStory object
	  // Build story
    
	  // Publish the news story object as json
	  // Publish story json
	  // publish(event=storyNode)
    
      // Completed event
      complete()
  }  
}

// Mongo
class NewsMongoStorer(args:StorerArgs) extends MongoStore(args) {
   
  def handleEvent(event:CollectEvent) {
    
	  	// Take event.data (NewsStory object stored as json) and store it in Mongo
	  	// insert(story)
	  	
	  	// Publish event.data complete message (Optional)
	  	// publish(event.data)
    
      	// Completed event      	
      	complete()
  }   
}

// Elasticsearch
class NewsESStorer(args:StorerArgs) extends ElasticsearchStore(args) {
   
  def handleEvent(event:CollectEvent) {
    
   	  	// Take event.data (NewsStory object stored as json) and store it in Elasticsearch
	  	// index(story)
    
	  	// Publish event.data complete message (Optional)
	  	// publish(event.data)
    
      	// Completed event      	
      	complete()
  }   
}

// Titan
class NewsTitanStorer(args:StorerArgs) extends TitanStore(args) {
   
  def handleEvent(event:CollectEvent) {

     	// Take event.data (NewsStory object stored as json) and store it in Titan
	  	// Build Template
	  	// index(storyTemplate)
    
	  	// Publish event.data complete message (Optional)
	  	// publish(event.data)
    
      	complete()
  }   
}