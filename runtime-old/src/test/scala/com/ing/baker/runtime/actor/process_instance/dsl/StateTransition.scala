package com.ing.baker.runtime.actor.process_instance.dsl

import cats.effect.IO

case class StateTransition[S, E](
    override val id: Long,
    override val label: String,
    override val isAutomated: Boolean,
    override val exceptionStrategy: TransitionExceptionHandler[Place],
    produceEvent: S => IO[E]) extends Transition {
}
