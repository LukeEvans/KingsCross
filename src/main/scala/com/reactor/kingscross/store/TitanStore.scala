package com.reactor.kingscross.store

import com.reactor.kingscross.control.Storer
import com.reactor.kingscross.control.StorerArgs
import com.tinkerpop.rexster.client.RexsterClient
import com.tinkerpop.rexster.client.RexsterClientFactory
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.HashMap
import java.util.List

abstract class TitanStore(args:StorerArgs) extends Storer(args) {
	
  val rexster:RexsterClient = RexsterClientFactory.open("ec2-54-221-10-7.compute-1.amazonaws.com");
	
  val mapper = new ObjectMapper()
  
  var indexCommand:String = null
  
  args.storeType match {
    case "News" => indexCommand = "g = rexster.getGraph('reactorgraph'); i = new NewsIndexer(g); i.index(json)"
    case "Twitter" => indexCommand = "g = rexster.getGraph('reactorgraph'); i = new TwitterIndexer(g); i.index(json)"
  }
  
  def index(obj:Any) {
	  try {
	    val jsonString = mapper.writeValueAsString(obj)
	    val map = new HashMap[String, Object]{{put("json",jsonString)}}
	    val results:List[String] = rexster.execute(indexCommand, map)

	  } catch {
	  		case e:Exception => e.printStackTrace() 
	  }    
  }
}