package com.reactor.base.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import akka.actor.Props
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.reactor.base.patterns.listeners.Listener
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.reactor.base.patterns.listeners.Listener
import com.reactor.kingscross.bootstrap.NewsBootstrap

class KingsCross extends Bootable {
	val ip = IPTools.getPrivateIp();
	val config = ConfigFactory.empty.withFallback(ConfigFactory.parseString("akka.cluster.roles = [kc-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"\nakka.remote.netty.tcp.port=2551")).withFallback(ConfigFactory.load("kingscross"))
    val system = ActorSystem("KingsCross-01", config)
    
    // Startup 
	def startup(){
	  
		 // Define cluster listener
		 val clusterListener = system.actorOf(Props(classOf[Listener], system), name = "clusterListener") 
		 Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
		 
		 // Bootstrap news
		 val news = system.actorOf(Props[NewsBootstrap])
	}

	def shutdown(){
		system.shutdown()
	}
}

object KingsCross {
	def main(args:Array[String]){
		var kingscross = new KingsCross
		kingscross.startup()
		
		println("Kings Cross node running...")
	}
}