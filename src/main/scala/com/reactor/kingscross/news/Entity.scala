package com.reactor.kingscross.news

class Entity {
  
  var entity_name:String = null
  var entity_type:String = null
  var sentiment:String = null
  
  def this(name:String)  {
    this()
    entity_name = name
    entity_type = ""
    sentiment = ""
  }
  
  
}