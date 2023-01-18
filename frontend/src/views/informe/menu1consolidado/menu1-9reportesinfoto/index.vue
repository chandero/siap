<template>
    <el-container>
    <el-header>
      <el-col :span="24">
        <span>{{ $t('informe.srsf')}}</span>
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
        label="Id"
        prop="_1"
        width="80"
      >
      </el-table-column>
    <el-table-column
        :label="$t('reporte.type')"
        prop="_2"
        width="250"
      >
        <template slot-scope="scope">
          {{ tipoReporte(scope.row._2) }}
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('informe.repo_consecutivo')"
        prop="_3"
        width="100"
      >
      </el-table-column>
      <el-table-column
        :label="$t('informe.repo_fechasolucion')"
        prop="_4"
        width="150"
      >
        <template slot-scope="scope">
          {{ scope.row._4 | moment('YYYY-MM-DD') }}
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('informe.aap_id')"
        prop="_5"
        width="150"
      >
      </el-table-column>
      <el-table-column
        :label="$t('informe.fotos')"
        prop="_6"
        width="80"
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
import { getTipos } from '@/api/reporte'
import { informe_siap_reporte_sin_foto } from '@/api/informe'
export default {
  data () {
    return {
      labelPosition: 'top',
      fecha_inicial: new Date(),
      fecha_final: new Date(),
      tableData: [],
      tipos: [],
      loading: false
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  beforeMount () {
    getTipos().then(response => {
      this.tipos = response.data
    }).catch(error => {
      console.log('getTipos: ' + error)
    })
  },
  methods: {
    obtenerDatos () {
      this.loading = true
      informe_siap_reporte_sin_foto(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log('Error Consultando Datos: ' + error)
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Id', 'Tipo de Reporte', 'Consecutivo', 'Fecha Solucion', 'Luminaria', 'Fotos']
        const filterVal = ['_1', '_2', '_3', '_4', '_5', '_6']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'Informe_Reportes_Sin_Fotos' + this.$moment(this.fecha_inicial).format('YYYYMMDD') + '_' + this.$moment(this.fecha_final).format('YYYYMMDD'))
        this.downloadLoading = false
      })
    },
    formatJson (filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => {
        if (j === '_4') {
          return parseTime(v[j])
        } else if (j === '_2') {
          return this.tipoReporte(v[j])
        } else {
          return v[j]
        }
      }))
    },
    tipoReporte (reti_id) {
      console.log('reti_id: ' + reti_id)
      if (reti_id === null) {
        return 'INDEFINIDO'
      } else {
        return this.tipos.find(o => o.reti_id === reti_id, { reti_descripcion: 'INDEFINIDO' }).reti_descripcion
      }
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
