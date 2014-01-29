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
import com.sun.syndication.io.XmlReader
import java.net.URL
import com.sun.syndication.io.SyndFeedInput
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import scala.collection.JavaConverters._
import java.util.Date;
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

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
class NewsEmitter(config:NewsConfig) extends Emitter(config) {
  
  
  def handleEvent() {
     println("News emitter handle event for " + config.source_id + " from url " + config.source_url); 
    
     // Fetch RSS
     try {
       val url = new URL(config.source_url)
       val reader = new XmlReader(url)
       val feed = new SyndFeedInput().build(reader);
       for (entry <- feed.getEntries().asScala) {
         // Publish each story as JsonNode representation RSS object
         println("New Story found for " + config.source_id);
         var entryMap:Map[String, Object] = Map()
         entryMap += ("entry_title" -> entry.asInstanceOf[SyndEntry].getTitle())
         
        if (entry.asInstanceOf[SyndEntry].getPublishedDate() == null) {
        	entryMap += ("entry_pubdate" -> new Date().toString())
        } else {
        	entryMap += ("entry_pubdate" -> entry.asInstanceOf[SyndEntry].getPublishedDate().toString())
        }
         
         entryMap += ("entry_url" -> entry.asInstanceOf[SyndEntry].getLink())
         entryMap += ("entry_author" -> entry.asInstanceOf[SyndEntry].getAuthor())
         
         //	Extract any categories associated with the RSS
         var categories:List[String] = List()
         for (category <- entry.asInstanceOf[SyndEntry].getCategories().asScala) {
        	 categories + category.asInstanceOf[SyndCategory].getName()
         }
         if (!categories.isEmpty) {
            entryMap += ("categories" -> categories)
         }
          
         //	Extract any text associated with RSS
         var entry_text:String = ""
         for (content <- entry.asInstanceOf[SyndEntry].getContents().asScala) {
        	 entry_text = entry_text + content.asInstanceOf[SyndContent].getValue()
         }
         
         if (entry_text == "") {
        	 entry_text = entry.asInstanceOf[SyndEntry].getDescription().getValue()
         } else {
        	 var description = entry.asInstanceOf[SyndEntry].getDescription().getValue()
        	 if (description.length() > entry_text.length()) {
        		 entry_text = description;
        	 }
         }
         entryMap += ("entry_text" -> entry_text)
         
         //	Create a json node out of entryMap and publish
         val mapper = new ObjectMapper()
         val entryNode:JsonNode = mapper.valueToTree(entryMap);
         publish(event=entryNode, key=config.source_id + entryMap("title"))
       }
     } catch {
       case e:Exception => println("Exception when fetching rss for " + config.source_id)
     }	  
  }  
}

// Collect News
class NewsCollector(args:CollectorArgs) extends Collector(args) {
  
  def handleEvent(event:EmitEvent) {
    
	 
	  // Take event.data (story in rss format as a String) and build a NewsStory object
	  // Build story
    
	  println("Collector is collecting");
    
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