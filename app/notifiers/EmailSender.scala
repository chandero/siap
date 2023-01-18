package notifiers

import notifiers._
import views._

class EmailSender {
    def send(usua_email: String, nombre: String, enlace: String) {
      val mail = new MailloyContext with Mailloy
      mail.setSubject("Recuperación de Contraseña")
      mail.addRecipient(usua_email)
      mail.addFrom("SIAP <siapisag@gmail.com>")
      mail.send(views.html.recovery(nombre, enlace).body,views.html.recovery(nombre, enlace).body )
    }
}