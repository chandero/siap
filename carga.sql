--
-- PostgreSQL database dump
--

-- Dumped from database version 10.4
-- Dumped by pg_dump version 10.4

-- Started on 2019-05-20 07:41:04

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 324 (class 1259 OID 76524)
-- Name: carga; Type: TABLE; Schema: siap; Owner: isaguser
--

CREATE TABLE siap.carga (
    empr_id integer,
    zanho integer,
    zperiodo integer,
    aacu_id integer,
    aap_tecnologia character varying(250),
    aap_potencia integer,
    cantidad integer,
    retirada integer,
    instalada integer,
    aaco_id integer
);


ALTER TABLE siap.carga OWNER TO isaguser;

--
-- TOC entry 2543 (class 0 OID 0)
-- Dependencies: 324
-- Name: TABLE carga; Type: COMMENT; Schema: siap; Owner: isaguser
--

COMMENT ON TABLE siap.carga IS 'almacena info para el cálculo de carga';


--
-- TOC entry 2537 (class 0 OID 76524)
-- Dependencies: 324
-- Data for Name: carga; Type: TABLE DATA; Schema: siap; Owner: isaguser
--



-- Completed on 2019-05-20 07:41:06

--
-- PostgreSQL database dump complete
--

