alter table siap.aap_adicional add column aap_fotocontrol varchar(100);
alter table siap.aap_adicional add column aap_medidor_comercializadora varchar(100);
alter table siap.aap_adicional_historia add column aap_fotocontrol varchar(100);
alter table siap.aap_medidor_comercializadora varchar(100);
alter table siap.aap_adicional_historia add column empr_id int4;
alter table siap.aap_corte_periodo add constraint aap_corte_periodo_pk primary key (aap_id, empr_id, zanho, zmes);
alter table siap.aap_adicional_corte_periodo add constraint aap_adicional_corte_periodo_pk primary key (aap_id, empr_id, zanho, zmes);
CREATE TABLE siap.cobro_factura (
	cofa_id serial4 NOT NULL,
	cofa_fecha date NOT NULL,
	cofa_anho int4 NULL,
	cofa_periodo int4 NULL,
	cofa_factura int4 NULL,
	cofa_prefijo varchar NULL,
	cofa_estado int4 NULL,
	empr_id int4 NOT NULL,
	CONSTRAINT cobro_factura_pk PRIMARY KEY (cofa_id)
);
CREATE TABLE siap.cobro_factura_orden_trabajo (
	cofa_id int4 NOT NULL,
	cotr_id int4 NOT NULL,
	cofaot_activa bool NOT NULL,
	CONSTRAINT cobro_factura_orden_trabajo_pk PRIMARY KEY (cofa_id, cotr_id)
);
CREATE TABLE siap.cobro_orden_trabajo (
	cotr_id serial4 NULL,
	cotr_anho int4 NULL,
	cotr_periodo int4 NULL,
	cotr_consecutivo int4 NULL,
	cotr_fecha date NULL,
	cotr_luminaria_anterior varchar NULL,
	cotr_luminaria_nueva varchar NULL,
	cotr_tecnologia_anterior varchar NULL,
	cotr_tecnologia_nueva varchar NULL,
	cotr_potencia_anterior int4 NULL,
	cotr_potencia_nueva int4 NULL,
	cotr_direccion varchar NULL,
	cotr_cantidad int4 NULL,
	cotr_tipo_obra int4 NULL,
	cotr_tipo_obra_tipo varchar NULL,
	empr_id int4 NULL,
	tireuc_id int4 NULL,
	barr_id int4 NULL,
	aaus_id int4 NULL,
	cotr_fecha_proceso timestamp NULL
);

CREATE TABLE siap.cobro_orden_trabajo_material (
	cotr_id int4 NULL,
	elem_id int4 NULL,
	cotrma_valor_unitario numeric NULL,
	cotrma_cantidad float8 NULL
);

CREATE TABLE siap.cobro_orden_trabajo_reporte (
	cotr_id int4 NULL,
	repo_id int4 NULL,
	tireuc_id int4 NULL,
	aap_id int4 NULL
);

CREATE TABLE siap.cobro_orden_trabajo_texto (
	cott_id serial4 NOT NULL,
	cott_codigo int4 NOT NULL,
	cott_texto text NULL,
	empr_id int4 NOT NULL,
	CONSTRAINT cobro_orden_trabajo_texto_pl PRIMARY KEY (cott_id, empr_id)
);

alter table siap."control" add column repo_subrepoconsecutivo varchar(255) null;
alter table siap."control" add constraint control_pk primary key (aap_id, empr_id);
alter table siap.control_reporte add column repo_subrepoconsecutivo varchar(255) null;
alter table siap.control_reporte add constraint control_reporte_pk primary key (repo_id);
alter table siap.control_reporte_adicional add constraint control_reporte_adicional_pk primary key (repo_id);
alter table siap.control_reporte_adicional drop column ortr_id;
alter table siap.control_reporte_direccion add column even_horaini varchar(8) null;
alter table siap.control_reporte_direccion add column even_horafin varchar(8) null;
CREATE TABLE siap.controlcarga (
	controlcarga_id bigserial NOT NULL,
	controlcarga_anho int4 NOT NULL,
	controlcarga_mes int4 NOT NULL,
	controlcarga_fecha timestamp NOT NULL,
	usua_id int4 NOT NULL,
	empr_id int4 NOT NULL,
	controlcarga_registros int8 NULL,
	controlcarga_tipo int4 NULL,
	controlcarga_exito bool NULL,
	controlcarga_mensaje varchar(255) NULL,
	controlcarga_estado int4 NULL,
	CONSTRAINT controlcarga_pkey PRIMARY KEY (controlcarga_id)
);
alter table siap.elemento_precio add column elpr_fecha date not null;
alter table siap.elemento_precio add constraint elemento_precio_pk primary key (elem_id, elpr_anho, elpr_fecha);
CREATE TABLE siap.elemento_unitario (
	elem_id int4 NOT NULL,
	unit_id int4 NOT NULL,
	elun_fecha date NULL,
	CONSTRAINT elemento_unitario_pk PRIMARY KEY (elem_id, unit_id)
);

CREATE TABLE siap.firma (
	firm_id serial4 NOT NULL,
	firm_descripcion varchar NULL,
	firm_titulo varchar NULL,
	firm_nombre varchar NOT NULL,
	empr_id int4 NOT NULL DEFAULT 1,
	firm_codigo int4 NOT NULL,
	CONSTRAINT firma_pk PRIMARY KEY (firm_id)
);

CREATE TABLE siap.mano_ingenieria (
	main_id serial4 NOT NULL,
	main_descripcion varchar NOT NULL,
	empr_id int4 NOT NULL DEFAULT 1,
	CONSTRAINT mano_ingenieria_pk PRIMARY KEY (main_id)
);

CREATE TABLE siap.mano_ingenieria_precio (
	main_id int4 NOT NULL,
	mainpr_anho int4 NOT NULL,
	mainpr_fecha date NOT NULL,
	mainpr_precio numeric NULL,
	mainpr_es_porcentaje bool NOT NULL,
	CONSTRAINT mano_ingenieria_precio_pk PRIMARY KEY (main_id, mainpr_anho, mainpr_fecha)
);

CREATE TABLE siap.mano_obra (
	maob_id serial4 NOT NULL,
	maob_descripcion varchar NOT NULL,
	empr_id int4 NOT NULL DEFAULT 1,
	maob_codigo int4 NULL
);

CREATE TABLE siap.mano_obra_precio (
	maob_id int4 NOT NULL,
	maobpr_anho int4 NOT NULL,
	maobpr_fecha date NOT NULL,
	maobpr_precio numeric NULL,
	maobpr_rendimiento numeric NULL,
	maob_codigo int4 NULL,
	empr_id int4 NULL,
	cotr_tipo_obra int4 NULL,
	cotr_tipo_obra_tipo varchar NULL
);

CREATE TABLE siap.mano_transporte_herramienta (
	math_id serial4 NOT NULL,
	math_descripcion varchar NOT NULL,
	math_codigo int4 NOT NULL DEFAULT 1,
	empr_id int4 NOT NULL DEFAULT 1
);

CREATE TABLE siap.mano_transporte_herramienta_precio (
	math_id int4 NOT NULL,
	mathpr_anho int4 NOT NULL,
	mathpr_fecha date NOT NULL,
	mathpr_precio numeric NOT NULL,
	mathpr_es_porcentaje bool NOT NULL,
	mathpr_rendimiento numeric NULL,
	mathpr_cantidad numeric NULL,
	math_codigo int4 NULL,
	empr_id int4 NULL,
	cotr_tipo_obra int4 NULL,
	cotr_tipo_obra_tipo varchar NULL
);

alter table siap.medidor add column medi_comenergia varchar(100) null;
CREATE TABLE siap.medidor_lectura (
	mele_id bigserial NOT NULL,
	medi_id int8 NOT NULL,
	mele_anho int4 NOT NULL,
	mele_mes int4 NOT NULL,
	mele_activa_anterior int4 NULL,
	mele_activa_actual int4 NULL,
	mele_reactiva_anterior int4 NULL,
	mele_reactiva_actual int4 NULL,
	mele_reactiva int4 NULL,
	mele_comenergia varchar(50) NULL,
	mele_cuenta varchar(100) NULL,
	mele_nombre varchar(500) NULL,
	empr_id int8 NULL,
	CONSTRAINT medidor_lectura_pkey PRIMARY KEY (mele_id)
);

alter table siap.reporte_estado add constraint reporte_estado_pk primary key (rees_id);
alter table siap.reporte_evento add column unit_id int4 null;
alter table siap.solicitud add column soli_fechaentrega timestamp null;
alter table siap.transformador rename column tran_id TO aap_id;
alter table siap.transformador rename column tran_numero TO aap_numero;
alter table siap.transformador rename column tran_direccion TO aap_direccion;
alter table siap.transformador rename column tran_estado TO aap_estado;
alter table siap.transformador add column aap_codigo_apoyo varchar null;
alter table siap.transformador add column aap_propietario varchar null;
alter table siap.transformador add column aap_marca varchar null;
alter table siap.transformador add column aap_serial varchar null;
alter table siap.transformador add column aap_kva numeric null;
alter table siap.transformador add column tipo_id int4 null;
alter table siap.transformador add column aap_fases varchar null;
alter table siap.transformador add column aap_tension_p numeric null;
alter table siap.transformador add column aap_tension_s numeric null;
alter table siap.transformador add column aap_referencia varchar null;
alter table siap.transformador_reporte add column tireuc_id int 4 null;
alter table siap.transformador_reporte add column repo_subrepoconsecutivo varchar null;
alter table siap.transformador_reporte_direccion add column even_horaini varchar null;
alter table siap.transformador_reporte_direccion add column even_horafin varchar null;
alter table siap.transformador_reporte_evento add column unit_id int4 null;
CREATE TABLE siap.unitario (
	unit_id serial4 NOT NULL,
	unit_codigo varchar NOT NULL,
	unit_descripcion varchar NOT NULL,
	empr_id int4 NOT NULL,
	CONSTRAINT unitario_pk PRIMARY KEY (unit_id)
);
INSERT INTO siap.unitario (unit_codigo,unit_descripcion,empr_id) VALUES
	 ('1.01','LUMINARIA',3),
	 ('1.02','POSTE',3),
	 ('1.03','RED',3),
	 ('1.05','PINTURA',3),
	 ('1.06','REPLANTEO',3),
	 ('1.07','PUESTA A TIERRA',3),
	 ('1.08','CANALIZACION',3),
	 ('1.09','BAJANTES',3),
	 ('1.11','TRANSFORMADOR',3),
	 ('1.12','ESTRUCTURA',3);
INSERT INTO siap.unitario (unit_codigo,unit_descripcion,empr_id) VALUES
	 ('1.10','EQUIPO DE MEDIDA Y CONTROL',3),
	 ('1.04','DESMONTE LUMINARIA',3);
