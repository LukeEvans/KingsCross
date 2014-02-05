package com.reactor.kingscross.news

import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.MongoOperations

import org.springframework.data.mongodb.core.query.Update


class Topic {
  var topic:String = null
  var parent_topic:Topic = null
  var child_topics:Set[Topic] = Set()
  var akas:Set[String] = Set()
}

class TopicMiss {
  var topic:String = null
  var count:Int = 0
}



class TopicSet {
  var mainTopics:Set[String] = Set()
  var relatedTopics:Set[String] = Set()
}

class TopicExtractor {
  
  def extractTopicsFromStory(story:NewsStory):TopicSet = {
    
    if (story.entities == null) {
      println("ERROR: null entity set")
      return null
    }
     
    val mongo:MongoOperations = null
    
    val extractedTopics:TopicSet = new TopicSet() 
    
    for(ent:Entity <- story.entities) {
      
      if(ent.entity_name != null) {
        val cleanedEntity:String = ent.entity_name.replace(" ","_")
        
        //	Try to find matching topic in mongo
        val query:Query = new Query(Criteria.where("topic").is(cleanedEntity.toLowerCase))
        var topicMatch:Topic = mongo.findOne(query, classOf[Topic], "news_topic_tree")
        
        if (topicMatch != null) {
          //	Match found, add topic as a related topic
          if(!extractedTopics.relatedTopics(topicMatch.topic)) {
            extractedTopics.relatedTopics + topicMatch.topic
            
            //	Add topic as a main topic if it is contained in the headline or summary
            if(story.headline.toLowerCase().contains(ent.entity_name.toLowerCase()) || story.summary.toLowerCase().contains(ent.entity_name.toLowerCase())) {
              if(!extractedTopics.mainTopics(topicMatch.topic)) {
                extractedTopics.mainTopics + topicMatch.topic
                //	Add parent topics of main topics to related topics
                extractedTopics.relatedTopics = getParentTopics(topicMatch,story.ceiling_topic,extractedTopics.relatedTopics)
              }
            }
          }
        }
        else {
          //	No direct match found, look for AKAs
          val akaQuery:Query = new Query(Criteria.where("aka").is(cleanedEntity.toLowerCase()))
          val akaTopic:Topic = mongo.findOne(akaQuery, classOf[Topic], "news_topic_tree")
          if (akaTopic != null) {
            //	Match found, add topic as a related topic
            if(!extractedTopics.relatedTopics(akaTopic.topic)) {
              extractedTopics.relatedTopics + akaTopic.topic
            
              //	Add topic as a main topic if it is contained in the headline or summary
              if(story.headline.toLowerCase().contains(ent.entity_name.toLowerCase()) || story.summary.toLowerCase().contains(ent.entity_name.toLowerCase())) {
                if(!extractedTopics.mainTopics(topicMatch.topic)) {
                  extractedTopics.mainTopics + topicMatch.topic
                  //	Add parent topics of main topics to related topics
                  extractedTopics.relatedTopics = getParentTopics(topicMatch,story.ceiling_topic,extractedTopics.relatedTopics)
                }
              }
            }
          }
          else {
            //	Entity is not matched with topic - save miss to mongo 
            val missQuery:Query = new Query(Criteria.where("topic").is(cleanedEntity.toLowerCase()))
			val missedTopic:TopicMiss = mongo.findOne(missQuery, classOf[TopicMiss], "topic_misses");
			if (missedTopic != null) {
			  //	Increment count by one
			  missedTopic.count += 1;
			  mongo.updateFirst(missQuery, Update.update("count", missedTopic.count), "topic_misses");			
			} 
			else {
			  //	Create a new missed Topic
			  val newMissedTopic:TopicMiss = new TopicMiss();
			  newMissedTopic.topic = cleanedEntity.toLowerCase
			  mongo.save(newMissedTopic,"topic_misses");					
			}
          }
        }
      }
    }
    
    if (extractedTopics.relatedTopics.size == 0) {
      extractedTopics.relatedTopics + story.ceiling_topic
    }
    return extractedTopics
  }
  
  def getParentTopics(topic:Topic, ceilingTopic:String, relatedTopics:Set[String]):Set[String] = {
    
    var modifiedTopics:Set[String] = relatedTopics
    if(topic.parent_topic != null) {
      
      //	See if the topic is the ceiling topic
      if(topic.parent_topic.equals(ceilingTopic)) {
    	  if (!modifiedTopics(ceilingTopic)) {
    	    modifiedTopics + ceilingTopic
    	  }
      }
      else {
        //	Find parent topic from Mongo
        val mongo:MongoOperations = null
        val query = new Query(Criteria.where("topic").is(topic.parent_topic))
        val parentTopic:Topic = mongo.findOne(query,classOf[Topic],"news-topic-tree")
        
        if (parentTopic != null) {
          // add to related topics
          if(!modifiedTopics(parentTopic.topic)) {
            modifiedTopics + parentTopic.topic
            modifiedTopics = getParentTopics(parentTopic,ceilingTopic,modifiedTopics)
          }
        } 
        else {
          //	Parent Topic not found -> this should never happen in mongo (maybe in storygraph idk)
          println("Could not find parent topic in Mongo, topic = " + topic.parent_topic)
        }     
      }
    }
    return modifiedTopics   
  }
  
}
