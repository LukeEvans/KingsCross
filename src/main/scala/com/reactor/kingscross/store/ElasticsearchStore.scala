package com.reactor.kingscross.store

import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import com.reactor.kingscross.control.StorerArgs
import com.fasterxml.jackson.databind.ObjectMapper

class ElasticsearchStore(args:StorerArgs) {

  val settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
  val client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("ec2-54-234-94-194.compute-1.amazonaws.com", 9300));
  
  var index:String = null
  var itemType:String = null
  
  val mapper = new ObjectMapper()
  
  args.storeType match {
    case "News" => 
      index = "news"
      itemType = "story"
    case "Twitter" =>
      index = "twitter"
      itemType = "tweet"      
  }
  
  def index(obj:Any) {
    try {
	    val json = mapper.writeValueAsString(obj)
	    client.prepareIndex(index, itemType).setSource(json)
	    
	  } catch {
	  		case e:Exception => e.printStackTrace() 
	  }
  }
}