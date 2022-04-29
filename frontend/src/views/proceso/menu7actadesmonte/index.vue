<template>
    <el-container>
        <el-header>
            <span>{{ $t('reporte.actadesmonte') }}</span>
        </el-header>
        <el-main>
            <el-form ref="actaForm" :label-position="labelPosition">
                <el-row :gutter="4">
                    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
                        <el-form-item :label="$t('reporte.fechaCorte')">
                            <el-date-picker v-model="fecha_corte" format="yyyy/MM/dd"></el-date-picker>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                  <el-col :span="24">
                    <img :title="$t('xls')" @click="imprimir('xls')" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
                  </el-col>
                </el-row>
            </el-form>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getActaDesmonteXls } from '@/api/reporte'
export default {
  data () {
    return {
      tipo: 1,
      fecha_corte: null,
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
    imprimir (formato) {
      getActaDesmonteXls(this.fecha_corte.getTime(), this.tipo).then(resp => {
        if (resp.status === 200) {
          var blob = resp.data
          const filename = resp.headers['content-disposition'].split(';')[1].split('=')[1]
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
        } else {
          this.$message({
            message: 'No hay datos para generar acta en esta fecha: ' + this.$moment(this.fecha_corte).format('YYYY-MM-DD'),
            type: 'warning'
          })
        }
      }).catch(() => {
        this.$message({
          message: 'No hay datos para generar acta en esta fecha: ' + this.$moment(this.fecha_corte).format('YYYY-MM-DD'),
          type: 'warning'
        })
      })
    }
  },
  beforeMount () {
    this.fecha_corte = new Date()
  }
}
</script>
