package startup

import scala.util.control.Breaks._
import javax.inject.Inject
import models.SmsRepository

class CicloEnviarSms @Inject()(service: SmsRepository) extends Runnable {
    override def run() {
        println("CicloEnviar esta siendo ejecutado")
         breakable {
            while (true) {
                service.SendSms()
                Thread.sleep(3600000)
            }
        }
    }
}