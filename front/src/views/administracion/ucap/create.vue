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
import { saveUcap } from '@/api/ucap'

export default {
  data() {
    return {
      ucap: {
        ucap_descripcion: '',
        ucap_abreviatura: '',
        ucap_tipo: '',
        ucap_estado: 1,
        usua_id: 0
      },
      tipos: [
        {
          tipo_id: 1,
          tipo_descripcion: 'Integer'
        },
        {
          tipo_id: 2,
          tipo_descripcion: 'Float'
        },
        {
          tipo_id: 3,
          tipo_descripcion: 'Date'
        },
        {
          tipo_id: 4,
          tipo_descripcion: 'Time'
        },
        {
          tipo_id: 5,
          tipo_descripcion: 'DateTime'
        },
        {
          tipo_id: 6,
          tipo_descripcion: 'String'
        }
      ],
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      passwordFieldType: 'password'
    }
  },
  methods: {
    aplicar() {
      saveUcap(this.ucap).then(response => {
        if (response.status === 201) {
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
        message: this.$i18n.t('ucap.created') + ' ' + this.ucap.ucap_descripcion,
        type: 'success',
        onClose: this.limpiar()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('ucap.error'),
        message: this.$i18n.t('ucap.notcreated') + ' ' + e
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 480px;
}
</style>