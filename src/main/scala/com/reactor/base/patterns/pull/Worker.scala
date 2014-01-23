package com.reactor.base.patterns.pull

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import com.reactor.base.patterns.pull.MasterWorkerProtocol._
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import akka.actor.actorRef2Scala

abstract class Worker(master: ActorRef) extends Actor with ActorLogging {
 
  var readyForWork = false
  
  // Required to be implemented
  def doWork(workSender: ActorRef, work: Any): Unit
 
  // Notify the Master that we're alive
  override def preStart() {
    master ! WorkerCreated(self)
  }
 
  def requestWork() {
	  if (readyForWork) {
	    master ! WorkerRequestsWork(self)
	  }
  }
  
  // Set timeout
  context.setReceiveTimeout(5.second)
  
  // This is the state we're in when we're working on something.
  // In this state we can deal with messages in a much more
  // reasonable manner
  def working(work: Any): Receive = {
    // Pass... we're already working
    case WorkIsReady =>
    // Pass... we're already working
    case NoWorkToBeDone =>
    // Pass... we shouldn't even get this
    case WorkToBeDone(_) =>
      log.error("Yikes. Master told me to do work, while I'm working.")
    // Our derivation has completed its task
    case WorkComplete(result) =>
      master ! WorkIsDone(self)
      requestWork()
      // We're idle now
      context.become(idle)
    case ReceiveTimeout =>
      master ! WorkFailed(self)
      requestWork()
      // We're idle now
      context.become(idle)
  }
 
  // In this state we have no work to do.  There really are only
  // two messages that make sense while we're in this state, and
  // we deal with them specially here
  def idle: Receive = {
    // Master says there's work to be done, let's ask for it
    case WorkIsReady =>
      requestWork()
    // Send the work off to the implementation
    case WorkToBeDone(work) =>
      doWork(sender, work)
      context.become(working(work))
    // We asked for it, but either someone else got it first, or
    // there's literally no work to be done
    case NoWorkToBeDone =>
    // Indicate that we're ready to start working
    case ReadyForWork =>
      readyForWork = true
      requestWork()
  }
 
  def receive = idle
}