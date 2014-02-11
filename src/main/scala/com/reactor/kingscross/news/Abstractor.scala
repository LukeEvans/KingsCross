package com.reactor.kingscross.news

import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import java.awt.Image
import org.apache.commons.lang.StringEscapeUtils
import com.gravity.goose.Configuration
import com.gravity.goose.Goose
import com.gravity.goose.Article
import com.gravity.goose.Article
import com.fasterxml.jackson.databind.ObjectMapper

class Abstraction {

  var title: String = null
  var text: String = null
  var url: String = null
  var primary_images: Set[String] = Set()
  var secondary_images: Set[String] = Set()
  var entities: Set[Entity] = Set()
}


class Abstractor {

  var baseDifbotURL: String = "http://www.diffbot.com/api/article?token=2a418fe6ffbba74cd24d03a0b2825ea5&url="

  def getGooseAbstraction(url: String): Option[Abstraction] = {

    try {

      val config = new Configuration()
      config.enableImageFetching_$eq(false)
      val goose = new Goose(config)

      try {
        val article: Article = goose.extractContent(url)
        if (article == null) {
          println("Null article from Goose " + url)
          None
        }

        if (article.title == null || article.title.equals("")) {
          println("No article title from Goose " + url)
          None
        }

        val gooseResult: Abstraction = new Abstraction()
        clean(article.title) match {
          case Some(s: String) => gooseResult.title = s
          case None =>
            println("ERROR: Bad Abstraction, missing title")
            None
        }
        clean(article.cleanedArticleText) match {
          case Some(s: String) => gooseResult.text = s
          case None =>
            println("ERROR: Bad Abstraction, missing text")
            None
        }
        gooseResult.url = article.finalUrl

        //	Get Entities for Abstraction
        //	TODO create extractor on actor init
        val extractor = new EntityExtractor()
        gooseResult.entities = extractor.getEntitiesFromAlchemy(gooseResult.text)

        Some(gooseResult)

      } catch {
        case e: Exception => e.printStackTrace()
          None
      }
    } catch {
      case e: Exception => e.printStackTrace()
        None
    }
  }

  def getDifbotAbstraction(url: String): Option[Abstraction] = {

    try {
      //	Call diffbot and create an Abstraction object      
      Tools.fetchURL(baseDifbotURL + url) match {
        case None => None
        case Some(difbotResult: JsonNode) =>

          /*val mapper:ObjectMapper = new ObjectMapper()
          println("\nDiffbot Result:")
          println(mapper.writeValueAsString(difbotResult)+"\n")*/

          var data = new Abstraction()

          val titleNode = difbotResult.get("title")
          if (titleNode == null || !titleNode.isTextual) {
            None
          }

          val textNode = difbotResult.get("text")
          if (textNode == null || !textNode.isTextual) {
            None
          }

          val urlNode = difbotResult.get("url")
          if (urlNode == null || urlNode.isTextual) {
            None
          }

          clean(titleNode.asText) match {
            case Some(s: String) => data.title = s
            case None =>
              println("ERROR: Bad Abstraction, missing title")
              None
          }
          clean(textNode.asText) match {
            case Some(s: String) => data.text = s
            case None =>
              println("ERROR: Bad Abstraction, missing text")
              None
          }
          data.url = urlNode.asText()

          // Get Images
          val mediaNode = difbotResult.path("media")
          if (mediaNode.asInstanceOf[JsonNode].isArray) {
            for (a <- 0 until mediaNode.asInstanceOf[ArrayNode].size()) {
              val media: JsonNode = mediaNode.asInstanceOf[ArrayNode].get(a)

              //	Only work with multimedia of type 'image'
              val mediaType: String = media.path("type").asText()
              if (mediaType != null && mediaType.equalsIgnoreCase("image")) {
                val primaryStatus: String = media.path("primary").asText()
                var link: String = media.path("link").asText()

                //println("Found image link in Difbot - "+link)

                //	Filter out small images and bad links
                if (isValidImageLink(link.toLowerCase)) {
                  val i: Image = Tools.getImageFromURL(link)
                  if (i.getHeight(null) > 100 && i.getWidth(null) > 100) {
                    link = link.replaceAll(" ", "%20")

                    //	Image is large enough, add to appropriate image set
                    if (primaryStatus != null && primaryStatus.equalsIgnoreCase("true")) {
                      data.primary_images += link
                      //println(link + " added to primary images")
                    }
                    else {
                      data.secondary_images += link
                      //println(link + " added to secondary images")
                    }
                  }
                  else {
                    //println(link + " is too small an image")
                  }
                }
                else {
                  //println(link + " is an invalid image link")
                }
              }
            }
          }

          //println("\nDifbot found " + data.primary_images.size + " primary images and " + data.secondary_images.size + " secondary images\n")

          //	Get Entities for Abstraction
          //	TODO create extractor on actor init
          val extractor = new EntityExtractor()


          data.entities = extractor.getEntitiesFromAlchemy(data.text)

          Some(data)
      }


    } catch {
      case e: Exception => e.printStackTrace;
        None
    }
  }


  def isValidImageLink(url: String): Boolean = {
    //  TODO do we need to make this source specific?
    if (url.contains(".jpeg") || url.contains(".jpg") || url.contains(".png") || url.contains(".yimg") || url.contains("reutersmedia")) {
      true
    }
    else {
      println("Unrecognized image format, URL = " + url)
      false
    }
  }

  def clean(text: String): Option[String] = {
    if (text == null) {
      None
    }
    var s: String = StringEscapeUtils.escapeHtml(text)
    s = s.replaceAll("&quot;", "\"")
    s = s.replaceAll("&rdquo;", "\"")
    s = s.replaceAll("&ldquo;", "\"")
    s = s.replaceAll("&rsquo;", "'")
    s = s.replaceAll("&lsquo;", "'")
    s = s.replaceAll("&mdash;", "-")
    s = s.replaceAll("&ndash;", "-")
    s = s.replaceAll("\\\\n", " ")
    s = s.replaceAll("\\\\\"", "\"") // replace (\") with (")
    s = StringEscapeUtils.unescapeHtml(s)
    if (s == null) {
      None
    }
    Some(s)
  }
}



