package com.reactor.kingscross.test

import com.reactor.base.patterns.pull.FlowControlActor
import com.reactor.base.patterns.pull.FlowControlArgs

case class TestArgs(val spaceshipSize:Int) extends FlowControlArgs

class TestActor(args:TestArgs) extends FlowControlActor(args) {

  ready()
  
  def receive = {
    case s:String => 
      println("Got a freaking query about my ship: " + s)
      println("I will respond with: " + args.spaceshipSize)
      complete()
  }
}