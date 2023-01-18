<template>
  <el-container>
    <el-header>Informe de Cambio de Información de Luminaria En Reportes</el-header>
    <el-main>
      <el-form>
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
          <el-col>
            <img :title="$t('xls')" @click="getXlsx()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
          </el-col>
        </el-row>
      </el-form>
    </el-main>
  </el-container>
</template>
<script>
import { siap_informe_cambios_en_reporte } from '@/api/ordentrabajo'
export default {
  data() {
    return {
      fecha_inicial: '',
      fecha_final: ''
    }
  },
  beforeMount() {
    this.fecha_inicial = new Date()
    this.fecha_final = this.fecha_inicial
  },
  methods: {
    getXlsx() {
      siap_informe_cambios_en_reporte(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        const filename = response.headers['content-disposition'].split('filename=')[1]
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
      })
    }
  }
}
</script>
