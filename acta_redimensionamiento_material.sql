select cotr1.cotr_id, cot1.cotr_consecutivo, er1.elre_descripcion, re1.even_cantidad_retirado, uiv.ucap_ipp_valor_valor from siap.cobro_orden_trabajo cot1
inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
inner join siap.reporte_evento re1 on re1.repo_id = cotr1.repo_id and re1.even_estado < 8 and re1.even_cantidad_retirado > 0
inner join siap.elemento_redimensionamiento er1 on er1.elem_id = re1.elem_id
inner join siap.ucap_ipp_valor uiv on uiv.elem_id = er1.elem_id and uiv.ucap_ipp_valor_anho = 2021
where cot1.cotr_anho = 2022 and cot1.cotr_periodo = 3 and cot1.cotr_tipo_obra = 6
order by 1,2

select cotr1.cotr_id, re1.elem_id, re1.even_cantidad_retirado, uiv.ucap_ipp_valor_valor from siap.cobro_orden_trabajo cot1
inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
inner join siap.reporte_evento re1 on re1.repo_id = cotr1.repo_id and re1.even_estado < 8 and re1.even_cantidad_retirado > 0
inner join siap.elemento_redimensionamiento er1 on er1.elem_id = re1.elem_id
inner join siap.ucap_ipp_valor uiv on uiv.elem_id = er1.elem_id and uiv.ucap_ipp_valor_anho = 2021
where cot1.cotr_id = 723
order by 1,2