<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.scdx')}}</span>
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
      <img :title="$t('pdf')" @click="exportarPdf()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/pdf.png')"/>
      <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
    </el-col>
  </el-row>
  </el-form>
  </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { informe_siap_cambio_direccion_xls } from '@/api/informe'
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
    exportarXls () {
      this.loading = true
      informe_siap_cambio_direccion_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime(), 'xls').catch(error => {
        this.$message({
          message: 'Informe No Generado : ' + error,
          type: 'error'
        })
      })
    },
    exportarPdf () {
      this.loading = true
      informe_siap_cambio_direccion_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime(), 'pdf').catch(error => {
        this.$message({
          message: 'Informe No Generado : ' + error,
          type: 'error'
        })
      })
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
