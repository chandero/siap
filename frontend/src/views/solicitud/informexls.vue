<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.sisx')}}</span>
    </el-col>
  </el-header>
  <el-main>
  <el-form :label-position="labelPosition">
  <el-row :gutter="4">
    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
      <el-form-item :label="$t('informe.initialDate')">
        <el-date-picker v-model="fecha_inicial" format="yyyy/MM/dd"></el-date-picker>
      </el-form-item>
    </el-col>
    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
      <el-form-item :label="$t('informe.endDate')">
        <el-date-picker v-model="fecha_final" format="yyyy/MM/dd"></el-date-picker>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-button type="primary" @click="obtenerDatos()">{{ $t('informe.process')}}</el-button>
    </el-col>
  </el-row>
  </el-form>
  <el-row>
    <el-col>
  <el-table
    :data="tableData"
    width="100%"
    height="600px"
    stripe
    v-loading="loading"
  >
    <el-table-column
      :label="$t('informe.soli_radicado')"
      prop="a.soli_radicado"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soti_descripcion')"
      prop="a.soti_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_estado_descripcion')"
      prop="b.soli_estado_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_fecha')"
      prop="a.soli_fecha"
      width="100"
    >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_fecha | moment('YYYY/MM/DD HH:mm:ss') }}</span>
          </template>
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_solicitud')"
      prop="a.soli_solicitud"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_fechalimite')"
      prop="b.soli_fechalimite"
      width="100"
    >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.b.soli_fechalimite | moment('YYYY/MM/DD HH:mm:ss') }}</span>
          </template>
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_consecutivo')"
      prop="a.soli_consecutivo"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_direccion')"
      prop="a.soli_direccion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.barr_descripcion')"
      prop="a.barr_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_nombre')"
      prop="a.soli_nombre"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_fecharespuesta')"
      prop="soli_fecharespuesta"
      width="100"
    >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.b.soli_fecharespuesta | moment('YYYY/MM/DD HH:mm:ss') }}</span>
          </template>
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_informetecnico')"
      prop="a.soli_informe"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_fechaalmacen')"
      prop="b.soli_fechaalmacen"
      width="100"
    >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.b.soli_fechaalmacen | moment('YYYY/MM/DD HH:mm:ss') }}</span>
          </template>
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_codigorespuesta')"
      prop="b.soli_codigorespuesta"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_fecharte')"
      prop="b.soli_fechaalmacen"
      width="100"
    >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.b.soli_fechaalmacen | moment('YYYY/MM/DD HH:mm:ss') }}</span>
          </template>
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_numerorte')"
      prop="b.soli_numerorte"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_puntos')"
      prop="b.soli_puntos"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_luminarias')"
      prop="b.soli_luminarias"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.soli_tipoexpansion')"
      prop="b.soli_tipoexpansion"
      width="100"
    >
    </el-table-column>
  </el-table>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
    </el-col>
  </el-row>
  </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { parseTime } from '@/utils'
import { informe_siap_solicitud_xls } from '@/api/informe'
export default {
  data () {
    return {
      labelPosition: 'top',
      fecha_inicial: new Date(),
      fecha_final: new Date(),
      tableData: [],
      loading: false
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  methods: {
    obtenerDatos () {
      this.loading = true
      informe_siap_solicitud_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log('Error Solicitud: ' + error)
      })
    },
    exportarXls () {
      this.downloadLoading = true
      var datos = []
      this.tableData.forEach(d => {
        const data = {
          soti_descripcion: d.a.soti_descripcion,
          soli_fecha: d.a.soli_fecha,
          soli_nombre: d.a.soli_nombre,
          soli_radicado: d.a.soli_radicado,
          soli_direccion: d.a.soli_direccion,
          barr_descripcion: d.a.barr_descripcion,
          soli_telefono: d.a.soli_telefono,
          soli_email: d.a.soli_email,
          soli_solicitud: d.a.soli_solicitud,
          soli_informe: d.a.soli_informe,
          soli_consecutivo: d.a.soli_consecutivo,
          soli_fecharespuesta: d.b.soli_fecharespuesta,
          soli_fechalimite: d.b.soli_fechalimite,
          soli_fechasupervisor: d.b.soli_fechasupervisor,
          soli_fechainforme: d.b.soli_fechainforme,
          soli_fechaalmacen: d.b.soli_fechaalmacen,
          soli_numerorte: d.b.soli_numerorte,
          soli_puntos: d.b.soli_puntos,
          soli_tipoexpansion: d.b.soli_tipoexpansion,
          soli_codigorespuesta: d.b.soli_codigorespuesta,
          soli_luminarias: d.b.soli_luminarias,
          soli_estado_descripcion: d.b.soli_estado_descripcion
        }
        datos.push(data)
      })
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Radicado', 'Tipo', 'Estado', 'Fecha Radicado', 'Fecha Límite', 'Nombre', 'Direccion', 'Barrio', 'Teléfono', 'Email', 'Descripción', 'Fecha Supervisor', 'Csc Atención', 'Informe', 'Fecha Informe', 'Fecha Programa', 'Puntos', 'Puesto', 'Luminarias', 'Tipo Expansión', 'Fecha Respuesta', 'Código Respuesta']
        const filterVal = ['soli_radicado', 'soti_descripcion', 'soli_estado_descripcion', 'soli_fecha', 'soli_fechalimite', 'soli_nombre', 'soli_direccion', 'barr_descripcion', 'soli_telefono', 'soli_email', 'soli_solicitud', 'soli_fechasupervisor', 'soli_consecutivo', 'soli_informe', 'soli_fechainforme', 'soli_fechaalmacen', 'soli_puntos', 'soli_numerorte', 'soli_luminarias', 'soli_tipoexpansion', 'soli_fecharespuesta', 'soli_codigorespuesta']
        const list = datos
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'detallado_solicitud_' + this.$moment(this.fecha_inicial).format('YYYYMMDD') + '_' + this.$moment(this.fecha_final).format('YYYYMMDD'))
        this.downloadLoading = false
      })
    },
    formatJson (filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => {
        if (j === 'timestamp') {
          return parseTime(v[j])
        } else {
          return v[j]
        }
      }))
    }
  }
}
</script>
<style lang="scss" scoped>
.fa-icon {
  width: auto;
  height: 1em; /* or any other relative font sizes */

  /* You would have to include the following two lines to make this work in Safari */
  max-width: 100%;
  max-height: 100%;
}
</style>
