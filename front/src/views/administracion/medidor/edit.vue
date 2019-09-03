<template>
    <el-container>
     <el-header slot="header" class="clearfix">
      <span>{{ $t('route.medidorcreate') }}</span>
     </el-header>
     <el-main>
       <el-form>
        <span>{{ $t('gestion.medidor.numero')}}</span>
        <el-input name="numero" v-model="medidor.medi_numero" v-validate="'required'"></el-input>
        <span>{{ errors.first('descripcion') }}</span>
        <p/>
        <span>{{ $t('gestion.medidor.acta')}}</span>
        <el-input name="acta" v-model="medidor.medi_acta" v-validate="'required'"></el-input>
        <span>{{ errors.first('direccion') }}</span>
        <p/>        
        <span>{{ $t('gestion.medidor.direccion')}}</span>
        <el-input name="direccion" v-model="medidor.medi_direccion" v-validate="'required'"></el-input>
        <span>{{ errors.first('direccion') }}</span>
        <p/>     
        <span>{{ $t('gestion.medidor.tipo.title')}}</span>
        <el-select v-model="medidor.amet_id" filterable clearable name="tipo" :placeholder="$t('gestion.medidor.tipo.select')">
          <el-option v-for="t in tiposMedidor" :key="t.amet_id" :label="t.amet_descripcion" :value="t.amet_id">
          </el-option>       
        </el-select>
        <p/>     
        <span>{{ $t('gestion.medidor.marca.title')}}</span>
        <el-select v-model="medidor.amem_id" filterable clearable name="tipo" :placeholder="$t('gestion.medidor.marca.select')">
          <el-option v-for="t in tiposMarcaMedidor" :key="t.amem_id" :label="t.amem_descripcion" :value="t.amem_id">
          </el-option>       
        </el-select>   
        <p/>     
        <span>{{ $t('gestion.medidor.cuentaap.title')}}</span>
        <el-select v-model="medidor.aacu_id" filterable clearable name="cuenta" :placeholder="$t('gestion.medidor.cuentaap.select')">
          <el-option v-for="t in tiposCuenta" :key="t.aacu_id" :label="t.aacu_descripcion" :value="t.aacu_id">
          </el-option>       
        </el-select>            
        <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
      </el-form>
     </el-main>
    </el-container>
</template>
<script>
import { updateMedidor, getMedidor } from '@/api/medidor'
import { getTiposMedidor } from '@/api/tipomedidor'
import { getAapMedidorMarcas } from '@/api/aap_medidor_marca'
import { getAapCuentasAp } from '@/api/aap_cuentaap'

export default {
  data() {
    return {
      medidor: {
        medi_id: null,
        medi_numero: null,
        medi_direccion: null,
        amem_id: null,
        amet_id: null,
        aacu_id: null,
        empr_id: null,
        usua_id: null,
        medi_acta: null
      },
      tiposMedidor: [],
      tiposMarcaMedidor: [],
      tiposCuenta: [],
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
    aplicar() {
      updateMedidor(this.medidor).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.medidor.medi_direccion && this.medidor.medi_numero) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.medidor = {
        medi_id: null,
        medi_numero: null,
        medi_direccion: null,
        amem_id: null,
        amet_id: null,
        aacu_id: null,
        empr_id: null,
        usua_id: null,
        medi_acta: null
      }
    },
    limpiar() {
      this.medidor = {
        medi_id: null,
        medi_numero: null,
        medi_direccion: null,
        amem_id: null,
        amet_id: null,
        aacu_id: null,
        empr_id: null,
        usua_id: null,
        medi_acta: null
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('gestion.medidor.success'),
        message: this.$i18n.t('gestion.medidor.updated') + ' ' + this.medidor.medi_direccion,
        type: 'success'
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('gestion.medidor.error'),
        message: this.$i18n.t('gestion.medidor.notupdated') + ' ' + e
      })
    },
    handleSizeChange(val) {
      this.page_size = val
    },
    handleCurrentChange(val) {
      this.current_page = val
    },
    getTipos() {
      getTiposMedidor().then(response => {
        this.tiposMedidor = response.data
      }).catch(() => {})
    },
    getTiposMarcaMedidor() {
      getAapMedidorMarcas().then(response => {
        this.tiposMarcaMedidor = response.data
      }).catch(() => {})
    },
    getTiposCuenta() {
      getAapCuentasAp().then(response => {
        this.tiposCuenta = response.data
        getMedidor(this.$route.params.id).then(response => {
          this.medidor = response.data
        })
      }).catch(() => {})
    }
  },
  mounted() {
    this.getTipos()
    this.getTiposMarcaMedidor()
    this.getTiposCuenta()
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