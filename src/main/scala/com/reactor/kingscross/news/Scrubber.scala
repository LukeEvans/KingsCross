package com.reactor.kingscross.news

import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang.StringEscapeUtils

class Scrubber {
  
  private var getAllScrubbedDataURL:String = "https://api.mongolab.com/api/1/databases/winston-db/collections/names?f={_id:0}&apiKey=505a236ae4b0aeb1c2e28086"
  
  def scrubSpeechEvent(dirtySpeech:String):Option[String] = {
    
    if (dirtySpeech == null || dirtySpeech.equals("")) {
      None
    }
    
    try {
      //	TODO load data in on initialization
      Tools.fetchURL(getAllScrubbedDataURL)  match {
        case None => None
        case Some(scrubData:JsonNode) =>
          if(!scrubData.isArray) {
            None
          }

          var cleanSpeech:String = clean(dirtySpeech)

          for (s:String <- dirtySpeech.split("(')|(\\W )|(\\W$)|(\\s)")) {
            var a = 0
            for (a <- 0 until scrubData.size()) {
              val wordNode:JsonNode = scrubData.get(a)
              val oldWord:String = wordNode.path("name").asText()
              val newWord:String = wordNode.path("pronounce").asText()

              if (s.equalsIgnoreCase(oldWord)) {
                cleanSpeech = cleanSpeech.replaceAll(s,newWord)
                //	TODO Break here
              }
            }
          }

          cleanSpeech = StringEscapeUtils.unescapeHtml(cleanSpeech)
          cleanSpeech = removeCircumflex(cleanSpeech)
          Some(cleanSpeech)
      }
    } catch {
      case e:Exception => e.printStackTrace()
      return None
    }
  } 
  
  def scrubFullText(dirtyText:String):String = {
    var cleanText:String = StringEscapeUtils.unescapeHtml(dirtyText)
    cleanText = removeCircumflex(cleanText)
    cleanText
  }
 
  
  private def clean(s:String):String = {
    var cleanString:String = s.replaceAll("\\<.*?\\>", " ")
    cleanString = cleanString.replaceAll("\"", " ");
	  cleanString = cleanString.replaceAll("-", " ");
	  cleanString = cleanString.replaceAll("\\p{Pd}", " ");
	  cleanString = cleanString.replaceAll("\\[UPDATE:.*\\]", " ");
    cleanString
  }  
  
  private def removeCircumflex(dirty:String):String = {
    if (dirty == null) {
      return null
    }
    var clean:String = StringEscapeUtils.escapeHtml(dirty)
		clean = clean.replaceAll("&\\S{3,7};", "")
		clean
  }  
}