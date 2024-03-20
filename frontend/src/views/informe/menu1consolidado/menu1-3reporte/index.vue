<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.scr')}}</span>
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
      :label="$t('informe.repo_consecutivo')"
      prop="repo_consecutivo"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_fecharecepcion')"
      prop="repo_fecharecepcion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_nombre')"
      prop="repo_nombre"
      width="150"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_telefono')"
      prop="repo_telefono"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_direccion')"
      prop="repo_direccion"
      width="150"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.barr_descripcion')"
      prop="barr_descripcion"
      width="150"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.vereda')"
      prop="vereda"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.medio')"
      prop="orig_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.acti_descripcion')"
      prop="acti_descripcion"
      width="200"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_descripcion')"
      prop="repo_descripcion"
      width="200"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_fechasolucion')"
      prop="repo_fechasolucion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_reportetecnico')"
      prop="repo_reportetecnico"
      width="250"
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
import { informe_siap_reporte_consolidado_xls } from '@/api/informe'
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
      informe_siap_reporte_consolidado_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log('Error Consolidado Reportes: ' + error)
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Tipo', 'Reporte Técnico', 'Fecha Solicitud', 'Persona/Entidad Usuario', 'Teléfono', 'Dirección', 'Barrio', 'Vereda', 'Medio', 'Actividad', 'Descripción', 'Fecha Solución', 'Solución Trabajo Realizado']
        const filterVal = ['tipo', 'repo_consecutivo', 'repo_fecharecepcion', 'repo_nombre', 'repo_telefono', 'repo_direccion', 'barr_descripcion', 'vereda', 'orig_descripcion', 'acti_descripcion', 'repo_descripcion', 'repo_fechasolucion', 'repo_reportetecnico']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'consolidado_reportes_' + this.$moment(this.fecha_inicial).format('YYYYMMDD') + '_' + this.$moment(this.fecha_final).format('YYYYMMDD'))
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
