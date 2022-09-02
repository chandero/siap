<template>
    <el-container class="box-card">
     <el-header>
      <span>{{ $t('route.cuadrillaedit') }}</span>
     </el-header>
     <el-main>
     <el-form>
       <el-form-item :label="$t('cuadrilla.description')">
         <el-input name="descripcion" v-model="cuadrilla.cuad_descripcion" v-validate="'required'"></el-input>
       </el-form-item>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-form>
    </el-main>
    </el-container>
</template>
<script>
import { getCuadrilla, updateCuadrilla } from '@/api/cuadrilla'

export default {
  data () {
    return {
      cuadrilla: {
        cuad_descripcion: '',
        cuad_estado: 1
      },
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1
    }
  },
  methods: {
    aplicar () {
      updateCuadrilla(this.cuadrilla).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate () {
      if (this.cuadrilla.cuad_descripcion) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack () {
      this.limpiar()
      this.$router.push({ path: '/administracion/cuadrilla' })
    },
    limpiar () {
      this.cuadrilla = {
        cuad_descripcion: '',
        cuad_estado: 1
      }
    },
    success () {
      this.$notify({
        title: this.$i18n.t('cuadrilla.success'),
        message: this.$i18n.t('cuadrilla.updated') + ' ' + this.cuadrilla.cuad_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('cuadrilla.error'),
        message: this.$i18n.t('cuadrilla.notupdated') + ' ' + e,
        onClose: this.limpiar()
      })
    }
  },
  mounted () {
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
