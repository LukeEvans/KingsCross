package com.reactor.kingscross.news

import java.net.URL
import java.util.Date
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.control.Breaks
import org.apache.commons.lang.StringEscapeUtils
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.mongodb.casbah.{MongoCollection, MongoURI}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.fasterxml.jackson.databind.node.ArrayNode


class NewsChannel {
  var name: String = null
  var spoken: String = null
  var tagline: String = null
  var db: String = null
  var img: String = null
  var twitter_handle: String = null
  var category: String = null
}

class News(config: NewsConfig) extends Actor {
  // Ignore messages
  def receive = {
    case _ =>
  }
}

// Fetch news from RSS
class NewsEmitter(config: NewsConfig) extends Emitter(config) {

  println("\n\nStarting emitter for " + config.source_id + "\n\n")

  def handleEvent() {
    println("News emitter handle event for " + config.source_id + " from url " + config.source_url)

    // Fetch RSS
    try {
      val url = new URL(config.source_url)
      val reader = new XmlReader(url)
      val feed = new SyndFeedInput().build(reader)

      val entryLimit = 20 // Defines how many entries will be published
      var entriesPublished = 0

      val loop = new Breaks
      loop.breakable {
        for (entry <- feed.getEntries.asScala) {
          // Publish each story as JsonNode representation RSS object
          //println("New Story found for " + config.source_id);
          var entryMap: Map[String, Object] = Map()
          entryMap += ("entry_title" -> entry.asInstanceOf[SyndEntry].getTitle)

          if (entry.asInstanceOf[SyndEntry].getPublishedDate == null) {
            entryMap += ("entry_pubdate" -> new Date().toString)
          } else {
            entryMap += ("entry_pubdate" -> entry.asInstanceOf[SyndEntry].getPublishedDate.toString)
          }

          entryMap += ("entry_url" -> entry.asInstanceOf[SyndEntry].getLink)
          entryMap += ("entry_author" -> entry.asInstanceOf[SyndEntry].getAuthor)


          var categories: Set[String] = Set()
          for (category <- entry.asInstanceOf[SyndEntry].getCategories.asScala) {
            categories += category.asInstanceOf[SyndCategory].getName
          }
          if (!categories.isEmpty) {
            entryMap += ("categories" -> categories)
          }

          //	Extract any text associated with RSS
          var entry_text: String = ""
          for (content <- entry.asInstanceOf[SyndEntry].getContents.asScala) {
            entry_text += content.asInstanceOf[SyndContent].getValue
          }

          if (entry.asInstanceOf[SyndEntry].getDescription != null) {
            val description = entry.asInstanceOf[SyndEntry].getDescription.getValue
            if (description != null) {
              if (description.length() > entry_text.length()) {
                entry_text = description
              }
            }
          }
          entryMap += ("entry_text" -> entry_text)

          //	Create a json node out of entryMap and publish
          val mapper = new ObjectMapper()
          val entryNode: JsonNode = mapper.valueToTree(entryMap.asJava)


          //println("Publishing " + entryNode.toString())
          publish(event = entryNode, key = config.source_id + entryMap("entry_title"))
          entriesPublished += 1
          if (entriesPublished >= entryLimit) {
            //println("Emitter limit reached for " + config.source_id)
            loop.break()
          }
        }
      }
      //println(config.source_id + " emitter published " + entriesPublished + " stories")   // TODO some kind of call back from super on whether or not a story is emitted
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}

// Collect News
class NewsCollector(args: CollectorArgs) extends Collector(args) {

  val abstractor = new Abstractor()
  val jsoupAbstractor = new JsoupAbstractor()
  val summarizor = new Summarizor()
  val scrubber = new Scrubber()
  val topicExtractor = new TopicExtractor()
  val mapper = new ObjectMapper()


  //  Mongo Variables
  val uri = MongoURI("mongodb://levans002:dakota1@ds031887.mongolab.com:31887/winston-db")
  val winstonDB = uri.connectDB

  //  Source Data
  var id:String = args.config.source_id
  var iconLink:String = ""
  var ceilingTopic:String = ""
  var name:String = ""
  var category:String = ""
  var twitterHandle:String = ""

  var devChannel = false

  def init() {
    //  Load in the source data from Mongo

    val channelCollection:MongoCollection = new MongoCollection(winstonDB.right.get.getCollection("reactor-news-sources"))
    val query = MongoDBObject("source_id" -> id)
    channelCollection.findOne(query) match {
      case Some(x:DBObject) => parseChannelData(new MongoDBObject(x))
      case None =>
        //  Check in Dev collection
        val channelCollection:MongoCollection = new MongoCollection(winstonDB.right.get.getCollection("reactor-news-sources-dev"))
        val query = MongoDBObject("source_id" -> id)
        channelCollection.findOne(query) match {
          case Some(x:DBObject) =>
            devChannel = true
            parseChannelData(new MongoDBObject(x))
          case None =>
            println("\nERROR: No channel entry found for " + id)
        }
    }
  }

  def parseChannelData(channelData:MongoDBObject) {

    channelData.getAs[String]("icon") match {
      case Some(link:String) => iconLink = link
      case None =>
        println("\nWARNING: No source link found for " + id)
    }

    channelData.getAs[String]("name") match {
      case Some(n:String) => name = n
      case None =>
        println("\nWARNING: No source name found for " + id)
    }

    channelData.getAs[String]("category") match {
      case Some(s:String) => category = s
      case None => println("WARNING: Category field missing for " + id)
    }

    channelData.getAs[String]("twitter_handle") match {
      case Some(s:String) => twitterHandle = s
      case None => println("WARNING: twitter handle field missing for " + id)
    }

    channelData.getAs[String]("ceiling_topic") match {
      case Some(n:String) => ceilingTopic = n
      case None =>
        println("\nWARNING: No ceiling topic found for " + id)
    }
  }

  def handleEvent(event: EmitEvent) {
    //	Each news source overrides this method  
  }

  def addChannelInfo(story:NewsStory):NewsStory = {
    //  Add channel variables
    story.source_id = id
    story.source_category = category
    story.source_twitter_handle = twitterHandle
    story.ceiling_topic = ceilingTopic
    story.source_name = name
    story.source_icon_link = iconLink
    story
  }

  def parseEventData(data: JsonNode):Option[NewsStory] = {

    // Take event.data JsonNode built by Emitter and build a NewsStory object
    var story = new NewsStory()
    story.story_type = "News"

    story = addChannelInfo(story)


    //  Add emitter variables
    val title: String = data.get("entry_title").asText()
    story.headline = StringEscapeUtils.unescapeHtml(title)
    story.pubdate = data.get("entry_pubdate").asText()
    story.date = new Date(story.pubdate)
    story.link = data.get("entry_url").asText()
    story.author = data.get("entry_author").asText()


    //	Add Categories as entities
    val categoryJson:String = data.path("categories").asText()
    if (!categoryJson.equals("")) {
      val categories:JsonNode = mapper.readTree(categoryJson)
      if (categories!= null && !categories.isMissingNode) {
        for( a <- 0 until categories.size()) {
          val category:String = categories.get(a).asText()
          val entity = new Entity(category)
          story.entities += entity
        }
      }
    }

    if (checkInitialValidity(story)) {
      Some(story)
    }
    else
    {
      None
    }
  }

  def checkInitialValidity(story:NewsStory):Boolean = {

    if (story.ceiling_topic.equals("")) {
      false
    }

    if (story.source_name.equals("")) {
      false
    }

    if (story.source_icon_link.equals("")) {
      false
    }

    if (story.headline.equals("")) {
      false
    }

    if (story.pubdate.equals("")) {
      false
    }

    if (story.link.equals("")) {
      false
    }
    true
  }

  def abstractWithDifbot(url: String): Option[Abstraction] = {
    abstractor.getDifbotAbstraction(url) match {
      case Some(a: Abstraction) => Some(a)
      case None => None
    }
  }

  def abstractWithGoose(url: String):Option[Abstraction] = {
    abstractor.getGooseAbstraction(url)
  }

  def getTextFromJsoup(url:String, rules:ExtractionRules):Option[String] = {
    jsoupAbstractor.getText(url,rules)
  }

  def getSummary(headline: String, fullText: String): String = {

    if (fullText == null | fullText.equals("")) {
      println("Full text not found, can't summarize")
      return null
    }

    summarizor.getSummary(headline, fullText) match {
      case Some(summary: String) => summary
      case None => null
    }
  }

  def scrubSpeech(speech: String): Option[String] = {
    scrubber.scrubSpeechEvent(speech)
  }

  def scrubFullText(text: String): String = {
    scrubber.scrubFullText(text)
  }

  def getTopics(story: NewsStory): TopicSet = {
    topicExtractor.extractTopicsFromStory(story)
  }

  def publishStory(story: NewsStory, forceDev: Boolean) {
    // Publish the news story object as json

    if (forceDev || devChannel) {
      write_platform = "store-/news/dev"
    }

    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val json: JsonNode = mapper.valueToTree(story)

    println("\nPublishing story from " + story.source_id + " headline = " + story.headline + "\n")

    publish(json)
  }

}

// Mongo
class NewsMongoStorer(args: StorerArgs) extends MongoStore(args) {

  def handleEvent(event: CollectEvent) {

    //	TODO - Make sure the story is not a duplicate

    val source_id: String = event.data.path("source_id").asText()
    val headline: String = event.data.path("headline").asText()
    val isValid: Boolean = event.data.path("valid").asBoolean(false)

    if (source_id == null || headline == null) {
      //  Don't save story
      complete()
      return
    }


    val newsCollection: MongoCollection = new MongoCollection(db.right.get.getCollection(collection))
    val query = MongoDBObject("source_id" -> source_id, "headline" -> headline)
    newsCollection.findOne(query) match {
      case Some(x: DBObject) =>
        //  Found a matching story in the DB, see if it is valid
        val storyMatch = new MongoDBObject(x)
        val oldStoryValid: Boolean = storyMatch.getAs[Boolean]("valid") match {
          case Some(a: Boolean) => a
          case None => false
        }
        if (oldStoryValid) {
          //  Don't overwrite valid story
          println("\nDuplicate Story, don't save to Mongo\n")
          complete()
          return
        }
        else {
          if (isValid) {
            // New story is valid, old one is not, delete old story and publish
            println("\nFixed old invalid story, saving\n")
            newsCollection.remove(storyMatch.asDBObject)
            insert(event.data)
            complete()
            return
          }
          else {
            // New story and old story are invalid, do not overwrite old story
            println("Duplicate story, both invalid, don't save to mongo")
            complete()
            return
          }
        }
      case None =>
        //  No matching story, save to Mongo
        insert(event.data)
        complete()
    }

  }
}

// Elasticsearch
class NewsESStorer(args: StorerArgs) extends ElasticsearchStore(args) {

  def handleEvent(event: CollectEvent) {

    // Take event.data (NewsStory object stored as json) and store it in Elasticsearch
    // index(story)

    // Publish event.data complete message (Optional)
    // publish(event.data)

    // Completed event
    complete()
  }
}

// Titan
class NewsTitanStorer(args: StorerArgs) extends TitanStore(args) {

  def handleEvent(event: CollectEvent) {

    // Take event.data (NewsStory object stored as json) and store it in Titan
    // Build Template
    // index(storyTemplate)

    // Publish event.data complete message (Optional)
    // publish(event.data)

    complete()
  }
}