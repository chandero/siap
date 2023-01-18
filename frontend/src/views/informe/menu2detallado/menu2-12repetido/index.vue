<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.smr')}}</span>
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
    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
      <el-form-item label="Tipo de Material">
        <el-select v-model="tiel_id" name="tipo" :placeholder="$t('tipoelemento.select')">
          <el-option v-for="tipoelemento in tiposElemento" :key="tipoelemento.tiel_id" :label="tipoelemento.tiel_descripcion" :value="tipoelemento.tiel_id" >
          </el-option>
        </el-select>
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
      :label="$t('informe.even_codigo_instalado')"
      prop="even_codigo_instalado"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.reti_descripcion')"
      prop="reti_descripcion"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_consecutivo')"
      prop="repo_consecutivo"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.repo_fechasolucion')"
      prop="repo_fechasolucion"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.aap_id')"
      prop="aap_id"
      width="125"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.aap_direccion')"
      prop="aap_direccion"
      width="220"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.barr_descripcion')"
      prop="barr_descripcion"
      width="220"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.tiel_descripcion')"
      prop="tiel_descripcion"
      width="220"
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
import { informe_siap_material_repetido_xls } from '@/api/informe'
import { getTiposElemento } from '@/api/tipoelemento'
export default {
  data () {
    return {
      labelPosition: 'top',
      fecha_inicial: new Date(),
      fecha_final: new Date(),
      tiel_id: 1,
      tableData: [],
      loading: false,
      tiposElemento: []
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  beforeMount () {
    getTiposElemento().then(response => {
      this.tiposElemento = response.data
    }).catch(() => {})
  },
  methods: {
    obtenerDatos () {
      this.loading = true
      informe_siap_material_repetido_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.tiel_id).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log('Error Expansión: ' + error)
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Código Material', 'Tipo de Reporte', 'Reporte Consecutivo', 'Fecha Solución', 'Código Luminaria', 'Dirección', 'Barrio', 'Material']
        const filterVal = ['even_codigo_instalado', 'reti_descripcion', 'repo_consecutivo', 'repo_fechasolucion', 'aap_id', 'aap_direccion', 'barr_descripcion', 'tiel_descripcion']
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'material_repetido_' + this.tiel(this.tiel_id) + '_' + this.$moment(this.fecha_inicial).format('YYYYMMDD') + '_' + this.$moment(this.fecha_final).format('YYYYMMDD'))
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
    },
    tiel (tiel_id) {
      if (tiel_id === null || tiel_id === undefined || tiel_id === 0) {
        return ''
      } else {
        return this.tiposElemento.find(o => o.tiel_id === tiel_id, { tiel_descripcion: '' }).tiel_descripcion
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
