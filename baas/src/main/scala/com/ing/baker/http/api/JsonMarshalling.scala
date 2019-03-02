package com.ing.baker.http.api

import akka.http.scaladsl.marshalling.PredefinedToEntityMarshallers
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.unmarshalling.{PredefinedFromEntityUnmarshallers, Unmarshaller}
import com.ing.baker.http.KryoUtil
import com.ing.baker.runtime.core.{ProcessEvent, SensoryEventStatus}

import scala.reflect.ClassTag

trait JsonMarshalling {


  def kryoUnmarshaller[T]: Unmarshaller[HttpEntity, T] = PredefinedFromEntityUnmarshallers.byteStringUnmarshaller.map { string =>
    val byteArray: Array[Byte] = string.toArray
    KryoUtil.defaultKryoPool.fromBytes(byteArray).asInstanceOf[T]
  }

  def kryoMarhaller[T : ClassTag] = PredefinedToEntityMarshallers.ByteArrayMarshaller.compose[T] { obj =>
    KryoUtil.defaultKryoPool.toBytesWithClass(obj)
  }


  def jsonLiftUnMarshaller[T : Manifest]: Unmarshaller[HttpEntity, T] = PredefinedFromEntityUnmarshallers.stringUnmarshaller.map { string =>

    import net.liftweb.json._
    implicit val formats = DefaultFormats

    parse(string).extract[T]
  }

  implicit val eventUnmarshaller = kryoUnmarshaller[ProcessEvent]

  implicit val sensoryEventStatusMarhaller = kryoMarhaller[SensoryEventStatus]
  implicit val ingredientsMarhaller = kryoMarhaller[Map[String, Any]]
  implicit val eventMarshaller = kryoMarhaller[ProcessEvent]
  implicit val eventListMarshaller = kryoMarhaller[List[ProcessEvent]]
  implicit val stringMarshaller = kryoMarhaller[String]

  implicit val recipeUnmarshaller = jsonLiftUnMarshaller[com.ing.baker.recipe.json.Recipe]
}
