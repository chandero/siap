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
        <span>{{ $t('gestion.medidor.direccion')}}</span>
        <el-input name="direccion" v-model="medidor.medi_direccion" v-validate="'required'"></el-input>
        <span>{{ errors.first('direccion') }}</span>
        <p/> 
        <span>{{ $t('gestion.medidor.acta')}}</span>
        <el-input name="acta" v-model="medidor.medi_acta" v-validate="'required'"></el-input>
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
        <p/>
        <el-row>
          <el-col :span="24" style="font-weight: bold; text-align: center;"><span >DATOS DEL EQUIPO DE MEDIDA</span></el-col>
        </el-row>
        <el-row :gutter="4">
          <el-col :xs="9" :sm="9" :md="9" :lg="9" :xl="9">
            <span>&nbsp;</span>
          </el-col>
          <el-col :xs="10" :sm="10" :md="10" :lg="10" :xl="10" style="text-align: center;">
            <span>EXISTENTES Y/O RETIRADOS</span>
          </el-col>
          <el-col :xs="5" :sm="5" :md="5" :lg="5" :xl="5">
            <span>&nbsp;</span>
          </el-col>
        </el-row>        
        <el-row :gutter="4">
          <el-col :xs="9" :sm="9" :md="9" :lg="9" :xl="9">
            <span>DESCRIPCION</span>
          </el-col>
          <el-col :xs="5" :sm="5" :md="5" :lg="5" :xl="5">
            <span>ACTIVA</span>
          </el-col>
          <el-col :xs="5" :sm="5" :md="5" :lg="5" :xl="5">
            <span>REACTIVA</span>
          </el-col>
          <el-col :xs="5" :sm="5" :md="5" :lg="5" :xl="5">
            <span>NUEVO</span>
          </el-col>
        </el-row>        
        <el-row v-for="d in medidor.datos" :key="d.metd_id" :gutter="4">
          <el-col :xs="9" :sm="9" :md="9" :lg="9" :xl="9">
            <span>{{ d.metd_descripcion }}</span>
          </el-col>
          <el-col :xs="5" :sm="5" :md="5" :lg="5" :xl="5">
            <el-input v-model="d.meda_activa" />
          </el-col>
          <el-col :xs="5" :sm="5" :md="5" :lg="5" :xl="5">
            <el-input v-model="d.meda_reactiva" />
          </el-col>
          <el-col :xs="5" :sm="5" :md="5" :lg="5" :xl="5">
            <el-input v-model="d.meda_nuevo" />
          </el-col>
        </el-row>            
        <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
      </el-form>
     </el-main>
    </el-container>
</template>
<script>
import { saveMedidor, getMedidorTablaDato } from '@/api/medidor'
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
        medi_acta: null,
        datos: []
      },
      tiposMedidor: [],
      tiposMarcaMedidor: [],
      tiposCuenta: [],
      medidorTablaDato: [],
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
      saveMedidor(this.medidor).then(response => {
        if (response.status === 201) {
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
        message: this.$i18n.t('gestion.medidor.created') + ' ' + this.medidor.medi_direccion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('gestion.medidor.error'),
        message: this.$i18n.t('gestion.medidor.notcreated') + ' ' + e,
        onClose: this.limpiar()
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
      }).catch(() => {})
    },
    getTablaDatos() {
      getMedidorTablaDato().then(response => {
        this.medidorTablaDato = response.data
        this.medidorTablaDato.forEach(d => {
          var dato = {
            medi_id: null,
            metd_id: d.metd_id,
            metd_descripcion: d.metd_descripcion,
            meda_activa: null,
            meda_reactiva: null,
            meda_nuevo: null
          }
          this.medidor.datos.push(dato)
        })
      })
    }
  },
  mounted() {
    this.getTipos()
    this.getTiposMarcaMedidor()
    this.getTiposCuenta()
    this.getTablaDatos()
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