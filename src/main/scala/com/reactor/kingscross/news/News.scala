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

class News(config:PollingConfig) extends Actor {

  // Emitter
  val emmitter = context.actorOf(Props(classOf[NewsEmitter], config))
  
  // Collector
  val flowConfig = FlowControlConfig(name="newsCollector", actorType="com.reactor.kingscross.news.NewsCollector")
  val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=config))
 
  // Ignore messages
  def receive = { case _ => }    
}

// Fetch news from RSS
class NewsEmitter(config:PollingConfig) extends Emitter(config) {
  
  var done = 0;
  
  def handleEvent() {
      // Fetch RSS
        
      // Publish each story as String representation RSS object
        
      // Testing
	  if (done <= 5) {
    	val rand = Random.nextInt
       	println("News Emitter: Fetching story - " + rand)
       	publish(event=rand.toString, key=rand.toString)
        	
       	done += 1
      }
  }
  
}

// Collect News
class NewsCollector(args:CollectorArgs) extends Collector(args) {
  
  val conf = args.config
  
  def handleEvent(event:EmitEvent) {
    
	  // Take event.data (story in rss format as a String) and build a NewsStory object
    
	  // Publish the news story object as json
    
	  // Testing
	  println("News Collector: Collecting story - " + event.data)
      publish(event.data)
      	
      Thread.sleep(5000)
      
      // Completed event
      complete()
  }  
}


// Store News

// Default storers
class NewsStorageBuilder(config:Config) extends Actor {

  // Storers
  
  // Mongo
  val mongoFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsMongoStorer")
  val mongoStorer = FlowControlFactory.flowControlledActorFor(context, mongoFlowConfig, StorerArgs(config=config, storeType="News"))
  
  // Elasticsearch
  val esFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsESStorer")
  val esStorer = FlowControlFactory.flowControlledActorFor(context, esFlowConfig, StorerArgs(config=config, storeType="News"))
  
  // Titan
  val titanFlowConfig = FlowControlConfig(name="newsMongoStorer", actorType="com.reactor.kingscross.news.NewsTitanStorer")
  val titanStorer = FlowControlFactory.flowControlledActorFor(context, titanFlowConfig, StorerArgs(config=config, storeType="News"))  
  
  // Ignore messages
  def receive = { case _ => }  
}

// Mongo
class NewsMongoStorer(args:StorerArgs) extends MongoStore(args) {
   
  def handleEvent(event:CollectEvent) {
    
	  	// Take event.data (NewsStory object stored as json) and store it in Mongo
	  	// insert(story)
	  	
	  	// Publish event.data complete message (Optional)
	  	// publish(event.data)
    
	  	// Testing
      	println("News Mongo Storer: Storing story - " + event.data)
      	publish(event.data)
      	
      	Thread.sleep(10000)
      	
      	complete()
  }   
}

// Elasticsearch
class NewsESStorer(args:StorerArgs) extends Storer(args) {
   
  def handleEvent(event:CollectEvent) {
    
   	  	// Take event.data (NewsStory object stored as json) and store it in Elasticsearch
    
	  	// Publish event.data complete message (Optional)

	  	// Testing
      	println("News ES Storer: Storing story - " + event.data)
      	publish(event.data)
      	
      	Thread.sleep(2000)
      	
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
    
	  	// Testing
	  	println("News Graph Storer: Storing story - " + event.data)
      	publish(event.data)
      	
      	Thread.sleep(7000)
      	
      	complete()
  }   
}