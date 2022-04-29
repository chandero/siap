insert into siap.ucap_ipp_valor (ucap_ipp_valor_anho, elem_id, ucap_ipp_valor_valor)
select 2021, uiv.elem_id, uiv.ucap_ipp_valor_valor * 147.55 / 100 from siap.ucap_ipp_valor uiv  where uiv.ucap_ipp_valor_anho = 2014;
