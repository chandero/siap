<template>
    <el-container>
     <el-header slot="header" class="clearfix">
      <span>{{ $t('route.transformadorcreate') }}</span>
     </el-header>
     <el-main>
       <el-form>
        <span>{{ $t('gestion.transformador.numero')}}</span>
        <el-input name="numero" v-model="transformador.aap_numero"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tran_codigo_apoyo')}}</span>
        <el-input name="numero" v-model="transformador.aap_codigo_apoyo"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.direccion')}}</span>
        <el-input name="direccion" v-model="transformador.aap_direccion"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.barr_descripcion')}}</span>
        <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="transformador.barr_id" name="barrio" :placeholder="$t('barrio.select')">
        <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
        </el-option>
        </el-select>
        <p/>
        <span>{{ $t('gestion.transformador.tran_propietario')}}</span>
        <el-input name="direccion" v-model="transformador.aap_propietario"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tran_marca')}}</span>
        <el-input name="direccion" v-model="transformador.aap_marca"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tran_serial')}}</span>
        <el-input name="direccion" v-model="transformador.aap_serial"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tran_kva')}}</span>
        <el-input type="number" name="direccion" v-model="transformador.aap_kva" @keypress="transformador.kva = parseFloat($event)"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tipo_id')}}</span>
          <el-form-item
            prop="transformador.tipo_id"
          >
            <el-select
              clearable
              filterable
              ref="post"
              v-model="transformador.tipo_id"
              name="post"
              :placeholder="$t('gestion.post.select')"
            >
              <el-option
                v-for="post in postes"
                :key="post.tipo_id"
                :label="post.tipo_descripcion"
                :value="post.tipo_id"
              ></el-option>
            </el-select>
          </el-form-item>
        <p/>
        <span>{{ $t('gestion.transformador.tran_fases')}}</span>
        <el-input name="direccion" v-model="transformador.aap_fases"></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tran_tension_p')}}</span>
        <el-input type="number" name="direccion" v-model="transformador.aap_tension_p" @keypress="transformador.aap_tension_p = parseFloat($event)" ></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tran_tension_s')}}</span>
        <el-input type="number" name="direccion" v-model="transformador.aap_tension_s" @keypress="transformador.aap_tension_s = parseFloat($event)" ></el-input>
        <p/>
        <span>{{ $t('gestion.transformador.tran_referencia')}}</span>
        <el-input name="direccion" v-model="transformador.aap_referencia"></el-input>
        <p/>
        <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
      </el-form>
     </el-main>
    </el-container>
</template>
<script>
import { saveTransformador } from '@/api/transformador'
import { getBarriosEmpresa } from '@/api/barrio'
import { getCaracteristica } from '@/api/caracteristica'
export default {
  data () {
    return {
      transformador: {
        aap_id: null,
        empr_id: null,
        aap_numero: null,
        aap_direccion: null,
        barr_id: null,
        usua_id: null,
        aap_estado: null,
        aap_codigo_apoyo: null,
        aap_propietario: null,
        aap_marca: null,
        aap_serial: null,
        aap_kva: null,
        tipo_id: null,
        aap_fases: null,
        aap_tension_p: null,
        aap_tension_s: null,
        aap_referencia: null
      },
      barrios: [],
      isIndeterminate: false,
      checkAll: false,
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0,
      postes: []
    }
  },
  methods: {
    aplicar () {
      this.transformador.aap_id = parseInt(this.transformador.aap_numero)
      this.transformador.aap_kva = parseFloat(this.transformador.aap_kva)
      this.transformador.aap_tension_p = parseFloat(this.transformador.aap_tension_p)
      this.transformador.aap_tension_s = parseFloat(this.transformador.aap_tension_s)
      saveTransformador(this.transformador).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate () {
      if (this.transformador.aap_direccion && this.transformador.aap_numero) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack () {
      this.transformador = {
        aap_id: null,
        empr_id: null,
        aap_numero: null,
        aap_direccion: null,
        barr_id: null,
        usua_id: null,
        aap_estado: null,
        aap_codigo_apoyo: null,
        aap_propietario: null,
        aap_marca: null,
        aap_serial: null,
        aap_kva: null,
        tipo_id: null,
        aap_fases: null,
        aap_tension_p: null,
        aap_tension_s: null,
        aap_referencia: null
      }
    },
    limpiar () {
      this.transformador = {
        aap_id: null,
        empr_id: null,
        aap_numero: null,
        aap_direccion: null,
        barr_id: null,
        usua_id: null,
        aap_estado: null,
        aap_codigo_apoyo: null,
        aap_propietario: null,
        aap_marca: null,
        aap_serial: null,
        aap_kva: null,
        tipo_id: null,
        aap_fases: null,
        aap_tension_p: null,
        aap_tension_s: null,
        aap_referencia: null
      }
    },
    success () {
      this.$notify({
        title: this.$i18n.t('gestion.transformador.success'),
        message: this.$i18n.t('gestion.transformador.created') + ' ' + this.transformador.aap_direccion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('gestion.transformador.error'),
        message: this.$i18n.t('gestion.transformador.notcreated') + ' ' + e,
        onClose: this.limpiar()
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
      }).catch(error => {
        console.log(error)
      })
    }
  },
  mounted () {
    this.getBarrios()
    getCaracteristica(8).then((response) => {
      const poste = response.data.cara_valores.split(',')
      for (var i = 0; i < poste.length; i++) {
        this.postes.push({ tipo_id: i + 1, tipo_descripcion: poste[i] })
      }
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
