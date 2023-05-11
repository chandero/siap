<template>
  <el-container>
    <el-header>
      <span>{{ $t('reporte.actaindisponibilidad') }}</span>
    </el-header>
    <el-main>
      <el-form>
        <el-row :gutter="8">
          <el-col :xs="24" :sm="24" :md="4" :xl="4" :lg="4">
            <span>Procesar Acta Periodo: </span>
          </el-col>
          <el-col :xs="24" :sm="24" :md="4" :xl="4" :lg="4">
            <el-form-item label="Año">
              <el-input type="number" v-model="anho" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="4" :xl="4" :lg="4">
            <el-form-item label="Periodo">
                <el-select v-model="mes">
                  <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                </el-select>
              </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="4" :xl="4" :lg="4">
            <el-form-item label="Tarifa TEEn">
              <el-input type="number" v-model="tarifa" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="4" :xl="4" :lg="4">
            <el-button type="primary" @click="generar">Generar Acta</el-button>
          </el-col>
        </el-row>
      </el-form>
      <el-row :gutter="8">
        <el-col :xs="24" :sm="24" :md="2" :xl="2" :lg="2">
          <span>Rango Lista</span>
        </el-col>
        <el-col :xs="24" :sm="24" :md="6" :xl="6" :lg="6">
          <el-date-picker v-model="fi" @change="handleDateChange"
            type="monthrange"
            range-separator="A"
            start-placeholder="Mes Inicial"
            end-placeholder="Mes Final">
          </el-date-picker>
        </el-col>
        <el-col>
          <el-button type="primary" circle icon="el-icon-refresh" @click="refrescar" />
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-table :data="actas" style="width: 80%" max-height="400" stripe @selection-change="handleSelectionChange">
            <el-table-column prop="acin_numero" label="Número Acta" width="100" align="center">
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
            <el-table-column prop="acin_tarifa" label="Tarifa" width="140" align="rigth">
              <template slot-scope="scope">
                <span>{{ scope.row.acin_tarifa }}</span>
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
      anho: null,
      mes: null,
      tarifa: null,
      fi: null,
      ff: null,
      labelPosition: 'top',
      actas: [],
      multipleSelection: []
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
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    handleDateChange(val) {
      console.log('Date Value:', val)
    },
    imprimir(formato) {
      for (var item in this.multipleSelection) {
        console.log('Item:', item)
        getActa(this.multipleSelection[item].acin_id, 1).then(resp => {
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
    refrescar() {
      this.getActas()
    },
    getActas() {
      const fi = this.fi[0]
      const ff = new Date(this.fi[1].getFullYear(), this.fi[1].getMonth() + 1, 0)
      fi.setDate(1)
      getTodos(fi.getTime(), ff.getTime()).then(resp => {
        this.actas = resp.data.data
        console.log('Actas: ', this.actas)
      })
    },
    generar() {
      const tarifa = parseFloat(this.tarifa)
      generarActas(this.anho, this.mes, tarifa).then(resp => {
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
    const fecha = new Date()
    fecha.setMonth(0)
    this.fi = [fecha, date]
    this.anho = date.getFullYear()
    this.mes = date.getMonth() + 1
    this.getActas()
  }
}
</script>
