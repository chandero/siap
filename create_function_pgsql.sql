CREATE OR REPLACE FUNCTION siap.fn_potencia_tecnologia ()
 RETURNS TABLE (
 aap_potencia VARCHAR(50),
 aap_tecnologia VARCHAR(250),
 cantidad INT
) 
AS $$
DECLARE
  var_potencia record;
  var_tecno record;
  var_cantidad integer;
BEGIN
  FOR var_potencia IN (SELECT DISTINCT ad.aap_potencia FROM siap.aap_adicional ad
                       WHERE ad.aap_id <> 9999999
                       ORDER BY ad.aap_potencia
                      ) 
  LOOP
    FOR var_tecno IN (SELECT DISTINCT ad.aap_tecnologia FROM siap.aap_adicional ad
                      WHERE ad.aap_id <> 9999999
                     )
    LOOP
      SELECT COUNT(*) FROM siap.aap a INNER JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
                      WHERE ad.aap_potencia = var_potencia.aap_potencia and ad.aap_tecnologia = var_tecno.aap_tecnologia INTO var_cantidad;
                      aap_potencia := var_potencia.aap_potencia::text;
                      aap_tecnologia := var_tecno.aap_tecnologia;
                      cantidad := var_cantidad;
                      RETURN NEXT;
 END LOOP;
END LOOP;
END; $$

LANGUAGE 'plpgsql';