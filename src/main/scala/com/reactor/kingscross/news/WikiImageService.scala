package com.reactor.kingscross.news

import scala.collection.mutable.ArrayBuffer
import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.{ObjectMapper, JsonNode}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.HttpResponse
import java.io.{InputStreamReader, BufferedReader}


/**
 * Created with IntelliJ IDEA.
 * User: Steve
 * Date: 2/11/14
 * Time: 1:33 PM
 * Company: Reactor Inc.
 */

class WikiImageEntity {
  var name:String = ""
  var weight:Int = 0

  def this(initName:String,initWeight:Int) {
    this()
    name = initName
    weight = initWeight
  }
}

class WikiImageService {

  def getTopNImagesFromAbstract(abstractText:String, n:Int):Option[Set[String]] = {

    var imageLinks:Set[String] = Set()

    getEntitiesFromAbstract(abstractText) match {
      case None => None
      case Some(result:ArrayBuffer[WikiImageEntity]) =>
        val entities = sortEntities(result)
        for (a <- 0 until n) {
          if (a < entities.size) {
            var name:String = entities(a).name
            name = name.substring(0,1).toUpperCase + name.substring(1)
            name = name.replaceAll(" +", "_")
            name = name.replaceAll("^\"|\"$", "")
            getLargestImageFromQuery(name) match {
              case Some(s:String) => imageLinks += s
              case None =>  println("\nNo Wiki Images found for "+name)
            }
          }
        }
        if(imageLinks.size > 0) {
          Some(imageLinks)
        }
        else
        {
          None
        }
    }
  }

  def getEntitiesFromAbstract(text:String):Option[ArrayBuffer[WikiImageEntity]] = {

    var entities:ArrayBuffer[WikiImageEntity] = ArrayBuffer()
    val url:String =  "http://nlp-development.elasticbeanstalk.com/NER"
    val postBody:String = "{\"text\":\""+text+"\"}"

    Tools.postJSON(url,postBody) match {
      case None =>
        None
      case Some(n:JsonNode) =>
        for (a <- 0 until n.path("namedEntities").size) {
          val entity = new WikiImageEntity()
          entity.name = n.path("namedEntities").get(a).get("title").toString
          entity.weight = n.path("namedEntities").get(a).get("weight").toString.toInt
          entities += entity
        }
    }
    if (entities.size > 0) {
      Some(entities)
    }
    else {
      None
    }
  }

  def sortEntities(entities:ArrayBuffer[WikiImageEntity]):ArrayBuffer[WikiImageEntity] = {
    for (i <- 0 until entities.size) {
      for (j <- 1 until entities.size) {
        if (entities(j-1).weight < entities(j).weight) {
          //  Swap Array Entries
          val temp:WikiImageEntity = new WikiImageEntity(entities(j).name,entities(j).weight)
          entities(j).weight = entities(j-1).weight
          entities(j).name = entities(j-1).name
          entities(j-1).weight = temp.weight
          entities(j-1).name = temp.name
        }
      }
    }
    entities
  }



  def getLargestImageFromQuery(entityName:String):Option[String] = {

    val url:String = "http://en.wikipedia.org/w/api.php?format=json&action=query&generator=images&prop=imageinfo&iiprop=url%7Csize&gimlimit=50&titles="+entityName
    Tools.fetchURL(url) match {
      case None => None
      case Some(n:JsonNode) =>
        //  Get image information from each image in the object and add it to the list
        var tempSize:Int = 0
        var largestImage:String = ""
        for (a <- 0 until n.path("query").path("pages").path("imageinfo").size) {
          val imageInfo:JsonNode = n.path("query").path("pages").path("imageinfo").get(a)
          var imageUrl:String = imageInfo.get("url").toString
          imageUrl = imageUrl.replaceAll("^\"|\"$", "")

          val size:Int = Integer.parseInt(imageInfo.findValue("size").toString)
          if (size > tempSize && size < 1000000) {
            largestImage = imageUrl
            tempSize = size
          }
        }
        if (!largestImage.equals("")) {
          Some(largestImage)
        }
        else {
          None
        }
    }
  }
}
