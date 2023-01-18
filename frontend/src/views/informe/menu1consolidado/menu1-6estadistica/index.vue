<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.sigex')}}</span>
    </el-col>
  </el-header>
  <el-main>
  <el-form :label-position="labelPosition">
  <el-row :gutter="4">
    <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
      <el-form-item :label="$t('informe.initialDate')">
        <el-date-picker
          v-model="fecha_inicial"
          type="month"
          placeholder="Seleccione el Periodo Inicial">
        </el-date-picker>
      </el-form-item>
    </el-col>
    <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
      <el-form-item :label="$t('informe.endDate')">
        <el-date-picker
          v-model="fecha_final"
          type="month"
          placeholder="Seleccione el Periodo Final">
        </el-date-picker>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-button type="primary" @click="obtenerDatos()">{{ $t('informe.process')}}</el-button>
    </el-col>
  </el-row>
  </el-form>
  </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { informe_siap_general_estadistica_xls } from '@/api/informe'
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
      console.log('fecha inicial:' + this.fecha_inicial)
      informe_siap_general_estadistica_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime(), 'xls', this.empresa.empr_id)
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
