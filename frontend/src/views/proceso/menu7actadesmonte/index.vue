<template>
    <el-container>
        <el-header>
            <span>{{ $t('reporte.actadesmonte') }}</span>
        </el-header>
        <el-main>
            <el-form ref="actaForm" :label-position="labelPosition">
                <el-row :gutter="4">
                    <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                        <el-form-item :label="$t('reporte.fechaInicio')">
                            <el-date-picker v-model="fecha_inicio" format="yyyy/MM/dd"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                        <el-form-item :label="$t('reporte.fechaFinal')">
                            <el-date-picker v-model="fecha_fin" format="yyyy/MM/dd"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col>
                      <el-popconfirm
                        title="Por Favor Confirme la Generación de las Actas en el Rango Seleccionado?"
                        @confirm="generar"
                      >
                        <el-button type="primary" slot="reference">Generar</el-button>
                      </el-popconfirm>
                    </el-col>
                </el-row>
                <el-card witdh="400px">
                <el-row>
                  <span>Filtrar por el Rango</span>
                </el-row>
                <el-row :gutter="4">
                    <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                        <el-form-item :label="$t('reporte.fechaInicio')">
                            <el-date-picker v-model="fecha_finicio" format="yyyy/MM/dd"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                        <el-form-item :label="$t('reporte.fechaFinal')">
                            <el-date-picker v-model="fecha_ffin" format="yyyy/MM/dd"></el-date-picker>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                  <el-col>
                    <el-table
                      :data="actas"
                      style="width: 40%"
                      max-height="400"
                      stripe
                      @selection-change="handleSelectionChange"
                    >
                      <el-table-column
                        prop="acde_numero"
                        label="Número Acta"
                        width="100"
                        align="center">
                      </el-table-column>
                      <el-table-column
                        prop="acde_fecha"
                        label="Fecha Acta"
                        width="140"
                        align="center">
                        <template slot-scope="scope">
                          <span>{{ scope.row.acde_fecha | moment('YYYY/MM/DD') }}</span>
                        </template>
                      </el-table-column>
                      <el-table-column
                        type="selection"
                        width="55">
                      </el-table-column>
                    </el-table>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="24">
                    <img :title="$t('xls')" @click="imprimir('xls')" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
                  </el-col>
                </el-row>
              </el-card>
            </el-form>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getTodos, generarActas, getActa } from '@/api/acta_desmonte'
export default {
  data () {
    return {
      tipo: 1,
      fecha_inicio: null,
      fecha_fin: null,
      fecha_finicio: null,
      fecha_ffin: null,
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
    imprimir (formato) {
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
      getTodos(this.fecha_finicio.getTime(), this.fecha_ffin.getTime()).then(resp => {
        this.actas = resp.data.data
        console.log('Actas: ', this.actas)
      })
    },
    generar () {
      generarActas(this.fecha_inicio.getTime(), this.fecha_fin.getTime(), 1).then(resp => {
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
  beforeMount () {
    this.fecha_inicio = new Date()
    this.fecha_fin = this.fecha_inicio
    this.fecha_finicio = new Date()
    this.fecha_ffin = this.fecha_finicio
    this.getActas()
  },
  watch: {
    fecha_finicio (val) {
      this.fecha_finicio = val
      this.getActas()
    },
    fecha_ffin (val) {
      this.fecha_ffin = val
      this.getActas()
    }
  }
}
</script>
