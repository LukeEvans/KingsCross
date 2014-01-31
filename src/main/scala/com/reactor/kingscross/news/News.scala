package com.reactor.kingscross.news

import java.net.URL
import java.util.Date

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.control.Breaks

import org.apache.commons.lang.StringEscapeUtils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.kingscross.config.NewsConfig
import com.reactor.kingscross.control.CollectEvent
import com.reactor.kingscross.control.Collector
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.EmitEvent
import com.reactor.kingscross.control.Emitter
import com.reactor.kingscross.control.StorerArgs
import com.reactor.kingscross.store.ElasticsearchStore
import com.reactor.kingscross.store.MongoStore
import com.reactor.kingscross.store.TitanStore
import com.sun.syndication.feed.synd.SyndCategory
import com.sun.syndication.feed.synd.SyndContent
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.io.SyndFeedInput
import com.sun.syndication.io.XmlReader

import akka.actor.Actor
import akka.actor.Props

class News(config:NewsConfig) extends Actor {

  // Emitter
  val emmitter = context.actorOf(Props(classOf[NewsEmitter], config))
  // Collector
	  val flowConfig = FlowControlConfig(name="newsCollector", actorType="com.reactor.kingscross.news.NewsCollector")
	  val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config))  
  
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
       
       var entryLimit = 20	// Defines how many entries will be published
       var entriesPublished = 0
       
       val loop = new Breaks
       loop.breakable {
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
           val entryNode:JsonNode = mapper.valueToTree(entryMap.asJava);
         
         
           println("Publishing " + entryNode.toString())
           publish(event=entryNode, key=config.source_id + entryMap("entry_title"))
           entriesPublished += 1
           if(entriesPublished >= entryLimit) {
             println("Emitter limit reached for " + config.source_id)
             loop.break
           }
         }
       }
     } catch {
       case e:Exception => e.printStackTrace;
     }	  
  }  
}

// Collect News
class NewsCollector(args:CollectorArgs) extends Collector(args) {
  
  def handleEvent(event:EmitEvent) {
    
    //	Each news source overrides this method
	 
	  // Publish the news story object as json
	  // Publish story json
	  // publish(event=storyNode)
    
      // Completed event
      //complete()
  }
  
  def parseEventData(data:JsonNode):NewsStory = {
    
    // Take event.data JsonNode built by Emitter and build a NewsStory object
	  val story = new NewsStory()
	  story.story_type = "News"
	  var title:String = data.get("entry_title").toString()
	  story.headline = StringEscapeUtils.unescapeHtml(title)
	  story.pubdate = data.get("entry_pubdate").toString()
	  story.link = data.get("entry_url").toString()
	  story.author = data.get("entry_author").toString()
	  story.extractedText = data.get("entry_text").toString()
	 
    
    return story
  }
  
  def abstractWithDifbot(url:String):Abstraction = {
    val abstractor = new Abstractor() // TODO: make one object with collector
    return abstractor.getDifbotAbstraction(url)
  }
  
  def abstractWithGoose(url:String):Abstraction = {
    val abstractor = new Abstractor()
    return abstractor.getGooseAbstraction(url)
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