package utilities

import java.util.ArrayList

import java.util.Calendar

import java.util.Date

import HolidayUtil._

import scala.beans.{BeanProperty, BooleanBeanProperty}

//remove if not needed
import scala.collection.JavaConversions._

object HolidayUtil {

  /**
    * Get the next available business day from a given date and days amount
    * @param date Start date to count from
    * @param days    Number of days to count
    * @return Date representation of the next business day
    */
  def getNextBusinessDay(date: Date, days: Int): Date = {
    var dias = days
    val calendar: Calendar = Calendar.getInstance
    calendar.setTime(date)
    var lobHolidayUtil: HolidayUtil = new HolidayUtil(
      calendar.get(Calendar.YEAR))
    while (dias > 0) {
      calendar.add(Calendar.DATE, 1)
      if (calendar.get(Calendar.YEAR) != lobHolidayUtil.getYear) {
        lobHolidayUtil = new HolidayUtil(calendar.get(Calendar.YEAR))
      }
      val dayOfWeek: Int = calendar.get(Calendar.DAY_OF_WEEK)
      if (dayOfWeek != 1 && dayOfWeek != 7 &&
          !lobHolidayUtil.isHoliday(calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DATE))) {
        { 
            dias -= 1 
        }
      }
    }
    calendar.getTime
  }

  /**
    * Get the amount of business days between two dates
    * @param dateInit Start date
    * @param dateEnd End date
    * @return	Amount for business days
    */
  def countBusinessDays(dateInit: Date, dateEnd: Date): Int = {
    val limitDay: Calendar = Calendar.getInstance
    var days: Int = 0
    if (dateEnd != null) {
      limitDay.setTime(dateEnd)
    }
    val startDay: Calendar = Calendar.getInstance
    startDay.setTime(dateInit)
    var lobHolidayUtil: HolidayUtil = new HolidayUtil(
      startDay.get(Calendar.YEAR))
    while (startDay.getTime.before(limitDay.getTime)) {
      startDay.add(Calendar.DATE, 1)
      if (startDay.get(Calendar.YEAR) != lobHolidayUtil.getYear) {
        lobHolidayUtil = new HolidayUtil(startDay.get(Calendar.YEAR))
      }
      val dayOfWeek: Int = startDay.get(Calendar.DAY_OF_WEEK)
      if (dayOfWeek != 1 && dayOfWeek != 7 &&
          !lobHolidayUtil.isHoliday(startDay.get(Calendar.MONTH),
                                    startDay.get(Calendar.DATE))) {
        { 
            days += 1 
        }
      }
    }
    days
  }

}

class HolidayUtil(year: Int) {

  private var holidays: ArrayList[String] = new ArrayList()

  val a: Int = year % 19

  val b: Int = year / 100

  val c: Int = year % 100

  val d: Int = b / 4

  val e: Int = b % 4

  val g: Int = (8 * b + 13) / 25

  val h: Int = (19 * a + b - d - g + 15) % 30

  val j: Int = c / 4

  val k: Int = c % 4

  val m: Int = (a + 11 * h) / 319

  val r: Int = (2 * e + 2 * j - k - h + m + 32) % 7

  private var easterMonth: Int = (h - m + r + 90) / 25

  private var easterDay: Int = (h - m + r + this.easterMonth + 19) % 32

  this.easterMonth -= 1

// Primero de Enero
  this.holidays.add("0:1")

// Dia del trabajo 1 de mayo
  this.holidays.add("4:1")

//Independencia 20 de Julio
  this.holidays.add("6:20")

//Batalla de boyaca 7 de agosto
  this.holidays.add("7:7")

//Maria inmaculada 8 de diciembre
  this.holidays.add("11:8")

//Navidad 25 de diciembre
  this.holidays.add("11:25")

// Reyes magos 6 de enero
  this.calculateEmiliani(0, 6)

//San jose 19 de marzo
  this.calculateEmiliani(2, 19)

//San pedro y san pablo 29 de junio
  this.calculateEmiliani(5, 29)

//Asuncion 15 de agosto
  this.calculateEmiliani(7, 15)

//Descubrimiento de america 12 de octubre
  this.calculateEmiliani(9, 12)

//Todos los santos 1 de noviembre
  this.calculateEmiliani(10, 1)

//Independencia de cartagena 11 de noviembre
  this.calculateEmiliani(10, 11)

//jueves santos
  this.calculateOtherHoliday(-3, false)

//viernes santo
  this.calculateOtherHoliday(-2, false)

//Asención del señor de pascua
  this.calculateOtherHoliday(40, true)

//Corpus cristi
  this.calculateOtherHoliday(60, true)

//Sagrado corazon
  this.calculateOtherHoliday(68, true)
  
  def getYear() = {
      this.year
  }
  /**
    * Move a holiday to monday by emiliani law
    * @param month Original month of the holiday
    * @param day Original day of the holiday
    */
  private def calculateEmiliani(month: Int, day: Int): Unit = {
    val date: Calendar = Calendar.getInstance
    date.set(this.year, month, day)
    val dayOfWeek: Int = date.get(Calendar.DAY_OF_WEEK)
    dayOfWeek match {
      case 1 => date.add(Calendar.DATE, 1)
      case 3 => date.add(Calendar.DATE, 6)
      case 4 => date.add(Calendar.DATE, 5)
      case 5 => date.add(Calendar.DATE, 4)
      case 6 => date.add(Calendar.DATE, 3)
      case 7 => date.add(Calendar.DATE, 2)
      case _ => //break

    }
    this.holidays.add(date.get(Calendar.MONTH) + ":" + date.get(Calendar.DATE))
  }

  /**
    * Calculate holidays according to easter day
    * @param days Number of days after (+) or before (-) of easter day
    * @param emiliani Indicates whether the emiliani law affects
    */
  private def calculateOtherHoliday(days: Int, emiliani: Boolean): Unit = {
    val date: Calendar = Calendar.getInstance
    date.set(this.year, this.easterMonth, this.easterDay)
    date.add(Calendar.DATE, days)
    if (emiliani) {
      this.calculateEmiliani(date.get(Calendar.MONTH), date.get(Calendar.DATE))
    } else {
      this.holidays
        .add(date.get(Calendar.MONTH) + ":" + date.get(Calendar.DATE))
    }
  }

  /**
    * Indicates if a day is a holiday
    * @param month Month in which the day is contained
    * @param day Day to evaluate
    * @return true if the day is a holiday, false otherwise
    */
  def isHoliday(month: Int, day: Int): Boolean =
    this.holidays.contains(month + ":" + day)

}
