package com.reactor.kingscross.news

import com.reactor.base.utilities.HeadlineTextPost
import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode

class Summarizor {
  
  private var reductoBaseUrl:String = "http://reducto-cron.winstonapi.com:8080/"
  
  def getSummary(headline:String, fullText:String):String = {
    
    val post = new HeadlineTextPost(headline,fullText)
    var url = reductoBaseUrl + "text"
    var result:JsonNode = Tools.postJSON(url,post)
    
    if (result == null || result.isMissingNode()) {
      println("Bad result from Reducto")
      return null
    }
    
    var summaryNode:JsonNode = result.path("summary")
    if (!summaryNode.isMissingNode()) {
      return summaryNode.asText
    }
    return null
  }
  
  def firstTwoSentencesSummary(fullText:String):String = {
    
    try {
      val result:JsonNode = Tools.fetchURL("http://nlp-winston.elasticbeanstalk.com/process?text="+fullText)
      var summary:String = ""
      if (result.has("sentences") && result.get("sentences").size() > 1) {
        summary = result.get("sentences").get(0).asText + " " + result.get("sentences").get(1).asText
      }
      else {
        println("Bad 2 sentence summary result from NLP server")
      }
      return summary
    } catch {
      case e:Exception => e.printStackTrace()
      return ""
    }
  }
}