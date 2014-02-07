package com.reactor.kingscross.news

import com.mongodb.casbah.MongoURI
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.{BasicDBList, DBObject}

class Topic {
  var topic:String = null
  var parent_topic:String = null
  var child_topics:List[Any] = List()
  var akas:List[Any] = List()
  
  def this(dbEntry:MongoDBObject) {
    this()
    val topic = dbEntry.getAs[String]("topic") match {
      case Some(s:String) => this.topic = s
      case None => println("Missing Topic")
    }
    val parentTopic = dbEntry.getAs[String]("parent_topic") match {
      case Some(s:String) => this.parent_topic = s
      case None =>
    }
    val childTopics = dbEntry.getAs[MongoDBList]("child_topics") match {
      case Some(s:MongoDBList) => this.child_topics = s.toList
      case None =>
    }
    val akaData = dbEntry.getAs[MongoDBList]("akas") match {
      case Some(s:MongoDBList) => this.akas = s.toList
      case None =>
    }
  }
}

class TopicSet {
  var mainTopics:Set[String] = Set()
  var relatedTopics:Set[String] = Set()
}

class TopicExtractor {
  val uri = MongoURI("mongodb://levans002:dakota1@ds031887.mongolab.com:31887/winston-db")
  val winstonDB = uri.connectDB
  def extractTopicsFromStory(story:NewsStory):TopicSet = {
    
    if (story.entities == null) {
      println("ERROR: null entity set")
      return null
    }

    // TODO Make sure story has summary and headline fields
    
    val extractedTopics:TopicSet = new TopicSet() 
    
    for(ent:Entity <- story.entities) {
      
      if(ent.entity_name != null) {
        val cleanedEntity:String = ent.entity_name.replace(" ","_")
        
        //	Try to find matching topic in mongo
        val collection = new MongoCollection(winstonDB.right.get.getCollection("news_topic_tree"))
        val query = MongoDBObject("topic" -> cleanedEntity.toLowerCase)
        val resultOption = collection.findOne(query) match {
          case Some(x:DBObject) => {
            
            //	Match found, add topic as a related topic
            val topicMatch:Topic = new Topic(new MongoDBObject(x))
            if (!extractedTopics.relatedTopics(topicMatch.topic)) {
              extractedTopics.relatedTopics += topicMatch.topic
            }
            
            //	Add topic as a main topic if it is contained in the headline or summary
            if(story.headline.toLowerCase().contains(ent.entity_name.toLowerCase()) || story.summary.toLowerCase().contains(ent.entity_name.toLowerCase())) {
              if(!extractedTopics.mainTopics(topicMatch.topic)) {
                extractedTopics.mainTopics += topicMatch.topic
                //	Add parent topics of main topics to related topics
                extractedTopics.relatedTopics = getParentTopics(topicMatch,story.ceiling_topic,extractedTopics.relatedTopics)
              }
            }
          }
          case None =>  {
            //	No direct match found, look for AKAs
            val query = MongoDBObject("aka" -> cleanedEntity.toLowerCase)
            val dbResult = collection.findOne(query) match {
              case Some(x:DBObject) => {
                val akaTopic:Topic = new Topic(new MongoDBObject(x))
                //	Match found, add topic as a related topic
                if(!extractedTopics.relatedTopics(akaTopic.topic)) {
                  extractedTopics.relatedTopics += akaTopic.topic
            
                  //	Add topic as a main topic if it is contained in the headline or summary
                  if(story.headline.toLowerCase().contains(ent.entity_name.toLowerCase()) || story.summary.toLowerCase().contains(ent.entity_name.toLowerCase())) {
                    if(!extractedTopics.mainTopics(akaTopic.topic)) {
                      extractedTopics.mainTopics += akaTopic.topic
                      //	Add parent topics of main topics to related topics
                      extractedTopics.relatedTopics = getParentTopics(akaTopic,story.ceiling_topic,extractedTopics.relatedTopics)
                }
              }
            }
              }
              case None => {
                //	Entity is not matched with topic - save miss to mongo
                val missCollection = new MongoCollection(winstonDB.right.get.getCollection("topic_misses"))
                val missQuery = MongoDBObject("topic" -> cleanedEntity.toLowerCase)
                val dbResult = missCollection.findOne(missQuery) match {
                  case Some(x:DBObject) => {
                    val missedTopic:MongoDBObject = new MongoDBObject(x)
                    val count = missedTopic.getAs[Int]("count") match {
                      case Some(x:Int) => {
                        val newCount:Int = x + 1
                        missedTopic.update("count", newCount.asInstanceOf[AnyRef])
                        missCollection.update(missQuery, missedTopic.asDBObject)
                      }
                      case None => {
                        //	Create a new missed Topic
                        missCollection.save(MongoDBObject("topic" -> cleanedEntity.toLowerCase, "count" -> 0 ))
                      }
                    }
                  }
                  case None => {
                    
                  }
                }
              }
            }
          }
        }
      }
    }
    
    if (extractedTopics.relatedTopics.size == 0) {
      extractedTopics.relatedTopics += story.ceiling_topic
    }
    return extractedTopics
  }
  
  def getParentTopics(topic:Topic, ceilingTopic:String, relatedTopics:Set[String]):Set[String] = {
    
    var modifiedTopics:Set[String] = relatedTopics
    if(topic.parent_topic != null) {
      
      //	See if the topic is the ceiling topic
      if(topic.parent_topic.equals(ceilingTopic)) {
    	  if (!modifiedTopics(ceilingTopic)) {
    	    modifiedTopics += ceilingTopic
    	  }
      }
      else {
        //	Find parent topic from Mongo
    	val topicCollection = new MongoCollection(winstonDB.right.get.getCollection("news_topic_tree"))
    	val query = MongoDBObject("topic" -> topic.parent_topic)
    	val dbResult = topicCollection.findOne(query) match {
    	  case Some(x:DBObject) => {
    	    //	Add to related topics
    	    val parentTopic = new Topic(new MongoDBObject(x))
    	    if (!modifiedTopics(parentTopic.topic)) {
              modifiedTopics += parentTopic.topic
              modifiedTopics = getParentTopics(parentTopic,ceilingTopic,modifiedTopics)
            }
    	  }
    	  case None => {
    	   //	Parent Topic not found -> this should never happen in mongo (maybe in storygraph idk)
            println("Could not find parent topic in Mongo, topic = " + topic.parent_topic)
    	  }
    	}    
      }
    }
    return modifiedTopics   
  }
  
}
