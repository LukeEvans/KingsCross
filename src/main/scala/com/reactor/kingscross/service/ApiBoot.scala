package com.reactor.kingscross.service

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.reactor.nlp.utilities.IPTools
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster
import akka.kernel.Bootable
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.reactor.base.patterns.listeners.Listener
import akka.actor.actorRef2Scala
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.reactor.base.patterns.listeners.Listener
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.reactor.base.patterns.listeners.Listener
import akka.actor.ActorRef
import akka.cluster.ClusterEvent.ClusterDomainEvent
import com.reactor.base.patterns.listeners.Listener


//class ApiBoot(args: Array[String]) extends Bootable {
class ApiBoot extends Bootable {

	val ip = IPTools.getPrivateIp();
      
	println("IP: " + ip)
	
	val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=2552") 
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [accio-frontend]\nakka.remote.netty.tcp.hostname=\""+ip+"\"")).withFallback(ConfigFactory.load("accio"))
      
    implicit val system = ActorSystem("Accio-0-1", config)
        
    //#registerOnUp
    Cluster(system) registerOnMemberUp {
	  
	      // Easy role change for debugging
          val worker_role = "accio-frontend"
          val supervisor_role = "accio-frontend"
          val default_parallelization = 1
		    
		  // Splitting master
//		  val splitMaster = system.actorOf(Props(classOf[SplitMaster], default_parallelization, worker_role).withRouter(ClusterRouterConfig(RoundRobinRouter(), 
//			ClusterRouterSettings(
//			totalInstances = 100, maxInstancesPerNode = 1,
//			allowLocalRoutees = true, useRole = Some(supervisor_role)))),
//			name = "splitMaster")

			
		// Actor actually handling the requests
//   		val service = system.actorOf(Props(classOf[ApiActor], reductoMaster).withRouter(	
//    	  ClusterRouterConfig(AdaptiveLoadBalancingRouter(akka.cluster.routing.MixMetricsSelector), 
//    	  ClusterRouterSettings(
//    	  totalInstances = 100, maxInstancesPerNode = 1,
//    	  allowLocalRoutees = true, useRole = Some("reducto-frontend")))),
//    	  name = "serviceRouter")
    		 
       val service:ActorRef = null;
       IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = 8080)
    }
  

     def startup(){
         val clusterListener = system.actorOf(Props(classOf[Listener], system),
             name = "clusterListener")
         Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
    }

	def shutdown(){
		system.shutdown()
	}
}

object ApiApp {
   def main(args: Array[String]) = {
     val api = new ApiBoot
     api.startup
   }
}
