<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.srmr')}}</span>
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
    <el-table-column :label="$t('informe.cantidad')">
    <el-table-column
      :label="$t('informe.even_cantidad_retirado')"
      prop="even_cantidad_retirado"
      width="100">
    </el-table-column>
    <el-table-column
      :label="$t('informe.even_cantidad_instalado')"
      prop="even_cantidad_instalado"
      width="100">
    </el-table-column>
    </el-table-column>
  </el-table>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <img :title="$t('pdf')" @click="exportarPdf()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/pdf.png')"/>
      <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
    </el-col>
  </el-row>
  </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { parseTime } from '@/utils'
import { informe_siap_resumen_material_reporte_xls, informe_siap_resumen_material_reporte } from '@/api/informe'
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
      informe_siap_resumen_material_reporte_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.tableData = response.data
      })
    },
    exportarPdf () {
      informe_siap_resumen_material_reporte(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.usuario.usua_id, this.empresa.empr_id)
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Código Material', 'Nombre Material', 'Tipo de Reporte', 'Cantidad Retirado', 'Cantidad Instalado']
        const filterVal = ['elem_codigo', 'elem_descripcion', 'reti_descripcion', 'even_cantidad_retirado', 'even_cantidad_instalado']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'consolidado_materiales_reporte' + this.fecha_final)
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
