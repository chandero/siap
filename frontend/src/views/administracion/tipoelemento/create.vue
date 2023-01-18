<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.tipoelementocreate') }}</span>
     </div>
     <span>{{ $t('tipoelemento.description')}}</span>
     <el-input name="descripcion" v-model="tipoelemento.tiel_descripcion" v-validate="'required'"></el-input>
     <span>{{ errors.first('descripcion') }}</span>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { saveTipoElemento } from '@/api/tipoelemento'

export default {
  data () {
    return {
      tipoelemento: {
        tiel_descripcion: '',
        tiel_estado: 1,
        usua_id: 0
      },
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1
    }
  },
  methods: {
    aplicar () {
      saveTipoElemento(this.tipoelemento).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate () {
      if (this.tipoelemento.tiel_descripcion) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack () {
      this.tipoelemento = {
        tiel_descripcion: '',
        tiel_estado: 1,
        usua_id: 0
      }
      this.$router.push({ path: '/administracion/tipoelemento' })
    },
    limpiar () {
      this.tipoelemento = {
        tiel_descripcion: '',
        tiel_estado: 1,
        usua_id: 0
      }
    },
    success () {
      this.$notify({
        title: this.$i18n.t('tipoelemento.success'),
        message: this.$i18n.t('tipoelemento.created') + ' ' + this.tipoelemento.tiel_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('tipoelemento.error'),
        message: this.$i18n.t('tipoelemento.notcreated') + ' ' + e,
        onClose: this.limpiar()
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 480px;
}
.footfont {
  font-size: 11px;
}
</style>
