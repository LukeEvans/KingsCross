package com.reactor.base.patterns.listeners

import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.actor.ActorSystem
import com.reactor.base.patterns.monitoring.MonitoredActor

class Listener(originSystem:ActorSystem) extends MonitoredActor("cluster-listener") {
  var memberCount = 0
  def receive = {
    case state: CurrentClusterState =>{
      log.info("Current members: {}:", state.members.mkString(", ")) 
      memberCount = state.members.size
    }
    case MemberUp(member) =>{
      log.info("Member is Up: {}", member.address)
      memberCount += 1 
    }
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>{
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
      memberCount -= 1
      if(memberCount < 2){
        log.error("Only member left up, shutting down...")
        originSystem.shutdown
        System.exit(-1)
      }
    }
    case domainEvent: ClusterDomainEvent => 
      //log.info("Domain Event: {}", domainEvent.toString()) //possible debugging info 
  }
}