package com.reactor.kingscross.news

import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class EntityExtractor {
  
  var alchemyAPIKey:String = "fb5e30fc9358653bee86ffd698ed024aae33325c"
  var alchemyBaseURL:String = "http://access.alchemyapi.com/calls/text/TextGetRankedNamedEntities"
  
  
  def getEntitiesFromAlchemy(text:String):Set[Entity] =  {    // TODO return option
    
    if (text == null || text.equals("")) {
      println("ERROR, no text to send to Alchemy")
      return Set()
    }

    val alchemyURL = alchemyBaseURL + "?apikey=" + alchemyAPIKey + "&outputMode=json" + "&sentiment=1"
    val alchemyResult:JsonNode = Tools.fetchAlchemyURL(alchemyURL, text)
    
    if(alchemyResult == null) {
      println("Bad Alchemy Result")
      return Set()
    }

    //println("\nAlchemy Received:\n"+alchemyResult.toString+"\n")
    
    
    if (alchemyResult.path("status").asText().equalsIgnoreCase("ok")) {
      if (alchemyResult.path("entities").isArray()) {
        val entities:JsonNode = alchemyResult.get("entities")
        var result:Set[Entity] = Set()
        for (a <- 0 until entities.size()) {
          val entityData = entities.get(a)
          val entity:Entity = new Entity()
          entity.entity_type = entityData.path("type").asText()
          entity.entity_name = entityData.path("text").asText()
          entity.sentiment = entityData.path("sentiment").path("type").asText
          result += entity
        }
        return result
      }
    }
    else {
      println("Bad alchemy result, status = "+alchemyResult.path("status").asText())
      val mapper:ObjectMapper = new ObjectMapper()
      val error:String = mapper.writeValueAsString(alchemyResult)
      println(error)
    }
    null
  }
}