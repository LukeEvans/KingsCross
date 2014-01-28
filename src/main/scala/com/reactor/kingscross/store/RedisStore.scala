package com.reactor.kingscross.store

import com.reactor.kingscross.control.StorerArgs
import com.reactor.kingscross.control.Storer
import redis.clients.jedis.Jedis

abstract class RedisStore(args:StorerArgs) extends Storer(args) {
  
  val jedis = new Jedis("reducto-words.1hm814.0001.use1.cache.amazonaws.com");
  
  def incrementWords(text:String) {
    text.split("\\s+").toList map { word =>
      if (!word.equalsIgnoreCase("")) {
        jedis.incr(word.toLowerCase())
      }
    }
  }
}