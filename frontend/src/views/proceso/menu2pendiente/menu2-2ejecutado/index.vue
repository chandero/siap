<template>
    <el-container>
        <el-header>
            <span>{{ $t('reporte.ejecutado') }}</span>
        </el-header>
        <el-main>
            <el-form ref="relacionForm" :label-position="labelPosition">
<!--                 <el-row :gutter="4">
                  <el-col :span="24">
                    <el-select autofocus :title="$t('reporte.tipo.select')" style="width: 80%" ref="tipo" v-model="tipo" name="tipo" :placeholder="$t('reporte.tipo.select')">
                      <el-option v-for="t in tipo_inventario" :key="t.id" :label="t.descripcion.toUpperCase()" :value="t.id" >
                      </el-option>
                    </el-select>
                  </el-col>
                </el-row> -->
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
                    <img :title="$t('xls')" @click="imprimir('xls')" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                    <el-form-item :label="$t('ordentrabajo.crew')">
                      <el-select clearable ref="crew" v-model="cuad_id" name="crew" :placeholder="$t('cuadrilla.select')"  style="width:250px;">
                        <el-option v-for="c in cuadrillas" :key="c.cuad_id" :label="c.cuad_descripcion" :value="c.cuad_id" >
                        </el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="24">
                    <img :title="$t('xls')" @click="imprimirFiltrado('xls')" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
                  </el-col>
                </el-row>
            </el-form>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getCuadrillas } from '@/api/cuadrilla'
import { printReporteEjecutado, printReporteEjecutadoFiltrado } from '@/api/reporte'
export default {
  data () {
    return {
      cuad_id: 6,
      tipo: 1,
      fecha_inicial: null,
      fecha_final: null,
      labelPosition: 'top',
      cuadrillas: null
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario',
      'tipo_inventario'
    ])
  },
  methods: {
    imprimir (formato) {
      printReporteEjecutado(this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.empresa.empr_id, this.usuario.usua_id, formato).then(response => {
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        const filename = response.headers['content-disposition'].split('filename=')[1]
        link.href = url
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
      })
    },
    imprimirFiltrado (formato) {
      printReporteEjecutadoFiltrado(this.cuad_id, this.fecha_inicial.getTime(), this.fecha_final.getTime(), this.empresa.empr_id, this.usuario.usua_id, formato).then(response => {
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        const filename = response.headers['content-disposition'].split('filename=')[1]
        link.href = url
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
      })
    }
  },
  beforeMount () {
    getCuadrillas().then(response => {
      console.log('Cuadrillas: ', response.data)
      this.cuadrillas = response.data
      this.fecha_inicial = new Date()
      this.fecha_inicial.setHours(0)
      this.fecha_inicial.setMinutes(0)
      this.fecha_inicial.setSeconds(0)
      this.fecha_inicial.setMilliseconds(0)
      this.fecha_final = this.fecha_inicial
    })
  }
}
</script>
