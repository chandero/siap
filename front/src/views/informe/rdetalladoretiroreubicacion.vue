<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.sdrr')}}</span>
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
      :label="$t('informe.repo_fecharecepcion')"
      prop="retiro.repo_fecharecepcion"
      width="100"
    >
    </el-table-column>  
    <el-table-column 
      :label="$t('informe.repo_fechasolucion')"
      prop="retiro.repo_fechasolucion"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.repo_fechadigitacion')"
      prop="retiro.repo_fechadigitacion"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.repo_consecutivo')"
      prop="retiro.repo_consecutivo"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_id')"
      prop="retiro.aap_id"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_direccion')"
      prop="retiro.aap_direccion"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aaus_descripcion')"
      prop="retiro.aaus_descripcion"
      width="100"
    >
    </el-table-column>     
    <el-table-column 
      :label="$t('informe.aamo_descripcion')"
      prop="retiro.aamo_descripcion"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_potencia')"
      prop="retiro.aap_potencia"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_tecnologia')"
      prop="retiro.aap_tecnologia"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aaco_descripcion')"
      prop="retiro.aaco_descripcion"
      width="100"
    >
    </el-table-column> 
    <el-table-column 
      :label="$t('informe.enbaja')"
      prop="retiro.enbaja"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.repo_fecharecepcion')"
      prop="reubicacion.repo_fecharecepcion"
      width="100"
    >
    </el-table-column>      
    <el-table-column 
      :label="$t('informe.repo_fechasolucion')"
      prop="reubicacion.repo_fechasolucion"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.repo_fechadigitacion')"
      prop="reubicacion.repo_fechasolucion"
      width="100"
    >
    </el-table-column> 
    <el-table-column 
      :label="$t('informe.reti_descripcion')"
      prop="reubicacion.reti_descripcion"
      width="100"
    >
    </el-table-column>      
    <el-table-column 
      :label="$t('informe.repo_consecutivo')"
      prop="reubicacion.aap_rte"
      width="100"
    >
    </el-table-column>     
    <el-table-column 
      :label="$t('informe.aap_id')"
      prop="reubicacion.aap_id"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_direccion_actual')"
      prop="reubicacion.aap_direccion"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.barr_descripcion_actual')"
      prop="reubicacion.barr_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aaus_descripcion')"
      prop="reubicacion.aaus_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aamo_descripcion')"
      prop="reubicacion.aamo_descripcion"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_potencia')"
      prop="reubicacion.aap_potencia"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aap_tecnologia')"
      prop="reubicacion.aap_tecnologia"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aaco_descripcion')"
      prop="reubicacion.aaco_descripcion"
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
import { informe_siap_detallado_retiro_reubicacion_xls } from '@/api/informe'
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
      informe_siap_detallado_retiro_reubicacion_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log('Error Retiro-Reubicacion: ' + error)
      })
    },
    exportarXls() {
      this.downloadLoading = true
      var dataTable = []
      this.tableData.forEach(data => {
        console.log('data: ' + JSON.stringify(data))
        const dato = {
          retiro_repo_fecharecepcion: data.retiro.repo_fecharecepcion,
          retiro_repo_fechasolucion: data.retiro.repo_fechasolucion,
          retiro_repo_fechadigitacion: data.retiro.repo_fechadigitacion,
          retiro_repo_consecutivo: data.retiro.repo_consecutivo,
          retiro_aap_id: data.retiro.aap_id,
          retiro_aap_direccion: data.retiro.aap_direccion,
          retiro_barr_descripcion: data.retiro.barr_descripcion,
          retiro_vereda: data.retiro.vereda,
          retiro_parque: data.retiro.parque,
          retiro_aaus_descripcion: data.retiro.aaus_descripcion,
          retiro_aamo_descripcion: data.retiro.aamo_descripcion,
          retiro_aap_potencia: data.retiro.aap_potencia,
          retiro_aap_tecnologia: data.retiro.aap_tecnologia,
          retiro_aaco_descripcion: data.retiro.aaco_descripcion,
          retiro_enbaja: data.retiro.enbaja
        }
        if (data.reubicacion !== null) {
          dato.reubicacion_repo_fecharecepcion = data.reubicacion.repo_fecharecepcion
          dato.reubicacion_repo_fechasolucion = data.reubicacion.repo_fechasolucion
          dato.reubicacion_repo_fechadigitacion = data.reubicacion.repo_fechadigitacion
          dato.reubicacion_reti_descripcion = data.reubicacion.reti_descripcion
          dato.reubicacion_aap_rte = data.reubicacion.aap_rte
          dato.reubicacion_aap_id = data.reubicacion.aap_id
          dato.reubicacion_aap_direccion = data.reubicacion.aap_direccion
          dato.reubicacion_barr_descripcion = data.reubicacion.barr_descripcion
          dato.reubicacion_vereda = data.reubicacion.vereda
          dato.reubicacion_parque = data.reubicacion.parque
          dato.reubicacion_aaus_descripcion = data.reubicacion.aaus_descripcion
          dato.reubicacion_aamo_descripcion = data.reubicacion.aamo_descripcion
          dato.reubicacion_aap_potencia = data.reubicacion.aap_potencia
          dato.reubicacion_aap_tecnologia = data.reubicacion.aap_tecnologia
          dato.reubicacion_aaco_descripcion = data.reubicacion.aaco_descripcion
        }
        dataTable.push(dato)
      })
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Retiro Fecha Reporte', 'Retiro Fecha Solución', 'Retiro Fecha Digitación', 'Retiro Reporte', 'Retiro Código Luminaria', 'Retiro Dirección', 'Retiro Barrio', 'Retiro Uso', 'Retiro Tipo', 'Retiro Pot.(W)', 'Retiro Tecnología', 'Retiro Tipo Medida', 'Dada De Baja', 'Reubicación Fecha Reporte', 'Reubicación Fecha Solución', 'Reubicación Fecha Digitación', 'Tipo Reporte', 'Reubicación Reporte', 'Reubicación Código Luminaria', 'Reubicación Dirección', 'Reubicación Barrio', 'Reubicación Uso', 'Reubicación Tipo', 'Reubicación Pot.(W)', 'Reubicación Tecnología', 'Reubicación Tipo Medida']
        const filterVal = ['retiro_repo_fecharecepcion', 'retiro_repo_fechasolucion', 'retiro_repo_fechadigitacion', 'retiro_repo_consecutivo', 'retiro_aap_id', 'retiro_aap_direccion', 'retiro_barr_descripcion', 'retiro_aaus_descripcion', 'retiro_aamo_descripcion', 'retiro_aap_potencia', 'retiro_aap_tecnologia', 'retiro_aaco_descripcion', 'retiro_enbaja', 'reubicacion_repo_fecharecepcion', 'reubicacion_repo_fechasolucion', 'reubicacion_repo_fechadigitacion', 'reubicacion_reti_descripcion', 'reubicacion_aap_rte', 'reubicacion_aap_id', 'reubicacion_aap_direccion', 'reubicacion_barr_descripcion', 'reubicacion_aaus_descripcion', 'reubicacion_aamo_descripcion', 'reubicacion_aap_potencia', 'reubicacion_aap_tecnologia', 'reubicacion_aaco_descripcion']
        const list = dataTable
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'detallado_retiro_reubicacion_reposicion_' + this.$moment(this.fecha_inicial).format('YYYYMMDD') + '_' + this.$moment(this.fecha_final).format('YYYYMMDD'))
        this.downloadLoading = false
      })
    },
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => {
        if (j === 'timestamp') {
          return parseTime(v[j])
        } else if (j === null) {
          return ''
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