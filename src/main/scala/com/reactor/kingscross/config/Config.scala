package com.reactor.kingscross.config

import scala.util.Random

class Config {
  
  var emit_channel:String = "default_emit"
  var collect_channel:String = "default_collect"
  var store_channel:String = "default_store"
  var complete_channel:String = "default_complete"
    
  def this(emitChannel:String, collectChannel:String, storeChannel:String) {
	this()
	 emit_channel = "emit-" + emitChannel
	 collect_channel = "collect-" + collectChannel
	 store_channel = "store-" + storeChannel
	 complete_channel = "complete-" + emitChannel
  }
}

class PollingConfig() extends Config {
  var start_delay:Int = 1
  var poll_time:Int = 1
}

class NewsConfig() extends PollingConfig {
  
  def this(id:String, url:String, emitChannel:String, collectChannel:String, storeChannel:String, pollTime:Int) {
	 this()
	 
	 emit_channel = "emit-" + emitChannel
	 collect_channel = "collect-" + collectChannel
	 store_channel = "store-" + storeChannel
	 complete_channel = "complete-" + emitChannel
	 
	 start_delay = Random.nextInt % 5
	 poll_time = pollTime
  }
}
