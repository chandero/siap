<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.cuadrillaedit') }}</span>
     </div>
     <span>{{ $t('cuadrilla.description')}}</span>
     <el-input name="descripcion" v-model="cuadrilla.cuad_descripcion" v-validate="'required'"></el-input>
     <span>{{ errors.first('descripcion') }}</span>
     <p/>
     <el-transfer 
       style="text-align: left; display: inline-block"
       v-model="cuadrilla.usuarios" :data="selectables"
       :format="{
        noChecked: '${total}',
        hasChecked: '${checked}/${total}'
       }"
      :titles="[$t('available'), $t('selected')]"
     >
     </el-transfer>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { getCuadrilla, updateCuadrilla } from '@/api/cuadrilla'
import { getParaCuadrilla } from '@/api/usuario'

export default {
  data() {
    return {
      cuadrilla: {
        cuad_descripcion: '',
        cuad_estado: 1,
        usua_id: 0,
        usuarios: []
      },
      usuarios: [],
      selectables: [],
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1
    }
  },
  methods: {
    aplicar() {
      updateCuadrilla(this.cuadrilla).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.cuadrilla.cuad_descripcion) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.limpiar()
      this.$router.push({ path: '/administracion/cuadrilla' })
    },
    limpiar() {
      this.cuadrilla = {
        cuad_descripcion: '',
        cuad_estado: 1,
        usua_id: 0,
        usuarios: []
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('cuadrilla.success'),
        message: this.$i18n.t('cuadrilla.updated') + ' ' + this.cuadrilla.cuad_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('cuadrilla.error'),
        message: this.$i18n.t('cuadrilla.notupdated') + ' ' + e,
        onClose: this.limpiar()
      })
    },
    getUsuarios() {
      getParaCuadrilla().then(response => {
        response.data.forEach(usuario => {
          this.selectables.push({
            key: usuario.usua_id,
            label: usuario.usua_nombre + ' ' + usuario.usua_apellido,
            disabled: false
          })
        })
      }).catch(() => {})
    }
  },
  mounted() {
    this.getUsuarios()
    getCuadrilla(this.$route.params.id).then(response => {
      this.cuadrilla = response.data
    }).catch(() => {})
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 560px;
}
.footfont {
  font-size: 11px;
}
</style>