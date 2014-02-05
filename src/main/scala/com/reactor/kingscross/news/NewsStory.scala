package com.reactor.kingscross.news

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang.StringEscapeUtils
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import com.reactor.base.utilities.TextPost
import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.node.ArrayNode
import java.util.regex.Pattern
import java.util.regex.Matcher

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
  var full_text:String = null
  var pubdate:String = null
  var link:String = null
  var source_icon_link:String= null
  var source_twitter_handle:String = null
  var image_links:Set[String] = Set()
  var reviewed:Boolean = false
  var video:JsonNode = null
  
  //	Topic Variables
  var ceiling_topic:String = null
  var related_topics:Set[String] = Set()
  var main_topics:Set[String] = Set()
  
  def parseAbstraction(abstractData:Abstraction)  {
    
    full_text = StringEscapeUtils.unescapeHtml(abstractData.text)
    if(full_text != null) {
      full_text = full_text.replaceAll("/n"," ")
    }
    
    if (abstractData.entities != null && abstractData.entities.size > 0) {
      for (abstractEntity:Entity <- abstractData.entities) {
        if(!entities.contains(abstractEntity)) {
          //	TODO merge entities of same name
          entities + abstractEntity
        }
      } 
    }
    
  }
  
  def parseAbstraction(gooseResult:Abstraction, difbotResult:Abstraction) {
	  // TODO !!!!
  }
  
  def buildSpeech():String = {
    if(full_text == null) {
      println("No full text found for " + headline + " from " + source_name);
      
      var doc:Document = Jsoup.parse(full_text)
      var parsed:String = cleanString(doc.text)
      
      //	Try for the NLP solution first
      var nlpAbstract:String = getReactorSpeech(parsed)
      if (nlpAbstract !=null && nlpAbstract.length > 20) {
        return cleanString(nlpAbstract)
      }
      
      //	Default Behavior - get the first 3 sentences
      val re:Pattern = Pattern.compile("^(.*?)[.?!] ")
	  val re2:Pattern = Pattern.compile("^(.*?)[.?!]$")
      // Get the first 3 sentences
	  var result = ""
	  var a = 0
	  for (a <- 0 until 3) {
	    val reMatcher:Matcher = re.matcher(parsed)
	    val reMatcher2:Matcher = re2.matcher(parsed)
	    if (reMatcher.find()) {
	      if (shouldAddSentence(reMatcher.group())) {
	        result += reMatcher.group()
	      }
	      parsed = parsed.replace(reMatcher.group(),"")
	    }
	    else if (reMatcher2.find()) {
	      if (shouldAddSentence(reMatcher2.group())) {
	        result += reMatcher2.group()
	      }
	      parsed = parsed.replace(reMatcher2.group(),"")
	    }
	    
	    if (result.length() >= 200) {
	      return cleanString(result)
	    }
	  }
      return cleanString(result)
    }
    return null
  }
  
  def cleanString(s:String):String = {
    
    var cleanS:String = StringEscapeUtils.escapeHtml(s)
    cleanS = cleanS.replaceAll("&rsquo;", "'");
	cleanS = cleanS.replaceAll("&lsquo;", "'");	
	cleanS = cleanS.replaceAll("&mdash;", "-");	
	cleanS = StringEscapeUtils.unescapeHtml(cleanS);	
    return cleanS
  }
  
  def getReactorSpeech(text:String):String =  {
    
    var postURL:String = "https://nlp.winstonapi.com/process"
    var s:String = text
    if (s.length > 400) {
      s = s.substring(0,400)
    }
      
    try {
      val textPost = new TextPost(s)
      var node:JsonNode = Tools.postJSON(postURL,textPost)
      if (node == null || node.path("sentences") == null) {
        println("Invalid JSON node from Reactor speech engine")
        return null
      }
      
      if (node.path("sentences").isArray) {
        val sentences = node.path("sentences")
        var a = 0
        var result:String = ""
        for(a <- 0 until sentences.asInstanceOf[ArrayNode].size()) {
          var sentence:String = sentences.get(a).asText
          if (shouldAddSentence(sentence)) {
            result += sentence + " "
          }
          if (result.length >= 200) {
            return result
          }
        }
      }
      
      
      
    } catch {
      case e:Exception => e.printStackTrace
    }    
    return null
  }
  
  def shouldAddSentence(s:String):Boolean = {
    if (s.contains("Add to Del.") || s.contains("This story was originally published")) {
      return false
    }
    return true
  }
  
  def checkValid():Boolean = {
    
    if (story_type != "News") {
      println("Story is invalid due to errant story_type")
      return false
    }
    
    if (source_id == null || source_id.length == 0) {
      println("Story is invalid due to errant source_id")
      return false
    }
    
    if (source_name == null || source_name.length == 0) {
      println("Story is invalid due to errant source_name")
      return false
    }
    
    if (source_category == null || source_category.length == 0) {
      println("Story is invalid due to errant source_category")
      return false
    }
    
    if (headline == null || headline.length == 0) {
      println("Story is invalid due to errant headline")
      return false
    }
    
    if (summary == null || summary.length == 0) {
      println("Story is invalid due to errant summary")
      return false
    }
    
    if (speech == null || speech.length() < 60 || isFirstPerson(speech)) {
     println("Story is invalid due to errant speech")
     return false
    }
    
    if (pubdate == null || pubdate.length == 0) {
      println("Story is invalid due to errant pubdate")
      return false
    }
    
    if (link == null || link.length == 0) {
      println("Story is invalid due to errant link")
      return false
    }
    
    if (source_icon_link == null || source_icon_link.length == 0) {
      println("Story is invalid due to errant source_icon_link")
      return false
    }
    
    if (image_links == null || image_links.size == 0) {
      println("Story is invalid due to errant image_links Set")
      return false
    }
    
    if (ceiling_topic == null || ceiling_topic.length == 0) {
      println("Story is invalid due to errant ceiling_topic")
      return false
    }
    
    if (related_topics == null || related_topics.size == 0) {
      println("Story is invalid due to errant related_topics Set")
      return false
    }  
    
    return true
  }

  private def isFirstPerson(text:String):Boolean = {
    
    if (text.contains(" my ") || text.contains(" My ") || text.contains(" I ") || text.contains(" I'")) {
      return true
    }
    return false
  }
  
  
  
  
  
}