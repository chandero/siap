<template>
    <el-card class="box-card">
     <div slot="header" class="clearfix">
      <span>{{ $t('route.unidadcreate') }}</span>
     </div>
     <span>{{ $t('unidad.description')}}</span>
     <el-input name="descripcion" v-model="unidad.unid_descripcion" v-validate="'required'"></el-input>
     <span>{{ errors.first('descripcion') }}</span>
     <p/>
     <span>{{ $t('unidad.symbol')}}</span>
     <el-input name="simbolo" v-model="unidad.unid_abreviatura" v-validate="'required'"></el-input>
     <p/>
     <span>{{ $t('unidad.type')}}</span>
     <el-select v-model="unidad.unid_tipo" name="tipo" :placeholder="$t('unidad.select')">
        <el-option v-for="tipo in tipos" :key="tipo.tipo_id" :label="$t(tipo.tipo_descripcion)" :value="tipo.tipo_descripcion" >
        </el-option>       
     </el-select>
     <p/>
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
    </el-card>
</template>
<script>
import { saveUnidad } from '@/api/unidad'

export default {
  data() {
    return {
      unidad: {
        unid_descripcion: '',
        unid_abreviatura: '',
        unid_tipo: '',
        unid_estado: 1,
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
      saveUnidad(this.unidad).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.unidad.unid_descripcion && this.unidad.unid_abreviatura && this.unidad.unid_tipo) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.unidad = {
        unid_descripcion: '',
        unid_abreviatura: '',
        unid_tipo: '',
        unid_estado: 1,
        usua_id: 0
      }
      this.$router.push({ path: '/administracion/unidad' })
    },
    limpiar() {
      this.usuario = {
        unid_descripcion: '',
        unid_abreviatura: '',
        unid_tipo: '',
        unid_estado: 1,
        usua_id: 0
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('unidad.success'),
        message: this.$i18n.t('unidad.created') + ' ' + this.unidad.unid_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('unidad.error'),
        message: this.$i18n.t('unidad.notcreated') + ' ' + e,
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
</style>