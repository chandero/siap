<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.siccmx')}}</span>
    </el-col>
  </el-header>
  <el-main>
  <el-form :label-position="labelPosition">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item :label="$t('ordentrabajo.crew')">
                             <el-select filterable clearable ref="crew" v-model="cuad_id" name="crew" :placeholder="$t('cuadrilla.select')"  style="width:250px;" @change="changeFocus('type')">
                              <el-option v-for="cuadrilla in cuadrillas" :key="cuadrilla.cuad_id" :label="cuadrilla.cuad_descripcion" :value="cuadrilla.cuad_id" >
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
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
      :label="$t('informe.cuad_descripcion')"
      prop="cuad_descripcion"
      width="300"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.reti_descripcion')"
      prop="reti_descripcion"
      width="300">
    </el-table-column>
    <el-table-column
      :label="$t('informe.elem_codigo')"
      prop="elem_codigo"
      width="90">
    </el-table-column>
    <el-table-column
      :label="$t('informe.elem_descripcion')"
      prop="elem_descripcion"
      width="300">
    </el-table-column>
    <el-table-column
      :label="$t('informe.ortr_fecha')"
      prop="ortr_fecha"
      width="90"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.even_cantidad_retirado')"
      prop="even_cantidad_retirado"
      width="90"
      align="right"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.even_cantidad_instalado')"
      prop="even_cantidad_instalado"
      width="90"
      align="right"
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
import { getCuadrillas } from '@/api/cuadrilla'
import { informe_siap_cuadrilla_consolidado_material_xls } from '@/api/informe'
export default {
  data () {
    return {
      labelPosition: 'top',
      fecha_inicial: new Date(),
      fecha_final: new Date(),
      tableData: [],
      cuadrillas: [],
      cuad_id: 0
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  beforeMount() {
    getCuadrillas().then(response => {
      this.cuadrillas = [{ cuad_id: 0, cuad_descripcion: 'Sin Asignar' }, ...response.data]
    }).catch(error => {
      console.log('getCuadrillas:' + error)
    })
  },
  methods: {
    obtenerDatos () {
      informe_siap_cuadrilla_consolidado_material_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.cuad_id).then(response => {
        this.tableData = response.data
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Fecha', 'Cuadrilla', 'Tipo Operación', 'Código Material', 'Descripción Material', 'Cantidad Retirada', 'Cantidad Instalada']
        const filterVal = ['ortr_fecha', 'cuad_descripcion', 'reti_descripcion', 'elem_codigo', 'elem_descripcion', 'even_cantidad_retirado', 'even_cantidad_instalado']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'consolidado_material_por_cuadrilla_' + this.fecha_inicial + '_' + this.fecha_final)
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
