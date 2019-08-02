<template>
  <el-container>
      <el-header>
        <span>{{ $t('route.empresacreate') }}</span>
      </el-header>
      <el-main>
          <el-form :model="empresa" :label-position="labelPosition" :rules="rules">
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="empr_descripcion" :label="$t('empresa.description')">
                  <el-input v-model="empresa.empr_descripcion" @input="empresa.empr_descripcion = $event.toUpperCase()"></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="empr_sigla" :label="$t('empresa.sigla')">
                  <el-input v-model="empresa.empr_sigla" @input="empresa.empr_sigla = $event.toUpperCase()"></el-input>                  
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="empr_identificacion" :label="$t('empresa.nit')">
                  <el-input v-model="empresa.empr_identificacion"></el-input>                  
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="empr_direccion" :label="$t('empresa.address')">
                  <el-input v-model="empresa.empr_direccion"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="empr_telefono" :label="$t('empresa.phone')">
                  <el-input v-model="empresa.empr_telefono"></el-input>
                </el-form-item>    
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="empr_concesion" :label="$t('empresa.contract')">
                  <el-input v-model="empresa.empr_concesion"></el-input>
                </el-form-item>    
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="depa_id" :label="$t('empresa.departamento')">
                  <el-select v-model="empresa.depa_id" name="departamento" :placeholder="$t('departamento.select')" v-on:change="actualizarMunicipios()" filterable>
                    <el-option v-for="departamento in departamentos" :key="departamento.depa_id" :label="departamento.depa_descripcion" :value="departamento.depa_id" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="muni_id" :label="$t('empresa.municipio')">
                  <el-select v-model="empresa.muni_id" name="municipio" :placeholder="$t('municipio.select')" filterable >
                    <el-option v-for="municipio in municipios" :key="municipio.muni_id" :label="municipio.muni_descripcion" :value="municipio.muni_id" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
              </el-col>
            </el-row>
          </el-form>
      </el-main>
  </el-container>
</template>
<script>
import { getDepartamentos } from '@/api/departamento'
import { getMunicipios } from '@/api/municipio'
import { getEmpresa, updateEmpresa } from '@/api/empresa'

export default {
  data() {
    return {
      labelPosition: 'top',
      empresa: {
        empr_descripcion: '',
        empr_sigla: '',
        empr_identificacion: '',
        empr_direccion: '',
        empr_telefono: '',
        empr_estado: 1,
        depa_id: '',
        muni_id: '',
        usua_id: 0
      },
      departamentos_lista: [],
      departamentos: [],
      departamento: {
        depa_id: 0,
        depa_descripcion: ''
      },
      municipios_lista: [],
      municipios: [],
      municipio: {
        muni_id: 0,
        muni_descripcion: ''
      },
      barrios: [],
      barrio: {
        barr_id: 0,
        barr_descripcion: ''
      },
      message: '',
      loading: false,
      rules: {
        empr_descripcion: [
          { required: true, message: 'Debe diligencia el Nombre de la Empresa', trigger: 'blur' }
        ],
        empr_sigla: [
          { required: true, message: 'Debe Diligenciar la Sigla de la Empresa', trigger: 'blur' }
        ],
        empr_identificacion: [
          { required: true, message: 'Debe Diligenciar el número de identificación de la Empresa', trigger: 'blur' }
        ],
        empr_direccion: [
          { required: false, message: 'Debe Diligenciar la dirección de la empresa', trigger: 'blur' }
        ],
        empr_telefono: [
          { required: false, message: 'Debe Diligenciar el número de teléfono de la empresa', trigger: 'blur' }
        ],
        empr_concesion: [
          { required: false, message: 'Debe Diligenciar el número de contrato de concesión', trigger: 'blur' }
        ],
        depa_id: [
          { required: true, message: 'Debe Seleccionar el Departamento de Ubicación de la Empresa', trigger: 'change' }
        ],
        muni_id: [
          { required: true, message: 'Debe Seleccionar el Municipio de Ubicación de la Empresa', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    aplicar() {
      updateEmpresa(this.empresa).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.empresa.empr_descripcion && this.empresa.empr_sigla && this.empresa.empr_identificacion && this.empresa.depa_id && this.empresa.muni_id) {
        return true
      } else {
        return false
      }
    },
    actualizarMunicipios() {
      if (this.empresa.depa_id) {
        getMunicipios(this.empresa.depa_id).then(response => {
          this.empresa.muni_id = ''
          this.municipios = response.data
          this.municipios_lista = response.data
        }).catch(error => {
          console.log(error)
        })
      }
    },
    remoteMethodDepartamentos(query) {
      if (query !== '') {
        this.loading = true
        setTimeout(() => {
          this.loading = false
          this.departamentos = this.departamentos_lista.filter(item => {
            console.log('item =>' + item)
            return item.depa_descripcion.toLowerCase()
              .indexOf(query.toLowerCase()) > -1
          })
        }, 200)
      } else {
        this.empresa.depa_id = ''
      }
    },
    remoteMethodMunicipios(query) {
      if (query !== '') {
        this.loading = true
        setTimeout(() => {
          this.loading = false
          this.municipios = this.municipios_lista.filter(item => {
            console.log('item =>' + item)
            return item.muni_descripcion.toLowerCase()
              .indexOf(query.toLowerCase()) > -1
          })
        }, 200)
      } else {
        this.empresa.muni_id = ''
      }
    },
    limpiarAndBack() {
      this.empresa = {
        empr_descripcion: '',
        empr_sigla: '',
        empr_identificacion: '',
        empr_direccion: '',
        empr_telefono: '',
        empr_estado: 1,
        depa_id: '',
        muni_id: '',
        usua_id: 0
      }
      this.$router.push({ path: '/administracion/empresa' })
    },
    success() {
      this.$notify({
        title: this.$i18n.t('empresa.success'),
        message: this.$i18n.t('empresa.updated') + ' ' + this.empresa.empr_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('empresa.error'),
        message: this.$i18n.t('empresa.notupdated') + ' ' + e
      })
    }
  },
  mounted() {
    getDepartamentos().then(response => {
      this.departamentos = response.data
      this.departamentos_lista = response.data
    }).catch(error => {
      console.log(error)
    })
    getEmpresa(this.$route.params.id).then(response => {
      this.empresa = response.data
      getMunicipios(this.empresa.depa_id).then(response => {
        this.municipios = response.data
        this.municipios_lista = response.data
      }).catch(error => {
        console.log(error)
      })
    }).catch(error => {
      console.log(error)
    })
  }
}
</script>
