package com.ing.baker.types

import java.nio.charset.StandardCharsets
import java.util

import scala.util.Random

object Value {

  def generate(t: Type): Value = t match {

    case CharArray => PrimitiveValue("")
    case Int32     => PrimitiveValue(Random.nextInt(Int.MaxValue))
    case Int64     => PrimitiveValue(Random.nextInt(Int.MaxValue).toLong)
    case Bool      => PrimitiveValue(Random.nextBoolean())
    case OptionType(entryType) =>
      if (Random.nextBoolean())
        generate(entryType)
      else
        NullValue
    case ListType(entryType) =>
      val n = Random.nextInt(7) + 1
      val entries = (0 to n).map(_ => generate(entryType))
      ListValue(entries.toList)
    case RecordType(fields) =>
      val entries = fields.map {
        case RecordField(name, fieldType) => name -> generate(fieldType)
      }.toMap
      RecordValue(entries)
    case _ => throw new IllegalStateException("Unsupported type")
  }
}

sealed trait Value extends Serializable {

  def isNull: Boolean = this == NullValue

  def isInstanceOf(t: Type): Boolean = (t, this) match {
    case (_, NullValue)                                      => true
    case (expected: PrimitiveType, PrimitiveValue(value))    => primitiveMappings.get(value.getClass) match {
      case None => false
      case Some(actual) => expected.isAssignableFrom(actual)
    }

    case (ListType(entryType), ListValue(entries))           => entries.forall(_.isInstanceOf(entryType))
    case (OptionType(entryType), v: Value)                   => v.isInstanceOf(entryType)
    case (EnumType(options), PrimitiveValue(value: String))  => options.contains(value)
    case (RecordType(entryTypes), RecordValue(entryValues))  => entryTypes.forall { f =>
      entryValues.get(f.name) match {
        case None        => false
        case Some(value) => value.isInstanceOf(f.`type`)
      }
    }
    case (MapType(valueType), RecordValue(entries))         => entries.values.forall(_.isInstanceOf(valueType))
    case _ => false
  }

  def validate(t: Type): Option[String] =  (t, this) match {

    case (_, NullValue)                  => None

    case (expected: PrimitiveType, PrimitiveValue(value)) => primitiveMappings.get(value.getClass) match {
      case Some(actual) if expected.isAssignableFrom(actual) => None
      case _ => Some(s"$value is not an instance of $expected")
    }
    case (ListType(entryType), ListValue(entries))          =>
      entries.flatMap(_.validate(entryType)).headOption
    case (OptionType(entryType), v: Value)                  =>
      v.validate(entryType)
    case (EnumType(options), PrimitiveValue(value: String)) =>
      if (!options.contains(value))
        Some(s"$value is not a valid option, expected one of: $options")
      else
        None
    case (RecordType(entryTypes), RecordValue(entryValues)) => entryTypes.flatMap { f =>
      entryValues.get(f.name) match {
        case None        => Some(s"Missing field: ${f.name}")
        case Some(value) => value.validate(f.`type`).map(reason => s"Invalid field: ${f.name}: $reason\n")
      }
    }.headOption
    case (MapType(valueType), RecordValue(entries))         =>
      entries.values.flatMap(_.validate(valueType)).headOption
    case (otherType, otherValue) => Some(s"${otherValue.getClass.getSimpleName} is not an instance of ${otherType.getClass.getSimpleName}")
  }
}

/**
  * Indicates the absence of a value.
  *
  * null, void, none, empty, etc...
  *
  */
case object NullValue extends Value {

  override def toString: String = "null"
}

// should inherit AnyVal
case class PrimitiveValue(value: Any) extends Value {
  if (!isPrimitiveValue(value))
    throw new IllegalArgumentException(s"value of type ${value.getClass} is not supported as a primitive value")

  def isAssignableTo(clazz: Class[_]): Boolean =
    (supportedPrimitiveClasses.contains(clazz) && clazz.isInstance(value)) ||
      (clazz.isPrimitive && javaPrimitiveMappings.get(value.getClass).contains(clazz))

  override def toString: String = value match {
    case str: String                         => "\"" + str + "\""
    case n: java.math.BigDecimal             => "\"" + n.toString + "\""
    case n: java.math.BigInteger             => "\"" + n.toString + "\""
    case n: BigDecimal                       => "\"" + n.toString + "\""
    case n: BigInt                           => "\"" + n.toString + "\""
    case bytes: Array[_]                     =>
      val str = new String(bytes.asInstanceOf[Array[Byte]], StandardCharsets.UTF_8)
      "\"" + str + "\""

    case other => other.toString
  }

  override def equals(obj: Any): Boolean = {
    obj match {
      case null => false
      case PrimitiveValue(bytes: Array[_]) if value.getClass.isArray =>
        util.Arrays.equals(bytes.asInstanceOf[Array[Byte]], value.asInstanceOf[Array[Byte]])
      case PrimitiveValue(otherValue) => otherValue.equals(value)
      case _ => false
    }
  }

  override def hashCode(): Int = {
    // Picking here a prime number multiplier other than 31 (used by the standard hashCode implementations
    // This reduces the chance that this holds true: "test".hashCode() == PrimitiveValue("test").hashCode()

    if (value.getClass.isArray) {
      util.Arrays.hashCode(value.asInstanceOf[Array[Byte]]) * 101
    } else {
      value.hashCode() * 101
    }
  }

}

case class RecordValue(entries: Map[String, Value]) extends Value {

  override def toString: String = entries.map { case (name, value) => "\"" + name + "\" : " + value  }.mkString("{", ",", "}")
}

case class ListValue(entries: List[Value]) extends Value {

  override def toString: String = entries.mkString("[", ",", "]")
}
