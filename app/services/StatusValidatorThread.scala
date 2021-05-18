package services

object StatusValidatorThread {
  implicit private val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global    
  private var notprocessing = true
  
  def ciclo() = {
    // loop infinito de actualización de estado mientras llega el fin
    if (notprocessing) {
      println("Iniciando Thread Ciclo Proceso")
      val thread = new Thread {
            override def run {
              while (true) {
                Thread.sleep(21600000)
              }
            }
      }
      notprocessing = false
      thread.start()
    }
  }
}