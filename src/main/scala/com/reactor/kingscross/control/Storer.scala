package com.reactor.kingscross.control

import akka.actor.Actor
import akka.actor.ActorLogging

abstract class Storer extends Actor with ActorLogging {

  // Required to be implemented
  def handleEvent(event: Any): Unit
  
   def receive = {
	  	case event:Any => handleEvent(event) 
   }  
}