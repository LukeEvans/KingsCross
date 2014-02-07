package com.reactor.kingscross.news

import com.reactor.base.utilities.HeadlineTextPost
import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import scala.util.control.Breaks._

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
          firstTwoSentencesSummaryFromNLP(fullText)
        }
      case None => firstTwoSentencesSummaryFromNLP(fullText)
    }
  }
  
  def firstTwoSentencesSummaryFromNLP(fullText:String):Option[String] = {
    
    try {
      Tools.fetchURL("http://nlp-winston.elasticbeanstalk.com/process?text="+fullText) match {
        case Some(result:JsonNode) =>
          var summary:String = ""
          if (result.has("sentences")) {
            if (result.get("sentences").size() > 1)   {
              summary = result.get("sentences").get(0).asText + " " + result.get("sentences").get(1).asText
              Some(summary)
            }
            else {
              firstTwoSentencesSummaryFromText(fullText)
            }
          }
          else {
            firstTwoSentencesSummaryFromText(fullText)
          }
        case None => firstTwoSentencesSummaryFromText(fullText)
      }
    } catch {
      case e:Exception => e.printStackTrace()
      firstTwoSentencesSummaryFromText(fullText)
    }
  }

  def firstTwoSentencesSummaryFromText(fullText:String):Option[String] = {

    //  Break up full text on periods
    val textFragments:Array[String] = fullText.split("\\.")

    //  Add fragments back until we exceed a certain minimum count
    var summary:String = ""
    breakable {
      for (a <- 0 until textFragments.size)   {
        var s:String = textFragments(a)

        s = s.replaceAll("ó", "")
        s = s.replaceAll("\"", "")

        // Replace anything like that looks like "(a.p.) -" or "(reuters) -" pretty much anything with parenthesis and a dash
        s = s.replaceAll(".*\\(.+\\) *-* *", "")
        s = s.replaceAll(".*- ", "")


        if (a == 0) {
          // Removes Capitalized tags at beginning of articles
          s = s.replaceAll("^[A-Z]{2,} *[A-Z]{2,} *-* *", "")
        }

        summary += s + "."

        if (summary.length > 100) {
          break()
        }
      }
    }

    if(summary.length > 100) {
      // Clean Summary
      summary = summary.replaceAll("&quot;", "\"")
      summary = summary.replaceAll("&rdquo;", "\"")
      summary = summary.replaceAll("&ldquo;", "\"")
      summary = summary.replaceAll("&rsquo;", "'")
      summary = summary.replaceAll("&lsquo;", "'")
      summary = summary.replaceAll("&mdash;", "-")
      summary = summary.replaceAll("&ndash;", "-")
      println("Reducto and NLP failed, returning dumb summary")
      Some(summary)
    }
    else {
      println("ERROR: Dumb summary is too short")
      None
    }

  }
}