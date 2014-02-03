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

class Abstraction {
  
  var title:String = null
  var text:String = null
  var url:String = null
  var primary_images:Set[String] = Set()
  var secondary_images:Set[String] = Set()
  var entities:Set[Entity] = Set()
}


class Abstractor {
  
  var baseDifbotURL:String = "http://www.diffbot.com/api/article?token=2a418fe6ffbba74cd24d03a0b2825ea5&url="
  
  def getGooseAbstraction(url:String):Abstraction = {
    
    try {
      
      val config = new Configuration()
      config.enableImageFetching_$eq(false)
      val goose = new Goose(config)
     
      try{
			val article:Article = goose.extractContent(url);
			if (article == null) {
			  println("Null article from Goose " + url)
			  return null
			}
			
			if (article.title == null || article.title.equals("")) {
			  println("No article title from Goose " + url)
			  return null
			}
			
			val gooseResult:Abstraction = new Abstraction()
			gooseResult.title = clean(article.title)
			gooseResult.text = clean(article.cleanedArticleText)
			gooseResult.url = article.finalUrl
			
			//	Get Entities for Abstraction
			//	TODO create extractor on actor init
			val extractor = new EntityExtractor()
			gooseResult.entities = extractor.getEntitiesFromAlchemy(gooseResult.text)
			
			return gooseResult 

		} catch {
		  case e:Exception => e.printStackTrace()
		}     
    } catch {
      case e:Exception => e.printStackTrace()
    }
    return null
  }
  
  def getDifbotAbstraction(url:String):Abstraction = {
    
    try {
      //	Call diffbot and create an Abstraction object
      val difbotResult = Tools.fetchURL(baseDifbotURL + url)
      var data = new Abstraction()
      data.title = clean(difbotResult.asInstanceOf[JsonNode].path("title").asText())
      data.text = clean(difbotResult.asInstanceOf[JsonNode].path("text").asText())
      data.url = difbotResult.asInstanceOf[JsonNode].path("url").asText()
      data = getImages(difbotResult,data)
		
      //	Get Entities for Abstraction
      //	TODO create extractor on actor init
      val extractor = new EntityExtractor()
      data.entities = extractor.getEntitiesFromAlchemy(data.text)
      
      return data
    
    } catch {
      case e:Exception => e.printStackTrace;
      return null
    }
  }
  
  def getImages(abstractResult:JsonNode, data:Abstraction):Abstraction = {
    
    var mediaNode = abstractResult.path("media")
    if (mediaNode.asInstanceOf[JsonNode].isArray()) {
      var a = 0
      for( a <- 0 until mediaNode.asInstanceOf[ArrayNode].size()) {
        var media:JsonNode = mediaNode.asInstanceOf[ArrayNode].get(a)
        
        //	Only work with multimedia of type 'image'
        var mediaType:String = media.path("type").asText()
        if (mediaType != null && mediaType.equalsIgnoreCase("image")) {
          var primaryStatus:String = media.path("primary").asText()
          var link:String = media.path("link").asText()
          
          //	Filter out small images and bad links
          if (isValidImageLink(link.toLowerCase())) {
            val i:Image = Tools.getImageFromURL(link)
            if (i.getHeight(null) > 100 && i.getWidth(null) > 100) {
              link = link.replaceAll(" ","%20")
              
              //	Image is large enough, add to appropriate image set
              if (primaryStatus != null && primaryStatus.equalsIgnoreCase("true")) {
                data.primary_images + link
              } 
              else {
                data.secondary_images + link
              }
            }
          }
        }
      }
    }   
    return data
  }
  
  def isValidImageLink(url:String):Boolean = {
    if (url.contains(".jpeg") || url.contains(".jpg") || url.contains(".png")) {
      return true;
    }
    println("Unrecognized image format, URL = " + url)
    return false
  }
  
  def clean(text:String):String = {
    var s:String = StringEscapeUtils.escapeHtml(text);	
	s = s.replaceAll("&quot;", "\"")
	s = s.replaceAll("&rdquo;", "\"")
	s = s.replaceAll("&ldquo;", "\"")
	s = s.replaceAll("&rsquo;", "'")
	s = s.replaceAll("&lsquo;", "'")
	s = s.replaceAll("&mdash;", "-")
	s = s.replaceAll("&ndash;", "-")	
	s = StringEscapeUtils.unescapeHtml(s);
    return s
  }
}



