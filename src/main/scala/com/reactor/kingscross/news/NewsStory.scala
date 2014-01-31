package com.reactor.kingscross.news

import com.fasterxml.jackson.databind.JsonNode

class NewsStory {
  
  //	Story Variables
  var story_type:String = null
  var entities:Set[Entity] = Set()
  var speech:String = null
  var valid:Boolean = false
  var source_category:String = null
  var headline:String = null

  
  //	News Story Variables
  var source_id:String = null
  var source_name:String = null
  var author:String = null
  var summary: String = null
  var extractedText:String = null
  var pubdate:String = null
  var link:String = null
  var source_icon_link:String= null
  var source_twitter_handle:String = null
  var image_links:Set[String] = Set()
  var reviewed:Boolean = false
  var video:JsonNode = null
  
  //	Topic Variables
  var ceilingTopic:String = null
  var relatedTopics:Set[String] = Set()
  var mainTopics:Set[String] = Set()
  
 
}