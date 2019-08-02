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
    <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
      <el-form-item :label="$t('informe.periodo')">
        <el-select v-model="periodo">
            <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t('months.' + m.label)"></el-option>
        </el-select>
      </el-form-item>
    </el-col>
    <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
      <el-form-item :label="$t('informe.anho')">
        <el-input v-model="anho" type="number"></el-input>
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
import { informe_siap_calculo_carga_xls } from '@/api/informe'
export default {
  data() {
    return {
      labelPosition: 'top',
      periodo: new Date().getMonth() + 1,
      anho: new Date().getFullYear()
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
      informe_siap_calculo_carga_xls(this.periodo, this.anho, this.empresa.empr_id)
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