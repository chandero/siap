<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.sipcx')}}</span>
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
  >
    <el-table-column
      :label="$t('informe.ortr_fecha')"
      prop="ortr_fecha"
      width="90"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.ortr_dia')"
      prop="ortr_dia"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.cuad_descripcion')"
      prop="cuad_descripcion"
      width="300"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.cuad_responsable')"
      prop="cuad_responsable"
      width="250">
    </el-table-column>
    <el-table-column
      :label="$t('informe.ortr_tipo')"
      prop="ortr_tipo"
      width="110">
    </el-table-column>
    <el-table-column
      :label="$t('informe.cuad_vehiculo')"
      prop="cuad_vehiculo"
      width="100">
    </el-table-column>
    <el-table-column
      :label="$t('informe.ortr_id')"
      prop="ortr_id"
      width="80">
    </el-table-column>
    <el-table-column
      :label="$t('informe.reportes')"
      prop="reportes"
      width="80">
    </el-table-column>
    <el-table-column
      :label="$t('informe.obras')"
      prop="obras"
      width="80">
    </el-table-column>
    <el-table-column
      :label="$t('informe.urbano')"
      prop="urbano"
      width="80">
    </el-table-column>
    <el-table-column
      :label="$t('informe.rural')"
      prop="rural"
      width="80">
    </el-table-column>
    <el-table-column
      :label="$t('informe.operaciones')"
      prop="operaciones"
      width="100">
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
import { informe_siap_por_cuadrilla_xls } from '@/api/informe'
export default {
  data () {
    return {
      labelPosition: 'top',
      fecha_inicial: new Date(),
      fecha_final: new Date(),
      tableData: []
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
      informe_siap_por_cuadrilla_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.tableData = response.data
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Fecha', 'Día', 'Cuadrilla', 'Responsable', 'Tipo(Mto/Exp)', 'Vehículo', 'Orden Trabajo', 'Reportes', 'Obras', 'Urbano', 'Rural', 'Operaciones']
        const filterVal = ['ortr_fecha', 'ortr_dia', 'cuad_descripcion', 'cuad_responsable', 'ortr_tipo', 'cuad_vehiculo', 'ortr_id', 'reportes', 'obras', 'urbano', 'rural', 'operaciones']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'consolidado_por_cuadrilla_' + this.fecha_inicial + '_' + this.fecha_final)
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
