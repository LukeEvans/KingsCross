package com.reactor.kingscross.news

import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import scala.collection.JavaConverters._
import java.util.ArrayList
import scala.collection.mutable.ArrayBuffer

/**
 * Created with IntelliJ IDEA.
 * User: Steve
 * Date: 2/10/14
 * Time: 5:00 PM
 * Company: Reactor Inc.
 */

class ExtractionRules {
  var tag:String = null
  var id:String = null
  var ignoreCases:List[String] = List()

  def this(extractTag:String,extractID:String,extractIgnoreCases:List[String]) {
    this()
    tag = extractTag
    id = extractID
    ignoreCases = extractIgnoreCases
  }
}


class JsoupAbstractor {


  def getText(url:String,rules:ExtractionRules):Option[String] = {

    if (rules.tag.equals("id")) {
      getTextByID(url,rules.id)
    }
    else if (rules.tag.equals("class")) {
      getTextByClass(url,rules.id,rules.ignoreCases)
    }
    else if (rules.tag.equals("p")) {
      getTextByP(url,rules.ignoreCases)
    }
    else if (rules.tag.equals("div")) {
      getTextByDiv(url)
    }
    else {
      println("\nWARNING: Unrecognized Extraction Rules Tag\n")
      None
    }
  }

  private def getTextByID(url:String,id:String):Option[String] = {
    try {
      val doc:Document = Jsoup.connect(url).get
      if (doc == null) {
        println("\nWARNING: Jsoup document is NULL for " + url)
        None
      }
      val elem:Element = doc.getElementById(id)
      getElementList(elem.children()) match {
        case None => None
        case Some(strings:ArrayBuffer[String]) => Some(getTextFromElements(strings))
      }
    }
    catch {
      case e:Exception =>
        println("\nJsoup failed to connect to " + url + " reason: " + e.getLocalizedMessage)
        None
    }
  }

  private def getTextByClass(url:String,htmlClass:String, ignoreCases:List[String]):Option[String] = {
    try {
      val doc:Document = Jsoup.connect(url).get()
      if (doc == null) {
        println("\nWARNING: Jsoup document is NULL for " + url)
        None
      }
      getElementList(doc.getElementsByClass(htmlClass), ignoreCases) match {
        case None => None
        case Some(strings:ArrayBuffer[String]) => Some(getTextFromElements(strings))
      }
    }
    catch {
      case e:Exception =>
        println("\nJsoup failed to connect to " + url + " reason: " + e.getLocalizedMessage)
        None
    }
  }

  private def getTextByP(url:String,ignoreCases:List[String]):Option[String] = {
    try {
      val doc:Document = Jsoup.connect(url).get
      if (doc == null) {
        println("\nWARNING: Jsoup document is NULL for " + url)
        None
      }
      getElementList(doc.select("p"),ignoreCases) match {
        case None => None
        case Some(paragraphs:ArrayBuffer[String]) => Some(getTextFromElements(paragraphs))
      }
    } catch {
      case e:Exception =>
        println("\nJsoup failed to connect to " + url + " reason: " + e.getLocalizedMessage)
        None
    }
  }

  private def getTextByDiv(url:String):Option[String] = {
    try {
      val doc:Document = Jsoup.connect(url).get
      if (doc == null) {
        println("\nWARNING: Jsoup document is NULL for " + url)
        None
      }
      getElementList(doc.select("div")) match {
        case None => None
        case Some(paragraphs:ArrayBuffer[String]) => Some(getTextFromElements(paragraphs))
      }
    } catch {
      case e:Exception =>
        println("\nJsoup failed to connect to " + url + " reason: " + e.getLocalizedMessage)
        None
    }
  }


  // Helper Methods
  private def getElementList(elems:Elements):Option[ArrayBuffer[String]] = {

    val strings:ArrayBuffer[String] = ArrayBuffer()

    for (a <- 0 until elems.size) {
      val elem:Element = elems.get(a)
      val text:String = elem.text
      if (!text.isEmpty) {
        val lastChar:Char = text.charAt(text.length()-1)
        if (lastChar == '.' || lastChar == '!' || lastChar == '"' || lastChar == '?') {
          strings += text
        }
      }
    }

    if (strings.size > 0) {
     Some(strings)
    }
    else {
      None
    }
}

  private def getElementList(elems:Elements, ignoreCases:List[String]):Option[ArrayBuffer[String]] = {

    val strings:ArrayBuffer[String] = ArrayBuffer()

    for (a <- 0 until elems.size()) {

      val elem:Element = elems.get(a)
      //  Check ignore cases
      val classNames = new ArrayList[String](elem.classNames())
      if (!containsClass(classNames.asScala.toList,ignoreCases)) {
         // Get children, check ignore on them
        for (b <- 0 until elem.children().size()) {
          val child:Element = elem.child(b)
          val childClassNames = new ArrayList[String](child.classNames())
          if (!containsClass(childClassNames.asScala.toList,ignoreCases)) {
            if(child.text != null && !child.text.equals("")) {
              val lastChar:Char = child.text.charAt(child.text.length - 1)
              if (lastChar == '.' || lastChar == '!' || lastChar == '"' || lastChar == '?') {
                strings += child.text
              }
            }
          }
        }
      }
    }

    if (strings.size > 0) {
      Some(strings)
    }
    else {
      None
    }
  }

  private def getTextFromElements(strings:ArrayBuffer[String]):String = {
    var combinedText:String = ""
    for (p:String <- strings) {
      if (!p.equals("") && p != null) {
        combinedText += (p + " ")
      }
    }
    combinedText
  }

  private def containsClass(list1:List[String],list2:List[String]):Boolean = {
    for (s:String <- list1) {
      if (list2.contains(s)) {
        true
      }
    }
    false
  }





}
