UPDATE siap.essa SET tiba_id = 1 WHERE aap_area = 'Urbano' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET tiba_id = 2 WHERE aap_area = 'Rural' and zanho = 2019 and zperiodo = 3;

UPDATE siap.essa SET aaus_id = 1 WHERE aap_uso LIKE '%Calle%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aaus_id = 3 WHERE aap_uso LIKE '%Avenida%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aaus_id = 4 WHERE aap_uso LIKE '%Escenarios%' or aap_uso LIKE '%Polideport%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aaus_id = 6 WHERE aap_uso LIKE '%Parque%' and zanho = 2019 and zperiodo = 3;

UPDATE siap.essa SET aaco_id = 1 WHERE aap_medida = 'Bajo contador' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aaco_id = 2 WHERE aap_medida <> 'Bajo contador' and zanho = 2019 and zperiodo = 3;

UPDATE siap.essa SET aap_tecnologia = 'LED' WHERE aap_tipo LIKE '%Led%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aap_tecnologia = 'METAL HALIDE' WHERE aap_tipo LIKE '%Metal%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aap_tecnologia = 'SODIO' WHERE aap_tipo LIKE '%Sodio%' and zanho = 2019 and zperiodo = 3;

UPDATE siap.essa set aap_potencia = NULLIF(regexp_replace(aap_tipo, '\D','','g'), '')::numeric WHERE zanho = 2019 and zperiodo = 3;

UPDATE siap.essa SET aacu_id = 2 WHERE aap_cuenta LIKE '%Urbano%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aacu_id = 4 WHERE aap_cuenta LIKE '%Parque%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aacu_id = 3 WHERE aap_cuenta LIKE '%Polideporti%' and zanho = 2019 and zperiodo = 3;
UPDATE siap.essa SET aacu_id = 1 WHERE aap_cuenta LIKE '%Avenid%' and zanho = 2019 and zperiodo = 3;