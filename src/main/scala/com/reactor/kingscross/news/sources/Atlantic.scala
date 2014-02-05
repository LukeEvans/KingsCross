package com.reactor.kingscross.news.sources

import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.EmitEvent
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.kingscross.config.NewsConfig
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.kingscross.news.Abstraction
import com.reactor.kingscross.news.Entity
import com.reactor.kingscross.news.News
import com.reactor.kingscross.news.NewsCollector
import com.reactor.kingscross.news.NewsStory
import akka.actor.ActorLogging
import com.reactor.kingscross.news.TopicSet

//================================================================================
// 	The Atlantic
//================================================================================

class AtlanticNews(config:NewsConfig)  extends News(config:NewsConfig) {

	// Collector
	override val flowConfig = FlowControlConfig(name="atlanticCollector", actorType="com.reactor.kingscross.news.sources.AtlanticNewsCollector")
	override val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=config))
}
  
  
class AtlanticNewsCollector(args:CollectorArgs) extends NewsCollector(args:CollectorArgs) {
 
  var isDevChannel = true
  
  override def handleEvent(event:EmitEvent) {

    println("Atlantic Collecter creating story")
    
    //	Fill out preliminary News Story fields
	val story:NewsStory = parseEventData(event.data)
	story.source_id = "atlantic"
	    
    //	TODO: Load these parameters in from Mongo
	story.source_name = "The Atlantic"
	story.source_category = "Culture"
	story.ceiling_topic = "all_topics"
	
	  
	//	TODO: Get Source Icon
	//	TODO: Get Twitter Handle
	    
	    
	//	Add Categories as entities
	val categories:JsonNode = event.data.path("categories")
	if (categories!= null) {
	  if (categories.asInstanceOf[JsonNode].isArray()) {
	    var a = 0
	    for( a <- 0 until categories.asInstanceOf[ArrayNode].size()) {
	      var category:String = categories.asInstanceOf[ArrayNode].get(a).toString()
	      if (shouldAddEntity(category)) {
	        var entity = new Entity(category)
            story.entities + entity
	      }
	    }
	  }
	}
	  
	  
	//	TODO: Check Initial Validity of story before continuing (move to super class?)
	  
	//	Build article abstraction - this gets entire text and image URLs
	var difbotAbstraction:Abstraction = abstractWithDifbot(story.link)
	
	//	Handle Images (custom to each news source)
	if (difbotAbstraction.primary_images.size > 0) {
	  story.image_links = difbotAbstraction.primary_images
	} 
	else {
	  story.image_links = difbotAbstraction.secondary_images
	}
	
	story.parseAbstraction(difbotAbstraction)
   	story.speech = story.buildSpeech  
	
	story.summary = getSummary(story.headline,story.full_text)
	story.speech = story.summary // TODO lots going on with speech field, can we simplify?

	story.speech = scrubSpeech(story.speech)
	story.full_text = scrubFullText(story.full_text)
	
	//	Topic Extraction
	val extractedTopics:TopicSet = getTopics(story)
	story.related_topics = extractedTopics.relatedTopics
	story.main_topics = extractedTopics.mainTopics
	
	story.valid = story.checkValid()
	
	publishStory(story, isDevChannel)
	
	// Completed Event
	complete()
	
  }
	
  def shouldAddEntity(entity:String):Boolean = {
	  
  //	Filter out bad source-specific entities
  return true
	  
  }
}

