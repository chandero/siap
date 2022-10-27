<template>
  <el-container>
    <el-header>
      <el-row>
        <el-col :span="20">
          <span
            >{{ $t('route.reporteedit') }} - Estado Actual: {{ estado() }}</span
          >
        </el-col>
        <el-col :span="4">
          <el-button
            align="right"
            type="primary"
            title="Convertir en Reporte de Control"
            @click="showConvertirDlg = true"
            >Convertir</el-button
          >
        </el-col>
      </el-row>
    </el-header>
    <el-main>
      <el-form
        ref="reporteForm"
        :model="reporte"
        :rules="rules"
        :label-position="labelPosition"
        disabled
      >
        <el-collapse v-model="activePages" @change="handleActivePagesChange">
          <el-collapse-item
            name="1"
            :title="$t('reporte.general')"
            style="font-weight: bold"
          >
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <template v-if="repo_fecharecepcion_state">
                  <el-form-item
                    prop="repo_fecharecepcion"
                    :label="$t('reporte.receptiondate')"
                  >
                    <el-date-picker
                      type="datetime"
                      ref="receptiondate"
                      v-model="reporte.repo_fecharecepcion"
                      width="85%"
                    ></el-date-picker>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit();
                        repo_fecharecepcion_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.repo_fecharecepcion = repo_fecharecepcion;
                        repo_fecharecepcion_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.receptiondate')">
                    <span style="font-size: 24px">{{
                      reporte.repo_fecharecepcion | moment('YYYY/MM/DD HH:mm')
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id === 1"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="
                        repo_fecharecepcion_state = !repo_fecharecepcion_state
                      "
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <el-form-item prop="reti_id" :label="$t('reporte.type')">
                  <span style="font-size: 24px">{{
                    reporte_tipo(reporte.reti_id)
                  }}</span>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                <el-form-item
                  prop="repo_consecutivo"
                  :label="$t('reporte.number')"
                >
                  <span style="font-size: 30px">{{
                    reporte.repo_consecutivo | fillZeros(6)
                  }}</span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col
                v-if="reporte.reti_id === 2"
                :xs="24"
                :sm="24"
                :md="4"
                :lg="4"
                :xl="4"
              >
                <el-form-item
                  prop="adicional.repo_tipo_expansion"
                  :label="$t('reporte.tipo_expansion.title')"
                >
                  <el-select
                    :disabled="reporte.rees_id === 3"
                    clearable
                    :title="$t('reporte.tipo_expansion.select')"
                    style="width: 80%"
                    ref="tipo"
                    v-model="reporte.adicional.repo_tipo_expansion"
                    name="tipo_expansion"
                    :placeholder="$t('reporte.tipo_expansion.select')"
                    @change="validarExpansion()"
                  >
                    <el-option
                      v-for="te in tipos_expansion"
                      :key="te.tiex_id"
                      :label="te.tiex_descripcion"
                      :value="te.tiex_id"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col
                v-if="reporte.adicional.repo_tipo_expansion === 5"
                :xs="24"
                :sm="24"
                :md="5"
                :lg="5"
                :xl="5"
              >
                <el-form-item
                  :label="$t('reporte.urba.title')"
                  prop="adicional.urba_id"
                >
                  <el-select
                    :disabled="
                      reporte.rees_id === 3 || reporte.direcciones.lenght
                    "
                    clearable
                    :title="$t('reporte.urba.select')"
                    style="width: 80%"
                    ref="tipo"
                    v-model="reporte.adicional.urba_id"
                    name="urbanizadora"
                    :placeholder="$t('reporte.urba.select')"
                  >
                    <el-option
                      v-for="u in urbanizadoras"
                      :key="u.urba_id"
                      :label="u.urba_descripcion"
                      :value="u.urba_id"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col
                v-if="reporte.reti_id === 2 || reporte.reti_id === 9"
                :xs="24"
                :sm="24"
                :md="3"
                :lg="3"
                :xl="3"
              >
                <el-form-item prop="muot_id" :label="$t('reporte.otm')">
                  <el-input
                    type="number"
                    style="font-size: 30px"
                    v-model="reporte.adicional.muot_id"
                    @input="reporte.adicional.muot_id = parseInt($event)"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col
                v-if="reporte.reti_id === 9"
                :xs="24"
                :sm="24"
                :md="4"
                :lg="4"
                :xl="4"
              >
                <el-form-item
                  :label="$t('reporte.aaco_id_anterior')"
                  prop="adicional.aaco_id_anterior"
                >
                  <el-select
                    clearable
                    filterable
                    ref="aaco_id_anterior"
                    v-model="reporte.adicional.aaco_id_anterior"
                    name="aaco_id_anterior"
                    :placeholder="$t('gestion.connection.select')"
                    @change="cambiarMedidaAnterior()"
                  >
                    <el-option
                      v-for="conexion in conexiones"
                      :key="conexion.aaco_id"
                      :label="conexion.aaco_descripcion"
                      :value="parseInt(conexion.aaco_id)"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col
                v-if="reporte.reti_id === 9"
                :xs="24"
                :sm="24"
                :md="4"
                :lg="4"
                :xl="4"
              >
                <el-form-item
                  :label="$t('reporte.aaco_id_nuevo')"
                  prop="adicional.aaco_id_nuevo"
                >
                  <el-select
                    clearable
                    filterable
                    ref="aaco_id_nuevo"
                    v-model="reporte.adicional.aaco_id_nuevo"
                    name="aaco_id_anterior"
                    :placeholder="$t('gestion.connection.select')"
                    @change="cambiarMedidaNuevo()"
                  >
                    <el-option
                      v-for="conexion in conexiones"
                      :key="conexion.aaco_id"
                      :label="conexion.aaco_descripcion"
                      :value="parseInt(conexion.aaco_id)"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col
                v-if="
                  (reporte.reti_id === 9) &
                  (reporte.adicional.aaco_id_nuevo == 2)
                "
                :xs="24"
                :sm="6"
                :md="4"
                :lg="4"
                :xl="4"
              >
                <el-form-item
                  prop="adicional.medi_id"
                  :label="$t('gestion.medidor.title')"
                >
                  <el-select
                    clearable
                    filterable
                    ref="medi_id"
                    v-model="reporte.adicional.medi_id"
                    name="medi_id"
                    :placeholder="$t('gestion.medidor.select')"
                  >
                    <el-option
                      v-for="m in medidores"
                      :key="m.medi_id"
                      :label="m.medi_id | fillZeros(4)"
                      :value="m.medi_id"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col
                v-if="
                  (reporte.reti_id === 9) &
                  (reporte.adicional.aaco_id_nuevo == 2)
                "
                :xs="24"
                :sm="6"
                :md="4"
                :lg="4"
                :xl="4"
              >
                <el-form-item
                  prop="adicional.tran_id"
                  :label="$t('gestion.transformador.title')"
                >
                  <el-select
                    clearable
                    filterable
                    ref="transformador"
                    v-model="reporte.adicional.tran_id"
                    name="transformador"
                    :placeholder="$t('gestion.transformador.select')"
                  >
                    <el-option
                      v-for="t in transformadores"
                      :key="t.tran_id"
                      :label="t.tran_id | fillZeros(4)"
                      :value="t.tran_id"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <template v-if="orig_id_state">
                  <el-form-item prop="orig_id" :label="$t('reporte.origin')">
                    <el-select
                      style="width: 100%"
                      ref="origin"
                      v-model="reporte.orig_id"
                      name="origen"
                      :placeholder="$t('origen.select')"
                      @change="changeFocus('code')"
                    >
                      <el-option
                        v-for="origen in origenes"
                        :key="origen.orig_id"
                        :label="origen.orig_descripcion"
                        :value="origen.orig_id"
                      ></el-option>
                    </el-select>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        orig_id_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.orig_id = orig_id
                        orig_id_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.origin')">
                    <span style="400 13.3333px Arial;">{{
                      origen(reporte.orig_id)
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id === 1"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="orig_id_state = !orig_id_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <template v-if="repo_codigo_state">
                  <el-form-item prop="repo_codigo" :label="$t('reporte.code')">
                    <el-input
                      ref="code"
                      v-model="reporte.adicional.repo_codigo"
                      @input="
                        reporte.adicional.repo_codigo = $event.toUpperCase()
                      "
                      @keyup.enter.native="changeFocus('apoyo')"
                    ></el-input>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        repo_codigo_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.adicional.repo_codigo = repo_codigo
                        repo_codigo_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.code')">
                    <span style="400 13.3333px Arial;">{{
                      reporte.adicional.repo_codigo
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id === 1"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="repo_codigo_state = !repo_codigo_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <template v-if="repo_apoyo_state">
                  <el-form-item prop="repo_apoyo" :label="$t('reporte.apoyo')">
                    <el-input
                      ref="apoyo"
                      v-model="reporte.adicional.repo_apoyo"
                      @input="
                        reporte.adicional.repo_apoyo = $event.toUpperCase()
                      "
                      @keyup.enter.native="changeFocus('nombre')"
                    ></el-input>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        repo_apoyo_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.adicional.repo_apoyo = repo_apoyo
                        repo_apoyo_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.apoyo')">
                    <span style="400 13.3333px Arial;">{{
                      reporte.adicional.repo_apoyo
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id === 1"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="repo_apoyo_state = !repo_apoyo_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <template v-if="ortr_id_state">
                  <el-form-item
                    prop="adicional.ortr_id"
                    :label="$t('reporte.ot')"
                    required
                  >
                    <el-select
                      style="width: 100%"
                      ref="ortr_id"
                      v-model="reporte.adicional.ortr_id"
                      name="ortr_id"
                      :placeholder="$t('ortr.select')"
                      @change="changeFocus('name')"
                    >
                      <el-option
                        v-for="ot in ordenestrabajo"
                        :key="ot.ortr_id"
                        :label="ordentrabajo_label(ot.ortr_id)"
                        :value="ot.ortr_id"
                      ></el-option>
                    </el-select>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmOrdenTrabajo()
                        ortr_id_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.adicional.ortr_id = ortr_id
                        ortr_id_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.ot')">
                    <span style="400 13.3333px Arial;">{{
                      ordenes(reporte.adicional.ortr_id)
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="ortr_id_state = !ortr_id_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <template v-if="repo_nombre_state">
                  <el-form-item prop="repo_nombre" :label="$t('reporte.name')">
                    <el-input
                      ref="nombre"
                      v-model="reporte.repo_nombre"
                      @input="reporte.repo_nombre = $event.toUpperCase()"
                      @keyup.enter.native="changeFocus('direccion')"
                    ></el-input>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        repo_nombre_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.repo_nombre = repo_nombre
                        repo_nombre_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.name')">
                    <span style="400 13.3333px Arial;">{{
                      reporte.repo_nombre
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="repo_nombre_state = !repo_nombre_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <template v-if="repo_direccion_state">
                  <el-form-item
                    prop="repo_direccion"
                    :label="$t('reporte.address')"
                  >
                    <el-input
                      ref="direccion"
                      v-model="reporte.repo_direccion"
                      @input="reporte.repo_direccion = $event.toUpperCase()"
                      @keyup.enter.native="changeFocus('barrio')"
                    ></el-input>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        repo_direccion_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.repo_direccion
                        repo_direccion_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.address')">
                    <span style="400 13.3333px Arial;">{{
                      reporte.repo_direccion
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="repo_direccion_state = !repo_direccion_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <template v-if="barr_id_state">
                  <el-form-item
                    prop="barr_id"
                    :label="$t('reporte.neighborhood')"
                  >
                    <el-select
                      style="width: 100%"
                      filterable
                      ref="barrio"
                      v-model="reporte.barr_id"
                      name="barrio"
                      :placeholder="$t('barrio.select')"
                      @change="changeFocus('tiba')"
                    >
                      <el-option
                        v-for="barrio in barrios"
                        :key="barrio.barr_id"
                        :label="barrio.barr_descripcion"
                        :value="barrio.barr_id"
                      ></el-option>
                    </el-select>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        barr_id_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.barr_id = barr_id
                        barr_id_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.neighborhood')">
                    <span style="400 13.3333px Arial;">{{
                      barrio(reporte.barr_id)
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="barr_id_state = !barr_id_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                <template v-if="tiba_id_state">
                  <el-form-item
                    prop="tiba_id"
                    :label="$t('reporte.sector')"
                    :read-only="reporte.rees_id == 3"
                  >
                    <el-select
                      style="width: 100%"
                      filterable
                      ref="tiba"
                      v-model="reporte.tiba_id"
                      name="tiba"
                      :placeholder="$t('tipobarrio.select')"
                      @change="changeFocus('telefono')"
                    >
                      <el-option
                        v-for="tiba in tiposbarrio"
                        :key="tiba.tiba_id"
                        :label="tiba.tiba_descripcion"
                        :value="tiba.tiba_id"
                      ></el-option>
                    </el-select>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        tiba_id_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.tiba_id = tiba_id
                        tiba_id_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.sector')">
                    <span style="400 13.3333px Arial;">{{
                      sector(reporte.tiba_id)
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="tiba_id_state = !tiba_id_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                <template v-if="repo_telefono_state">
                  <el-form-item
                    prop="repo_telefono"
                    :label="$t('reporte.phone')"
                  >
                    <el-input
                      ref="telefono"
                      v-model="reporte.repo_telefono"
                      @keyup.enter.native="changeFocus('descripcion')"
                    ></el-input>
                  </el-form-item>
                  <el-button
                    circle
                    size="mini"
                    icon="el-icon-check"
                    type="success"
                    @click="
                      confirmEdit()
                      repo_telefono_state = false
                    "
                  />
                  <el-button
                    class="cancel-btn"
                    size="mini"
                    icon="el-icon-close"
                    type="warning"
                    circle
                    @click="
                      reporte.repo_telefono = repo_telefono
                      repo_telefono_state = false
                    "
                  />
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.phone')">
                    <span style="400 13.3333px Arial;">{{
                      reporte.repo_telefono
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="repo_telefono_state = !repo_telefono_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <template v-if="acti_id_state">
                  <el-form-item
                    prop="adicional.acti_id"
                    :label="$t('reporte.activity')"
                  >
                    <el-select
                      style="width: 90%"
                      filterable
                      ref="tiba"
                      v-model="reporte.adicional.acti_id"
                      name="actividad"
                      :placeholder="$t('actividad.select')"
                      @change="changeFocus('descripcion')"
                    >
                      <el-option
                        v-for="acti in actividades"
                        :key="acti.acti_id"
                        :label="acti.acti_descripcion"
                        :value="acti.acti_id"
                      ></el-option>
                    </el-select>
                    <el-popover
                      placement="top"
                      width="300"
                      trigger="click"
                      v-model="dialogonuevodanhovisible"
                    >
                      <el-form ref="danho" :model="actividad">
                        <el-form-item
                          prop="acti_descripcion"
                          label="Descripción del Daño"
                        >
                          <el-input
                            :disabled="reporte.rees_id === 3"
                            autofocus
                            v-model="actividad.acti_descripcion"
                            @input="
                              actividad.acti_descripcion = $event.toUpperCase()
                            "
                          ></el-input>
                        </el-form-item>
                        <el-form-item>
                          <el-button
                            size="mini"
                            type="primary"
                            icon="el-icon-check"
                            @click="guardarNuevoDanho()"
                          ></el-button>
                          <el-button
                            size="mini"
                            type="warning"
                            icon="el-icon-close"
                            @click="dialogonuevodanhovisible = false"
                          ></el-button>
                        </el-form-item>
                      </el-form>
                      <el-button
                        disabled
                        slot="reference"
                        type="primary"
                        size="mini"
                        circle
                        icon="el-icon-plus"
                        title="Adicionar Nuevo Daño"
                      />
                    </el-popover>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        acti_id_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.adicional.acti_id = acti_id
                        acti_id_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.activity')">
                    <span style="400 13.3333px Arial;">{{
                      tipo_actividad(reporte.adicional.acti_id)
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="acti_id_state = !acti_id_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <template v-if="repo_descripcion_state">
                  <el-form-item
                    prop="repo_descripcion"
                    :label="$t('reporte.description')"
                  >
                    <el-input
                      ref="descripcion"
                      v-model="reporte.repo_descripcion"
                      type="textarea"
                      :rows="2"
                      @input="reporte.repo_descripcion = $event.toUpperCase()"
                      @keyup.enter.native="changeFocus('submit')"
                    ></el-input>
                    <el-button
                      circle
                      size="mini"
                      icon="el-icon-check"
                      type="success"
                      @click="
                        confirmEdit()
                        repo_descripcion_state = false
                      "
                    />
                    <el-button
                      class="cancel-btn"
                      size="mini"
                      icon="el-icon-close"
                      type="warning"
                      circle
                      @click="
                        reporte.repo_descripcion = repo_descripcion
                        repo_descripcion_state = false
                      "
                    />
                  </el-form-item>
                </template>
                <template v-else>
                  <el-form-item :label="$t('reporte.description')">
                    <span style="400 13.3333px Arial;">{{
                      reporte.repo_descripcion
                    }}</span>
                    <el-button
                      v-if="reporte.rees_id !== 3"
                      circle
                      size="mini"
                      icon="el-icon-edit"
                      style="border-style: hidden"
                      @click="repo_descripcion_state = !repo_descripcion_state"
                    />
                  </el-form-item>
                </template>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item
            name="5"
            :title="$t('reporte.subreporte')"
            style="font-weight: bold"
          >
            <el-row>
              <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
                <span>Agregar Subreporte</span>
                <el-input
                  :disabled="reporte.rees_id === 3"
                  type="number"
                  v-model="subreporte"
                  size="mini"
                  placeholder="Nuevo SubReporte"
                  style="width: 10%"
                />
                <el-button
                  :disabled="reporte.rees_id === 3"
                  circle
                  mini
                  type="primary"
                  icon="el-icon-plus"
                  @click="addSubReporte()"
                />
                <el-row :gutter="4">
                  <el-col
                    v-for="item in reporte.subreportes"
                    :key="item.csc"
                    :span="2"
                  >
                    <template>
                      <span>{{ item.csc }}</span>
                    </template>
                    <template>
                      <el-button
                        :disabled="reporte.rees_id === 3"
                        type="danger"
                        icon="el-icon-delete"
                        circle
                        @click="delSubReporte(item.csc)"
                      ></el-button>
                    </template>
                  </el-col>
                </el-row>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item
            name="4"
            :title="$t('reporte.novedades').toUpperCase()"
          >
            <el-row :gutter="4" class="hidden-sm-and-down">
              <el-col :md="1" :lg="1" :xl="1">
                <span style="font-weight: bold">No.</span>
              </el-col>
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <span style="font-weight: bold">Novedad</span>
              </el-col>
              <el-col :xs="24" :sm="24" :md="3" :lg="3" :xl="3">
                <span style="font-weight: bold">Hora Inicio</span>
              </el-col>
              <el-col :xs="24" :sm="24" :md="3" :lg="3" :xl="3">
                <span style="font-weight: bold">Hora Terminación</span>
              </el-col>
              <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
                <span style="font-weight: bold">Observación</span>
              </el-col>
            </el-row>
            <div v-for="(evento, id) in reporte.novedades" v-bind:key="id">
              <el-form :model="evento" ref="novedadform">
                <el-row :gutter="4">
                  <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                    <span style="font-weight: bold">No.</span>
                  </el-col>
                  <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{
                    evento.even_id
                  }}</el-col>
                  <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                    <span style="font-weight: bold">Novedad</span>
                  </el-col>
                  <el-col :xs="13" :sm="13" :md="6" :lg="6" :xl="6">
                    <el-form-item>
                      <el-select
                        :disabled="evento.even_estado > 7"
                        filterable
                        clearable
                        ref="type"
                        v-model="evento.nove_id"
                        name="nove"
                        :placeholder="$t('ordentrabajo.novedad.select')"
                        style="width: 95%"
                      >
                        <el-option
                          v-for="nove in novedades"
                          :key="nove.nove_id"
                          :label="nove.nove_descripcion"
                          :value="nove.nove_id"
                        >
                        </el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                    <span style="font-weight: bold">Hora Inicio</span>
                  </el-col>
                  <el-col :xs="16" :sm="16" :md="3" :lg="3" :xl="3">
                    <el-form-item prop="reno_horaini">
                      <el-time-select
                        :disabled="evento.even_estado > 7"
                        v-model="evento.reno_horaini"
                        style="width: 90%"
                        :picker-options="{
                          start: '07:00',
                          step: '00:15',
                          end: '19:00'
                        }"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                    <span style="font-weight: bold">Hora Terminacion</span>
                  </el-col>
                  <el-col :xs="16" :sm="16" :md="3" :lg="3" :xl="3">
                    <el-form-item prop="reno_horafin">
                      <el-time-select
                        :disabled="evento.even_estado > 7"
                        v-model="evento.reno_horafin"
                        style="width: 90%"
                        :picker-options="{
                          start: '07:00',
                          step: '00:15',
                          end: '23:45',
                          minTime: evento.reno_horaini
                        }"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                    <span style="font-weight: bold">Observaciön</span>
                  </el-col>
                  <el-col :xs="16" :sm="16" :md="9" :lg="9" :xl="9">
                    <el-form-item>
                      <el-input
                        :disabled="evento.even_estado > 7"
                        class="sinpadding"
                        v-model="evento.reno_observacion"
                      ></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                    <el-button
                      v-if="evento.even_estado < 8 || reporte.rees_id === 3"
                      size="mini"
                      type="danger"
                      circle
                      icon="el-icon-minus"
                      title="Quitar Fila"
                      @click="
                        evento.even_estado === 1
                          ? (evento.even_estado = 8)
                          : (evento.even_estado = 9)
                      "
                    ></el-button>
                    <el-button
                      v-if="evento.even_estado > 7 || reporte.rees_id === 3"
                      size="mini"
                      type="success"
                      circle
                      icon="el-icon-success"
                      title="Restaurar Fila"
                      @click="
                        evento.even_estado === 9
                          ? (evento.even_estado = 2)
                          : (evento.even_estado = 1)
                      "
                    ></el-button>
                  </el-col>
                </el-row>
              </el-form>
              <el-row class="hidden-md-and-up">
                <el-col style="border-bottom: 1px dotted #000"></el-col>
              </el-row>
            </div>
            <el-row>
              <el-col :span="24">
                <el-button
                  :disabled="reporte.rees_id === 3"
                  style="display: table-cell"
                  type="primary"
                  size="mini"
                  circle
                  icon="el-icon-plus"
                  title="Adicionar Nueva Fila"
                  @click="onAddNovedad()"
                />
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item name="2" :title="$t('reporte.inform')">
            <el-row :gutter="4">
              <el-col :span="8">
                <el-form-item
                  ref="f_repo_fechasolucion"
                  prop="repo_fechasolucion"
                  :label="$t('reporte.solutiondate')"
                  :read-only="reporte.rees_id == 3"
                >
                  <el-date-picker
                    ref="i_repo_fechasolucion"
                    :disabled="reporte.rees_id == 3"
                    v-model="reporte.repo_fechasolucion"
                    :picker-options="datePickerOptions"
                    @change="validarAntiguedadFecha"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item
                  prop="repo_horainicio"
                  :label="$t('reporte.timestart')"
                  :read-only="reporte.rees_id == 3"
                >
                  <el-time-select
                    :disabled="reporte.rees_id == 3"
                    v-model="reporte.repo_horainicio"
                    :picker-options="{
                      start: '07:00',
                      step: '00:15',
                      end: '19:00'
                    }"
                  ></el-time-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item
                  prop="repo_horafin"
                  :label="$t('reporte.timeend')"
                  :read-only="reporte.rees_id == 3"
                >
                  <el-time-select
                    :disabled="reporte.rees_id == 3"
                    v-model="reporte.repo_horafin"
                    :picker-options="{
                      start: '07:00',
                      step: '00:15',
                      end: '23:45',
                      minTime: reporte.repo_horainicio
                    }"
                  ></el-time-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :span="24">
                <el-form-item
                  prop="repo_reportetecnico"
                  :label="$t('reporte.tecnicalreport')"
                >
                  <el-input
                    :disabled="reporte.rees_id == 3"
                    type="textarea"
                    :rows="3"
                    ref="tecnico"
                    v-model="reporte.repo_reportetecnico"
                    @input="reporte.repo_reportetecnico = $event.toUpperCase()"
                    @keyup.enter.native="changeFocus('evento.aap_id')"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :span="24">
                <el-form-item prop="meams" :label="$t('reporte.environment')">
                  <el-checkbox
                    :disabled="reporte.rees_id == 3"
                    :indeterminate="isIndeterminate"
                    v-model="checkAll"
                    @change="handleCheckAllChange"
                    >Marcar todos</el-checkbox
                  >
                  <div style="margin: 15px 0"></div>
                  <el-checkbox-group
                    v-model="reporte.meams"
                    @change="handleReporteMeamChange"
                  >
                    <el-checkbox
                      :disabled="reporte.rees_id == 3"
                      border
                      v-for="meam in medioambiente"
                      :label="meam.meam_id"
                      :key="meam.meam_id"
                      >{{ meam.meam_descripcion }}</el-checkbox
                    >
                  </el-checkbox-group>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item name="3" title="DATOS LUMINARIAS">
            <div>
              <el-row>
                <el-col :span="24">
                  <el-tag
                    v-for="tag in reporte.direcciones"
                    :key="tag.idx"
                    closable
                    :type="tag.type"
                    effect="dark"
                    size="medium"
                    @click="handleTag(tag.idx)"
                    :title="'Información Luminaria ' + tag.aap_id"
                    style="cursor: pointer"
                    >L: {{ tag.aap_id }}</el-tag
                  >
                  <el-input
                    class="input-new-address"
                    v-if="inputVisible01"
                    v-model="inputValue01"
                    ref="saveTagInputAddress01"
                    size="mini"
                    @keyup.enter.native="onAddAddress(inputValue01)"
                    @blur="onAddAddress(inputValue01)"
                  ></el-input>
                  <el-button
                    v-else-if="reporte.rees_id != 3"
                    size="small"
                    @click="showInputAddress01"
                    >+ Agregar Luminaria</el-button
                  >
                </el-col>
              </el-row>
              <el-form
                :disabled="reporte.rees_id == 3"
                :model="reporte.direcciones[didx]"
                :ref="'dirform_' + reporte.direcciones[didx].even_id"
                :name="'dirform_' + reporte.direcciones[didx].even_id"
                label-position="left"
                :rules="dirrules"
              >
                <el-row :gutter="4">
                  <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                    <span style="font-weight: bold">No.</span>
                  </el-col>
                  <el-col :xs="24" :sm="1" :md="1" :lg="1" :xl="1">{{
                    reporte.direcciones[didx].even_id
                  }}</el-col>
                  <el-col :xs="24" :sm="10" :md="10" :lg="10" :xl="10">
                    <el-form-item prop="aap_id" label="Código Luminaria">
                      <div style="display: table">
                        <el-input
                          :disabled="
                            reporte.direcciones[didx].even_estado === 3 ||
                            reporte.direcciones[didx].even_estado > 7
                          "
                          autofocus
                          :ref="'aap_id_' + didx"
                          type="number"
                          class="sinpadding"
                          style="display: table-cell"
                          v-model="reporte.direcciones[didx].aap_id"
                          @input="
                            reporte.direcciones[didx].aap_id = parseInt(
                              $event,
                              10
                            )
                          "
                          @blur="validateAap(reporte.direcciones[didx], didx)"
                        ></el-input>
                        <span
                          :class="
                            reporte.direcciones[didx].dato !== undefined &&
                            reporte.direcciones[didx].dato.aaco_id_anterior ===
                              3
                              ? 'errorClass'
                              : 'activeClass'
                          "
                          >{{ status }}</span
                        >
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="7"
                    :md="7"
                    :lg="7"
                    :xl="7"
                  >
                    <el-form-item
                      prop="dato_adicional.aap_apoyo"
                      :label="$t('reporte.apoyo')"
                    >
                      <el-input
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        ref="aap_apoyo"
                        v-model="
                          reporte.direcciones[didx].dato_adicional.aap_apoyo
                        "
                        name="aap_apoyo"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="5"
                    :md="5"
                    :lg="5"
                    :xl="5"
                  >
                    <el-form-item
                      prop="aap_fechatoma"
                      :label="$t('reporte.aap_fechatoma')"
                    >
                      <el-date-picker
                        :disabled="
                          reporte.direcciones[didx].even_estado > 7 ||
                          reporte.reti_id !== 3
                        "
                        ref="aap_fechatoma"
                        v-model="reporte.direcciones[didx].aap_fechatoma"
                        name="aap_fechatoma"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item
                      prop="even_horaini"
                      :label="$t('reporte.timestart')"
                    >
                      <el-time-select
                        v-model="reporte.direcciones[didx].even_horaini"
                        :picker-options="{
                          start: '07:00',
                          step: '00:15',
                          end: '19:00'
                        }"
                      ></el-time-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item
                      prop="even_horafin"
                      :label="$t('reporte.timeend')"
                    >
                      <el-time-select
                        v-model="reporte.direcciones[didx].even_horafin"
                        :picker-options="{
                          start: '07:00',
                          step: '00:15',
                          end: '23:45',
                          minTime: reporte.direcciones[didx].even_horaini
                        }"
                      ></el-time-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="16"
                    :md="16"
                    :lg="16"
                    :xl="16"
                  >
                    <el-form-item prop="even_direccion" label="Nueva Dirección">
                      <el-input
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        :name="'even_direccion_' + didx"
                        v-model="reporte.direcciones[didx].even_direccion"
                        @input="
                          reporte.direcciones[didx].even_direccion =
                            $event.toUpperCase()
                        "
                      ></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="8"
                    :md="8"
                    :lg="8"
                    :xl="8"
                  >
                    <el-form-item prop="barr_id" label="Barrio/Vereda">
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        style="width: 100%"
                        filterable
                        clearable
                        v-model="reporte.direcciones[didx].barr_id"
                        name="barrio"
                        :placeholder="$t('barrio.select')"
                      >
                        <el-option
                          v-for="barrio in barrios"
                          :key="barrio.barr_id"
                          :label="barrio.barr_descripcion"
                          :value="barrio.barr_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="12"
                    :md="12"
                    :lg="12"
                    :xl="12"
                  >
                    <el-form-item prop="dato_adicional.aap_lat" label="Latitud">
                      <el-input
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        :name="'aap_lat_' + didx"
                        v-model="
                          reporte.direcciones[didx].dato_adicional.aap_lat
                        "
                      />
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="12"
                    :md="12"
                    :lg="12"
                    :xl="12"
                  >
                    <el-form-item
                      prop="dato_adicional.aap_lng"
                      label="Longitud"
                    >
                      <el-input
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        :name="'aap_lng_' + didx"
                        v-model="
                          reporte.direcciones[didx].dato_adicional.aap_lng
                        "
                      />
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="24"
                    :md="8"
                    :lg="8"
                    :xl="8"
                  >
                    <el-form-item prop="dato.aatc_id" label="Tipo Luminaria">
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        style="width: 100%"
                        filterable
                        clearable
                        v-model="reporte.direcciones[didx].dato.aatc_id"
                        :name="'aatc_id_' + didx"
                        :placeholder="$t('cover.select')"
                      >
                        <el-option
                          v-for="carcasa in carcasas"
                          :key="carcasa.aatc_id"
                          :label="carcasa.aatc_descripcion"
                          :value="parseInt(carcasa.aatc_id)"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="24"
                    :md="8"
                    :lg="8"
                    :xl="8"
                  >
                    <el-form-item prop="dato.aama_id" label="Marca">
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        style="width: 100%"
                        filterable
                        clearable
                        v-model="reporte.direcciones[didx].dato.aama_id"
                        name="marca"
                        :placeholder="$t('brand.select')"
                      >
                        <el-option
                          v-for="marca in marcas"
                          :key="marca.aama_id"
                          :label="marca.aama_descripcion"
                          :value="marca.aama_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="24"
                    :md="8"
                    :lg="8"
                    :xl="8"
                  >
                    <el-form-item prop="dato.aamo_id" label="Modelo">
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        style="width: 100%"
                        filterable
                        clearable
                        v-model="reporte.direcciones[didx].dato.aamo_id"
                        name="modelo"
                        :placeholder="$t('model.select')"
                      >
                        <el-option
                          v-for="modelo in modelos"
                          :key="modelo.aamo_id"
                          :label="modelo.aamo_descripcion"
                          :value="modelo.aamo_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="4">
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="24"
                    :md="4"
                    :lg="4"
                    :xl="4"
                  >
                    <el-form-item prop="dato.aap_tecnologia" label="Tecnología">
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        style="width: 100%"
                        filterable
                        clearable
                        v-model="reporte.direcciones[didx].dato.aap_tecnologia"
                        name="tecnologia"
                        :placeholder="$t('gestion.tecnology.select')"
                      >
                        <el-option
                          v-for="tec in tecnologias"
                          :key="tec"
                          :label="tec"
                          :value="tec"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="6"
                    :md="4"
                    :lg="4"
                    :xl="4"
                  >
                    <el-form-item
                      prop="dato.aap_potencia"
                      :label="$t('gestion.power.title')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="power"
                        v-model="reporte.direcciones[didx].dato.aap_potencia"
                        name="potencia"
                        :placeholder="$t('gestion.power.select')"
                      >
                        <el-option
                          v-for="power in potencias"
                          :key="power"
                          :label="power"
                          :value="parseFloat(power)"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="6"
                    :md="4"
                    :lg="4"
                    :xl="4"
                  >
                    <el-form-item
                      prop="dato.aaco_id"
                      :label="$t('gestion.connection.title')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="conexion"
                        v-model="reporte.direcciones[didx].dato.aaco_id"
                        name="conexion"
                        :placeholder="$t('gestion.connection.select')"
                      >
                        <el-option
                          v-for="conexion in conexiones"
                          :key="conexion.aaco_id"
                          :label="conexion.aaco_descripcion"
                          :value="parseInt(conexion.aaco_id)"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="
                      (reporte.reti_id !== 0) &
                      (reporte.direcciones[didx].dato.aaco_id === 2)
                    "
                    :xs="24"
                    :sm="6"
                    :md="4"
                    :lg="4"
                    :xl="4"
                  >
                    <el-form-item
                      prop="dato_adicional.medi_id"
                      :label="$t('gestion.medidor.title')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="medidor"
                        v-model="
                          reporte.direcciones[didx].dato_adicional.medi_id
                        "
                        name="medidor"
                        :placeholder="$t('gestion.medidor.select')"
                      >
                        <el-option
                          v-for="m in medidores"
                          :key="m.medi_id"
                          :label="m.medi_id | fillZeros(4)"
                          :value="m.medi_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="6"
                    :md="4"
                    :lg="4"
                    :xl="4"
                  >
                    <el-form-item
                      prop="dato_adicional.tran_id"
                      :label="$t('gestion.transformador.title')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="transformador"
                        v-model="
                          reporte.direcciones[didx].dato_adicional.tran_id
                        "
                        name="transformador"
                        :placeholder="$t('gestion.transformador.select')"
                        :change="
                          reporte.direcciones[didx].dato_adicional.tran_id == ''
                            ? (reporte.direcciones[
                                didx
                              ].dato_adicional.tran_id = null)
                            : (reporte.direcciones[
                                didx
                              ].dato_adicional.tran_id =
                                reporte.direcciones[
                                  didx
                                ].dato_adicional.tran_id)
                        "
                      >
                        <el-option
                          v-for="t in transformadores"
                          :key="t.tran_id"
                          :label="t.tran_id | fillZeros(4)"
                          :value="t.tran_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="8"
                    :md="8"
                    :lg="8"
                    :xl="8"
                  >
                    <el-form-item
                      prop="dato.tipo_id"
                      :label="$t('gestion.post.title')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="post"
                        v-model="reporte.direcciones[didx].dato.tipo_id"
                        name="post"
                        :placeholder="$t('gestion.post.select')"
                      >
                        <el-option
                          v-for="post in postes"
                          :key="post.tipo_id"
                          :label="post.tipo_descripcion"
                          :value="post.tipo_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="8"
                    :md="8"
                    :lg="8"
                    :xl="8"
                  >
                    <el-form-item
                      prop="dato.aap_poste_altura"
                      :label="$t('gestion.post.size')"
                    >
                      <el-input
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        ref="postsize"
                        v-model="
                          reporte.direcciones[didx].dato.aap_poste_altura
                        "
                        @input="
                          reporte.direcciones[didx].dato.aap_poste_altura =
                            parseInt($event)
                        "
                        name="postsize"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="8"
                    :md="8"
                    :lg="8"
                    :xl="8"
                  >
                    <el-form-item
                      prop="dato.aap_poste_propietario"
                      :label="$t('gestion.post.own')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="postowner"
                        v-model="
                          reporte.direcciones[didx].dato.aap_poste_propietario
                        "
                        name="postowner"
                        :placeholder="$t('gestion.post.selectown')"
                      >
                        <el-option
                          v-for="own in owns"
                          :key="own"
                          :label="own"
                          :value="own"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="6"
                    :md="6"
                    :lg="6"
                    :xl="6"
                  >
                    <el-form-item
                      prop="dato.aap_brazo"
                      :label="$t('gestion.arm')"
                    >
                      <el-input
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        ref="arm"
                        v-model="reporte.direcciones[didx].dato.aap_brazo"
                        name="arm"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="6"
                    :md="6"
                    :lg="6"
                    :xl="6"
                  >
                    <el-form-item
                      prop="dato.aap_collarin"
                      :label="$t('gestion.collar')"
                    >
                      <el-input
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        ref="collar"
                        v-model="reporte.direcciones[didx].dato.aap_collarin"
                        name="collar"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="6"
                    :md="6"
                    :lg="6"
                    :xl="6"
                  >
                    <el-form-item
                      prop="dato_adicional.aaus_id"
                      :label="$t('gestion.use')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="use"
                        v-model="
                          reporte.direcciones[didx].dato_adicional.aaus_id
                        "
                        name="use"
                        :placeholder="$t('use.select')"
                      >
                        <el-option
                          v-for="aapuso in aap_usos"
                          :key="aapuso.aaus_id"
                          :label="aapuso.aaus_descripcion"
                          :value="aapuso.aaus_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col
                    v-if="reporte.reti_id !== 0"
                    :xs="24"
                    :sm="6"
                    :md="6"
                    :lg="6"
                    :xl="6"
                  >
                    <el-form-item
                      prop="dato_adicional.aacu_id"
                      :label="$t('gestion.account')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="account"
                        v-model="
                          reporte.direcciones[didx].dato_adicional.aacu_id
                        "
                        name="account"
                        :placeholder="$t('account.select')"
                      >
                        <el-option
                          v-for="aapcuentaap in aap_cuentasap"
                          :key="aapcuentaap.aacu_id"
                          :label="aapcuentaap.aacu_descripcion"
                          :value="aapcuentaap.aacu_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col
                    v-if="reporte.reti_id == 8"
                    :xs="24"
                    :sm="6"
                    :md="4"
                    :lg="4"
                    :xl="4"
                  >
                    <el-form-item
                      prop="tire_id"
                      :label="$t('gestion.tiporetiro')"
                    >
                      <el-select
                        :disabled="reporte.direcciones[didx].even_estado > 7"
                        clearable
                        filterable
                        ref="tiporetiro"
                        v-model="reporte.direcciones[didx].tire_id"
                        name="tiporetiro"
                        :placeholder="$t('tiporetiro.select')"
                      >
                        <el-option
                          v-for="tire in tiposretiro"
                          :key="tire.tire_id"
                          :label="tire.tire_descripcion"
                          :value="tire.tire_id"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                    <el-button
                      v-if="reporte.direcciones[didx].even_estado < 8"
                      size="mini"
                      type="danger"
                      circle
                      icon="el-icon-minus"
                      title="Quitar Fila"
                      @click="
                        reporte.direcciones[didx].even_estado === 1
                          ? (reporte.direcciones[didx].even_estado = 8)
                          : (reporte.direcciones[didx].even_estado = 9)
                      "
                    ></el-button>
                    <el-button
                      v-if="reporte.direcciones[didx].even_estado > 7"
                      size="mini"
                      type="success"
                      circle
                      icon="el-icon-success"
                      title="Restaurar Fila"
                      @click="
                        reporte.direcciones[didx].even_estado === 9
                          ? (reporte.direcciones[didx].even_estado = 2)
                          : (reporte.direcciones[didx].even_estado = 1)
                      "
                    ></el-button>
                  </el-col>
                </el-row>
                <!-- <el-row>
                          <el-col style="border-bottom: 1px dotted #000;"></el-col>
                </el-row>-->
              </el-form>
              <el-row class="hidden-md-and-up">
                <el-col style="border-bottom: 1px dotted #000"></el-col>
              </el-row>
            </div>
            <el-card :disabled="reporte.direcciones[didx].even_estado > 7">
              <el-row>
                <el-col :span="24">
                  <el-tag
                    v-for="tag in reporte.direcciones"
                    :key="tag.idx"
                    closable
                    :type="tag.type"
                    effect="dark"
                    size="medium"
                    @click="handleTag(tag.idx)"
                    :title="'Material Luminaria ' + tag.aap_id"
                    style="cursor: pointer"
                    >L: {{ tag.aap_id }}</el-tag
                  >
                  <el-input
                    class="input-new-address"
                    v-if="inputVisible02"
                    v-model="inputValue02"
                    ref="saveTagInputAddress02"
                    size="mini"
                    @keyup.enter.native="onAddAddress(inputValue02)"
                    @blur="onAddAddress(inputValue02)"
                  ></el-input>
                  <el-button
                    v-else-if="reporte.rees_id != 3"
                    size="small"
                    @click="showInputAddress02"
                    >+ Agregar Luminaria</el-button
                  >
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="24">
                  <span
                    >MATERIAL LUMINARIA
                    {{ reporte.direcciones[didx].aap_id }}</span
                  >
                </el-col>
              </el-row>
              <el-row :gutter="4" class="hidden-sm-and-down">
                <el-col :md="1" :lg="1" :xl="1">
                  <span style="font-weight: bold">No.</span>
                </el-col>
                <el-col :md="3" :lg="3" :xl="3">
                  <span style="font-weight: bold">Código de la Luminaria</span>
                </el-col>
                <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
                  <span style="font-weight: bold">Nombre del Material</span>
                </el-col>
                <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                  <span style="font-weight: bold"
                    >Código Material Retirado</span
                  >
                </el-col>
                <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                  <span style="font-weight: bold"
                    >Cantidad Material Retirado</span
                  >
                </el-col>
                <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                  <span style="font-weight: bold"
                    >Código Material Instalado</span
                  >
                </el-col>
                <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                  <span style="font-weight: bold"
                    >Cantidad Material Instalado</span
                  >
                </el-col>
                <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                  <span style="font-weight: bold">Ucap</span>
                </el-col>
              </el-row>
              <div style="max-height: 600px; overflow: auto">
                <el-form
                  :disabled="reporte.rees_id == 9"
                  :model="evento"
                  :ref="'matform_' + evento.even_id"
                  :rules="matrules"
                >
                  <el-row
                    :gutter="4"
                    v-for="(evento, id) in reporte.direcciones[didx].materiales"
                    v-bind:key="evento.even_id"
                  >
                    <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                      <span style="font-weight: bold">No.</span>
                    </el-col>
                    <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{
                      id + 1
                    }}</el-col>
                    <el-col class="hidden-md-and-up" :xs="9" :sm="9">
                      <span style="font-weight: bold"
                        >Código de la Luminaria</span
                      >
                    </el-col>
                    <el-col :xs="13" :sm="13" :md="3" :lg="3" :xl="3">
                      <el-form-item prop="aap_id">
                        <div style="display: table">
                          <el-input
                            disabled
                            class="sinpadding"
                            style="display: table-cell"
                            type="number"
                            v-model="evento.aap_id"
                            @input="evento.aap_id = parseInt($event, 10)"
                          ></el-input>
                        </div>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="7" :sm="7">
                      <span style="font-weight: bold">Nombre del Material</span>
                    </el-col>
                    <el-col :xs="2" :sm="2" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="elem_codigo">
                        <el-input
                          :disabled="evento.even_estado > 7"
                          class="sinpadding"
                          v-model="evento.elem_codigo"
                          @blur="buscarCodigoElemento(evento)"
                        ></el-input>
                      </el-form-item>
                      <!-- <span style="width: 100%;">{{ codigoElemento(evento.elem_id) }}</span> -->
                    </el-col>
                    <el-col :xs="15" :sm="15" :md="7" :lg="7" :xl="7">
                      <el-form-item prop="elem_id">
                        <el-select
                          :disabled="evento.even_estado > 7"
                          filterable
                          :clearable="evento.even_estado === 1"
                          v-model="evento.elem_id"
                          :placeholder="$t('elemento.select')"
                          style="width: 100%"
                          @change="codigoElemento(evento)"
                          remote
                          :remote-method="remoteMethodElemento"
                          :loading="loadingElemento"
                        >
                          <el-option
                            v-for="elemento in elementos"
                            :key="elemento.elem_codigo"
                            :label="elemento.elem_descripcion"
                            :value="elemento.elem_id"
                          ></el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Código Material Retirado</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="even_codigo_retirado">
                        <el-input
                          :disabled="evento.even_estado > 7"
                          class="sinpadding"
                          v-model="evento.even_codigo_retirado"
                          @blur="
                            validarCodigoElementoRetirado(
                              evento.elem_id,
                              evento.even_codigo_retirado
                            )
                          "
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Cantidad Material Retirado</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="even_cantidad_retirado">
                        <el-input
                          :disabled="evento.even_estado > 7"
                          class="sinpadding"
                          v-model="evento.even_cantidad_retirado"
                          @blur="
                            evento.even_cantidad_retirado = parseFloat(
                              evento.even_cantidad_retirado
                            )
                          "
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Código Material Instalado</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="even_codigo_instalado">
                        <el-input
                          :disabled="evento.even_estado > 7"
                          class="sinpadding"
                          v-model="evento.even_codigo_instalado"
                          @blur="
                            validarCodigoElementoInstalado(
                              evento.elem_id,
                              evento.even_codigo_instalado
                            )
                          "
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Cantidad Material Instalado</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="even_cantidad_instalado">
                        <el-input
                          :disabled="evento.even_estado === 9"
                          class="sinpadding"
                          v-model="evento.even_cantidad_instalado"
                          @blur="
                            evento.even_cantidad_instalado = parseFloat(
                              evento.even_cantidad_instalado
                            )
                          "
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold">Ucap</span>
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="unit_id">
                        <el-select
                          :disabled="evento.even_estado === 9"
                          class="sinpadding"
                          v-model="evento.unit_id"
                        >
                          <el-option
                            v-for="unitario in unitarios[evento.elem_id]"
                            :key="unitario.unit_id"
                            :label="
                              unitario.unit_codigo +
                              '-' +
                              unitario.unit_descripcion
                            "
                            :value="unitario.unit_id"
                          />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                      <el-button
                        v-if="evento.even_estado < 8"
                        size="mini"
                        type="danger"
                        circle
                        icon="el-icon-minus"
                        title="Quitar Fila"
                        @click="
                          evento.even_estado === 1
                            ? (evento.even_estado = 8)
                            : (evento.even_estado = 9)
                        "
                      ></el-button>
                      <el-button
                        v-if="evento.even_estado > 7"
                        size="mini"
                        type="success"
                        circle
                        icon="el-icon-success"
                        title="Restaurar Fila"
                        @click="
                          evento.even_estado === 9
                            ? (evento.even_estado = 2)
                            : (evento.even_estado = 1)
                        "
                      ></el-button>
                    </el-col>
                  </el-row>
                </el-form>
                <el-row class="hidden-md-and-up">
                  <el-col style="border-bottom: 1px dotted #000"></el-col>
                </el-row>
              </div>
              <el-row>
                <el-form>
                <el-col :span="2">
                  <el-input type="number" v-model="addinputevent"></el-input>
                </el-col>
                <el-col :span="22">
                  <el-button
                    style="display: table-cell"
                    type="primary"
                    size="mini"
                    circle
                    icon="el-icon-plus"
                    title="Adicionar Nueva Fila"
                    @click="onAddEvent()"
                  />
                </el-col>
                </el-form>
              </el-row>
            </el-card>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </el-main>
    <el-footer>
      <el-button
        v-if="canSave"
        ref="submit"
        :disabled="!validate()"
        size="medium"
        type="primary"
        icon="el-icon-check"
        @click="confirmacionGuardar = !confirmacionGuardar"
        >Guardar Material</el-button
      >
    </el-footer>
    <el-dialog title="Atención" :visible.sync="centerDialogVisible" center>
      <span style="font-size: 20px">
        El Código de Luminaria
        <b>{{ aap.aap_id }}</b
        >, No Existe, Por Favor Verifique.
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button
          v-if="reporte.reti_id === 2"
          type="primary"
          @click="
            centerDialogVisible = false
            showAapModal = !showAapModal
          "
          >Crear Nuevo Código</el-button
        >
        <el-button type="primary" @click="centerDialogVisible = false"
          >Cerrar</el-button
        >
      </span>
    </el-dialog>
    <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
      <span style="font-size: 20px"
        >Seguro de Guardar las Modificaciones al Material?</span
      >
      <span slot="footer" class="dialog-footer">
        <el-button @click="confirmacionGuardar = false">No</el-button>
        <el-button type="primary" @click="aplicar">Sí</el-button>
      </span>
    </el-dialog>
    <el-dialog title="Atención" :visible.sync="retiradoDialogVisible" center>
      <span style="font-size: 20px">
        El Código de Luminaria
        <b>{{ aap.aap_id }}</b
        >, No Esta en Estado RETIRADO, Por Favor Verifique.
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="retiradoDialogVisible = false"
          >Cerrar</el-button
        >
      </span>
    </el-dialog>
    <el-dialog title="Atención" :visible.sync="yaretiradoDialogVisible" center>
      <span style="font-size: 20px">
        El Código de Luminaria
        <b>{{ aap.aap_id }}</b
        >, Se encuentra en Estado RETIRADO, Por Favor Verifique.
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="yaretiradoDialogVisible = false"
          >Cerrar</el-button
        >
      </span>
    </el-dialog>
    <el-dialog
      title="Recuperar Información"
      :visible.sync="recoveryVisible"
      width="30%"
    >
      <span
        >Existe información de Recuperación para el Reporte
        {{ reporte_previo.repo_consecutivo }}</span
      >
      <span>Desea recuperarla ?</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">No</el-button>
        <el-button type="primary" @click="recuperarReporte()">Si</el-button>
      </span>
    </el-dialog>
    <el-dialog
      title="Convertir Reporte de Luminaria a Reporte de Control"
      :visible.sync="showConvertirDlg"
      width="50%"
    >
      <span>Se convertirá el reporte en reporte de Control, continuar ?</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showConvertirDlg = false">No</el-button>
        <el-button type="primary" @click="convertirReporte()">Si</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>
<script>
import { getActividades } from '@/api/actividad'
import { getOrigenes } from '@/api/origen'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import {
  getReporte,
  updateReporteElemento,
  getTipos,
  getEstados,
  validarCodigo,
  validarReporteDiligenciado,
  convertirReporte,
  updateReporteParcial
} from '@/api/reporte'
import { getAcciones } from '@/api/accion'
import {
  getElementos,
  getElementoByDescripcion,
  getElementoByCode
} from '@/api/elemento'
import { getAapEdit, getAapValidar, validar, buscarSiguiente } from '@/api/aap'
import { getMedioambiente } from '@/api/medioambiente'
import { getAapTiposCarcasa } from '@/api/aap_tipo_carcasa'
import { getAapMarcas } from '@/api/aap_marca'
import { getAapModelos } from '@/api/aap_modelo'
import { getCaracteristica } from '@/api/caracteristica'
import { getAapConexiones } from '@/api/aap_conexion'
import { getAapUsos } from '@/api/aap_uso'
import { getAapCuentasAp } from '@/api/aap_cuentaap'
import { getTiposRetiro } from '@/api/tiporetiro'
import { getUrbanizadoraTodas } from '@/api/urbanizadora'
import { getMedidors } from '@/api/medidor'
import { getTransformadors } from '@/api/transformador'
import { getOrdenes, addReporteAOrden } from '@/api/ordentrabajo'
import { getNovedades } from '@/api/novedad'
import { getUnitariosTodas } from '@/api/unitario'
// component
// import { inspect } from 'util'

export default {
  data() {
    var validateAapEventoRule = (rule, value, callback) => {
      if (value) {
        this.aap.aap_id = value
        getAapValidar(value)
          .then((response) => {
            var result = response.data._1
            var reports = response.data._2
            this.luminaria_reportes.set(value, {
              status: result,
              reports: reports
            })
            console.log('result:', result, 'reports: ', reports)
            if (result === 404) {
              this.existe = false
              if (this.reporte.reti_id !== 2) {
                callback(new Error('No Existe'))
              } else {
                callback()
              }
            } else if (result === 401) {
              this.existe = false
              callback(new Error('Dada de Baja'))
            } else if (result === 200) {
              if (this.reporte.reti_id === 3) {
                callback(new Error('No Retirada'))
              } else if (this.reporte.reti_id === 2) {
                this.existe = true
                if (
                  this.reporte.adicional.repo_tipo_expansion !== 4 &&
                  this.reporte.rees_id === 1
                ) {
                  this.$alert(
                    'Error: Código de Luminaria "' + value + '" ya Existe',
                    'Error',
                    {
                      confirmButtonText: 'Reintentar'
                    }
                  )
                  callback(new Error('Ya Existe'))
                } else {
                  callback()
                }
              } else {
                this.existe = true
                callback()
              }
            } else if (result === 204) {
              if (this.reporte.reti_id === 3 || this.reporte.reti_id === 7) {
                const l_fecha_reporte_retiro =
                  reports[this.reportIndex(reports, 8)]._2
                console.log('fecha solucion:', this.reporte.repo_fechasolucion)
                if (
                  this.reporte.repo_fechasolucion === undefined ||
                  this.reporte.repo_fechasolucion.is_null
                ) {
                  this.$alert(
                    'Por favor ingrese la fecha de solución',
                    'Atención',
                    {
                      callback: (action) => {
                        this.$refs.i_repo_fechasolucion.focus()
                      }
                    }
                  )
                  callback(new Error('Por favor ingrese la fecha de solución'))
                }
                const d_fecha_reporte = new Date(
                  this.reporte.repo_fechasolucion
                )
                const d_now = new Date()
                d_fecha_reporte.setHours(d_now.getHours())
                d_fecha_reporte.setMinutes(d_now.getMinutes())
                d_fecha_reporte.setSeconds(d_now.getSeconds())
                if (d_fecha_reporte.getTime() < l_fecha_reporte_retiro) {
                  this.$alert(
                    'La fecha de solución de la reubicación es previa a la fecha del reporte de retiro. Por favor verifique la fecha de solución',
                    'Atención',
                    {
                      callback: (action) => {
                        this.$refs.i_repo_fechasolucion.focus()
                      }
                    }
                  )
                  callback(new Error('Por favor revise la fecha de solución'))
                } else {
                  callback()
                }
              } else if (
                (this.reporte.reti_id === 1 || this.reporte.reti_id === 8) &&
                this.reporte.rees_id === 1
              ) {
                callback(new Error('Retirada'))
              } else if (this.reporte.reti_id === 2) {
                this.existe = true
                if (
                  this.reporte.adicional.repo_tipo_expansion === 3 &&
                  this.reporte.rees_id === 1
                ) {
                  callback(new Error('Ya Existe'))
                } else {
                  console.log('Fecha solucion valida')
                  callback()
                }
              } else {
                this.existe = true
                callback()
              }
            }
          })
          .catch(() => {
            this.existe = false
            callback(new Error('Error consultando código'))
          })
      } else {
        console.log('En Validator sin aap_id')
        this.existe = false
        callback()
      }
    }
    var validateUnitEventoRule = (rule, value, callback) => {
      console.log('rule:', rule)
      console.log('value:', value)
      if (this.reporte.reti_id === 2 || this.reporte.reti_id === 6) {
        if (!value) {
          callback(new Error('Por favor seleccione la ucap'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }
    return {
      repo_fecharecepcion_state: false,
      repo_direccion_state: false,
      repo_nombre_state: false,
      repo_telefono_state: false,
      repo_codigo_state: false,
      repo_apoyo_state: false,
      repo_descripcion_state: false,
      orig_id_state: false,
      acti_id_state: false,
      tiba_id_state: false,
      barr_id_state: false,
      ortr_id_state: false,
      // variables a almacenar
      repo_fecharecepcion: null,
      repo_direccion: null,
      repo_nombre: null,
      repo_telefono: null,
      repo_codigo: null,
      repo_apoyo: null,
      repo_descripcion: null,
      orig_id: null,
      acti_id: null,
      tiba_id: null,
      barr_id: null,
      // subreporte
      subreporte: null,
      // variables a almacenar
      invalid: false,
      labelPosition: 'top',
      loadingElemento: false,
      showAapModal: false,
      showConvertirDlg: false,
      activePages: ['1', '2', '3', '4', '5'],
      activePages2: ['2-1', '2-2'],
      nopopover: false,
      inputValue01: null,
      inputValue02: null,
      inputVisible01: null,
      inputVisible02: null,
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      reporte_previo: {
        reti_id: null,
        tireuc_id: null,
        repo_id: null,
        repo_consecutivo: 0,
        repo_numero: null,
        repo_fecharecepcion: null,
        repo_direccion: null,
        repo_nombre: null,
        repo_telefono: null,
        repo_fechasolucion: null,
        repo_horainicio: null,
        repo_horafin: null,
        repo_reportetecnico: null,
        repo_descripcion: null,
        repo_subrepoconsecutivo: null,
        subreporte: null,
        rees_id: 2,
        orig_id: null,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        meams: [],
        eventos: [],
        direcciones: [],
        adicional: [],
        novedades: []
      },
      evento_siguiente_consecutivo: 1,
      direccion_siguiente_consecutivo: 1,
      novedad_siguiente_consecutivo: 1,
      autorizacion: '',
      addinputevent: 4,
      coau_tipo: 0,
      siguiente_consecutivo: null,
      reporte: {
        reti_id: null,
        repo_id: null,
        repo_consecutivo: 0,
        repo_numero: null,
        repo_fecharecepcion: null,
        repo_direccion: null,
        repo_nombre: null,
        repo_telefono: null,
        repo_fechasolucion: null,
        repo_horainicio: null,
        repo_horafin: null,
        repo_reportetecnico: null,
        repo_descripcion: null,
        rees_id: 2,
        orig_id: null,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        meams: [],
        eventos: [],
        direcciones: [
          {
            materiales: [],
            dato: {
              aatc_id: null
            },
            dato_adicional: {
              aap_apoyo: null
            }
          }
        ],
        adicional: {
          repo_id: null,
          repo_fechadigitacion: null,
          repo_modificado: null,
          repo_tipo_expansion: null,
          repo_luminaria: null,
          repo_redes: null,
          repo_poste: null,
          repo_subreporte: null,
          repo_subid: null,
          repo_email: null,
          acti_id: null,
          repo_codigo: null,
          repo_apoyo: null,
          urba_id: null,
          muot_id: null,
          autorizacion: null,
          ortr_id: null
        }
      },
      evento: {
        even_codigo_instalado: null,
        even_codigo_retirado: null,
        even_cantidad_instalado: 1,
        even_cantidad_retirado: 1,
        even_estado: 1,
        aap_id: null,
        repo_id: null,
        elem_id: null,
        elem_codigo: null,
        elem_descripcion: null,
        empr_id: 0,
        usua_id: 0,
        even_id: null,
        unit_id: null
      },
      direccion: {
        repo_id: null,
        aap_id: null,
        even_direccion: null,
        barr_id: null,
        even_direccion_anterior: null,
        barr_id_anterior: null,
        even_estado: null,
        even_horaini: null,
        even_horafin: null,
        even_esbaja: null,
        even_valido: {
          aap_id: true,
          aap_direccion: true,
          barr_id: true,
          aatc_id: true,
          aama_id: true,
          aamo_id: true,
          aaco_id: true,
          aap_potencia: true,
          aap_tecnologia: true,
          aap_brazo: true,
          aap_collarin: true,
          tipo_id: true,
          aap_poste_altura: true,
          aap_poste_propietario: true
        },
        dato: {
          aatc_id: null,
          aatc_id_anterior: null,
          aama_id: null,
          aama_id_anterior: null,
          aamo_id: null,
          aamo_id_anterior: null,
          aaco_id: null,
          aaco_id_anterior: null,
          aap_potencia: null,
          aap_potencia_anterior: null,
          aap_tecnologia: null,
          aap_tecnologia_anterior: null,
          aap_brazo: null,
          aap_brazo_anterior: null,
          aap_collarin: null,
          aap_collarin_anterior: null,
          tipo_id: null,
          tipo_id_anterior: null,
          aap_poste_altura: null,
          aap_poste_altura_anterior: null,
          aap_poste_propietario: null,
          aap_poste_propietario_anterior: null
        },
        dato_adicional: {
          aacu_id_anterior: null,
          aacu_id: null,
          aaus_id_anterior: null,
          aaus_id: null,
          medi_id_anterior: null,
          medi_id: null,
          tran_id_anterior: null,
          tran_id: null,
          aap_apoyo_anterior: null,
          aap_apoyo: null,
          aap_lat_anterior: null,
          aap_lat: null,
          aap_lng_anterior: null,
          aap_lng: null
        }
      },
      rules: {
        orig_id: [
          {
            required: true,
            message: 'Debe Seleccionar el Origen del Reporte',
            trigger: 'change'
          }
        ],
        repo_nombre: [
          {
            required: true,
            message:
              'Debe Digitar el Nombre de quién reporta el daño o actividad',
            trigger: 'blur'
          }
        ],
        repo_direccion: [
          {
            required: true,
            message: 'Debe Digitar la dirección del daño o actividad',
            trigger: 'blur'
          }
        ],
        repo_telefono: [
          {
            required: true,
            message:
              'Debe Digitar el Teléfono de quién reporta el daño o actividad',
            trigger: 'blur'
          }
        ],
        barr_id: [
          {
            required: true,
            message: 'Debe Seleccionar el Barrio del Daño o Actividad',
            trigger: 'change'
          }
        ],
        tiba_id: [
          {
            required: true,
            message: 'Debe Seleccionar el Tipo de Sector del Daño o Actividad',
            trigger: 'blur'
          }
        ],
        adicional: {
          repo_tipo_expansion: [
            {
              required: false,
              message: 'Debe Seleccionar el Tipo de Expansión',
              trigger: 'change'
            }
          ],
          muot_id: [
            {
              required: true,
              message: 'Debe digitar el número de Orden de Trabajo',
              trigger: 'blur'
            }
          ],
          urba_id: [
            {
              required: false,
              message: 'Debe Seleccionar la Urbanizadora',
              trigger: 'change'
            }
          ],
          ortr_id: [
            {
              required: true,
              message: 'Debe Seleccionar el Orden de Trabajo',
              trigger: 'change'
            }
          ],
          acti_id: [
            {
              required: true,
              message: 'Debe Seleccionar el Tipo de Daño',
              trigger: 'change'
            }
          ]
        }
      },
      danhorules: {
        acti_descripcion: [
          {
            required: true,
            message: 'Debe Diligenciar la Descripción del nuevo Daño',
            trigger: 'blur'
          }
        ]
      },
      matrules: {
        'evento.aap_id': [
          { validator: validateAapEventoRule, trigger: 'blur' }
        ],
        'evento.unit_id': [
          { validator: validateUnitEventoRule, trigger: 'change' }
        ]
      },
      dirrules: {
        aap_id: [
          { validator: validateAapEventoRule, trigger: 'blur' },
          {
            type: 'number',
            required: true,
            message: 'Ingrese el código de la luminaria',
            trigger: 'blur'
          }
        ],
        even_horaini: [
          {
            required: true,
            message: 'Seleccione la Hora Inicial',
            trigger: 'change'
          }
        ],
        even_horafin: [
          {
            required: true,
            message: 'Seleccione la Hora Final',
            trigger: 'change'
          }
        ],
        even_direccion: [
          {
            required: true,
            message: 'Ingrese la nueva dirección de la luminaria',
            trigger: 'blur'
          }
        ],
        barr_id: [
          {
            required: true,
            message: 'Seleccione el barrio de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.aatc_id': [
          {
            required: true,
            message: 'Seleccione el tipo de luminaria',
            trigger: 'change'
          }
        ],
        'dato.aama_id': [
          {
            required: true,
            message: 'Seleccione la marca de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.aamo_id': [
          {
            required: true,
            message: 'Seleccione el modelo de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.aap_tecnologia': [
          {
            required: true,
            message: 'Seleccione la tecnología de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.aap_potencia': [
          {
            required: true,
            message: 'Seleccione la potencia de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.aaco_id': [
          {
            required: true,
            message: 'Seleccione el tipo de medida de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.tipo_id': [
          {
            required: true,
            message: 'Seleccione el tipo de poste de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.aap_poste_altura': [
          {
            required: true,
            message: 'Digite la altura del poste de la luminaria',
            trigger: 'blur'
          }
        ],
        'dato.aap_poste_propietario': [
          {
            required: true,
            message: 'Seleccione el propietario del poste de la luminaria',
            trigger: 'change'
          }
        ],
        'dato.aap_brazo': [
          {
            required: true,
            message: 'Digite el tipo de brazo de la luminaria',
            trigger: 'blur'
          }
        ],
        'dato.aap_collarin': [
          {
            required: true,
            message: 'Digite el tipo de collarin de la luminaria',
            trigger: 'blur'
          }
        ],
        tire_id: [
          {
            required: true,
            message: 'Seleccione el Motivo de Retiro',
            trigger: 'change'
          }
        ],
        'dato_adicional.aaus_id': [
          { required: true, message: 'Seleccione el Uso', trigger: 'change' }
        ],
        'dato_adicional.aacu_id': [
          {
            required: true,
            message: 'Seleccione la Cuenta de Alumbrado',
            trigger: 'change'
          }
        ],
        'dato_adicional.medi_id': [
          {
            required: false,
            message: 'Seleccione el Medidor',
            trigger: 'change'
          }
        ],
        'dato_adicional.tran_id': [
          {
            required: false,
            message: 'Seleccione el Transformador',
            trigger: 'change'
          }
        ]
      },
      timeOptions: {
        start: '07:00',
        step: '00:15',
        end: '23:59'
      },
      origenes: [],
      barrios: [],
      barrios_lista: [],
      acciones: [],
      elementos: [],
      elementos_n: [],
      elementos_list: [],
      medioambiente: [],
      medioambiente_keys: [],
      meams: [],
      estados: [],
      carcasas: [],
      marcas: [],
      modelos: [],
      tecnologias: [],
      potencias: [],
      postes: [],
      conexiones: [],
      medidores: [],
      transformadores: [],
      aap_usos: [],
      aap_cuentasap: [],
      tiposretiro: [],
      novedades: [],
      checkAll: false,
      isIndeterminate: false,
      aap: { aap_id: null },
      tipos: [],
      tiposbarrio: [],
      actividades: [],
      urbanizadoras: [],
      ordenestrabajo: [],
      owns: [],
      unitarios: [],
      unitario_lista: [],
      luminaria_reportes: new Map(),
      status: '',
      dialogonuevodanhovisible: false,
      actividad: {
        acti_id: null,
        acti_descripcion: null,
        acti_estado: 1,
        usua_id: 0
      },
      centerDialogVisible: false,
      retiradoDialogVisible: false,
      recoveryVisible: false,
      yaretiradoDialogVisible: false,
      eventrules: [],
      conDirecciones: false,
      didx: 0,
      eidx: 0,
      idx: 1,
      tipos_expansion: [
        {
          tiex_id: 1,
          tiex_descripcion: 'TIPO I',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 2,
          tiex_descripcion: 'TIPO II',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 3,
          tiex_descripcion: 'TIPO III',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 4,
          tiex_descripcion: 'TIPO IV',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 5,
          tiex_descripcion: 'URBANIZADORA',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        }
      ],
      datePickerOptions: {
        disabledDate: this.validarFecha
      }
    }
  },
  timers: {
    autosave: { name: 'autosave', time: 10000, autostart: false, repeat: true },
    pending: { name: 'pending', time: 30000, autostart: false, repeat: true }
  },
  methods: {
    reportIndex(reports, repo_id) {
      return reports.map((r) => r._3).indexOf(repo_id)
    },
    addSubReporte() {
      console.log('adicionando subreporte número: ' + this.subreporte)
      if (
        this.subreporte !== undefined &&
        this.subreporte !== null &&
        this.subreporte > 0
      ) {
        this.reporte.subreportes.push({ csc: this.subreporte })
      }
      this.subreporte = null
    },
    delSubReporte(i) {
      var item = this.reporte.subreportes.find((o) => o.csc === i)
      if (item) {
        var idx = this.reporte.subreportes.indexOf(item)
        if (idx > -1) {
          this.reporte.subreportes.splice(idx, 1)
        }
      }
    },
    confirmOrdenTrabajo() {
      addReporteAOrden(
        this.reporte.adicional.ortr_id,
        this.reporte.repo_id,
        this.reporte.tireuc_id
      )
        .then((response) => {
          if (response.data === 'true') {
            this.$message({
              message: 'Orden de Trabajo Actualizada',
              type: 'success'
            })
          } else {
          }
        })
        .catch((error) => {
          this.$message({
            message: 'Orden de Trabajo NO Actualizada, error:' + error,
            type: 'warning'
          })
        })
    },
    confirmEdit() {
      const data = {
        reporte: this.reporte
      }
      updateReporteParcial(data)
        .then((response) => {
          this.repo_fecharecepcion = this.reporte.repo_fecharecepcion
          this.repo_direccion = this.reporte.repo_direccion
          this.repo_nombre = this.reporte.repo_nombre
          this.repo_telefono = this.reporte.repo_telefono
          this.repo_codigo = this.reporte.adicional.repo_codigo
          this.repo_apoyo = this.reporte.adicional.repo_apoyo
          this.repo_descripcion = this.reporte.repo_descripcion
          this.orig_id = this.reporte.orig_id
          this.acti_id = this.reporte.acti_id
          this.tiba_id = this.reporte.tiba_id
          this.barr_id = this.reporte.barr_id
          this.$message({ message: 'Reporte Actualizado.', type: 'success' })
        })
        .catch((error) => {
          this.$message({
            message: 'Reporte NO se actualizó, error:' + error,
            type: 'warning'
          })
        })
    },
    ordenes(id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var orden = this.ordenestrabajo.find((o) => o.ortr_id === id)
        if (orden) {
          return orden.ortr_consecutivo + ' - ' + orden.cuad_descripcion
        } else {
          return ''
        }
      }
    },
    ordentrabajo_label(id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var orden = this.ordenestrabajo.find((o) => o.ortr_id === id)
        if (orden) {
          return orden.ortr_consecutivo + ' - ' + orden.cuad_descripcion
        } else {
          return ''
        }
      }
    },
    origen(id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var origen = this.origenes.find((o) => o.orig_id === id)
        if (origen) {
          return origen.orig_descripcion
        } else {
          return ''
        }
      }
    },
    barrio(id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var barrio = this.barrios.find((o) => o.barr_id === id)
        if (barrio) {
          return barrio.barr_descripcion
        } else {
          return ''
        }
      }
    },
    sector(id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var tipobarrio = this.tiposbarrio.find((o) => o.tiba_id === id)
        if (tipobarrio) {
          return tipobarrio.tiba_descripcion
        } else {
          return ''
        }
      }
    },
    tipo_actividad(id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var actividad = this.actividades.find((o) => o.acti_id === id)
        if (actividad) {
          return actividad.acti_descripcion
        } else {
          return ''
        }
      }
    },
    estadoLuminaria() {
      console.log('existe: ' + this.existe)
      if (this.existe === undefined) {
        this.status = ''
      } else if (this.existe === false) {
        this.status = 'NUEVA'
      } else if (
        this.reporte.direcciones[this.didx].dato.aaco_id_anterior === 3
      ) {
        this.status = 'RETIRADA'
      } else {
        this.status = 'ACTIVA'
      }
    },
    handleTag(idx) {
      this.reporte.direcciones.forEach((d) => {
        if (d.idx === idx) {
          d.type = 'success'
          this.didx = idx - 1
          console.log('Didx =' + this.didx)
          const promise = new Promise((resolve, reject) => {
            this.completarMaterial()
            resolve()
          })
        } else {
          d.type = 'info'
        }
      })
    },
    showInputAddress01() {
      this.inputVisible01 = true
      this.$nextTick((_) => {
        this.$refs.saveTagInputAddress01.focus()
      })
    },
    showInputAddress02() {
      this.inputVisible02 = true
      this.$nextTick((_) => {
        this.$refs.saveTagInputAddress02.focus()
      })
    },
    validarTipo() {
      if (this.reporte.reti_id === 2) {
        this.reporte.orig_id = 5
        this.rules.adicional.acti_id[0].required = false
        this.rules.adicional.repo_tipo_expansion[0].required = true
        this.validarExpansion()
      } else if (this.reporte.reti_id === 1) {
        this.rules.adicional.acti_id[0].required = true
        this.rules.adicional.repo_tipo_expansion[0].required = false
      } else {
        this.rules.adicional.acti_id[0].required = false
        this.rules.adicional.repo_tipo_expansion[0].required = false
      }
    },
    validarExpansion() {
      if (this.reporte.adicional.repo_tipo_expansion === 5) {
        this.rules.adicional.urba_id[0].required = true
      } else {
        this.rules.adicional.urba_id[0].required = false
      }
    },
    validarFecha(date) {
      const repo_fecha = new Date(this.reporte.repo_fecharecepcion)
      const recepcion = new Date(
        repo_fecha.getFullYear(),
        repo_fecha.getMonth(),
        repo_fecha.getDate()
      )
      const result1 = date.getTime() >= new Date(recepcion).getTime()
      const result2 = date.getTime() <= new Date().getTime()
      const result = result1 && result2
      return !result
    },
    validarAntiguedadFecha() {
      const hoy = new Date()
      const mes_actual = hoy.getMonth()
      const mes_solucion = this.reporte.repo_fechasolucion.getMonth()
      if (mes_actual > mes_solucion) {
        if (hoy - this.reporte.repo_fechasolucion > 7) {
          this.$alert(
            'Fecha de Reporte y de Solución de un periodo anterior',
            'Atención',
            {
              confirmButtonText: 'Aceptar'
            }
          )
        }
      }
    },
    autosave() {
      if (this.reporte.rees_id !== 3) {
        localStorage.setItem(
          'currEditRepFecha',
          JSON.stringify({ fecha: Date.now(), data: this.reporte })
        )
      }
    },
    pending() {
      /* const _ini = localStorage.getItem('currEditRepFechaIni')
      const _curr = localStorage.getItem('currEditRepFecha')
      if (!this.$equals(_ini, _curr)) {
        this.open7()
      } */
    },
    changeFocus(next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange(val) {},
    handleDelete(index, row) {
      this.reporte.eventos.splice(index, 1)
      this.$refs['evento.aap_id'].focus()
    },
    handleCheckAllChange(val) {
      this.reporte.meams = val ? this.medioambiente_keys : []
      this.isIndeterminate = false
    },
    handleReporteMeamChange(value) {
      const meamCount = value.length
      this.checkAll = meamCount === this.medioambiente_keys.length
      this.isIndeterminate =
        meamCount > 0 && meamCount < this.medioambiente_keys.length
    },
    validate() {
      var valido = true
      if (
        this.reporte.repo_fechasolucion &&
        this.reporte.repo_horainicio &&
        this.reporte.repo_horafin &&
        (this.reporte.rees_id === 2 || this.reporte.rees_id === 3)
      ) {
        valido = true
        return valido
      } else {
        return false
      }
    },
    convertirReporte() {
      this.showConvertirDlg = false
      convertirReporte(this.reporte.repo_id).then((response) => {
        if (response.status === 200) {
          this.$router.push({
            path:
              '/proceso/menu1reporte/menu1-2control/menu1-2-3edit/' +
              response.data
          })
        } else {
          this.$alert('No se pudo convertir el reporte', 'Convertir Reporte', {
            confirmButtonText: 'Aceptar'
          })
        }
      })
    },
    validateAap(direccion, id) {
      if (direccion.aap_id) {
        this.aap.aap_id = direccion.aap_id
        for (var i = 0; i < this.reporte.direcciones.length; i++) {
          var d = this.reporte.direcciones[i]
          if (
            d.aap_id === direccion.aap_id &&
            d.even_id !== direccion.even_id
          ) {
            const msg = 'Código de luminaria ya está incluido en el reporte'
            this.alerta(msg)
            direccion.aap_id = null
            break
          }
        }
        if (direccion.aap_id) {
          // Limpiar Datos Direccion
          direccion.even_direccion_anterior = null
          direccion.barr_id_anterior = null
          direccion.dato.aatc_id_anterior = null
          direccion.dato.aama_id_anterior = null
          direccion.dato.aamo_id_anterior = null
          direccion.dato.aaco_id_anterior = null
          direccion.dato.aap_potencia_anterior = null
          direccion.dato.aap_tecnologia_anterior = null
          direccion.dato_adicional.aacu_id_anterior = null
          direccion.dato_adicional.aaus_id_anterior = null
          direccion.dato_adicional.aap_apoyo_anterior = null
          direccion.dato_adicional.aap_lat_anterior = null
          direccion.dato_adicional.aap_lng_anterior = null
          direccion.even_direccion = null
          direccion.barr_id = null
          direccion.dato.aatc_id = null
          direccion.dato.aama_id = null
          direccion.dato.aamo_id = null
          direccion.dato.aaco_id = null
          direccion.dato.aap_potencia = null
          direccion.dato.aap_tecnologia = null
          direccion.dato_adicional.aacu_id = null
          direccion.dato_adicional.aaus_id = null
          direccion.dato_adicional.aap_apoyo = null
          direccion.dato_adicional.aap_lat = null
          direccion.dato_adicional.aap_lng = null
          direccion.dato.aap_brazo_anterior = null
          direccion.dato.aap_brazo = null
          direccion.dato.aap_collarin_anterior = null
          direccion.dato.tipo_id_anterior = null
          direccion.dato.aap_poste_altura_anterior = null
          direccion.dato.aap_collarin = null
          direccion.dato.tipo_id = null
          direccion.dato.aap_poste_altura = null
          direccion.dato.aap_poste_propietario = null
          direccion.dato.aap_poste_propietario_anterior = null
          // Fin Limpiar Datos Direccion
          getAapEdit(direccion.aap_id)
            .then((response) => {
              const activo = response.data
              if (
                activo.aap === null ||
                activo.aap.aap_id < 1 ||
                activo.aap.esta_id === 9
              ) {
                this.existe = false
                if (
                  this.reporte.reti_id === 2 &&
                  this.reporte.adicional.repo_tipo_expansion !== 4
                ) {
                  console.log('Ingrese a llamar validar siguiente consecutivo')
                  this.validarSiguienteConsecutivo(direccion)
                }
              } else {
                this.existe = true
                if (this.reporte.reti_id !== 2) {
                  if (direccion.even_estado === 1) {
                    direccion.even_direccion_anterior = activo.aap.aap_direccion
                    direccion.barr_id_anterior = activo.aap.barr_id
                    direccion.dato.aatc_id_anterior = activo.aap.aatc_id
                    direccion.dato.aama_id_anterior = activo.aap.aama_id
                    direccion.dato.aamo_id_anterior = activo.aap.aamo_id
                    direccion.dato.aaco_id_anterior = activo.aap.aaco_id
                    direccion.dato.aap_potencia_anterior =
                      activo.aap_adicional.aap_potencia
                    direccion.dato.aap_tecnologia_anterior =
                      activo.aap_adicional.aap_tecnologia
                    direccion.dato_adicional.aacu_id_anterior =
                      activo.aap.aacu_id
                    direccion.dato_adicional.aaus_id_anterior =
                      activo.aap.aaus_id
                    direccion.dato_adicional.aap_apoyo_anterior =
                      activo.aap.aap_apoyo
                    direccion.dato_adicional.aap_lat_anterior =
                      activo.aap.aap_lat
                    direccion.dato_adicional.aap_lng_anterior =
                      activo.aap.aap_lng
                    direccion.even_direccion = activo.aap.aap_direccion
                    direccion.barr_id = activo.aap.barr_id
                    direccion.dato.aatc_id = activo.aap.aatc_id
                    direccion.dato.aama_id = activo.aap.aama_id
                    direccion.dato.aamo_id = activo.aap.aamo_id
                    direccion.dato.aaco_id = activo.aap.aaco_id
                    direccion.dato.aap_potencia =
                      activo.aap_adicional.aap_potencia
                    direccion.dato.aap_tecnologia =
                      activo.aap_adicional.aap_tecnologia
                    direccion.dato_adicional.aacu_id = activo.aap.aacu_id
                    direccion.dato_adicional.aaus_id = activo.aap.aaus_id
                    direccion.dato_adicional.aap_apoyo = activo.aap.aap_apoyo
                    direccion.dato_adicional.aap_lat = activo.aap.aap_lat
                    direccion.dato_adicional.aap_lng = activo.aap.aap_lng
                    if (
                      activo.aap_adicional.aap_brazo !== null &&
                      activo.aap_adicional.aap_brazo !== undefined
                    ) {
                      direccion.dato.aap_brazo_anterior =
                        activo.aap_adicional.aap_brazo.toString()
                      direccion.dato.aap_brazo =
                        activo.aap_adicional.aap_brazo.toString()
                    } else {
                      direccion.dato.aap_brazo_anterior = ''
                      direccion.dato.aap_brazo = ''
                    }
                    direccion.dato.aap_collarin_anterior =
                      activo.aap_adicional.aap_collarin
                    direccion.dato.tipo_id_anterior =
                      activo.aap_adicional.tipo_id
                    direccion.dato.aap_poste_altura_anterior =
                      activo.aap_adicional.aap_poste_altura
                    direccion.dato.aap_collarin =
                      activo.aap_adicional.aap_collarin
                    direccion.dato.tipo_id = activo.aap_adicional.tipo_id
                    direccion.dato.aap_poste_altura =
                      activo.aap_adicional.aap_poste_altura
                    if (
                      activo.aap_adicional.aap_poste_propietario !== null &&
                      activo.aap_adicional.aap_poste_propietario !== undefined
                    ) {
                      direccion.dato.aap_poste_propietario_anterior =
                        activo.aap_adicional.aap_poste_propietario
                      direccion.dato.aap_poste_propietario =
                        activo.aap_adicional.aap_poste_propietario
                    } else {
                      direccion.dato.aap_poste_propietario = null
                      direccion.dato.aap_poste_propietario_anterior = null
                    }
                    // validar si es reubicación y no es retirada
                    if (
                      this.reporte.reti_id === 3 ||
                      this.reporte.reti_id === 7
                    ) {
                      if (activo.aap.aaco_id !== 3) {
                        this.retiradoDialogVisible = true
                        direccion.even_valido.aap_id = false
                      } else {
                        this.retiradoDialogVisible = false
                        direccion.even_valido.aap_id = true
                        direccion.dato.aaco_id = null
                      }
                    } else {
                      this.retiradoDialogVisible = false
                    }
                    // validar si es retiro y está ya retirada
                    if (this.reporte.reti_id === 8) {
                      if (activo.aap.aaco_id === 3) {
                        this.yaretiradoDialogVisible = true
                        direccion.even_valido.aap_id = false
                      } else {
                        this.yaretiradoDialogVisible = false
                        direccion.even_valido.aap_id = true
                      }
                    }
                    if (this.reporte.reti_id === 9) {
                      console.log(
                        'Cambiando tipo de medida a : ' +
                          this.reporte.adicional.aaco_id_nuevo
                      )
                      direccion.dato.aaco_id =
                        this.reporte.adicional.aaco_id_nuevo
                      if (this.reporte.adicional.aaco_id_nuevo === 2) {
                        direccion.dato_adicional.medi_id =
                          this.reporte.adicional.medi_id
                        direccion.dato_adicional.tran_id =
                          this.reporte.adicional.tran_id
                      } else {
                        direccion.dato_adicional.medi_id = null
                        direccion.dato_adicional.tran_id = null
                      }
                    }
                    if (this.reporte.reti_id === 8) {
                      direccion.dato.aaco_id = 3
                    }
                    direccion.materiales.forEach((m) => {
                      m.aap_id = direccion.aap_id
                    })
                    this.estadoLuminaria()
                  } else {
                    this.estadoLuminaria()
                    console.log('No se puede cambiar la info')
                  }
                } else {
                  this.existe = true
                  // Cargar datos de la luminaria
                  if (direccion.even_estado === 1) {
                    direccion.even_direccion_anterior = activo.aap.aap_direccion
                    direccion.barr_id_anterior = activo.aap.barr_id
                    direccion.dato.aatc_id_anterior = activo.aap.aatc_id
                    direccion.dato.aama_id_anterior = activo.aap.aama_id
                    direccion.dato.aamo_id_anterior = activo.aap.aamo_id
                    direccion.dato.aaco_id_anterior = activo.aap.aaco_id
                    direccion.dato.aap_potencia_anterior =
                      activo.aap_adicional.aap_potencia
                    direccion.dato.aap_tecnologia_anterior =
                      activo.aap_adicional.aap_tecnologia
                    direccion.dato_adicional.aacu_id_anterior =
                      activo.aap.aacu_id
                    direccion.dato_adicional.aaus_id_anterior =
                      activo.aap.aaus_id
                    direccion.dato_adicional.aap_apoyo_anterior =
                      activo.aap.aap_apoyo
                    direccion.dato_adicional.aap_lat_anterior =
                      activo.aap.aap_lat
                    direccion.dato_adicional.aap_lng_anterior =
                      activo.aap.aap_lng
                    direccion.even_direccion = activo.aap.aap_direccion
                    direccion.barr_id = activo.aap.barr_id
                    direccion.dato.aatc_id = activo.aap.aatc_id
                    direccion.dato.aama_id = activo.aap.aama_id
                    direccion.dato.aamo_id = activo.aap.aamo_id
                    direccion.dato.aaco_id = activo.aap.aaco_id
                    direccion.dato.aap_potencia =
                      activo.aap_adicional.aap_potencia
                    direccion.dato.aap_tecnologia =
                      activo.aap_adicional.aap_tecnologia
                    direccion.dato_adicional.aacu_id = activo.aap.aacu_id
                    direccion.dato_adicional.aaus_id = activo.aap.aaus_id
                    direccion.dato_adicional.aap_apoyo = activo.aap.aap_apoyo
                    direccion.dato_adicional.aap_lat = activo.aap.aap_lat
                    direccion.dato_adicional.aap_lng = activo.aap.aap_lng
                    if (
                      activo.aap_adicional.aap_brazo !== null &&
                      activo.aap_adicional.aap_brazo !== undefined
                    ) {
                      direccion.dato.aap_brazo_anterior =
                        activo.aap_adicional.aap_brazo.toString()
                      direccion.dato.aap_brazo =
                        activo.aap_adicional.aap_brazo.toString()
                    } else {
                      direccion.dato.aap_brazo_anterior = ''
                      direccion.dato.aap_brazo = ''
                    }
                    direccion.dato.aap_collarin_anterior =
                      activo.aap_adicional.aap_collarin
                    direccion.dato.tipo_id_anterior =
                      activo.aap_adicional.tipo_id
                    direccion.dato.aap_poste_altura_anterior =
                      activo.aap_adicional.aap_poste_altura
                    direccion.dato.aap_collarin =
                      activo.aap_adicional.aap_collarin
                    direccion.dato.tipo_id = activo.aap_adicional.tipo_id
                    direccion.dato.aap_poste_altura =
                      activo.aap_adicional.aap_poste_altura
                    if (
                      activo.aap_adicional.aap_poste_propietario !== null &&
                      activo.aap_adicional.aap_poste_propietario !== undefined
                    ) {
                      direccion.dato.aap_poste_propietario_anterior =
                        activo.aap_adicional.aap_poste_propietario
                      direccion.dato.aap_poste_propietario =
                        activo.aap_adicional.aap_poste_propietario
                    } else {
                      direccion.dato.aap_poste_propietario = null
                      direccion.dato.aap_poste_propietario_anterior = null
                    }
                    direccion.materiales.forEach((m) => {
                      m.aap_id = direccion.aap_id
                    })
                  }
                  // Fin Cargar datos de la luminaria
                  this.estadoLuminaria()
                }
              }
            })
            .catch(() => {
              this.existe = false
              direccion.materiales.forEach((m) => {
                m.aap_id = direccion.aap_id
              })
              if (
                this.reporte.reti_id === 2 &&
                this.reporte.adicional.repo_tipo_expansion !== 4
              ) {
                console.log('Ingrese a llamar validar siguiente consecutivo')
                this.validarSiguienteConsecutivo(direccion)
              } else {
                this.$message({
                  type: 'error',
                  message: 'El código (' + direccion.aap_id + ') no existe',
                  duration: 5000
                })
                this.invalid = true
                this.$refs['aap_id_' + id].select()
                this.$refs['aap_id_' + id].focus()
              }
              // this.estadoLuminaria()
              // console.log('Estoy en Error: ' + error)
              // this.centerDialogVisible = true
            })
        }
      }
    },
    validarSiguienteConsecutivo(direccion) {
      console.log('Estoy en validar siguiente consecutivo')
      buscarSiguiente().then((response) => {
        var siguiente_consecutivo = response.data
        this.reporte.direcciones.forEach((d) => {
          console.log('validando direccion por crear: ' + d.aap_id)
          if (d.aap_id >= siguiente_consecutivo) {
            console.log(
              'aap_id por crear mayor que siguiente consecutivo: ' + d.aap_id
            )
            if (d.aap_id !== direccion.aap_id) {
              console.log('validando aap_id: ' + d.aap_id)
              siguiente_consecutivo = d.aap_id + 1
            }
          }
        })
        console.log('Siguiente Consecutivo: ' + siguiente_consecutivo)
        if (direccion.aap_id !== siguiente_consecutivo) {
          this.$prompt(
            'Por favor ingrese el código de autorización si lo tiene:',
            'El consecutivo digitado no es el siguiente',
            {
              confirmButtonText: 'Confirmar',
              cancelButtonText: 'Cancelar'
            }
          )
            .then(({ value }) => {
              validar(1, value)
                .then((response) => {
                  if (response.data === true) {
                    this.invalid = false
                    direccion.coau_codigo = value
                    this.$message({
                      type: 'success',
                      message: 'El código es válido, puede continuar',
                      duration: 5000
                    })
                  } else {
                    direccion.aap_id = null
                    this.$alert(
                      'El código ingresado no es válido, por favor confirmelo',
                      'Error',
                      {
                        confirmButtonText: 'Cerrar'
                      }
                    )
                    this.invalid = true
                  }
                })
                .catch((error) => {
                  direccion.aap_id = null
                  this.$message({
                    type: 'error',
                    message:
                      'Se presentó error al válidar el código (' + error + ')',
                    duration: 5000
                  })
                  this.invalid = true
                })
            })
            .catch(() => {
              direccion.aap_id = null
              direccion.materiales.forEach((m) => {
                m.aap_id = null
              })
              this.$message({
                type: 'info',
                message: 'Cancelado',
                duration: 5000
              })
              this.invalid = true
            })
        }
      })
    },
    validateAapEvento(aap_id, id) {
      if (aap_id) {
        this.aap.aap_id = aap_id
        getAapValidar(aap_id)
          .then((response) => {
            var result = response.data
            if (result === 'false') {
              this.existe = false
              this.centerDialogVisible = true
            }
          })
          .catch(() => {
            this.existe = false
            this.centerDialogVisible = true
          })
      }
    },
    codigoElemento(evento) {
      if (
        evento.elem_id === '' ||
        evento.elem_id === null ||
        evento.elem_id === undefined
      ) {
        return '-'
      } else {
        const promise = new Promise((resolve, reject) => {
          console.info('DEBUG: llamando completarMaterial')
          this.completarMaterial()
          console.info('DEBUG: Retornando de completarMaterial')
          resolve()
        })
        promise.then(() => {
          console.info('DEBUG: En el then de la promesa de completarMaterial')
          var elemento = this.elementos.find((e) => {
            return e.elem_id === evento.elem_id
          })
          if (elemento) {
            if (elemento.unitarios.length > 0) {
              this.unitarios[evento.elem_id] = elemento.unitarios
              //  this.unitario_lista = elemento.unitarios
            } else {
              this.unitarios[evento.elem_id] = []
              //  this.unitario_lista = this.unitarios
            }
            evento.elem_codigo = elemento.elem_codigo
            evento.unit_id = this.unitarios[evento.elem_id][0].unit_id
          } else {
            evento.elem_codigo = '-'
          }
        })
      }
    },
    buscarCodigoElemento(evento) {
      if (
        evento.elem_codigo !== undefined &&
        evento.elem_codigo !== null &&
        evento.elem_codigo !== ''
      ) {
        const elemento = this.elementos.find(
          (e) => parseInt(e.elem_codigo) === parseInt(evento.elem_codigo)
        )
        if (!elemento) {
          getElementoByCode(evento.elem_codigo)
            .then((response) => {
              if (response.status === 200) {
                this.elementos = []
                var elemento = response.data
                this.elementos.unshift(elemento)
              } else {
                this.$notify({
                  title: 'Atención',
                  message:
                    'No se encontró Material con ese código: (' +
                    response.status +
                    ')',
                  type: 'warning'
                })
              }
            })
            .catch((error) => {
              this.$notify({
                title: 'Atención',
                message:
                  'No se encontró Material con ese código: (' + error + ')',
                type: 'warning'
              })
            })
        } else {
          this.elementos = []
          this.elementos.unshift(elemento)
        }
      }
    },
    validarCodigoElementoRetirado(elem_id, codigo) {
      if (elem_id !== null && elem_id > 0 && codigo !== null && codigo !== '') {
        validarCodigo(elem_id, codigo).then((response) => {
          const resultado = response.data
          if (resultado === '10') {
            const msg = 'Código de material ya fue retirado'
            this.alerta(msg)
          } else if (resultado === '11') {
            const msg = 'Código de material ya fue instalado y retirado'
            this.alerta(msg)
          }
        })
      }
    },
    validarCodigoElementoInstalado(elem_id, codigo) {
      if (elem_id !== null && elem_id > 0 && codigo !== null && codigo !== '') {
        validarCodigo(elem_id, codigo).then((response) => {
          const resultado = response.data
          if (resultado === '10') {
            const msg = 'Código de material ya fue retirado'
            this.alerta(msg)
          } else if (resultado === '11') {
            const msg = 'Código de material ya fue instalado y retirado'
            this.alerta(msg)
          } else if (resultado === '01') {
            const msg = 'Código de material ya fue instalado'
            this.alerta(msg)
          }
        })
      }
    },
    alerta(msg) {
      this.$alert(msg, 'Atención', {
        confirmButtonText: 'Continuar'
      })
    },
    aplicar() {
      var valido = true
      this.confirmacionGuardar = false
      var validacion = true
      // Validar si el reporte tiene la orden de trabajo asignada
      if (
        this.reporte.adicional.ortr_id === undefined ||
        this.reporte.adicional.ortr_id === null ||
        this.reporte.adicional.ortr_id < 1
      ) {
        this.$notify.warning({
          title: 'Atención',
          message:
            'Reporte ' +
            this.reporte.repo_consecutivo +
            ' No Asignado a Orden de Trabajo, Por favor verifique la información',
          offset: 100
        })
        return false
      }
      // Mover subreportes a repo_subrepoconsecutivos
      this.reporte.repo_subrepoconsecutivo = ''
      this.reporte.subreportes.forEach((i) => {
        this.reporte.repo_subrepoconsecutivo =
          this.reporte.repo_subrepoconsecutivo + i.csc + ','
      })
      this.reporte.repo_subrepoconsecutivo =
        this.reporte.repo_subrepoconsecutivo.slice(0, -1)
      // Mover material a reporte.eventos
      // // var even_length = 1
      this.reporte.eventos = []
      this.reporte.direcciones.forEach((d) => {
        d.materiales.forEach((m) => {
          // // m.even_id = even_length
          if (
            m.aap_id !== undefined &&
            m.aap_id !== null &&
            m.elem_id !== undefined &&
            m.elem_id !== null
          ) {
            this.reporte.eventos.push(m)
          }
          // // even_length++
        })
      })
      // Validar cada direccion dato por todos sus valores requeridos
      // const dirForm = 'dirform_' + this.reporte.direcciones.length
      // valido = valido && this.$refs[dirForm].validate()
      this.reporte.direcciones.forEach((d) => {
        if (
          d.aap_id !== null &&
          this.reporte.reti_id !== 0 &&
          d.even_estado < 8
        ) {
          // Validar Información
          const dt = d.dato
          if (
            dt.aatc_id === null ||
            dt.aama_id === null ||
            dt.aamo_id === null ||
            dt.aaco_id === null ||
            dt.aap_potencia === null ||
            dt.aap_tecnologia === null ||
            dt.aap_brazo === null ||
            dt.aap_collarin === null ||
            dt.tipo_id === null ||
            dt.aap_poste_altura === null ||
            dt.aap_poste_propietario === null
          ) {
            validacion = false
          }
          // Validar estado de la luminaria y tipo de reporte
          var aap_no_en_retiro = []
          var aap_no_nueva = []
          if (this.reporte.reti_id === 3 || this.reporte.reti_id === 7) {
            if (dt.aaco_id_anterior !== 3) {
              aap_no_en_retiro.push(d.aap_id)
              this.$notify.error({
                title: 'Luminaria No Retirada',
                message: 'Verifique la luminaria: ' + d.aap_id,
                offset: 0
              })
            }
            // validar fecha del reporte de retiro
            /*
            const aapData = this.luminaria_reportes.get(d.aap_id)
            const reports = aapData.reports
            const l_fecha_reporte_retiro = reports[this.reportIndex(reports, 8)]._2
            console.log('fecha solucion:', this.reporte.repo_fechasolucion)
            if (this.reporte.repo_fechasolucion === undefined || this.reporte.repo_fechasolucion.is_null) {
              validacion = false
              this.$alert('Por favor ingrese la fecha de solución', 'Atención', {
                callback: action => { this.$refs.i_repo_fechasolucion.focus() }
              })
            }
            const d_fecha_reporte = new Date(this.reporte.repo_fechasolucion)
            const d_now = new Date()
            d_fecha_reporte.setHours(d_now.getHours())
            d_fecha_reporte.setMinutes(d_now.getMinutes())
            d_fecha_reporte.setSeconds(d_now.getSeconds())
            if (d_fecha_reporte.getTime() < l_fecha_reporte_retiro) {
              validacion = false
              this.$alert('La fecha de solución de la reubicación es previa a la fecha del reporte de retiro. Por favor verifique la fecha de solución', 'Atención', {
                callback: action => { this.$refs.i_repo_fechasolucion.focus() }
              })
            }
            */
          }

          if (
            this.reporte.reti_id === 2 &&
            this.reporte.adicional.repo_tipo_expansion === 3
          ) {
            if (d.esnueva === false && d.even_estado === 1) {
              aap_no_nueva.push(d.aap)
              this.$notify.error({
                title: 'Luminaria Ya Existe',
                message: 'Verifique el código de la luminaria: ' + d.aap_id,
                offset: 0
              })
            }
          }

          if (aap_no_en_retiro.length > 0) {
            validacion = false
          }
          // validar luminaria ya retirada
          if (this.reporte.reti_id === 8) {
            if (dt.aaco_id_anterior === 3) {
              validacion = false
              this.$notify.error({
                title: 'Luminaria Ya Está Retirada',
                message: 'Verifique la luminaria: ' + d.aap_id,
                offset: 0
              })
            }
          }
        }
      })
      //
      const start = async () => {
        validacion = true
        valido = validacion
        if (!valido) {
          this.$notify.info({
            title: 'Atención',
            message: 'Por favor verifique la información',
            offset: 100
          })
          return false
        }
        for (var i = 0; i < this.reporte.eventos.length; i++) {
          if (this.reporte.eventos[i].elem_id === '') {
            this.reporte.eventos[i].elem_id = null
          }
        }
        this.reporte.rees_id = 3
        const data = {
          reporte: this.reporte,
          coau_tipo: this.coau_tipo,
          coau_codigo: this.autorizacion
        }
        updateReporteElemento(data)
          .then((response) => {
            if (response.status === 200) {
              this.success()
            } else {
              this.reporte.rees_id = 2
              this.error(
                'Se presentó un inconveniente al guardar los cambios, por favor reintente'
              )
            }
          })
          .catch((error) => {
            this.reporte.rees_id = 2
            this.error(error)
          })
      }
      start()
    },
    async asyncForEach(array, callback) {
      for (let index = 0; index < array.length; index++) {
        await callback(array[index], index, array)
      }
    },
    async validatForm(form) {
      console.log('Form Name: ' + form)
      if (this.$refs[form] !== undefined) {
        if (form.includes('dirform')) {
          const name = form.split('_')
          const index = name[1] - 1
          if (
            this.reporte.direcciones[index].even_estado <= 8 &&
            this.reporte.direcciones[index].aap_id > 0
          ) {
            var valido = new Promise((resolve, reject) => {
              this.$refs[form].validate((valid) => {
                console.log(form + ' validation :' + valid)
                resolve(valid)
              })
            })
            return valido
          }
        } else if (form.includes('matform')) {
          const name = form.split('_')
          const index = name[1] - 1
          if (
            this.reporte.eventos[index].even_estado <= 8 &&
            this.reporte.eventos[index].aap_id > 0
          ) {
            valido = new Promise((resolve, reject) => {
              this.$refs[form][0].validate((valid) => {
                console.log(form + ' validation :' + valid)
                resolve(valid)
              })
            })
            return valido
          }
        } else {
          valido = new Promise((resolve, reject) => {
            this.$refs[form].validate((valid) => {
              console.log(form + ' validation :' + valid)
              resolve(valid)
            })
          })
          return valido
        }
      }
      return true
    },
    imprimir() {},
    success() {
      this.$notify({
        title: this.$i18n.t('reporte.success'),
        message:
          this.$i18n.t('reporte.updated') + ' ' + this.reporte.repo_consecutivo,
        type: 'success'
      })
      this.$timer.stop('autosave')
      this.$timer.stop('pending')
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('reporte.error'),
        message: this.$i18n.t('reporte.notupdated') + ' ' + e
      })
    },
    open7() {
      this.$message({
        showClose: true,
        message: 'Atención, existe información por guardar en el reporte.',
        type: 'warning',
        duration: 5000
      })
    },
    onAddNovedad() {
      this.novedad_siguiente_consecutivo =
        this.novedad_siguiente_consecutivo + 1
      var novedad = {
        nove_id: null,
        reno_horaini: null,
        reno_horafin: null,
        reno_observacion: null,
        even_id: this.novedad_siguiente_consecutivo,
        even_estado: 1
      }
      this.reporte.novedades.push(novedad)
    },
    onAddEvent(cantidad = 0) {
      if (cantidad === 0) {
        cantidad = this.addinputevent
      }
      for (var i = 1; i <= cantidad; i++) {
        var evento = {
          even_fecha: null,
          even_codigo_instalado: null,
          even_codigo_retirado: null,
          even_cantidad_instalado: 1.0,
          even_cantidad_retirado: 1.0,
          even_estado: 1,
          aap_id: this.reporte.direcciones[this.didx].aap_id,
          repo_id: this.reporte.repo_id,
          elem_id: null,
          elem_codigo: null,
          empr_id: 0,
          usua_id: 0,
          even_id: this.evento_siguiente_consecutivo,
          unit_id: null,
          even_valido: {
            aap_id: true,
            codigo_retirado: true,
            cantidad_retirado: true,
            codigo_instalado: true,
            cantidad_instalado: true
          }
        }
        this.reporte.direcciones[this.didx].materiales.push(evento)
        this.evento_siguiente_consecutivo =
          this.evento_siguiente_consecutivo + 1
      }
    },
    onAddAddress(l) {
      if (l) {
        const inputValue = parseInt(l)
        var direccion = {
          repo_id: this.reporte.repo_id,
          aap_id: parseInt(inputValue),
          even_id: this.direccion_siguiente_consecutivo,
          even_direccion: null,
          barr_id: null,
          even_direccion_anterior: null,
          barr_id_anterior: null,
          even_estado: 1,
          tire_id: null,
          type: 'info',
          even_valido: {
            aap_id: true,
            aap_direccion: true,
            barr_id: true,
            aatc_id: true,
            aama_id: true,
            aamo_id: true,
            aaco_id: true,
            aap_potencia: true,
            aap_tecnologia: true,
            aap_brazo: true,
            aap_collarin: true,
            tipo_id: true,
            aap_poste_altura: true,
            aap_poste_propietario: true
          },
          dato: {
            aatc_id: null,
            aatc_id_anterior: null,
            aama_id: null,
            aama_id_anterior: null,
            aamo_id: null,
            aamo_id_anterior: null,
            aaco_id: null,
            aaco_id_anterior: null,
            aap_potencia: null,
            aap_potencia_anterior: null,
            aap_tecnologia: null,
            aap_tecnologia_anterior: null,
            aap_brazo: null,
            aap_brazo_anterior: null,
            aap_collarin: null,
            aap_collarin_anterior: null,
            tipo_id: null,
            tipo_id_anterior: null,
            aap_poste_altura: null,
            aap_poste_altura_anterior: null,
            aap_poste_propietario: null,
            aap_poste_propietario_anterior: null
          },
          dato_adicional: {
            aacu_id_anterior: null,
            aacu_id: null,
            aaus_id_anterior: null,
            aaus_id: null,
            medi_id_anterior: null,
            medi_id: null,
            tran_id_anterior: null,
            tran_id: null,
            aap_apoyo_anterior: null,
            aap_apoyo: null,
            aap_lat_anterior: null,
            aap_lat: null,
            aap_lng_anterior: null,
            aap_lng: null
          },
          materiales: [],
          esnueva: null,
          codigoautorizacion: null,
          aap_fechatoma: null,
          idx: this.idx
        }
        this.reporte.direcciones.push(direccion)
        this.validateAap(direccion, direccion.even_id - 1)
        this.handleTag(direccion.idx)
        this.onAddEvent(10)
        this.direccion_siguiente_consecutivo =
          this.direccion_siguiente_consecutivo + 1
        this.idx++
      }
      this.inputVisible01 = false
      this.inputVisible02 = false
      this.inputValue01 = null
      this.inputValue02 = null
    },
    limpiarAndBack() {
      this.obtenerReporte()
    },
    elemento(elem_id) {
      if (elem_id === null) {
        return ''
      } else {
        const elemento = this.elementos_list.find(
          (o) => o.elem_id === elem_id,
          { elem_descripcion: null }
        )
        return elemento.elem_descripcion
      }
    },
    elemento_codigo(elem_id) {
      if (elem_id === null) {
        return ''
      } else {
        const elemento = this.elementos_list.find(
          (o) => o.elem_id === elem_id,
          { elem_codigo: null }
        )
        return elemento.elem_codigo
      }
    },
    unitario_s(elem_id) {
      if (elem_id === null) {
        return ''
      } else {
        const elemento = this.elementos_list.find(
          (o) => o.elem_id === elem_id,
          { unitarios: this.unitarios[elem_id] }
        )
        return elemento.unitarios
      }
    },
    getElementos() {
      return this.elementos_list
    },
    reporte_tipo(reti_id) {
      if (reti_id === null) {
        return ''
      } else {
        return this.tipos.find((o) => o.reti_id === reti_id, {
          reti_descripcion: 'INDEFINIDO'
        }).reti_descripcion
      }
    },
    estado() {
      if (this.reporte && this.reporte.rees_id !== null) {
        var rees_id = this.reporte.rees_id
        if (rees_id === null || rees_id === undefined) {
          return ''
        } else {
          if (this.estados && this.estados.length > 0) {
            return this.estados.find((o) => o.rees_id === rees_id, {
              rees_descripcion: 'INDEFINIDO'
            }).rees_descripcion
          } else {
            return 'INDEFINIDO'
          }
        }
      }
    },
    accion(acci_id) {
      if (acci_id === null) {
        return ''
      } else {
        return this.acciones.find((o) => o.acci_id === acci_id, {
          acci_descripcion: null
        }).acci_descripcion
      }
    },
    abrirReporte() {
      this.$prompt(
        'Por favor ingrese el código de autorización si lo tiene:',
        'Confirmación',
        {
          confirmButtonText: 'Confirmar',
          cancelButtonText: 'Cancelar'
        }
      ).then(({ value }) => {
        validar(3, value)
          .then((response) => {
            if (response.data === true) {
              this.autorizacion = value
              this.coau_tipo = 3
              this.$message({
                type: 'success',
                message: 'El código es válido, puede continuar',
                duration: 5000
              })
              this.reporte.rees_id = 2
              this.$timer.start('autosave')
              this.$timer.start('pending')
            } else {
              this.$alert(
                'El código ingresado no es válido, por favor confirmelo',
                'Error',
                {
                  confirmButtonText: 'Cerrar'
                }
              )
            }
          })
          .catch((error) => {
            this.$message({
              type: 'error',
              message: 'Se presentó error al válidar el código (' + error + ')',
              duration: 5000
            })
          })
      })
    },
    obtenerReporte() {
      getReporte(this.$route.params.id).then((response) => {
        this.reporte_previo = response.data
        if (this.reporte_previo.repo_subrepoconsecutivo === undefined) {
          this.reporte_previo.repo_subrepoconsecutivo = null
        }
        if (this.reporte_previo.repo_subrepoconsecutivo === null) {
          this.reporte_previo.subreportes = []
        } else {
          this.reporte_previo.subreportes = []
          var subreportes =
            this.reporte_previo.repo_subrepoconsecutivo.split(',')
          subreportes.forEach((i) => {
            this.reporte_previo.subreportes.push({ csc: i })
          })
        }
        if (this.reporte_previo.rees_id === 1) {
          validarReporteDiligenciado(
            this.reporte_previo.reti_id,
            this.reporte_previo.repo_consecutivo
          )
            .then((resp) => {
              if (resp.data[0] === true) {
                this.invalid = false
                this.inicioReporte(this.reporte_previo)
              } else {
                this.$prompt(
                  'Por favor ingrese el código de autorización si lo tiene:',
                  'Primero debe diligenciar el(los) reporte(s) Tipo ' +
                    this.reporte_tipo(this.reporte_previo.reti_id) +
                    ' No(s).' +
                    resp.data[1],
                  'Atención',
                  {
                    confirmButtonText: 'Confirmar',
                    cancelButtonText: 'Cancelar'
                  }
                )
                  .then(({ value }) => {
                    validar(2, value)
                      .then((response) => {
                        if (response.data === true) {
                          this.invalid = false
                          this.coau_tipo = 2
                          this.autorizacion = value
                          this.$message({
                            type: 'success',
                            message: 'El código es válido, puede continuar',
                            duration: 5000
                          })
                          this.inicioReporte()
                        } else {
                          this.$alert(
                            'El código ingresado no es válido, por favor confirmelo',
                            'Error',
                            {
                              confirmButtonText: 'Cerrar'
                            }
                          )
                          this.invalid = true
                        }
                      })
                      .catch((error) => {
                        this.$message({
                          type: 'error',
                          message:
                            'Se presentó error al válidar el código (' +
                            error +
                            ')',
                          duration: 5000
                        })
                        this.invalid = true
                      })
                  })
                  .catch(() => {
                    this.$message({
                      type: 'info',
                      message: 'Cancelado',
                      duration: 5000
                    })
                    this.invalid = true
                  })
              }
            })
            .catch((error) => {
              this.invalid = true
              this.$alert(
                'No se pudo validar el estado del reporte anterior. Error: ' +
                  error,
                'Error',
                {
                  confirmButtonText: 'Cerrar'
                }
              )
            })
        } else {
          this.inicioReporte(this.reporte_previo)
        }
      })
    },
    inicioReporte(reporte) {
      if (this.reporte_previo.adicional === null) {
        this.adicional = {
          repo_id: this.reporte_previo.repo_id,
          repo_fechadigitacion: new Date(),
          repo_modificado: new Date(),
          repo_tipo_expansion: null,
          repo_luminaria: null,
          repo_redes: null,
          repo_poste: null,
          repo_subreporte: null,
          repo_subid: null,
          acti_id: null,
          repo_codigo: null,
          repo_apoyo: null,
          urba_id: null
        }
      } else {
        this.reporte_previo.adicional.repo_modificado = new Date()
        this.reporte_previo.adicional.repo_fechadigitacion = new Date()
      }
      this.reporte_previo.direcciones.forEach((d) => {
        d.even_valido = {
          aap_id: true,
          aap_direccion: true,
          barr_id: true,
          aatc_id: true,
          aama_id: true,
          aamo_id: true,
          aaco_id: true,
          aap_potencia: true,
          aap_tecnologia: true,
          aap_brazo: true,
          aap_collarin: true,
          tipo_id: true,
          aap_poste_altura: true,
          aap_poste_propietario: true
        }
        d.materiales = []
        d.esnueva = false
        d.codigoautorizacion = null
        d.aap_fechatoma = null
      })
      if (
        this.reporte_previo.reti_id === 2 ||
        this.reporte_previo.reti_id === 3 ||
        this.reporte_previo.reti_id === 4 ||
        this.reporte_previo.reti_id === 5 ||
        this.reporte_previo.reti_id === 6 ||
        this.reporte_previo.reti_id === 7 ||
        this.reporte_previo.reti_id === 8
      ) {
        this.conDirecciones = true
      } else {
        this.conDirecciones = false
      }
      this.cargarEventos()
      this.validarConsecutivo()
      this.reporte = this.reporte_previo
      this.$timer.start('autosave')
      this.$timer.start('pending')
      this.validarTipo()
      this.pending()
      this.didx = 0
      if (this.reporte_previo.rees_id !== 3) {
        localStorage.setItem(
          'currEditRepFecha',
          JSON.stringify({ fecha: Date.now(), data: this.reporte })
        )
      }
      if (this.reporte.reti_id === 8) {
        this.conexiones.splice(0, 2)
      } else {
        this.conexiones.splice(2, 1)
      }
      this.repo_fecharecepcion = this.reporte.repo_fecharecepcion
      this.repo_direccion = this.reporte.repo_direccion
      this.repo_nombre = this.reporte.repo_nombre
      this.repo_telefono = this.reporte.repo_telefono
      this.repo_codigo = this.reporte.adicional.repo_codigo
      this.repo_apoyo = this.reporte.adicional.repo_apoyo
      this.repo_descripcion = this.reporte.repo_descripcion
      this.orig_id = this.reporte.orig_id
      this.acti_id = this.reporte.acti_id
      this.tiba_id = this.reporte.tiba_id
      this.barr_id = this.reporte.barr_id
      if (this.reporte.adicional.ortr_id === null) {
        this.ortr_id_state = true
      }
    },
    validarConsecutivo() {
      // var consecutivo = 1
      for (var i = 0; i < this.reporte_previo.eventos.length; i++) {
        if (
          this.reporte_previo.eventos[i].elem_id !== undefined &&
          this.reporte_previo.eventos[i].elem_id > 0
        ) {
          if (
            this.elementos.find(
              (e) => e.elem_id === this.reporte_previo.eventos[i].elem_id
            ) === undefined
          ) {
            this.elementos.push({
              elem_id: this.reporte_previo.eventos[i].elem_id,
              elem_descripcion: this.elemento(
                this.reporte_previo.eventos[i].elem_id
              )
            })
          }
        }
        // consecutivo++
      }
    },
    cargarEventos() {
      // validar si existe un reporte previo
      var stringReporteAnterior = null
      if (this.reporte_previo.rees_id < 3) {
        stringReporteAnterior = localStorage.getItem('currEditRepFecha')
      }
      if (
        stringReporteAnterior !== undefined &&
        stringReporteAnterior !== null &&
        stringReporteAnterior !== ''
      ) {
        const fecha = JSON.parse(stringReporteAnterior).fecha
        const diferencia = (Date.now() - fecha) / 1000
        if (diferencia < 43200) {
          var currEditRepFecha = JSON.parse(stringReporteAnterior).data
          if (currEditRepFecha.repo_id === this.reporte_previo.repo_id) {
            this.reporte_previo = currEditRepFecha
            this.reporte_previo.adicional.repo_fechadigitacion = new Date()
            this.reporte_previo.adicional.repo_modificado = new Date()
            this.reporte_previo.eventos = []
            this.reporte_previo.direcciones.forEach((d) => {
              d.materiales.forEach((m) => {
                if (m.unit_id === undefined) {
                  m.unit_id = null
                }
                this.reporte_previo.eventos.push(m)
              })
            })
          }
        }
      }
      var even_length = 0
      var dire_length = 0
      var nove_length = 0
      this.reporte_previo.novedades.forEach((n) => {
        if (n.even_id > nove_length) {
          nove_length = n.even_id
        }
      })
      this.reporte_previo.eventos.forEach((e) => {
        if (e.even_id > even_length) {
          even_length = e.even_id
        }
      })
      this.reporte_previo.eventos.forEach((e) => {
        if (e.even_id === undefined || e.even_id === null || e.even_id < 1) {
          console.log(
            'renumerando valor de e.even_id a even_length + 1:' + even_length
          )
          e.even_id = even_length + 1
          even_length = even_length + 1
        }
      })
      if (this.reporte_previo.direcciones.length === 0) {
        this.idx = 1
        if (this.reporte_previo.eventos.length > 0) {
          var aap_id = ''
          this.reporte_previo.eventos.forEach((e) => {
            if (e.aap_id !== aap_id) {
              var direccion = {
                repo_id: this.reporte_previo.repo_id,
                aap_id: e.aap_id,
                even_id: dire_length + 1,
                even_direccion: null,
                barr_id: null,
                even_direccion_anterior: null,
                barr_id_anterior: null,
                even_estado: 1,
                tire_id: null,
                type: 'info',
                even_valido: {
                  aap_id: true,
                  aap_direccion: true,
                  barr_id: true,
                  aatc_id: true,
                  aama_id: true,
                  aamo_id: true,
                  aaco_id: true,
                  aap_potencia: true,
                  aap_tecnologia: true,
                  aap_brazo: true,
                  aap_collarin: true,
                  tipo_id: true,
                  aap_poste_altura: true,
                  aap_poste_propietario: true
                },
                dato: {
                  aatc_id: null,
                  aatc_id_anterior: null,
                  aama_id: null,
                  aama_id_anterior: null,
                  aamo_id: null,
                  aamo_id_anterior: null,
                  aaco_id: null,
                  aaco_id_anterior: null,
                  aap_potencia: null,
                  aap_potencia_anterior: null,
                  aap_tecnologia: null,
                  aap_tecnologia_anterior: null,
                  aap_brazo: null,
                  aap_brazo_anterior: null,
                  aap_collarin: null,
                  aap_collarin_anterior: null,
                  tipo_id: null,
                  tipo_id_anterior: null,
                  aap_poste_altura: null,
                  aap_poste_altura_anterior: null,
                  aap_poste_propietario: null,
                  aap_poste_propietario_anterior: null
                },
                dato_adicional: {
                  aacu_id_anterior: null,
                  aacu_id: null,
                  aaus_id_anterior: null,
                  aaus_id: null,
                  medi_id_anterior: null,
                  medi_id: null,
                  tran_id_anterior: null,
                  tran_id: null,
                  aap_apoyo_anterior: null,
                  aap_apoyo: null,
                  aap_lat_anterior: null,
                  aap_lat: null,
                  aap_lng_anterior: null,
                  aap_lng: null
                },
                materiales: [],
                codigoautorizacion: null,
                aap_fechatoma: null,
                idx: this.idx
              }

              // materiales: this.reporte_previo.eventos.filter(m => m.aap_id === e.aap_id)
              var eventos = this.reporte_previo.eventos.filter(
                (m) => m.aap_id === e.aap_id
              )
              eventos.forEach((e) => {
                var evento = {
                  even_fecha: e.even_fecha,
                  even_codigo_instalado:
                    e.even_codigo_instalado === undefined
                      ? null
                      : e.even_codigo_instalado,
                  even_codigo_retirado:
                    e.even_codigo_retirado === undefined
                      ? null
                      : e.even_codigo_retirado,
                  even_cantidad_instalado: e.even_cantidad_instalado,
                  even_cantidad_retirado: e.even_cantidad_retirado,
                  even_estado: e.even_estado,
                  aap_id: e.aap_id,
                  repo_id: e.repo_id,
                  elem_id: e.elem_id,
                  elem_codigo: e.elem_codigo,
                  empr_id: e.empr_id,
                  usua_id: e.usua_id,
                  even_id: e.even_id,
                  unit_id: e.unit_id,
                  even_valido: {
                    aap_id: true,
                    codigo_retirado: true,
                    cantidad_retirado: true,
                    codigo_instalado: true,
                    cantidad_instalado: true
                  }
                }
                this.unitarios[e.elem_id] = this.unitario_s(e.elem_id)
                direccion.materiales.push(evento)
              })
              this.reporte_previo.direcciones.push(direccion)
              this.idx++
              dire_length++
              aap_id = e.aap_id
            }
          })
        } else {
          this.idx = 1
          for (var i = 1; i <= 4; i++) {
            var direccion = {
              repo_id: this.reporte_previo.repo_id,
              aap_id: null,
              even_id: dire_length + 1,
              even_direccion: null,
              barr_id: null,
              even_direccion_anterior: null,
              barr_id_anterior: null,
              even_estado: 1,
              tire_id: null,
              type: 'info',
              even_valido: {
                aap_id: true,
                aap_direccion: true,
                barr_id: true,
                aatc_id: true,
                aama_id: true,
                aamo_id: true,
                aaco_id: true,
                aap_potencia: true,
                aap_tecnologia: true,
                aap_brazo: true,
                aap_collarin: true,
                tipo_id: true,
                aap_poste_altura: true,
                aap_poste_propietario: true
              },
              dato: {
                aatc_id: null,
                aatc_id_anterior: null,
                aama_id: null,
                aama_id_anterior: null,
                aamo_id: null,
                aamo_id_anterior: null,
                aaco_id: null,
                aaco_id_anterior: null,
                aap_potencia: null,
                aap_potencia_anterior: null,
                aap_tecnologia: null,
                aap_tecnologia_anterior: null,
                aap_brazo: null,
                aap_brazo_anterior: null,
                aap_collarin: null,
                aap_collarin_anterior: null,
                tipo_id: null,
                tipo_id_anterior: null,
                aap_poste_altura: null,
                aap_poste_altura_anterior: null,
                aap_poste_propietario: null,
                aap_poste_propietario_anterior: null
              },
              dato_adicional: {
                aacu_id_anterior: null,
                aacu_id: null,
                aaus_id_anterior: null,
                aaus_id: null,
                medi_id_anterior: null,
                medi_id: null,
                tran_id_anterior: null,
                tran_id: null,
                aap_apoyo_anterior: null,
                aap_apoyo: null,
                aap_lat_anterior: null,
                aap_lat: null,
                aap_lng_anterior: null,
                aap_lng: null
              },
              materiales: [],
              codigoautorizacion: null,
              aap_fechatoma: null,
              idx: this.idx
            }
            this.reporte_previo.direcciones.push(direccion)
            dire_length++
            this.idx++
          }
        }
      }
      this.idx = 1
      this.reporte_previo.direcciones.forEach((d) => {
        if (d.even_id > dire_length) {
          dire_length = d.even_id
        }
        if (d.materiales === undefined) {
          console.log('Se crea materiales vacio para aap_id: ' + d.aap_id)
          d.materiales = []
        }
        if (d.materiales.length === 0) {
          console.log(
            'Se adiciona materiales a direccion desde los eventos: ' + d.aap_id
          )
          var eventos = this.reporte_previo.eventos.filter(
            (e) => e.aap_id === d.aap_id
          )
          eventos.forEach((e) => {
            var evento = {
              even_fecha: e.even_fecha,
              even_codigo_instalado:
                e.even_codigo_instalado === undefined
                  ? null
                  : e.even_codigo_instalado,
              even_codigo_retirado:
                e.even_codigo_retirado === undefined
                  ? null
                  : e.even_codigo_retirado,
              even_cantidad_instalado: e.even_cantidad_instalado,
              even_cantidad_retirado: e.even_cantidad_retirado,
              even_estado: e.even_estado,
              aap_id: e.aap_id,
              repo_id: e.repo_id,
              elem_id: e.elem_id,
              empr_id: e.empr_id,
              usua_id: e.usua_id,
              even_id: e.even_id,
              even_valido: {
                aap_id: true,
                codigo_retirado: true,
                cantidad_retirado: true,
                codigo_instalado: true,
                cantidad_instalado: true
              },
              unit_id: e.unit_id,
              elem_codigo: this.elemento_codigo(e.elem_id)
            }
            d.materiales.push(evento)
          })
        }
        d.idx = this.idx
        this.idx++
      })
      this.reporte_previo.direcciones.forEach((d) => {
        if (d.even_id === 1) {
          d.type = 'success'
        } else {
          d.type = 'info'
        }
        if (d.even_id === undefined || d.even_id === null || d.even_id < 1) {
          d.even_id = dire_length + 1
          dire_length = dire_length + 1
        }
        if (d.materiales.length === 0) {
          for (var i = 1; i <= 10; i++) {
            var evento = {
              even_fecha: null,
              even_codigo_instalado: null,
              even_codigo_retirado: null,
              even_cantidad_instalado: 1.0,
              even_cantidad_retirado: 1.0,
              even_estado: 1,
              aap_id: d.aap_id,
              repo_id: this.reporte_previo.repo_id,
              elem_id: null,
              elem_codigo: null,
              empr_id: 0,
              usua_id: 0,
              even_id: even_length + i,
              unit_id: null,
              even_valido: {
                aap_id: true,
                codigo_retirado: true,
                cantidad_retirado: true,
                codigo_instalado: true,
                cantidad_instalado: true
              }
            }
            console.log('agregando material vacio a direccion: ' + d.even_id)
            d.materiales.push(evento)
          }
          even_length += 10
        }
      })
      this.evento_siguiente_consecutivo = even_length + 1
      this.direccion_siguiente_consecutivo = dire_length + 1
      this.novedad_siguiente_consecutivo = nove_length
      this.minDate = this.reporte_previo.repo_fecharecepcion
      var temp = JSON.stringify(this.reporte_previo)
      this.reporte_previo = JSON.parse(temp)
    },
    remoteMethodElemento(query) {
      if (query !== '') {
        getElementoByDescripcion(query).then((response) => {
          this.elementos = response.data
          // this.completarMaterial()
        })
      } else {
        this.elementos = []
      }
    },
    async completarMaterial() {
      console.info('DEBUG: En completarMaterial')
      for (var j = 0; j < this.reporte.direcciones.length; j++) {
        if (this.reporte.direcciones[j].materiales !== undefined) {
          for (
            var i = 0;
            i < this.reporte.direcciones[j].materiales.length;
            i++
          ) {
            if (
              this.reporte.direcciones[j].materiales[i] !== undefined &&
              this.reporte.direcciones[j].materiales[i].elem_id !== undefined &&
              this.reporte.direcciones[j].materiales[i].elem_id > 0
            ) {
              if (
                this.elementos.find(
                  (e) =>
                    e.elem_id ===
                    this.reporte.direcciones[j].materiales[i].elem_id
                ) === undefined
              ) {
                console.info(
                  'DEBUG: Agregando elemento a la lista:',
                  this.reporte.direcciones[j].materiales[i].elem_id
                )
                this.elementos.push({
                  elem_id: this.reporte.direcciones[j].materiales[i].elem_id,
                  elem_descripcion: this.elemento(
                    this.reporte.direcciones[j].materiales[i].elem_id
                  ),
                  unitarios: this.unitario_s(
                    this.reporte.direcciones[j].materiales[i].elem_id
                  )
                })
              }
            }
          }
        }
      }
      console.info('DEBUG: Saliendo de completarMaterial')
    }
  },
  beforeMount() {
    getOrigenes()
      .then((response) => {
        this.origenes = response.data
        getBarriosEmpresa()
          .then((response) => {
            this.barrios = response.data
            this.barrios_lista = response.data
            getActividades()
              .then((response) => {
                this.actividades = response.data
                getAcciones()
                  .then((response) => {
                    this.acciones = response.data
                    getMedioambiente()
                      .then((response) => {
                        this.medioambiente = response.data
                        this.medioambiente.forEach((o) => {
                          this.medioambiente_keys.push(o.meam_id)
                        })
                        getEstados()
                          .then((response) => {
                            this.estados = response.data
                            getTipos()
                              .then((response) => {
                                this.tipos = response.data
                                this.tipos_lista = response.data
                                getTiposBarrio()
                                  .then((response) => {
                                    this.tiposbarrio = response.data
                                    getElementos()
                                      .then((response) => {
                                        this.elementos_list = response.data
                                        getAapTiposCarcasa()
                                          .then((response) => {
                                            this.carcasas = response.data
                                            getAapMarcas()
                                              .then((response) => {
                                                this.marcas = response.data
                                                getAapModelos()
                                                  .then((response) => {
                                                    this.modelos = response.data
                                                    getCaracteristica(7)
                                                      .then((response) => {
                                                        this.tecnologias =
                                                          response.data.cara_valores.split(
                                                            ','
                                                          )
                                                        getCaracteristica(5)
                                                          .then((response) => {
                                                            this.potencias =
                                                              response.data.cara_valores.split(
                                                                ','
                                                              )
                                                            getCaracteristica(8)
                                                              .then(
                                                                (response) => {
                                                                  const poste =
                                                                    response.data.cara_valores.split(
                                                                      ','
                                                                    )
                                                                  for (
                                                                    var i = 0;
                                                                    i <
                                                                    poste.length;
                                                                    i++
                                                                  ) {
                                                                    this.postes.push(
                                                                      {
                                                                        tipo_id:
                                                                          i + 1,
                                                                        tipo_descripcion:
                                                                          poste[
                                                                            i
                                                                          ]
                                                                      }
                                                                    )
                                                                  }
                                                                  getCaracteristica(
                                                                    9
                                                                  )
                                                                    .then(
                                                                      (
                                                                        response
                                                                      ) => {
                                                                        this.owns =
                                                                          response.data.cara_valores.split(
                                                                            ','
                                                                          )
                                                                        getAapConexiones()
                                                                          .then(
                                                                            (
                                                                              response
                                                                            ) => {
                                                                              this.conexiones =
                                                                                response.data
                                                                              getAapUsos()
                                                                                .then(
                                                                                  (
                                                                                    response
                                                                                  ) => {
                                                                                    this.aap_usos =
                                                                                      response.data
                                                                                    getAapCuentasAp()
                                                                                      .then(
                                                                                        (
                                                                                          response
                                                                                        ) => {
                                                                                          this.aap_cuentasap =
                                                                                            response.data
                                                                                          getTiposRetiro()
                                                                                            .then(
                                                                                              (
                                                                                                response
                                                                                              ) => {
                                                                                                this.tiposretiro =
                                                                                                  response.data
                                                                                                getUrbanizadoraTodas().then(
                                                                                                  (
                                                                                                    response
                                                                                                  ) => {
                                                                                                    this.urbanizadoras =
                                                                                                      response.data
                                                                                                    getMedidors()
                                                                                                      .then(
                                                                                                        (
                                                                                                          response
                                                                                                        ) => {
                                                                                                          this.medidores =
                                                                                                            response.data
                                                                                                          getTransformadors()
                                                                                                            .then(
                                                                                                              (
                                                                                                                response
                                                                                                              ) => {
                                                                                                                this.transformadores =
                                                                                                                  response.data
                                                                                                                getOrdenes()
                                                                                                                  .then(
                                                                                                                    (
                                                                                                                      response
                                                                                                                    ) => {
                                                                                                                      this.ordenestrabajo =
                                                                                                                        response.data
                                                                                                                      getNovedades(
                                                                                                                        1
                                                                                                                      )
                                                                                                                        .then(
                                                                                                                          (
                                                                                                                            response
                                                                                                                          ) => {
                                                                                                                            this.novedades =
                                                                                                                              response.data
                                                                                                                            getUnitariosTodas().then(
                                                                                                                              (
                                                                                                                                response
                                                                                                                              ) => {
                                                                                                                                this.unitario_lista =
                                                                                                                                  response.data
                                                                                                                                this.obtenerReporte()
                                                                                                                              }
                                                                                                                            )
                                                                                                                          }
                                                                                                                        )
                                                                                                                        .catch(
                                                                                                                          (
                                                                                                                            error
                                                                                                                          ) => {
                                                                                                                            console.log(
                                                                                                                              'getNovedades:' +
                                                                                                                                error
                                                                                                                            )
                                                                                                                          }
                                                                                                                        )
                                                                                                                    }
                                                                                                                  )
                                                                                                                  .catch(
                                                                                                                    (
                                                                                                                      error
                                                                                                                    ) => {
                                                                                                                      console.log(
                                                                                                                        'Error Ordenes Trabajo: ' +
                                                                                                                          error
                                                                                                                      )
                                                                                                                    }
                                                                                                                  )
                                                                                                              }
                                                                                                            )
                                                                                                            .catch(
                                                                                                              (
                                                                                                                error
                                                                                                              ) => {
                                                                                                                console.log(
                                                                                                                  'Error Transformadores: ' +
                                                                                                                    error
                                                                                                                )
                                                                                                              }
                                                                                                            )
                                                                                                        }
                                                                                                      )
                                                                                                      .catch(
                                                                                                        (
                                                                                                          error
                                                                                                        ) => {
                                                                                                          console.log(
                                                                                                            'Error Medidores' +
                                                                                                              error
                                                                                                          )
                                                                                                        }
                                                                                                      )
                                                                                                  }
                                                                                                )
                                                                                              }
                                                                                            )
                                                                                            .catch(
                                                                                              (
                                                                                                error
                                                                                              ) => {
                                                                                                console.log(
                                                                                                  'get Tipos Retiro: ' +
                                                                                                    error
                                                                                                )
                                                                                              }
                                                                                            )
                                                                                        }
                                                                                      )
                                                                                      .catch(
                                                                                        (
                                                                                          error
                                                                                        ) => {
                                                                                          console.log(
                                                                                            error
                                                                                          )
                                                                                        }
                                                                                      )
                                                                                  }
                                                                                )
                                                                                .catch(
                                                                                  (
                                                                                    error
                                                                                  ) => {
                                                                                    console.log(
                                                                                      error
                                                                                    )
                                                                                  }
                                                                                )
                                                                            }
                                                                          )
                                                                          .catch(
                                                                            (
                                                                              error
                                                                            ) => {
                                                                              console.log(
                                                                                'getConexiones :' +
                                                                                  error
                                                                              )
                                                                            }
                                                                          )
                                                                      }
                                                                    )
                                                                    .catch(
                                                                      (
                                                                        error
                                                                      ) => {
                                                                        console.log(
                                                                          'Caracteristica 9: ' +
                                                                            error
                                                                        )
                                                                      }
                                                                    )
                                                                }
                                                              )
                                                              .catch(
                                                                (error) => {
                                                                  console.log(
                                                                    'Caracteristica 8: ' +
                                                                      error
                                                                  )
                                                                }
                                                              )
                                                          })
                                                          .catch((error) => {
                                                            console.log(
                                                              'Caracteristica 5: ' +
                                                                error
                                                            )
                                                          })
                                                      })
                                                      .catch((error) => {
                                                        console.log(
                                                          'getCaracteristica 7: ' +
                                                            error
                                                        )
                                                      })
                                                  })
                                                  .catch((error) => {
                                                    console.log(
                                                      'getModelos: ' + error
                                                    )
                                                  })
                                              })
                                              .catch((error) => {
                                                console.log(
                                                  'getMarcas: ' + error
                                                )
                                              })
                                          })
                                          .catch((error) => {
                                            console.log('getCarcasas: ' + error)
                                          })
                                      })
                                      .catch((error) => {
                                        console.log('getElementos: ' + error)
                                      })
                                  })
                                  .catch((error) => {
                                    console.log('getTiposBarrio: ' + error)
                                  })
                              })
                              .catch((error) => {
                                console.log('getTipos: ' + error)
                              })
                          })
                          .catch((error) => {
                            console.log('getEstados: ' + error)
                          })
                      })
                      .catch((error) => {
                        console.log('getMedioambiente: ' + error)
                      })
                  })
                  .catch((error) => {
                    console.log('getAcciones: ' + error)
                  })
              })
              .catch((error) => {
                console.log('Actividades: ' + error)
              })
          })
          .catch((error) => {
            console.log(error)
          })
      })
      .catch((error) => {
        console.log('Origenes: ' + error)
      })
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
div.el-input input {
  padding: 0 5px;
}
.el-collapse-item__header {
  font-weight: bold;
}

.input-new-address {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}

.activeClass {
  background-color: green;
  color: whitesmoke;
}
.errorClass {
  background-color: red;
  color: whitesmoke;
}
</style>
