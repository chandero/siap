SELECT 
e.aap_id as codigo_essa,
a.aap_id as codigo_isag,
a.esta_id as isag_estado,
e.aap_apoyo as essa_apoyo, 
a.aap_apoyo as isag_apoyo, 
CASE WHEN e.aap_apoyo=a.aap_apoyo THEN '-'
    ELSE '1'
END AS DIFIERE,
e.aap_direccion as essa_direccion,
a.aap_direccion as isag_direccion,
e.aap_barrio as essa_barrio,
b.barr_descripcion as isag_barrio,
e.aap_area as essa_area,
tb.tiba_descripcion as isag_area,
CASE WHEN e.tiba_id = tb.tiba_id THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aap_tecnologia as essa_tecnologia,
ad.aap_tecnologia as isag_tecnologia,
CASE WHEN e.aap_tecnologia = ad.aap_tecnologia THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aap_potencia as essa_potencia,
ad.aap_potencia as isag_potencia,
CASE WHEN e.aap_potencia = ad.aap_potencia THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aaus_id,
e.aap_uso as essa_uso,
a.aaus_id,
au.aaus_descripcion as isag_uso,
CASE WHEN (e.aaus_id IN (1,2,3) AND a.aaus_id  IN (1,2,3)) THEN '-'
	 WHEN (e.aaus_id = 3 AND a.aaus_id = 3) THEN '-'
	 WHEN (e.aaus_id = 4 AND a.aaus_id = 4) THEN '-'
	 WHEN (e.aaus_id = 6 AND a.aaus_id = 6) THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aap_cuenta as essa_cuenta,
ac.aacu_descripcion as issa_cuenta,
CASE WHEN e.aacu_id = a.aacu_id THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aap_fechatoma as essa_fecha_toma,
a.aap_fechatoma as isag_fecha_toma,
CASE WHEN e.aap_fechatoma = a.aap_fechatoma THEN '-'
     ELSE '1'
END AS DIFIERE
FROM siap.essa e 
LEFT JOIN siap.aap a ON a.aap_id = e.aap_id
LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
LEFT JOIN siap.tipobarrio tb ON tb.tiba_id = b.tiba_id
LEFT JOIN siap.aap_uso au ON au.aaus_id = a.aaus_id
LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = a.aacu_id
WHERE e.zanho = 2019 and e.zperiodo = 3
UNION
SELECT 
e.aap_id as codigo_essa,
a.aap_id as codigo_isag,
a.esta_id as isag_estado,
e.aap_apoyo as essa_apoyo, 
a.aap_apoyo as isag_apoyo, 
CASE WHEN e.aap_apoyo=a.aap_apoyo THEN '-'
    ELSE '1'
END AS DIFIERE,
e.aap_direccion as essa_direccion,
a.aap_direccion as isag_direccion,
e.aap_barrio as essa_barrio,
b.barr_descripcion as isag_barrio,
e.aap_area as essa_area,
tb.tiba_descripcion as isag_area,
CASE WHEN e.tiba_id = tb.tiba_id THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aap_tecnologia as essa_tecnologia,
ad.aap_tecnologia as isag_tecnologia,
CASE WHEN e.aap_tecnologia = ad.aap_tecnologia THEN '-'
     ELSE '1'
END,
e.aap_potencia as essa_potencia,
ad.aap_potencia as isag_potencia,
CASE WHEN e.aap_potencia = ad.aap_potencia THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aaus_id,
e.aap_uso as essa_uso,
a.aaus_id,
au.aaus_descripcion as isag_uso,
CASE WHEN (e.aaus_id IN (1,2,3) AND a.aaus_id  IN (1,2,3)) THEN '-'
	 WHEN (e.aaus_id = 4 AND a.aaus_id = 4) THEN '-'
	 WHEN (e.aaus_id = 6 AND a.aaus_id = 6) THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aap_cuenta as essa_cuenta,
ac.aacu_descripcion as issa_cuenta,
CASE WHEN e.aacu_id = a.aacu_id THEN '-'
     ELSE '1'
END AS DIFIERE,
e.aap_fechatoma as essa_fecha_toma,
a.aap_fechatoma as isag_fecha_toma,
CASE WHEN e.aap_fechatoma = a.aap_fechatoma THEN '-'
     ELSE '1'
END AS DIFIERE
FROM siap.aap a 
LEFT JOIN siap.essa e ON e.aap_id = a.aap_id AND e.zanho = 2019 AND e.zperiodo = 3
LEFT JOIN siap.aap_adicional ad ON ad.aap_id = a.aap_id
LEFT JOIN siap.barrio b ON a.barr_id = b.barr_id
LEFT JOIN siap.tipobarrio tb ON tb.tiba_id = b.tiba_id
LEFT JOIN siap.aap_uso au ON au.aaus_id = a.aaus_id
LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = a.aacu_id
WHERE e.zanho = 2019 and e.zperiodo = 3