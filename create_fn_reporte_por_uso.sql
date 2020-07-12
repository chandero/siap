CREATE OR REPLACE FUNCTION siap.fn_reporte_por_sector(IN fecha_inicial timestamp, IN fecha_final timestamp, IN vempr_id INTEGER)
RETURNS TABLE(tiba_descripcion character varying, cantidad integer) AS
$$
DECLARE
 var_reporte record;
 var_sector record;
 var_total integer;
BEGIN
  var_total := 0;
  SELECT COUNT(*) AS total FROM siap.reporte r WHERE 
        r.repo_fecharecepcion BETWEEN fecha_inicial and fecha_final and r.reti_id = 1 and r.empr_id = vempr_id
        INTO var_reporte;
  FOR var_sector IN (SELECT o.tiba_descripcion, COUNT(o.repo_id) as CANTIDAD FROM
			(SELECT DISTINCT u.tiba_descripcion, r.repo_id FROM siap.reporte r
			 LEFT JOIN siap.barrio b ON b.barr_id = r.barr_id
			 LEFT JOIN siap.tipobarrio u ON u.tiba_id = b.tiba_id
			 WHERE r.repo_fecharecepcion BETWEEN fecha_inicial and fecha_final and r.reti_id = 1 and r.empr_id = vempr_id ) o
			 GROUP BY o.tiba_descripcion
			 ORDER BY o.tiba_descripcion)
   LOOP
      tiba_descripcion := var_sector.tiba_descripcion::text;
      cantidad := var_sector.cantidad::integer;
      var_total := var_total + var_sector.cantidad::integer;
      return next;
   END LOOP;
   IF (var_total <> var_reporte.total) THEN
      tiba_descripcion := 'OTRO';
      cantidad := var_reporte.total - var_total;
      return next;
   END IF;
END; $$

LANGUAGE 'plpgsql';