<template>
  <el-container>
    <el-header>
      <el-col :span="24">
        <span>{{ $t('informe.srf')}}</span>
      </el-col>
    </el-header>
    <el-main>
    <el-form :label-position="labelPosition">
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
        <el-col :xs="13" :sm="13" :md="6" :lg="6" :xl="6">
          <el-form-item>
            <el-select filterable clearable ref="type" v-model="tireuc_id" name="tireuc" :placeholder="$t('reporte.operacion.select')"  style="width:250px;">
              <el-option v-for="tu in tipo_ucap" :key="tu.tireuc_id" :label="tu.tireuc_descripcion" :value="tu.tireuc_id" >
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="13" :sm="13" :md="6" :lg="6" :xl="6">
          <el-form-item>
            <el-select filterable clearable ref="type" v-model="reti_id" name="reti" :placeholder="$t('reporte.tipo.select')"  style="width:250px;">
              <el-option v-for="reti in tipos" :key="reti.reti_id" :label="reti.reti_descripcion" :value="reti.reti_id" >
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-row>
      <el-col :span="24">
        <el-button type="primary" @click="obtenerDatos()">{{ $t('informe.process')}}</el-button>
      </el-col>
    </el-row>
    </el-main>
  </el-container>
</template>
<script>
import { siap_reporte_foto } from '@/api/informe'
import { getTipos } from '@/api/reporte'

export default {
  data() {
    return {
      labelPosition: 'top',
      fecha_inicial: '',
      fecha_final: '',
      tipos: [],
      tireuc_id: null,
      reti_id: null,
      tipo_ucap: [
        {
          tireuc_id: 1,
          tireuc_descripcion: 'LUMINARIA'
        },
        {
          tireuc_id: 2,
          tireuc_descripcion: 'CONTROL'
        },
        {
          tireuc_id: 3,
          tireuc_descripcion: 'TRANSFORMADOR'
        },
        {
          tireuc_id: 4,
          tireuc_descripcion: 'MEDIDOR'
        }
      ]
    }
  },
  beforeMount() {
    this.fecha_inicial = new Date()
    this.fecha_final = new Date()
    getTipos().then(response => {
      this.tipos = response.data
    }).catch(error => {
      console.log('getTipos:' + error)
    })
  },
  methods: {
    obtenerDatos() {
      /* const loading = this.$loading({
        lock: true,
        text: 'Procesando Informe...'
      }) */
      siap_reporte_foto(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.tireuc_id, this.reti_id)/* .then(response => {
        loading.close()
        var filename = response.headers['content-disposition'].split('filename=')[1]
        var blob = new Blob([response.data], { type: 'application/pdf' })
        var link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = filename
        link.click()
      }).catch(error => {
        loading.close()
        this.$message(`No de pudo obtener el informe: ${error}`, 'error')
      }) */
    }
  }
}
</script>
