<template>
    <el-container>
     <el-header slot="header" class="clearfix">
      <span>{{ $t('route.transformadorcreate') }}</span>
     </el-header>
     <el-main>
       <el-form>
        <span>{{ $t('gestion.transformador.numero')}}</span>
        <el-input name="numero" v-model="transformador.tran_numero" v-validate="'required'"></el-input>
        <span>{{ errors.first('descripcion') }}</span>
        <p/>
        <span>{{ $t('gestion.transformador.direccion')}}</span>
        <el-input name="direccion" v-model="transformador.tran_direccion" v-validate="'required'"></el-input>
        <span>{{ errors.first('direccion') }}</span>
        <p/>
        <span>{{ $t('gestion.transformador.barr_descripcion')}}</span>
        <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="transformador.barr_id" name="barrio" :placeholder="$t('barrio.select')">
        <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
        </el-option>
        </el-select>
        <p/>
        <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
      </el-form>
     </el-main>
    </el-container>
</template>
<script>
import { updateTransformador, getTransformador } from '@/api/transformador'
import { getBarriosEmpresa } from '@/api/barrio'

export default {
  data () {
    return {
      transformador: {
        tran_id: null,
        tran_numero: null,
        empr_id: null,
        usua_id: null,
        tran_direccion: null,
        barr_id: null,
        tran_estado: null
      },
      barrios: [],
      isIndeterminate: false,
      checkAll: false,
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0
    }
  },
  methods: {
    aplicar () {
      updateTransformador(this.transformador).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate () {
      if (this.transformador.tran_direccion && this.transformador.tran_numero) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack () {
      this.transformador = {
        tran_id: null,
        tran_numero: null,
        tran_direccion: null,
        amem_id: null,
        amet_id: null,
        aacu_id: null,
        empr_id: null,
        usua_id: null
      }
    },
    limpiar () {
      this.transformador = {
        tran_id: null,
        tran_numero: null,
        tran_direccion: null,
        amem_id: null,
        amet_id: null,
        aacu_id: null,
        empr_id: null,
        usua_id: null
      }
    },
    success () {
      this.$notify({
        title: this.$i18n.t('gestion.transformador.success'),
        message: this.$i18n.t('gestion.transformador.updated') + ' ' + this.transformador.tran_direccion,
        type: 'success'
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('gestion.transformador.error'),
        message: this.$i18n.t('gestion.transformador.notupdated') + ' ' + e
      })
    },
    handleSizeChange (val) {
      this.page_size = val
    },
    handleCurrentChange (val) {
      this.current_page = val
    },
    getBarrios () {
      getBarriosEmpresa().then(response => {
        this.barrios = response.data
        this.obtenerTransformador()
      }).catch(error => {
        console.log(error)
      })
    },
    obtenerTransformador () {
      getTransformador(this.$route.params.id).then(response => {
        if (response.status === 200) {
          this.transformador = response.data
        }
      }).catch(error => {
        console.log('error transformador: ' + error)
      })
    }
  },
  mounted () {
    this.getBarrios()
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
