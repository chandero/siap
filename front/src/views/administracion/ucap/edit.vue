<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.ucapcreate') }}</span>
     </div>
     <span>{{ $t('ucap.description')}}</span>
     <el-input name="descripcion" v-model="ucap.ucap_descripcion" v-validate="'required'"></el-input>
     <span>{{ errors.first('descripcion') }}</span>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { updateUcap } from '@/api/ucap'
import { getUcap } from '@/api/ucap'

export default {
  data() {
    return {
      ucap: {
        ucap_descripcion: '',
        ucap_estado: 1,
        usua_id: 0
      },
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      passwordFieldType: 'password'
    }
  },
  methods: {
    aplicar() {
      updateUcap(this.ucap).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.ucap.ucap_descripcion) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.ucap = {
        ucap_descripcion: '',
        ucap_estado: 1,
        usua_id: 0
      }
      this.$router.push({ path: '/administracion/ucap' })
    },
    limpiar() {
      this.usuario = {
        ucap_descripcion: '',
        ucap_estado: 1,
        usua_id: 0
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('ucap.success'),
        message: this.$i18n.t('ucap.updated') + ' ' + this.ucap.ucap_descripcion,
        type: 'success'
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('ucap.error'),
        message: this.$i18n.t('ucap.notupdated') + ' ' + e
      })
    }
  },
  mounted() {
    getUcap(this.$route.params.id).then(response => {
      this.ucap = response.data
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