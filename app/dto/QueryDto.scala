package dto

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

case class RuleDto(rule: String, operator: String, operand: String, value: JsValue)

case class QueryDto(logicalOperator: Option[String], children: Option[List[ChildrenDto]])

case class ChildrenDto(qtype: String, query: JsValue)

case class GroupDto(groupType: String, groupQuery: Option[QueryDto])

object ChildrenDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

    implicit val childWrites = new Writes[ChildrenDto] {
        def writes(child: ChildrenDto) = Json.obj(
            "qtype" -> child.qtype,
            "query" -> child.query
        )
    }

    implicit val childReads: Reads[ChildrenDto] = (
        (__ \ "type").read[String] and
        (__ \ "query").read[JsValue]
    )(ChildrenDto.apply _)    
}

object RuleDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

    implicit val ruleWrites = new Writes[RuleDto] {
        def writes(rule: RuleDto) = Json.obj(
            "rule" -> rule.rule,
            "operator" -> rule.operator,
            "operand" -> rule.operand,
            "value" -> rule.value
        )
    }

    implicit val ruleReads: Reads[RuleDto] = (
        (__ \ "rule").read[String] and
        (__ \ "operator").read[String] and
        (__ \ "operand").read[String] and
        (__ \ "value").read[JsValue]
    )(RuleDto.apply _)
}

object QueryDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

    implicit val queryWrites = new Writes[QueryDto] {
        def writes(query: QueryDto) = Json.obj(
            "logicalOperator" -> query.logicalOperator,
            "children" -> query.children,
        )
    }

    implicit val queryReads: Reads[QueryDto] = (
        (__ \ "logicalOperator").readNullable[String] and
        (__ \ "children").readNullable[List[ChildrenDto]]
    )(QueryDto.apply _)
}

object GroupDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

    implicit val groupWrites = new Writes[GroupDto] {
        def writes(group: GroupDto) = Json.obj(
            "groupType" -> group.groupType,
            "groupQuery" -> group.groupQuery,
        )
    }

    implicit val groupReads: Reads[GroupDto] = (
        (__ \ "groupType").read[String] and
        (__ \ "groupQuery").readNullable[QueryDto]
    )(GroupDto.apply _)
}