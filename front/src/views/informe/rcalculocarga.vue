<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.scc')}}</span>
    </el-col>
  </el-header>
  <el-main>
  <el-form :label-position="labelPosition">
  <el-row :gutter="4">
    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
      <el-form-item :label="$t('informe.periodo')">
        <el-date-picker
          v-model="periodo"
          type="month">
        </el-date-picker>
        <!-- <el-date-picker v-model="fecha_inicial" format="yyyy/MM/dd" @change="establecerFechaFinal"></el-date-picker> -->
      </el-form-item>
    </el-col>
    <!-- <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
      <el-form-item :label="$t('informe.endDate')">
        <el-date-picker v-model="fecha_final" format="yyyy/MM/dd"></el-date-picker>
      </el-form-item>
    </el-col> -->
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
import { informe_siap_calculo_carga_xls } from '@/api/informe'
export default {
  data() {
    return {
      labelPosition: 'top',
      fecha_inicial: null,
      fecha_final: null,
      periodo: null
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario',
      'months'
    ])
  },
  methods: {
    obtenerDatos() {
      // Calcular Fecha Inicial y Fecha Final
      console.log('Periodo: ' + this.periodo)
      this.fecha_inicial = new Date(this.periodo.getFullYear(), this.periodo.getMonth(), 1)
      this.fecha_final = new Date(this.periodo.getFullYear(), this.periodo.getMonth() + 1, 0)
      informe_siap_calculo_carga_xls(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.empresa.empr_id)
    },
    establecerFechaFinal() {
      this.fecha_final = new Date()
      this.fecha_final.setYear(this.fecha_inicial.getFullYear())
      this.fecha_final.setMonth(this.fecha_inicial.getMonth() + 1)
      this.fecha_final.setDate(this.fecha_inicial.getDate() - 1)
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