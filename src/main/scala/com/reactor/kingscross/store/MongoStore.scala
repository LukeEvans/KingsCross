package com.reactor.kingscross.store

import com.reactor.kingscross.control.StorerArgs
import com.reactor.kingscross.control.Storer
import com.mongodb.casbah.MongoURI
import com.mongodb.casbah.Imports._
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.util.JSON

abstract class MongoStore(args:StorerArgs) extends Storer(args) {
  val uri = MongoURI("mongodb://levans002:dakota1@ds031887.mongolab.com:31887/winston-db")
  val db = uri.connectDB
  
  var collection:String = null

  args.storeType match {
    case "News" => collection = "reactor-news"
    case "News-Dev" => collection = "reactor-news-dev"
    case "Twitter" => collection = "reactor-tweets"
  }
  
  val mapper = new ObjectMapper()
  
  def insert(obj:Any) {
	  val coll = db.right.get.getCollection(collection)
	  
	  try {
	    val json = mapper.writeValueAsString(obj)
	    val dbobj = JSON.parse(json).asInstanceOf[DBObject]
	    coll.insert(dbobj)
	    
	  } catch {
	  		case e:Exception => e.printStackTrace() 
	  }
  }
}