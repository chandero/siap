<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.barriocreate') }}</span>
     </div>
     <span>{{ $t('barrio.description')}}</span>
     <el-input v-model="barrio.barr_descripcion" @input="barrio.barr_descripcion = $event.toUpperCase()" autofocus></el-input>
     <span>{{ $t('barrio.code')}}</span>
     <el-input v-model="barrio.barr_codigo" @input="barrio.barr_codigo = $event.toUpperCase()"></el-input>
     <span>{{ $t('barrio.departamento')}}</span>
     <el-select v-model="barrio.depa_id" name="departamento" :placeholder="$t('departamento.select')" v-on:change="actualizarMunicipios()" filterable remote :remote-method="remoteMethodDepartamentos" :loading="loading_depa">
        <el-option v-for="departamento in departamentos" :key="departamento.depa_id" :label="departamento.depa_descripcion" :value="departamento.depa_id" >
        </el-option>       
     </el-select>
     <p/>
     <span>{{ $t('barrio.municipio')}}</span>
     <el-select v-model="barrio.muni_id" name="municipio" :placeholder="$t('municipio.select')" filterable remote :remote-method="remoteMethodMunicipios" :loading="loading_muni">
        <el-option v-for="municipio in municipios" :key="municipio.muni_id" :label="municipio.muni_descripcion" :value="municipio.muni_id" >
        </el-option>       
     </el-select>
     <p/>
     <span>{{ $t('barrio.type')}}</span>
     <el-select v-model="barrio.tiba_id" name="tipobarrio" :placeholder="$t('tipobarrio.select')">
        <el-option v-for="tipobarrio in tiposbarrio" :key="tipobarrio.tiba_id" :label="tipobarrio.tiba_descripcion" :value="tipobarrio.tiba_id" >
        </el-option>       
     </el-select>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { getDepartamentos } from '@/api/departamento'
import { getMunicipios } from '@/api/municipio'
import { getTipoBarrio, saveBarrio } from '@/api/barrio'

export default {
  data() {
    return {
      departamentos_lista: [],
      departamentos: [],
      departamento: {
        depa_id: '',
        depa_descripcion: ''
      },
      municipios_lista: [],
      municipios: [],
      municipio: {
        muni_id: '',
        muni_descripcion: ''
      },
      barrio: {
        barr_id: '',
        barr_descripcion: '',
        barr_codigo: '',
        depa_id: '',
        muni_id: '',
        tiba_id: '',
        usua_id: ''
      },
      tiposbarrio: [],
      message: '',
      loading_depa: false,
      loading_muni: false,
      ruta: ''
    }
  },
  methods: {
    aplicar() {
      saveBarrio(this.barrio).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.barrio.barr_descripcion && this.barrio.barr_codigo && this.barrio.depa_id && this.barrio.muni_id && this.barrio.tiba_id) {
        return true
      } else {
        return false
      }
    },
    actualizarMunicipios() {
      if (this.barrio.depa_id) {
        getMunicipios(this.barrio.depa_id).then(response => {
          this.barrio.muni_id = ''
          this.municipios = response.data
        }).catch(error => {
          console.log(error)
        })
      }
    },
    remoteMethodDepartamentos(query) {
      if (query !== '') {
        this.loading_depa = true
        setTimeout(() => {
          this.loading_depa = false
          this.departamentos = this.departamentos_lista.filter(item => {
            console.log('item =>' + item)
            return item.depa_descripcion.toLowerCase()
              .indexOf(query.toLowerCase()) > -1
          })
        }, 200)
      } else {
        this.barrio.depa_id = ''
      }
    },
    remoteMethodMunicipios(query) {
      if (query !== '') {
        this.loading_muni = true
        setTimeout(() => {
          this.loading_muni = false
          this.municipios = this.municipios_lista.filter(item => {
            console.log('item =>' + item)
            return item.muni_descripcion.toLowerCase()
              .indexOf(query.toLowerCase()) > -1
          })
        }, 200)
      } else {
        this.barrio.muni_id = ''
      }
    },
    limpiarAndBack() {
      this.ruta = '/administracion/barrio/lista/' + this.barrio.depa_id + '/' + this.barrio.muni_id
      this.barrio.barr_descripcion = ''
      this.barrio.barr_codigo = ''
      this.barrio.barr_estado = 1
      this.barrio.tiba_id = 1
      this.barrio.usua_id = 0
      // this.$router.push({ path: this.ruta })
    },
    limpiar() {
      this.barrio = {
        barr_descripcion: '',
        barr_codigo: '',
        barr_estado: 1,
        depa_id: this.departamento.depa_id,
        muni_id: this.municipio.muni_id,
        tiba_id: 1,
        usua_id: 0
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('barrio.success'),
        message: this.$i18n.t('barrio.created') + ' ' + this.barrio.barr_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('barrio.error'),
        message: this.$i18n.t('barrio.notcreated') + ' ' + e,
        onClose: this.limpiar()
      })
    }
  },
  mounted() {
    this.departamento.depa_id = parseInt(this.$route.params.did)
    this.municipio.muni_id = parseInt(this.$route.params.mid)
    getDepartamentos().then(response => {
      this.departamentos = response.data
      this.departamentos_lista = response.data
      getMunicipios(this.departamento.depa_id).then(response => {
        this.municipios = response.data
        this.municipios_lista = response.data
        getTipoBarrio().then(response => {
          this.tiposbarrio = response.data
          this.limpiar()
        })
      }).catch(error => {
        this.barrio.muni_id = ''
        console.log(error)
      })
    }).catch(error => {
      this.barrio.depa_id = ''
      console.log(error)
    })
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 480px;
}
</style>
