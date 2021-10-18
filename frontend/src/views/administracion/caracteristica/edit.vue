<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.caracteristicacreate') }}</span>
     </div>
     <span>{{ $t('caracteristica.description')}}</span>
     <el-input name="descripcion" v-model="caracteristica.cara_descripcion" v-validate="'required'"></el-input>
     <span>{{ errors.first('descripcion') }}</span>
     <p/>
     <span>{{ $t('caracteristica.unit')}}</span>
     <el-select v-model="caracteristica.unid_id" name="tipo" :placeholder="$t('unidad.select')">
        <el-option v-for="unidad in unidades" :key="unidad.unid_id" :label="$t(unidad.unid_descripcion)" :value="unidad.unid_id" >
        </el-option>
     </el-select>
     <p/>
     <span>{{ $t('caracteristica.values')}}</span>
     <el-input type="textarea" :rows="4" v-model="caracteristica.cara_valores"></el-input>
     <span class="footfont">{{ $t('caracteristica.fmtvalues')}}</span>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { getUnidadesTodas } from '@/api/unidad'
import { getCaracteristica, updateCaracteristica } from '@/api/caracteristica'

export default {
  data () {
    return {
      caracteristica: {
        cara_descripcion: '',
        cara_estado: 1,
        cara_valores: '',
        unid_id: '',
        usua_id: 0
      },
      unidades: [],
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1
    }
  },
  methods: {
    aplicar () {
      updateCaracteristica(this.caracteristica).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate () {
      if (this.caracteristica.cara_descripcion && this.caracteristica.unid_id && this.caracteristica.cara_valores) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack () {
      this.caracteristica = {
        cara_descripcion: '',
        cara_estado: 1,
        cara_valores: '',
        unid_id: '',
        usua_id: 0
      }
      this.$router.push({ path: '/administracion/caracteristica' })
    },
    limpiar () {
      this.caracteristica = {
        cara_descripcion: '',
        cara_estado: 1,
        unid_id: 0,
        usua_id: 0
      }
    },
    success () {
      this.$notify({
        title: this.$i18n.t('caracteristica.success'),
        message: this.$i18n.t('caracteristica.updated') + ' ' + this.caracteristica.cara_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('caracteristica.error'),
        message: this.$i18n.t('caracteristica.notupdated') + ' ' + e,
        onClose: this.limpiar()
      })
    },
    getUnidades () {
      getUnidadesTodas().then(response => {
        this.unidades = response.data
      }).catch(() => {})
    }
  },
  mounted () {
    this.getUnidades()
    getCaracteristica(this.$route.params.id).then(response => {
      this.caracteristica = response.data
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
.footfont {
  font-size: 11px;
}
</style>
