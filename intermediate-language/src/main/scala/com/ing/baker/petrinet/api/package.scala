package com.ing.baker.petrinet


package object api extends MultiSetOps with MarkingOps {

  /**
    * Identifier type for elements.
    */
  type Id = String

  /**
    * Type alias for something that is identifiable.
    */
  type Identifiable[T] = T => Id

  /**
    * Type alias for a multi set.
    */
  type MultiSet[T] = Map[T, Int]

  /**
    * Type alias for a marking.
    */
  type Marking[P] = Map[P, MultiSet[Any]]

  extension [T : Identifiable](e: T) {

    def getId: Id = summon[Identifiable[T]].apply(e)
  }

  extension [T : Identifiable](seq: Iterable[T]) {

    def findById(id: Id): Option[T] = seq.find(e => summon[Identifiable[T]].apply(e) == id)

    def getById(id: Id, name: String = "element"): T = findById(id).getOrElse { throw new IllegalStateException(s"No $name found with id: $id") }
  }
}

