<template>
  <el-container>
    <el-header>
      <span>{{ $t('reporte.actaindisponibilidad') }}</span>
    </el-header>
    <el-main>
      <el-row>
        <el-col>
          <el-table :data="actas" style="width: 80%" max-height="400" stripe @selection-change="handleSelectionChange">
            <el-table-column prop="acde_numero" label="Número Acta" width="100" align="center">
            </el-table-column>
            <el-table-column prop="acin_anho" label="Año" width="140" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.acin_anho }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="acin_periodo" label="Periodo" width="140" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.acin_periodo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="acin_fechagenerado" label="Fecha" width="140" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row.acin_fechagenerado | moment('YYYY-MM-DD') }}</span>
              </template>
            </el-table-column>
            <el-table-column type="selection" width="55">
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <img :title="$t('xls')" @click="imprimir('xls')" style="width:32px; height: 36px; cursor: pointer;"
            :src="require('@/assets/xls.png')" />
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getTodos, generarActas, getActa } from '@/api/acta_indisponibilidad'
export default {
  data() {
    return {
      tipo: 1,
      anho: null,
      periodo: null,
      labelPosition: 'top',
      actas: [],
      multipleSelection: []
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  methods: {
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    imprimir(formato) {
      for (var item in this.multipleSelection) {
        console.log('Item:', item)
        getActa(this.multipleSelection[item].acde_id, 1).then(resp => {
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
    getActas() {
      getTodos().then(resp => {
        this.actas = resp.data.data
        console.log('Actas: ', this.actas)
      })
    },
    generar() {
      generarActas(this.anho, this.periodo, 1).then(resp => {
        if (resp.status === 200) {
          this.$message({
            message: 'Actas Generadas Correctamente',
            type: 'success'
          })
          this.getActas()
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
  beforeMount() {
    const date = new Date()
    this.anho = date.getFullYear()
    this.periodo = date.getMonth() + 1
    this.getActas()
  }
}
</script>
