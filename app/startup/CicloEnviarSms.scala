package startup

import scala.util.control.Breaks._
import play.api.Configuration

import javax.inject.Inject
import models.SmsRepository

class CicloEnviarSms @Inject()(service: SmsRepository, config: Configuration) extends Runnable {
    override def run() {
        println("CicloEnviar esta siendo ejecutado")
        val tiempo: Int = config.get[Int]("sms.ciclo") * 60000
         breakable {
            while (true) {
                service.SendSms()
                Thread.sleep(tiempo)
            }
        }
    }
}