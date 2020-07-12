<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.sgr')}}</span>
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
    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
      <el-button type="primary" @click="obtenerDatos(1)">{{ $t('informe.graficorecibidos')}}</el-button>
    </el-col>
    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
      <el-button type="primary" @click="obtenerDatos(2)">{{ $t('informe.graficouso')}}</el-button>
    </el-col>
    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
      <el-button type="primary" @click="obtenerDatos(3)">{{ $t('informe.graficoatendidas')}}</el-button>
    </el-col>
    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
      <el-button type="primary" @click="obtenerDatos(4)">{{ $t('informe.graficorecibidosvspendientes')}}</el-button>
    </el-col>
    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
      <el-button type="primary" @click="obtenerDatos(5)">{{ $t('informe.graficosectorrecibidos')}}</el-button>
    </el-col>
  </el-row>
  </el-form>
  </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { informe_siap_graficos_reporte } from '@/api/informe'
export default {
  data() {
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
    obtenerDatos(num_id) {
      informe_siap_graficos_reporte(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.usuario.usua_id, this.empresa.empr_id, num_id)
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