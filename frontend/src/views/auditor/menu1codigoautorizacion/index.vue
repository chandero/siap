<template>
  <el-container>
    <el-header>
      <span>Código de Autorización Usados</span>
    </el-header>
    <el-main>
      <el-row>
      <el-form>
        <el-row>
          <el-col :xs="12" :sm="12" :md="12" :xl="12" :lg="12">
            <el-form-item label="Fecha Inicial">
              <el-date-picker
                v-model="fechaInicial"
                type="date"
                placeholder="Seleccione una fecha"
                :picker-options="pickerOptions"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :xs="12" :sm="12" :md="12" :xl="12" :lg="12">
              <el-form-item label="Fecha Final">
                <el-date-picker
                  v-model="fechaFinal"
                  type="date"
                  placeholder="Seleccione una fecha"
                  :picker-options="pickerOptions"></el-date-picker>
              </el-form-item>
            </el-col>
        </el-row>
        <el-row>
          <el-col>
            <el-button type="primary" icon="el-icon-search" @click="handleObtenerClick">Obtener Datos</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-row>
    <el-row>
      <el-col>
        <el-table
          :data="tablaData"
          height="600"
        >
        <el-table-column
          prop="tireuc_id"
          label="Tipo"
          width="180">
        >
          <template slot-scope="scope">
            <span>{{ scope.row._1 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="reti_descripcion"
          label="Tipo de Operación"
          width="250">
        >
          <template slot-scope="scope">
            <span>{{ scope.row._2 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="repo_consecutivo"
          label="Número"
          width="150">
        >
          <template slot-scope="scope">
            <span>{{ scope.row._3 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="coau_fecha"
          label="Fecha Generación"
          width="130">
          <template slot-scope="scope">
            <span>{{ scope.row._5 | moment('YYYY-MM-DD')}}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="coau_fecha"
          label="Fecha Uso"
          width="130">
          <template slot-scope="scope">
            <span>{{ scope.row._6 | moment('YYYY-MM-DD')}}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="coau_codigo"
          label="Codigo de Autorizacion"
          width="180">
          <template slot-scope="scope">
            <span>{{ scope.row._4 | moment('YYYY-MM-DD')}}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="usua_nombre_generador"
          label="Generador"
          width="250">
        >
          <template slot-scope="scope">
            <span>{{ scope.row._7 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="usua_nombre_generador"
          label="Usuario"
          width="250">
        >
          <template slot-scope="scope">
            <span>{{ scope.row._8 }}</span>
          </template>
        </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <el-row>
      <el-col>
        <el-pagination
          layout="prev, pager, next"
          :total="tablaData.length"
          :page-size="10">
        </el-pagination>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
          <img :title="$t('xls')" @click="handleExcelClick()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
      </el-col>
    </el-row>
    </el-main>
  </el-container>
</template>
<script>
import { obtenerInforme, obtenerInformeXlsx } from '@/api/auditor'

export default {
  data() {
    return {
      tablaData: [],
      fechaInicial: null,
      fechaFinal: null,
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now()
        },
        shortcuts: [{
          text: 'Hoy',
          onClick(picker) {
            picker.$emit('pick', new Date())
          }
        }, {
          text: 'Ayer',
          onClick(picker) {
            const date = new Date()
            date.setTime(date.getTime() - 3600 * 1000 * 24)
            picker.$emit('pick', date)
          }
        }, {
          text: 'Una semana atrás',
          onClick(picker) {
            const date = new Date()
            date.setTime(date.getTime() - 3600 * 1000 * 24 * 7)
            picker.$emit('pick', date)
          }
        }]
      }
    }
  },
  methods: {
    handleObtenerClick() {
      console.log('Fecha Inicial: ' + this.fechaInicial, 'type of: ' + typeof this.fechaFinal)
      obtenerInforme(this.fechaInicial.getTime(), this.fechaFinal.getTime())
        .then((response) => {
          this.tablaData = response.data
        })
        .catch((error) => {
          console.log(error)
        })
    },
    handleExcelClick() {
      const loading = this.$loading({
        lock: true,
        text: 'Generando Informe...',
        spinner: 'el-icon-loading'
      })
      obtenerInformeXlsx(this.fechaInicial.getTime(), this.fechaFinal.getTime())
        .then((response) => {
          loading.close()
          console.log(response)
          const filename = response.headers['content-disposition'].split('filename=')[1]
          const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
          const link = document.createElement('a')
          link.href = window.URL.createObjectURL(blob)
          link.download = filename
          link.click()
        })
        .catch((error) => {
          loading.close()
          console.log('Error generando excel:', error)
        })
    }
  }
}
</script>
