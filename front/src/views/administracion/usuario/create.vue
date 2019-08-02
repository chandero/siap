<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.usuariocreate') }}</span>
     </div>
     <span>{{ $t('usuario.lastname')}}</span>
     <el-input name="apellido" v-model="usuario.usua_apellido" @input="usuario.usua_apellido = $event.toUpperCase()" v-validate="'required'"></el-input>
     <span>{{ errors.first('apellido') }}</span>
     <p/>
     <span>{{ $t('usuario.name')}}</span>
     <el-input name="nombre" v-model="usuario.usua_nombre" @input="usuario.usua_nombre = $event.toUpperCase()" v-validate="'required'"></el-input>
     <span>{{ $t('usuario.email')}}</span>
     <el-input name="email" v-model="usuario.usua_email" v-validate="'required|email'" @input="usuario.usua_email = $event.toLowerCase()"></el-input>
     <span>{{ errors.first('email') }}</span>
     <p/>
     <span>{{ $t('usuario.password')}}</span>
     <div><el-input name="clave" v-model="usuario.usua_clave" v-validate="'required'" :type="passwordFieldType"></el-input><el-button size="mini" type="primary" icon="el-icon-view" @click="cambiarVisibilidad"></el-button><el-button size="mini" type="primary" icon="el-icon-refresh" @click="generate"></el-button></div>
     <span>{{ errors.first('clave') }}</span>
     <p/>
     <span>{{ $t('perfil.title')}}</span>
     <el-select v-model="usuario.perf_id" name="perfil" :placeholder="$t('perfil.select')">
        <el-option v-for="perfil in perfiles" :key="perfil.perf_id" :label="perfil.perf_descripcion" :value="perfil.perf_id" >
        </el-option>       
     </el-select>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { getPerfiles } from '@/api/perfil'
import { saveUsuario } from '@/api/usuario'
import { generatePassword } from '@/api/util'

export default {
  data() {
    return {
      usuario: {
        usua_apellido: '',
        usua_nombre: '',
        usua_email: '',
        usua_clave: '',
        perf_id: ''
      },
      perfiles: [],
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      passwordFieldType: 'password'
    }
  },
  methods: {
    cambiarVisibilidad() {
      this.passwordFieldType = this.passwordFieldType === 'password' ? 'text' : 'password'
    },
    aplicar() {
      saveUsuario(this.usuario).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.usuario.usua_apellido && this.usuario.usua_nombre && this.usuario.usua_email && this.usuario.perf_id) {
        return true
      } else {
        return false
      }
    },
    validEmail: function(email) {
      var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
      return re.test(email)
    },
    generate: function() {
      this.usuario.usua_clave = generatePassword()
    },
    limpiarAndBack() {
      this.usuario = {
        usua_apellido: '',
        usua_nombre: '',
        usua_email: '',
        perf_id: ''
      }
      this.$router.push({ path: '/administracion/usuario' })
    },
    limpiar() {
      this.usuario = {
        usua_apellido: '',
        usua_nombre: '',
        usua_email: '',
        perf_id: ''
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('usuario.success'),
        message: this.$i18n.t('usuario.created') + ' ' + this.usuario.usua_apellido + ' ' + this.usuario.usua_nombre,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('empresa.error'),
        message: this.$i18n.t('empresa.notcreated') + ' ' + e,
        onClose: this.limpiar()
      })
    }
  },
  mounted() {
    getPerfiles(this.page_size, this.current_page).then(response => {
      this.perfiles = response.data.perfiles
      this.total = response.data.total
    }).catch(error => {
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
