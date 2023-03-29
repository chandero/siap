<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.srmupt')}}</span>
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
      :label="$t('informe.reti_descripcion')"
      prop="_1"
      width="250"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.elem_codigo')"
      prop="_2"
      width="140"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.elem_descripcion')"
      prop="_3"
      width="300"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.elem_unidad')"
      prop="_4"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('informe.even_cantidad_instalado')"
      prop="_5"
      width="80">
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
import { siap_informe_material_usado_por_reti, siap_informe_material_usado_por_reti_xlsx } from '@/api/reporte'
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
      const loading = this.$loading({
        lock: true,
        text: this.$t('informe.processing'),
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      siap_informe_material_usado_por_reti(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        loading.close()
        this.tableData = response.data
      }).catch(error => {
        loading.close()
        this.$message.error(error)
      })
    },
    exportarXls () {
      const loading = this.$loading({
        lock: true,
        text: this.$t('informe.processing'),
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      siap_informe_material_usado_por_reti_xlsx(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(resp => {
        loading.close()
        const filename = resp.headers['content-disposition'].split('filename=')[1]
        const blob = new Blob([resp.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = filename
        link.click()
      }).catch(error => {
        loading.close()
        this.$message.error(error)
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
