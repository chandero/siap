<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.sdax')}}</span>
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
      :label="$t('informe.repo_fechasolucion')"
      prop="repo_fechasolucion"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.repo_fechadigitacion')"
      prop="repo_fechasolucion"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.repo_consecutivo')"
      prop="aap_rte"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_id')"
      prop="aap_id"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_direccion')"
      prop="aap_direccion"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.barr_descripcion')"
      prop="barr_descripcion"
      width="100"
    >
    </el-table-column>            
    <el-table-column 
      :label="$t('informe.uso')"
      prop="aaus_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.cuentaap')"
      prop="aacu_descripcion"
      width="100"
    > 
    </el-table-column>       
    <el-table-column 
      :label="$t('informe.aamo_descripcion_anterior')"
      prop="aamo_descripcion_anterior"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_potencia_anterior')"
      prop="aap_potencia_anterior"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_tecnologia_anterior')"
      prop="aap_tecnologia_anterior"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aamo_descripcion')"
      prop="aamo_descripcion"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_potencia')"
      prop="aap_potencia"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_tecnologia')"
      prop="aap_tecnologia"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aaco_descripcion')"
      prop="aaco_descripcion"
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
import { informe_siap_detallado_actualizacion_xls } from '@/api/informe'
export default {
  data() {
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
    obtenerDatos() {
      this.loading = true
      informe_siap_detallado_actualizacion_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log('Error Actualización: ' + error)
      })
    },
    exportarXls() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Fecha Reporte', 'Fecha Digitación', 'Reporte', 'Código Luminaria Anterior', 'Dirección', 'Barrio', 'Uso', 'Cuenta Alumbrado', 'Tipo Anterior', 'Pot.(W) Anterior', 'Tecnología Anterior', 'Tipo Actual', 'Pot.(W) Actual', 'Tecnología Actual', 'Tipo Medida']
        const filterVal = ['repo_fechasolucion', 'repo_fechasolucion', 'aap_rte', 'aap_id', 'aap_direccion', 'barr_descripcion', 'aaus_descripcion', 'aacu_descripcion', 'aamo_descripcion_anterior', 'aap_potencia_anterior', 'aap_tecnologia_anterior', 'aamo_descripcion', 'aap_potencia', 'aap_tecnologia', 'aaco_descripcion']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'detallado_actualizacion_' + this.$moment(this.fecha_inicial).format('YYYYMMDD') + '_' + this.$moment(this.fecha_final).format('YYYYMMDD'))
        this.downloadLoading = false
      })
    },
    formatJson(filterVal, jsonData) {
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