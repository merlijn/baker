package com.ing.baker.petrinet.api

/**
  * Type alias for a multi set.
  */
type MultiSet[T] = Map[T, Int]


object MultiSet {

  /**
   * The empty multi set.
   */
  def empty[T]: MultiSet[T] = Map.empty[T, Int]

  /**
   * Copies a the given elements into a multi set.
   *
   * Equal elements in the sequence will increase the multiplicity of that element in multi set.
   */
  def copyOff[T](elements: Iterable[T]): MultiSet[T] = elements.foldLeft(empty[T]) { case (mset, e) => mset.multisetIncrement(e, 1) }

  /**
    * Creates a multiset of the provided elements.
    *
    * Equal elements in the arguments will increase the multiplicity of that element in multi set.
    */
  def apply[T](elements: T*): MultiSet[T] = copyOff[T](elements)
}

extension [T](mset: MultiSet[T]) {
  def multisetDifference(other: MultiSet[T]): MultiSet[T] =
    other.foldLeft(mset) {
      case (result, (p, count)) => result.get(p) match {
        case None                  => result
        case Some(n) if n <= count => result - p
        case Some(n)               => result + (p -> (n - count))
      }
    }

  def multisetSum(other: MultiSet[T]): MultiSet[T] =
    other.foldLeft(mset) {
      case (m, (p, count)) => m.get(p) match {
        case None    => m + (p -> count)
        case Some(n) => m + (p -> (n + count))
      }
    }

  /**
    * This checks that the given (other) multiset is a sub set of this one.
    *
    * @param other
    * @return
    */
  def isSubSet(other: MultiSet[T]): Boolean =
    !other.exists {
      case (element, count) => mset.get(element) match {
        case None                 => true
        case Some(n) if n < count => true
        case _                    => false
      }
    }

  def multisetSize: Int = mset.values.sum

  def setMultiplicity(map: Map[T, Int])(element: T, m: Int) = map + (element -> m)

  def allElements: Iterable[T] = mset.foldLeft(List.empty[T]) {
    case (list, (e, count)) => List.fill[T](count)(e) ::: list
  }

  def multisetDecrement(element: T, count: Int): MultiSet[T] =
    mset.get(element) match {
      case None                  => mset
      case Some(n) if n <= count => mset - element
      case Some(n)               => mset + (element -> (n - count))
    }

  def multisetIncrement(element: T, count: Int): MultiSet[T] = mset + (element -> (count + mset.getOrElse(element, 0)))

  def multisetIntersects(other: MultiSet[T]): Boolean = {
    mset.exists { case (p, n) => other.getOrElse(p, 0) > 0 }
  }
}
