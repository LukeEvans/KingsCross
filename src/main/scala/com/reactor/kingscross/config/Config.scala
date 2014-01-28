package com.reactor.kingscross.config

import scala.util.Random

class Config {
  
  var emit_platform:String = "default_emit"
  var collect_platform:String = "default_collect"
  var store_platform:String = "default_store"
  var complete_platform:String = "default_complete"
    
  def this(emitPlatform:String, collectPlatform:String, storePlatform:String) {
	this()
	 emit_platform = "emit-" + emitPlatform
	 collect_platform = "collect-" + collectPlatform
	 store_platform = "store-" + storePlatform
	 complete_platform = "complete-" + emitPlatform
  }
}

class PollingConfig() extends Config {
  var start_delay:Int = 1
  var poll_time:Int = 1
}

class NewsConfig() extends PollingConfig {
  
  def this(id:String, url:String, emitPlatform:String="/news", collectPlatform:String = "/news", storePlatform:String = "/news", pollTime:Int) {
	 this()
	 
	 emit_platform = "emit-" + emitPlatform
	 collect_platform = "collect-" + collectPlatform
	 store_platform = "store-" + storePlatform
	 complete_platform = "complete-" + emitPlatform
	 
	 start_delay = Random.nextInt % 5
	 poll_time = pollTime
  }
}
