package com.reactor.base.patterns.pull

import akka.actor.ActorRef

object MasterWorkerProtocol {
  
  // Messages from Workers
  case class WorkerCreated(worker: ActorRef)
  case class WorkerRequestsWork(worker: ActorRef)
  case class WorkIsDone(worker: ActorRef)
  case class WorkFailed(worker: ActorRef)
 
  // Messages to Workers
  case class WorkToBeDone(work: Any)
  case object WorkIsReady
  case object NoWorkToBeDone
  
  // Messages between Workers and their Actors
  case class WorkComplete(msg: Any)
  case object ReadyForWork
  
  // Master Worker Stats
  case object GetStats
  
}