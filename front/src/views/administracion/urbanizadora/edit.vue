<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.urbacreate') }}</span>
     </div>
     <span>{{ $t('urbanizadora.description')}}</span>
     <el-input name="descripcion" v-model="urba.urba_descripcion"></el-input>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { updateUrbanizadora } from '@/api/urbanizadora'
import { getUrbanizadora } from '@/api/urbanizadora'

export default {
  data() {
    return {
      urba: {
        urba_descripcion: '',
        urba_estado: 1,
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
      updateUrbanizadora(this.urba).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.urba.urba_descripcion) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.urba = {
        urba_descripcion: '',
        urba_estado: 1,
        usua_id: 0
      }
      this.$router.push({ path: '/administracion/urbanizadora' })
    },
    limpiar() {
      this.usuario = {
        urba_descripcion: '',
        urba_estado: 1,
        usua_id: 0
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('urba.success'),
        message: this.$i18n.t('urba.updated') + ' ' + this.urba.urba_descripcion,
        type: 'success'
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('urba.error'),
        message: this.$i18n.t('urba.notupdated') + ' ' + e
      })
    }
  },
  mounted() {
    getUrbanizadora(this.$route.params.id).then(response => {
      this.urba = response.data
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