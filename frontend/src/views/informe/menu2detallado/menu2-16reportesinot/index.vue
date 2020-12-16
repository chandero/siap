<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.srsot')}}</span>
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
      :label="$t('informe.reti_descripcion')"
      prop="_1"
      width="250"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_consecutivo')"
      prop="_2"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_fecharecepcion')"
      prop="_3"
      width="110"
    >
      <template slot-scope="scope">
        <span>{{ scope.row._3 | moment('YYYY-MM-DD')}}</span>
      </template>
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_fechasolucion')"
      prop="_4"
      width="110"
    >
      <template slot-scope="scope">
        <span>{{ scope.row._4 | moment('YYYY-MM-DD')}}</span>
      </template>
    </el-table-column>
    <el-table-column
      :label="$t('informe.cuad_descripcion')"
      prop="_5"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.luminarias')"
      prop="_6"
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
import { reporteSinOt } from '@/api/reporte'
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
      reporteSinOt(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log('Error Luminaria Por Reporte: ' + error)
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Tipo Reporte', 'Reporte', 'Fecha Recepcion', 'Fecha Solucion', 'Cuadrilla', 'Luminarias']
        const filterVal = ['_1', '_2', '_3', '_4', '_5', '_6']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'detallado_reporte_sin_orden_de_trabajo' + this.$moment(this.fecha_inicial).format('YYYYMMDD') + '_' + this.$moment(this.fecha_final).format('YYYYMMDD'))
        this.downloadLoading = false
      })
    },
    formatJson (filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => {
        if (j === '_3' || j === '_4') {
          return parseTime(v[j], '{y}-{m}-{d}')
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
