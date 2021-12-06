<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.stx')}}</span>
    </el-col>
  </el-header>
  <el-main>
  <el-form :label-position="labelPosition">
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
    v-loading="loading"
    element-loading-text="Obteniendo Información..."
    stripe
  >
    <el-table-column
      :label="$t('gestion.transformador.numero')"
      prop="tran_numero"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_codigo_apoyo')"
      prop="tran_codigo_apoyo"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.direccion')"
      prop="tran_direccion"
      width="250"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.barr_descripcion')"
      prop="barr_descripcion"
      width="200"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_propietario')"
      prop="tran_propietario"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_marca')"
      prop="tran_marca"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_serial')"
      prop="tran_serial"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_kva')"
      prop="tran_kva"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tipo_id')"
      prop="tipo_id"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_fases')"
      prop="tran_fases"
      width="110"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_tension_p')"
      prop="tran_tension_p"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_tension_s')"
      prop="tran_tension_s"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_referencia')"
      prop="tran_referencia"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.cantidad')"
      prop="cantidad"
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
import { informe_siap_transformador_xls } from '@/api/informe'
import { informe_siap_transformador } from '@/api/transformador'
import { getCaracteristica } from '@/api/caracteristica'
export default {
  data () {
    return {
      labelPosition: 'top',
      fecha_corte: new Date(),
      tableData: [],
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0,
      postes: []
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  mounted () {
    getCaracteristica(8).then((response) => {
      const poste = response.data.cara_valores.split(',')
      for (var i = 0; i < poste.length; i++) {
        this.postes.push({ tipo_id: i + 1, tipo_descripcion: poste[i] })
      }
    })
  },
  methods: {
    obtenerDatos () {
      this.loading = true
      informe_siap_transformador(this.empresa.empr_id).then(response => {
        this.loading = false
        this.tableData = response.data
      }).catch(error => {
        this.loading = false
        console.log(error)
      })
    },
    exportarXls () {
      informe_siap_transformador_xls().then(resp => {
        var blob = resp.data
        const filename = 'Informe_Transformador.xlsx'
        if (window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveBlob(blob, filename)
        } else {
          var downloadLink = window.document.createElement('a')
          downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
          downloadLink.download = filename
          document.body.appendChild(downloadLink)
          downloadLink.click()
          document.body.removeChild(downloadLink)
        }
      })
        .catch(() => {})
    },
    cellValueRenderer (row, column, cellValue, index) {
      let value = ''
      if (cellValue) {
        value = 'X'
      }
      return value
    },
    handleSizeChange (val) {
      this.page_size = val
      this.obtenerDatos()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.obtenerDatos()
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
