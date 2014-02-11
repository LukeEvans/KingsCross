package com.reactor.kingscross.news.sources

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.JsonNode
import com.reactor.kingscross.config.NewsConfig
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.kingscross.news._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.MongoCollection
import akka.actor.Props
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.EmitEvent
import com.reactor.base.patterns.pull.FlowControlConfig
import scala.Some
import java.awt.Image
import com.reactor.base.utilities.Tools

//================================================================================
// 	NPR Sports
//  Notes: - abstract with Difbot, get text with Jsoup
//================================================================================

class NPRSportsNews(config:NewsConfig) extends News(config:NewsConfig) {
   //Emitter
   val emitter = context.actorOf(Props(classOf[NewsEmitter], config))
   // Collector
   val flowConfig = FlowControlConfig(name="nprSportsCollector", actorType="com.reactor.kingscross.news.sources.NPRSportsNewsCollector")
   val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=config))

 }


class NPRSportsNewsCollector(args:CollectorArgs) extends NewsCollector(args:CollectorArgs) {

   var isDevChannel:Boolean = false

   override def handleEvent(event:EmitEvent) {

     //	Fill out preliminary News Story fields
     val story:NewsStory = parseEventData(event.data)
     story.source_id = "spr"


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
     val categories:JsonNode = event.data.path("categories")
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


     //	TODO: Check Initial Validity of story before continuing (move to super class?)

     //	Build article abstraction - this gets entire text and image URLs
     abstractWithDifbot(story.link)  match {
       case None =>
         println("COLLECTOR ERROR - Story creation failed at extraction creation for "+story.source_id)
         complete()
         return
       case Some(difbotAbstraction:Abstraction) =>
         //	Handle Images (custom to each news source)

         //  Filter out bad NPR images
         for(link:String <- difbotAbstraction.primary_images) {
           if(link.contains("branding_main") || link.contains("branding_icon")) {
             difbotAbstraction.primary_images -= link
           }
           //  Filter out thumbnails from other stories
           val i: Image = Tools.getImageFromURL(link)
           if (i.getHeight(null) == 175 && i.getWidth(null) == 175) {
             difbotAbstraction.primary_images -= link
           }
         }

         for(link:String <- difbotAbstraction.secondary_images) {
           if(link.contains("branding_main") || link.contains("branding_icon")) {
             difbotAbstraction.secondary_images -= link
           }
           //  Filter out thumbnails from other stories
           val i: Image = Tools.getImageFromURL(link)
           if (i.getHeight(null) == 175 && i.getWidth(null) == 175) {
             difbotAbstraction.secondary_images -= link
           }
         }

         if (difbotAbstraction.primary_images.size > 0) {
           story.image_links = difbotAbstraction.primary_images
         }
         else {
           story.image_links = difbotAbstraction.secondary_images
         }

         //println(story.image_links.size + " images links added to story\n")

         story.parseAbstraction(difbotAbstraction)
         story.speech = story.buildSpeech()

         //  Use Jsoup to find the full text
         val ignoreCases:List[String] = List("bucketwrap")
         story.full_text = getTextFromJsoup(story.link,new ExtractionRules("id", "storytext", ignoreCases)) match {
           case Some(s:String) => s
           case None => story.full_text
         }

         story.summary = getSummary(story.headline,story.full_text)
         story.speech = story.summary // TODO lots going on with speech field, can we simplify?

         story.speech = scrubSpeech(story.speech) match {
           case Some(newSpeech:String) => newSpeech
           case None => story.speech
         }

         story.full_text = scrubFullText(story.full_text)


         //	Topic Extraction
         val extractedTopics:TopicSet = getTopics(story)
         story.related_topics = extractedTopics.relatedTopics
         story.main_topics = extractedTopics.mainTopics

         val allowFirstPersonSpeech:Boolean = false
         story.valid = story.checkValid(allowFirstPersonSpeech)

         publishStory(story, isDevChannel)

         // Completed Event
         complete()
     }

   }

   //	Filter out bad source-specific entities
   def shouldAddEntity(entity:String):Boolean = true
 }

