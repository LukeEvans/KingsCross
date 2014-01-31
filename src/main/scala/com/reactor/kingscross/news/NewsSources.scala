package com.reactor.kingscross.news

import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.EmitEvent
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.JsonNode

//================================================================================
// 	The Atlantic
//================================================================================
class AtlanticNewsCollector(args:CollectorArgs) extends NewsCollector(args:CollectorArgs) {
	
	override def handleEvent(event:EmitEvent) {
	  
	  //	Fill out preliminary News Story fields
	  val story = super.parseEventData(event.data)
	  story.source_id = "atlantic"
	    
	  //	TODO: Load these parameters in from Mongo
	  story.source_name = "The Atlantic"
	  story.source_category = "Culture"
	  story.ceilingTopic = "all_topics"
	    //	TODO: Get Source Icon
	    //	TODO: Get Twitter Handle
	    
	    
	  //	Add Categories as entities
	  val categories = event.data.get("categories")
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
	  
	  //	TODO: Check Initial Validity of story before continuing (move to super class?)
	  
	  //	Build article abstraction - this gets entire text and image URLs
	  
	  
	  
	  
	}
	
	
	def shouldAddEntity(entity:String):Boolean = {
	  
	  //	Filter out bad source-specific entities
	  return true
	  
	}
  
  
  
}