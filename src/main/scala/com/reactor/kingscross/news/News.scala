package com.reactor.kingscross.news

import akka.actor.Actor
import com.reactor.kingscross.control.Emitter
import akka.actor.ActorLogging
import com.reactor.kingscross.control.Collector
import scala.util.Random
import com.reactor.kingscross.config._
import akka.actor.Props
import com.reactor.kingscross.control.Storer
import com.reactor.kingscross.control.FetchEvent
import com.reactor.kingscross.control.EmitEvent
import com.reactor.kingscross.control.CollectEvent
import com.reactor.kingscross.control.CollectEvent

class News(config:PollingConfig) extends Actor {

  // Emitter
  val emmitter = context.actorOf(Props(classOf[NewsEmitter], config))
  
  // Collector
  val collector = context.actorOf(Props(classOf[NewsCollector], config))
 
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
       	publish(rand.toString)
        	
       	done += 1
      }
  }
  
}

// Collect News
class NewsCollector(config:Config) extends Collector(config) {
  
  def handleEvent(event:EmitEvent) {
    
	  // Take event.data (story in rss format as a String) and build a NewsStory object
    
	  // Publish the news story object as json
    
	  // Testing
	  println("News Collector: Collecting story - " + event.data)
      publish(event.data)
      	
      Thread.sleep(5000)
  }  
}


// Store News

// Default storers
class NewsStorageBuilder(config:Config) extends Actor {

  // Storers
  val mongoStorer = context.actorOf(Props(classOf[NewsMongoStorer], config))
  val esStorer = context.actorOf(Props(classOf[NewsESStorer], config))
  val graphStorer = context.actorOf(Props(classOf[NewsTitanStorer], config))
  
  // Ignore messages
  def receive = { case _ => }  
}

// Mongo
class NewsMongoStorer(config:Config) extends Storer(config) {
   
  def handleEvent(event:CollectEvent) {
    
	  	// Take event.data (NewsStory object stored as json) and store it in Mongo
    
	  	// Publish event.data complete message (Optional)
    
	  	// Testing
      	println("News Mongo Storer: Storing story - " + event.data)
      	publish(event.data)
      	
      	Thread.sleep(10000)
  }   
}

// Elasticsearch
class NewsESStorer(config:Config) extends Storer(config) {
   
  def handleEvent(event:CollectEvent) {
    
   	  	// Take event.data (NewsStory object stored as json) and store it in Elasticsearch
    
	  	// Publish event.data complete message (Optional)

	  	// Testing
      	println("News ES Storer: Storing story - " + event.data)
      	publish(event.data)
      	
      	Thread.sleep(2000)
  }   
}

// Titan
class NewsTitanStorer(config:Config) extends Storer(config) {
   
  def handleEvent(event:CollectEvent) {

     	// Take event.data (NewsStory object stored as json) and store it in Titan
    
	  	// Publish event.data complete message (Optional)
    
	  	// Testing
	  	println("News Graph Storer: Storing story - " + event.data)
      	publish(event.data)
      	
      	Thread.sleep(7000)
  }   
}