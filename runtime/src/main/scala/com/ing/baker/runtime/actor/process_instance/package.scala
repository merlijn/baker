package com.ing.baker.runtime.actor

import com.ing.baker.petrinet.api.{Id, Identifiable, Marking}
import com.ing.baker.petrinet.api.getById

package object process_instance {

  extension [P : Identifiable](marking: Marking[P]) {

    def marshall: Marking[Id] = translateMapKeys(marking, (p: P) => summon[Identifiable[P]].apply(p))
  }
  
  extension(marking: Marking[Id]) {
    
    def unmarshall[P : Identifiable](places: Set[P]) =
      translateMapKeys(marking, (id: Id) => places.getById(id, "place in petrinet"))
  }

  def translateMapKeys[K1, K2, V](map: Map[K1, V], fn: K1 => K2): Map[K2, V] = map.map { case (key, value) => fn(key) -> value }
}
