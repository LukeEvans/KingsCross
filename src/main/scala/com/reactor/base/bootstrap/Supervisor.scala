package com.reactor.base.bootstrap

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.kernel.Bootable
import com.reactor.nlp.utilities.IPTools
import akka.actor.Props
import akka.cluster.Cluster
import com.reactor.base.patterns.listeners.Listener
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.reactor.base.patterns.listeners.Listener

class Supervisor extends Bootable {
	val ip = IPTools.getPrivateIp();

	println("IP: " + ip)
	
    val config = ConfigFactory.parseString("akka.cluster.roles = [accio-supervisor]\nakka.remote.netty.tcp.hostname=\""+ip+"\"").withFallback(ConfigFactory.load("accio"))
        
    val system = ActorSystem("Accio-0-1", config)
    
    println("Supervisor node running...")	  
    
	def startup(){
		val clusterListener = system.actorOf(Props(classOf[Listener], system),
             name = "clusterListener")
         Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
	}

	def shutdown(){
		system.shutdown()
	}
}

object Supervisor {
	def main(args:Array[String]){
		var supervisor = new Supervisor
		supervisor.startup()
	}
}