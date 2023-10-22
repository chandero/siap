package startup

import javax.inject.Inject

class Startup @Inject()(ciclo:CicloEnviarSms) {
    println("Startup fue iniciado")
    startup()

    def startup() {
        println("CicloEnviarSms siendo iniciado")
        new Thread(ciclo).start()
    }
}
