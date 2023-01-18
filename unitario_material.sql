select 
	u1.unit_id as ucap, 
	u1.unit_codigo as ucap_codigo,
    case u1.unit_codigo 
      when '1.01' then
          'SUMINISTRO E INSTALACION DE LUMINARIA'
      when '1.02' then
          CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	  when '1.03' then
          CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	  when '1.08' then
          CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	  when '1.09' then
          CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	  when '1.10' then
          CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	  when '1.11' then
          CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)
	  when '1.12' then
          CONCAT('SUMINISTRO E INSTALACION DE ', u1.unit_descripcion)          
      when '1.04' then  u1.unit_descripcion
      when '1.05' then CONCAT('SUMINISTRO DE ', u1.unit_descripcion)
      when '1.06' then u1.unit_descripcion
      when '1.07' then CONCAT('INSTALACION ', u1.unit_descripcion)
      else u1.unit_descripcion
    end as ucap_descripcion, 
	e1.elem_id, 
	e1.elem_codigo as material_codigo, 
	e1.elem_descripcion as material_nombre,
	(select ep1.elpr_precio from siap.elemento_precio ep1 where ep1.elem_id = e1.elem_id and ep1.elpr_anho = 2021 order by ep1.elpr_fecha desc limit 1) as material_precio
from siap.unitario u1 
left join siap.elemento_unitario eu1 on eu1.unit_id = u1.unit_id 
left join siap.elemento e1 on e1.elem_id = eu1.elem_id 
order by u1.unit_id, e1.elem_descripcion 