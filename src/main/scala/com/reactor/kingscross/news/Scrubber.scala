package com.reactor.kingscross.news

import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang.StringEscapeUtils

class Scrubber {
  
  private var getAllScrubbedDataURL:String = "https://api.mongolab.com/api/1/databases/winston-db/collections/names?f={_id:0}&apiKey=505a236ae4b0aeb1c2e28086"
  
  def scrubSpeechEvent(dirtySpeech:String):String = {
    
    if (dirtySpeech == null || dirtySpeech.equals("")) {
      return null
    }
    
    try {
      var scrubData:JsonNode = Tools.fetchURL(getAllScrubbedDataURL) //	TODO load data in on initialization
      
      if (scrubData == null || !scrubData.isArray) {
        return null
      }
      
      var cleanSpeech:String = clean(dirtySpeech)
      
      for (s:String <- dirtySpeech.split("(')|(\\W )|(\\W$)|(\\s)")) {
        var a = 0
        for (a <- 0 until scrubData.size()) {
          var wordNode:JsonNode = scrubData.get(a)
          var oldWord:String = wordNode.path("name").asText()
          var newWord:String = wordNode.path("pronounce").asText()
          
          if (s.equalsIgnoreCase(oldWord)) {
            cleanSpeech = cleanSpeech.replaceAll(s,newWord)
            //	TODO Break here
          }
        }
      }
      
      cleanSpeech = StringEscapeUtils.unescapeHtml(cleanSpeech)
      cleanSpeech = removeCircumflex(cleanSpeech)
      return cleanSpeech
      
    } catch {
      case e:Exception => e.printStackTrace()
      return dirtySpeech
    }
  } 
  
  def scrubFullText(dirtyText:String):String = {
    var cleanText:String = StringEscapeUtils.unescapeHtml(dirtyText)
    cleanText = removeCircumflex(cleanText)
    return cleanText
  }
 
  
  private def clean(s:String):String = {
    var cleanString:String = s.replaceAll("\\<.*?\\>", " ")
    cleanString = cleanString.replaceAll("\"", " ");
	cleanString = cleanString.replaceAll("-", " ");
	cleanString = cleanString.replaceAll("\\p{Pd}", " ");
	cleanString = cleanString.replaceAll("\\[UPDATE:.*\\]", " ");
    return cleanString
  }  
  
  private def removeCircumflex(dirty:String):String = {
    if (dirty == null) {
      return null
    }
    var clean:String = StringEscapeUtils.escapeHtml(dirty)
		clean = clean.replaceAll("&\\S{3,7};", "")
		return clean;
  }  
}