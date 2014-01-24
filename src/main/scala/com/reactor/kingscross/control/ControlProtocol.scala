package com.reactor.kingscross.control

import com.fasterxml.jackson.databind.JsonNode

trait ControlEvent

case object FetchEvent extends ControlEvent
case class EmitEvent(data:JsonNode) extends ControlEvent
case class CollectEvent(data:JsonNode) extends ControlEvent
case class StoreEvent(data:JsonNode) extends ControlEvent
case object CompleteEvent extends ControlEvent
