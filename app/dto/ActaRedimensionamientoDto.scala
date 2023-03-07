package dto

case class ActaRedimensionamientoDto(
    numero: String,
    periodo: String,
    fecha_corte: String,
    fecha_corte_anterior: String,
    periodo_corte: String,
    fecha_firma: String,
    valor: String,
    valor_acumulado: String,
    valor_acumulado_anterior: String,
    subtotal_expansion: String,
    subtotal_modernizacion: String,
    subtotal_desmonte: String,
    subtotal_total: String,
    total: String
    // , tableData: List[(String, String, String, String, String)]
)