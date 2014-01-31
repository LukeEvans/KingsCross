package com.reactor.kingscross.news

import com.reactor.base.utilities.Tools
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

class EntityExtractor {
  
  var alchemyAPIKey:String = "fb5e30fc9358653bee86ffd698ed024aae33325c"
  var alchemyBaseURL:String = "http://access.alchemyapi.com/calls/text/TextGetRankedNamedEntities"
  
  
  def getEntitiesFromAlchemy(text:String):Set[Entity] =  {
    var alchemyURL = alchemyBaseURL + "?apikey=" + alchemyAPIKey + "&outputMode=json" + "&sentiment=1" + "&text" + text
    var alchemyResult:JsonNode = Tools.fetchURL(alchemyURL)
    if (alchemyResult.path("status").asText().equalsIgnoreCase("ok")) {
      println("Alchemy Recieved")
      var a = 0
      if (alchemyResult.path("entities").isArray()) {
        var entities:JsonNode = alchemyResult.get("entities")
        val result:Set[Entity] = Set()
        for (a <- 0 until entities.size()) {
          var entityData = entities.get(a)
          var entity:Entity = new Entity()
          entity.entity_type = entityData.path("type").asText()
          entity.entity_name = entityData.path("text").asText()
          entity.sentiment = entityData.path("sentiment").path("type").asText
          result + entity
        }
        return result
      }
    }
    else {
      println("Bad alchemy result, status = "+alchemyResult.path("status").asText())
    }
    
    
    return null
  }
}