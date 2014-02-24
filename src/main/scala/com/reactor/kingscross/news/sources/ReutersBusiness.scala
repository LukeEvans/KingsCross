package com.reactor.kingscross.news.sources

import com.reactor.kingscross.config.NewsConfig
import com.reactor.base.patterns.pull.FlowControlFactory
import com.reactor.kingscross.news._
import akka.actor.Props
import com.reactor.kingscross.control.CollectorArgs
import com.reactor.kingscross.control.EmitEvent
import com.reactor.base.patterns.pull.FlowControlConfig
import scala.Some

//================================================================================
// 	Reuters Business
//  Notes: - abstract with Difbot, get text with Jsoup
//================================================================================

class ReutersBusinessNews(config:NewsConfig)  extends News(config:NewsConfig) {
  //Emitter
  val emitter = context.actorOf(Props(classOf[NewsEmitter], config))
  // Collector
	val flowConfig = FlowControlConfig(name="reutersBusinessCollector", actorType="com.reactor.kingscross.news.sources.ReutersBusinessNewsCollector")
	val collector = FlowControlFactory.flowControlledActorFor(context, flowConfig, CollectorArgs(config=config))

}


class ReutersBusinessNewsCollector(args:CollectorArgs) extends NewsCollector(args:CollectorArgs) {

  val allowFirstPersonSpeech:Boolean = false
  val isDevChannel:Boolean = false

  override def handleEvent(event:EmitEvent) {

    //	Fill out preliminary News Story fields
	  parseEventData(event.data) match {
      case None =>
        println("\nERROR: Channel variables are missing, can't collect story")
        complete()
        return
      case Some(story:NewsStory) =>
        //	Build article abstraction - this gets entire text and image URLs
        abstractWithDifbot(story.link)  match {
          case None =>
            println("COLLECTOR ERROR - Story creation failed at extraction creation for "+story.source_id)
            complete()
            return
          case Some(difbotAbstraction:Abstraction) =>
            //	Handle Images (custom to each news source)
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
            story.full_text = getTextFromJsoup(story.link,new ExtractionRules("id", "articleText", List())) match {
              case Some(s:String) => s
              case None => story.full_text
            }

            story.summary = getSummary(story.headline,story.full_text)
            story.speech = story.summary // TODO lots going on with speech field, can we simplify?

            //  Post Processing
            story.speech = scrubSpeech(story.speech) match {
              case Some(newSpeech:String) => newSpeech
              case None => story.speech
            }
            story.full_text = scrubFullText(story.full_text)
            story.entities = removeBadEntities(story.entities)

            //	Topic Extraction
            val extractedTopics:TopicSet = getTopics(story)
            story.related_topics = extractedTopics.relatedTopics
            story.main_topics = extractedTopics.mainTopics

            story.valid = story.checkValid(allowFirstPersonSpeech)

            publishStory(story, isDevChannel)

            // Completed Event
            complete()
        }
    }
  }

  //	Filter out bad source-specific entities
  def removeBadEntities(dirtyEnt:Set[Entity]):Set[Entity] = {
    dirtyEnt
  }
}

