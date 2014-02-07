package com.reactor.kingscross.news

import com.reactor.base.utilities.HeadlineTextPost
import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode

class Summarizor {
  
  private var reductoBaseUrl:String = "http://reducto-cron.winstonapi.com:8080/"
  
  def getSummary(headline:String, fullText:String):Option[String] = {
    
    val post = new HeadlineTextPost(headline,fullText)
    val url = reductoBaseUrl + "text"
    Tools.postJSON(url,post) match {
      case Some(result:JsonNode) =>
        val summaryNode:JsonNode = result.path("summary")
        if (!summaryNode.isMissingNode) {
          Some(summaryNode.asText())
        }
        else {
          firstTwoSentencesSummary(fullText)
        }
      case None => firstTwoSentencesSummary(fullText)
    }
  }
  
  def firstTwoSentencesSummary(fullText:String):Option[String] = {
    
    try {
      val result:JsonNode = Tools.fetchURL("http://nlp-winston.elasticbeanstalk.com/process?text="+fullText)
      var summary:String = ""
      if (result.has("sentences") && result.get("sentences").size() > 1) {
        summary = result.get("sentences").get(0).asText + " " + result.get("sentences").get(1).asText
        Some(summary)
      }
      else {
        None
      }
    } catch {
      case e:Exception => e.printStackTrace()
      None
    }
  }
}