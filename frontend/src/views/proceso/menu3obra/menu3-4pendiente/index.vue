<template>
    <el-container>
        <el-header>
            <span>{{ $t('obra.relacion') }}</span>
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
                        <el-form-item>
                            <el-button type="primary" @click="imprimir()">{{ $t('obra.duedate.procesar') }}</el-button>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { printObraRelacion } from '@/api/obra'
export default {
  data () {
    return {
      fecha_inicial: new Date(),
      fecha_final: new Date(),
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
    imprimir () {
      printObraRelacion(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.empresa.empr_id, this.usuario.usua_id)
    }
  }
}
</script>
