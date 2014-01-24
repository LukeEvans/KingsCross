package com.reactor.kingscross.config

class Config {
  var read_channel:String = "default_in"
  var write_channel:String = "default_out"
}

class NewsConfig() extends Config {
  def this(url:String, poll_time:Int, rc:String, wc:String) {
	 this()
	 
	 read_channel = rc
	 write_channel = wc
  }
}
