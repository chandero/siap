package models

import enumeratum.{ Enum, EnumEntry }

object ProcessingStatus {

  sealed trait Status extends EnumEntry

  object Status extends Enum[Status] {

    val values = findValues

    case object Preparing extends Status

    case object Parsing extends Status

    case object Optimising extends Status

    case object Done extends Status

    case object Alive extends Status

  }

}
