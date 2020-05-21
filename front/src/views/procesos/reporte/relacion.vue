<template>
    <el-container>
        <el-header>
            <span>{{ $t('reporte.relacion') }}</span>
        </el-header>
        <el-main>
            <el-form ref="relacionForm" :label-position="labelPosition">
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
                    <img :title="$t('pdf')" @click="imprimir('pdf')" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/prnt.png')"/>
                    <img :title="$t('xls')" @click="imprimir('xls')" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
                  </el-col>
                </el-row>
            </el-form>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { printReporteRelacion } from '@/api/reporte'
export default {
  data() {
    return {
      fecha_inicial: null,
      fecha_final: null,
      labelPosition: 'top'
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  methods: {
    imprimir(tipo) {
      printReporteRelacion(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.empresa.empr_id, this.usuario.usua_id, tipo)
    }
  },
  beforeMount() {
    this.fecha_inicial = new Date()
    this.fecha_inicial.setHours(0)
    this.fecha_inicial.setMinutes(0)
    this.fecha_inicial.setSeconds(0)
    this.fecha_inicial.setMilliseconds(0)
    this.fecha_final = this.fecha_inicial
  }
}
</script>

