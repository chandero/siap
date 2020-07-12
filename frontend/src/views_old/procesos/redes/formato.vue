<template>
    <el-container>
        <el-header>
            <span>{{ $t('reporte.formato') }}</span>
        </el-header>
        <el-main>
            <el-form ref="formatoForm" :model="formato" :label-position="labelPosition">
                <el-row>
                    <el-col :span="24">
                            <el-form-item prop="reti_id" :label="$t('reporte.type')">
                                <el-select autofocus clearable :title="$t('reporte.tipo.select')" style="width: 80%" ref="tipo" v-model="formato.reti_id" name="tipo" :placeholder="$t('reporte.tipo.select')">
                                    <el-option v-for="tipo in tipos" :key="tipo.reti_id" :label="tipo.reti_descripcion" :value="tipo.reti_id" >
                                    </el-option>
                                </el-select>
                            </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="24">
                        <el-form-item>
                            <el-button :disabled="!formato.reti_id" type="primary" @click="imprimir()">{{ $t('reporte.duedate.procesar') }}</el-button>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { printReporteBlanco, getTipos } from '@/api/reporte'
export default {
  data () {
    return {
      formato: {
        reti_id: null
      },
      labelPosition: 'top',
      tipos: []
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
      printReporteBlanco(this.formato.reti_id, this.empresa.empr_id)
    }
  },
  mounted () {
    getTipos().then(response => {
      this.tipos = response.data
    }).catch(error => {
      console.log('getTipos: ' + error)
    })
  }
}
</script>
