package com.reactor.kingscross.news.sources

import com.reactor.kingscross.control.{CollectorArgs, EmitEvent}
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.{ObjectMapper, JsonNode}
import com.reactor.kingscross.config.NewsConfig
import com.reactor.base.patterns.pull.FlowControlConfig
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.kingscross.news.Abstraction
import com.reactor.kingscross.news.Entity
import com.reactor.kingscross.news.News
import com.reactor.kingscross.news.NewsEmitter
import com.reactor.kingscross.news.NewsCollector
import com.reactor.kingscross.news.NewsStory
import com.reactor.kingscross.news.TopicSet
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.MongoCollection
import akka.actor.Props
import com.reactor.base.utilities.Tools
import scala.collection.JavaConverters.mapAsJavaMapConverter
import java.text.SimpleDateFormat
import java.util.Date

//================================================================================
// 	ESPN
//  Notes: - abstract with Difbot
//================================================================================

class ESPNNews(config:NewsConfig)  extends News(config:NewsConfig) {
  //Emitter
  val emitter = context.actorOf(Props(classOf[ESPNNewsEmitter], config))
  // Collector
	val flowConfig = FlowControlConfig(name="espnCollector", actorType="com.reactor.kingscross.news.sources.ESPNNewsCollector")
	val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=config))

}

class ESPNNewsEmitter(config:NewsConfig) extends NewsEmitter(config:NewsConfig) {

  override def handleEvent() {
    println("News emitter handle event for " + config.source_id + " from url " + config.source_url)
    Tools.fetchURL("http://api.espn.com/v1/sports/news/headlines/top?apikey=mtwby5psjv48mh7eemys3yqe") match {
      case None =>
        println("\nERROR: ESPN api call did not work")
      case Some(results:JsonNode) =>
        val storiesNode:JsonNode = results.path("headlines")
        for (a <- 0 until storiesNode.size) {
          val storyNode:JsonNode = storiesNode.get(a)
          var entryMap: Map[String, Object] = Map()
          entryMap += ("espn_pubdate" -> storyNode.path("published").asText())
          entryMap += ("espn_headline" -> storyNode.path("title").asText())
          entryMap += ("espn_auther" -> storyNode.path("byline").asText())
          entryMap += ("espn_description" -> storyNode.path("description").asText())
          entryMap += ("espn_url" -> storyNode.path("links").path("web").path("href").asText())

          val imagesNode:JsonNode = storyNode.path("images")
          entryMap += ("espn_images" -> imagesNode.toString)

          val entitiesNode:JsonNode = storyNode.path("categories")
          entryMap += ("espn_entities" -> entitiesNode.toString)

          //	Create a json node out of entryMap and publish
          val mapper = new ObjectMapper()
          val entryNode: JsonNode = mapper.valueToTree(entryMap.asJava)

          publish(event = entryNode, key = config.source_id + entryMap("espn_headline"))
        }
    }
  }
}


class ESPNNewsCollector(args:CollectorArgs) extends NewsCollector(args:CollectorArgs) {

  var isDevChannel:Boolean = false

  override def handleEvent(event:EmitEvent) {

    //	Fill out preliminary News Story fields
	  val story:NewsStory = new NewsStory()

    story.story_type = "News"
    story.pubdate = event.data.path("espn_pubdate").asText()
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'")
    story.date = dateFormat.parse(story.pubdate)

    story.headline = event.data.path("espn_headline").asText()
    story.author = event.data.path("espn_author").asText()
    story.speech = event.data.path("espn_description").asText()
    story.full_text = event.data.path("espn_description").asText()
    story.summary = event.data.path("espn_description").asText()
    story.link = event.data.path("espn_url").asText()

    story.source_id = "espn"
	    
	  
	  //	TODO: Make a Mongo call only once a day - load data in an init method?
    //	TODO: Load parameters from Mongo
	  story.ceiling_topic = "sports"

	  val channelCollection:MongoCollection = new MongoCollection(winstonDB.right.get.getCollection("winston-channels"))
	  val query = MongoDBObject("db" -> story.source_id)
	  channelCollection.findOne(query) match {
      case Some(x:DBObject) =>
        val channel = new MongoDBObject(x)
        channel.getAs[String]("img") match {
          case Some(link:String) => story.source_icon_link = link
          case None =>
            //	Story is invalid, stop execution
            println("Story is invalid, no source link found")
            complete()
            return
        }


        channel.getAs[String]("name") match {
          case Some(name:String) => story.source_name = name
          case None =>
            //	Story is invalid, stop execution
            println("Story is invalid, no source name found")
            complete()
            return
        }

        channel.getAs[String]("category") match {
          case Some(s:String) => story.source_category = s
          case None => println("WARNING: Category channel field missing for " + story.source_id)
        }

        channel.getAs[String]("twitter_handle") match {
          case Some(s:String) => story.source_twitter_handle = s
          case None => println("WARNING: twitter handle channel field missing for "+story.source_id)
        }

      case None =>
        //	Check for channel from dev channel list
        val channelCollection:MongoCollection = new MongoCollection(winstonDB.right.get.getCollection("winston-channels-development"))
	      val query = MongoDBObject("db" -> story.source_id)

        channelCollection.findOne(query) match {
          case Some(x:DBObject) =>
            val channel = new MongoDBObject(x)
            channel.getAs[String]("img") match {
              case Some(link:String) => story.source_icon_link = link
              case None =>
                //	Story is invalid, stop execution
                println("Story is invalid, no source link found")
                complete()
                return
            }

            channel.getAs[String]("name") match {
              case Some(name:String) => story.source_name = name
              case None =>
                //	Story is invalid, stop execution
                println("Story is invalid, no source name found")
                complete()
                return
            }
            channel.getAs[String]("category") match {
              case Some(s:String) => story.source_category = s
              case None => println("WARNING: Category channel field missing for "+story.source_id)
            }
            channel.getAs[String]("twitter_handle") match {
              case Some(s:String) => story.source_twitter_handle = s
              case None => println("WARNING: twitter handle channel field missing for "+story.source_id)
            }

          case None =>
            println("ERROR: channel entry not found for "+story.source_id)
            complete()
            return

        }
    }

    
	  //	Add Categories as entities
    val mapper = new ObjectMapper()
	  val jsonData:String = event.data.path("espn_entities").asText

    if (!jsonData.equals("")) {

      val categories:JsonNode = mapper.readTree(jsonData)
      if (categories!= null) {
        if (categories.asInstanceOf[JsonNode].isArray) {
          for( a <- 0 until categories.asInstanceOf[ArrayNode].size()) {
            val category:String = categories.asInstanceOf[ArrayNode].get(a).toString
            if (shouldAddEntity(category)) {
              val entity = new Entity(category)
              story.entities += entity
            }
          }
        }
      }
    }

    //	Add images
    val json:String = event.data.path("espn_images").asText
    val images:JsonNode = mapper.readTree(json)
    if (images!= null) {
      for (a <- 0 until images.size) {
        story.image_links += images.get(a).toString
      }
    }
	  
	  
	  //	TODO: Check Initial Validity of story before continuing (move to super class?)

	  //	Get other entites from Difbot - broken TODO fix
	  /*abstractWithDifbot(story.link)  match {
      case None =>
        println("Difbot abstraction failed for "+story.source_id)

      case Some(difbotAbstraction:Abstraction) =>

        if (difbotAbstraction.entities != null && difbotAbstraction.entities.size > 0) {
          for (abstractEntity:Entity <- difbotAbstraction.entities) {
            if(!story.entities.contains(abstractEntity)) {
              //	TODO merge entities of same name
              story.entities += abstractEntity
            }
          }
        }
    } */

    //story.summary = getSummary(story.headline,story.full_text)
    story.speech = scrubSpeech(story.speech) match {
      case Some(newSpeech:String) => newSpeech
      case None => story.speech
    }
    story.full_text = scrubFullText(story.full_text)  // TODO should we be scrubbing the summary too?

    //	Topic Extraction
    val extractedTopics:TopicSet = getTopics(story)
    story.related_topics = extractedTopics.relatedTopics
    story.main_topics = extractedTopics.mainTopics

    story.valid = story.checkValid(false)

    publishStory(story, isDevChannel)

    // Completed Event
    complete()









  }

  //	Filter out bad source-specific entities
  def shouldAddEntity(entity:String):Boolean = true
}

