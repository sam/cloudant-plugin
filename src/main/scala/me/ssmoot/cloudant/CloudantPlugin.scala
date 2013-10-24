package me.ssmoot.cloudant

import sbt.Plugin
import scala.io.Source.fromInputStream
import dispatch._
import dispatch.Defaults._
import spray.json._
import spray.json.DefaultJsonProtocol._
import com.ning.http.client
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.annotation.tailrec
import scala.util.{Failure, Try, Success}

object CloudantPlugin extends App {

  object as {
    object String extends (client.Response => String) {
      def apply(r: client.Response) = r.getResponseBody
    }

    object JsObject extends (client.Response => JsObject) {
      def apply(r: client.Response) = r.getResponseBody.asJson.asJsObject
    }

    object seqOf_JsObject extends (client.Response => Seq[JsObject]) {
      def apply(r: client.Response) = r.getResponseBody.asJson.convertTo[Seq[JsObject]]
    }
  }
  // read config data from a file with 4 lines: hostname, user name, password, and database name
  val List(hostname, user,pass,db) = fromInputStream(getClass.getResourceAsStream("/config")).getLines.toList

  // base request builder for all requests containing user name and database name
  def baseRequest = host(hostname).secure.as_!(user, pass) / db

  def allDocs(startKey: Option[String], limit: Int): Future[Seq[(String,String)]] = {

    val queryString = Map("limit" -> Some(limit), "startKey" -> startKey) collect {
      case (key, Some(value)) => key -> value.toString
    }

    val allDocsRequest = baseRequest / "_all_docs" <<? queryString

    Http(allDocsRequest OK as.JsObject) map { response =>
      response.fields("rows").convertTo[Seq[JsObject]].map { row =>
        row.getFields("key", "value") match {
          case Seq(key, value) => {
              key.convertTo[String] ->
              value.asJsObject.fields("rev").convertTo[String]
          }
        }
      }
    }
  }

  def bulkDelete(docs: Seq[(String,String)]): Future[Seq[JsObject]] = {

    val json = JsObject(
      "docs" -> JsArray(
        docs.map {
          case (id, rev) => JsObject(
            "_id" -> JsString(id),
            "_rev" -> JsString(rev),
            "_deleted" -> JsBoolean(true))
        }:_*
      )
    )

    val request = baseRequest.POST / "_bulk_docs" <:< Map("Content-Type" -> "application/json") << json.compactPrint

    Http(request OK as.seqOf_JsObject)
  }

  def truncate:Int = {

    @tailrec
    def truncate(limit: Int, acc: Int): Int = {
      println(s"$acc DELETED...")

      val revisions = Await.result(allDocs(None, limit), 30 seconds)

      if(revisions.isEmpty) {
        acc
      } else {
        val docs = Await.result(bulkDelete(revisions), 30 seconds)
        truncate(limit, acc + docs.size)
      }
    }

    truncate(1000, 0)
  }

  println(s"TOTAL ROWS DELETED: ${truncate}")
}
