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

class FrontendDaemon(args:Array[String]) extends Bootable {
	val ip = IPTools.getPrivateIp();

	val config =
      (if (args.nonEmpty) ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}") else ConfigFactory.empty)
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [accio-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("accio"))
      
    val system = ActorSystem("Accio-0-1", config)
    
	def startup(){
		val clusterListener = system.actorOf(Props(classOf[Listener], system),
             name = "clusterListener")
         Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
	}

	def shutdown(){
		system.shutdown()
	}
}

object FrontendDaemon {
	def main(args:Array[String]){
		var bootstrap = new FrontendDaemon(args)
		bootstrap.startup()
		println("Frontend node running...")
	}
}