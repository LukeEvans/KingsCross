package com.reactor.kingscross.twitter

import com.reactor.kingscross.config.PollingConfig
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlFactory
import akka.actor.Actor
import akka.actor.Props
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.news.NewsEmitter
import com.reactor.kingscross.control._
import com.reactor.kingscross.store.ElasticsearchStore
import com.reactor.kingscross.store.MongoStore
import com.reactor.kingscross.store.TitanStore

class Twitter(config:PollingConfig) extends Actor {

  // Emitter
  val emmitter = context.actorOf(Props(classOf[TwitterEmitter], config))
  
  // Collector
  val flowConfig = FlowControlConfig(name="tweetCollector", actorType="com.reactor.kingscross.twitter.TwitterCollector")
  val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=config))
 
  // Ignore messages
  def receive = { case _ => }  
}

class TwitterEmitter(config:PollingConfig) extends Emitter(config) {
  
  def handleEvent() {
      // Fetch Tweet
        
      // Publish each tweet as String representation twitter object
	  // publish(event=tweet_json, key=handle+pubdate)
  }   
}

class TwitterCollector(args:CollectorArgs) extends Collector(args) {

    def handleEvent(event:EmitEvent) {
    
	  // Take event.data (raw tweet format as a json string) and build a Tweet object
	  // Build tweet
    
	  // Publish the tweet object as json
	  // Publish tweet json
	  // publish(event=tweetNode)
    
      // Completed event
      complete()
  }
}

// Mongo
class TwitterMongoStorer(args:StorerArgs) extends MongoStore(args) {
   
  def handleEvent(event:CollectEvent) {
    
	  	// Take event.data (Twitter object stored as json) and store it in Mongo
	  	// insert(tweet)
	  	
	  	// Publish event.data complete message (Optional)
	  	// publish(event.data)
    
      	complete()
  }   
}

// Elasticsearch
class TwitterESStorer(args:StorerArgs) extends ElasticsearchStore(args) {
   
  def handleEvent(event:CollectEvent) {
    
   	  	// Take event.data (Twitter object stored as json) and store it in Elasticsearch
	  	// index(tweet)
    
	  	// Publish event.data complete message (Optional)
	  	// publish(event.data)
    
      	complete()
  }   
}

// Titan
class TwitterTitanStorer(args:StorerArgs) extends TitanStore(args) {
   
  def handleEvent(event:CollectEvent) {

     	// Take event.data (Twitter object stored as json) and store it in Titan
	  	// Build Template
	  	// index(tweetTemplate)
    
	  	// Publish event.data complete message (Optional)
	  	// publish(event.data)
    
      	complete()
  }   
}