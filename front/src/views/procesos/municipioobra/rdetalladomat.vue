<template>
  <el-container>
  <el-header>Municipio Obra - Consecutivo: {{ this.muob_consecutivo }} </el-header>
  <el-main>
  <el-row>
    <el-col>
  <el-table 
    :data="tableData"
    width="100%"
    height="600px"
    stripe 
  >
    <el-table-column 
      :label="$t('informe.elem_codigo')"
      prop="elem_codigo"
      width="140"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.elem_descripcion')"
      prop="elem_descripcion"
      width="300"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.reti_descripcion')"
      prop="reti_descripcion"
      width="300"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.repo_consecutivo')"
      prop="repo_numero"
      width="100"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.repo_fechasolucion')"
      prop="repo_fechasolucion"
      width="100"
    >
     <template  slot-scope="scope">
       <span>{{ scope.row.repo_fechasolucion }}</span>
     </template>
    </el-table-column>        
    <el-table-column :label="$t('informe.retirado')">
    <el-table-column 
      :label="$t('informe.codigo')"
      prop="even_codigo_retirado"
      width="100">
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.cantidad')"
      prop="even_cantidad_retirado"
      width="100">
    </el-table-column>    
    </el-table-column>
    <el-table-column :label="$t('informe.instalado')">
    <el-table-column 
      :label="$t('informe.codigo')"
      prop="even_codigo_instalado"
      width="100">
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.cantidad')"
      prop="even_cantidad_instalado"
      width="100">
    </el-table-column>    
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
import { informe_siap_detallado_material_muob_xls } from '@/api/muob'
export default {
  data() {
    return {
      labelPosition: 'top',
      fecha_inicial: new Date(),
      fecha_final: new Date(),
      tableData: [],
      muob_consecutivo: null
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  mounted() {
    this.muob_consecutivo = this.$route.params.consecutivo
    this.obtenerDatos()
  },
  methods: {
    obtenerDatos() {
      informe_siap_detallado_material_muob_xls(this.muob_consecutivo).then(response => {
        this.tableData = response.data
      })
    },
    exportarXls() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Código Material', 'Nombre Material', 'Tipo de Reporte', 'Número Reporte', 'Fecha Solución', 'Código Retirado', 'Cantidad Retirado', 'Código Instalado', 'Cantidad Instalado']
        const filterVal = ['elem_codigo', 'elem_descripcion', 'reti_descripcion', 'repo_numero', 'repo_fechasolucion', 'even_codigo_retirado', 'even_cantidad_retirado', 'even_codigo_instalado', 'even_cantidad_instalado']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'detallado_materiales_municipio_obra_' + this.muob_consecutivo)
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