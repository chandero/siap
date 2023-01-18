<template>
  <el-container>
    <el-header>
      <el-row :gutter="4">
        <el-col :span="24">
          <span>Nueva Mano de Obra</span>
        </el-col>
      </el-row>
    </el-header>
    <el-main>
      <el-form>
        <el-row :gutter="4">
          <el-col :span="4">
            <el-form-item label="Tipo Mano de Obra">
              <el-select v-model="manoobra.maob_tipo" name="tipo" :placeholder="$t('manoobra.tipo')">
                <el-option v-for="t in tiposManoObra" :key="t.id" :label="t.descripcion" :value="t.id" >
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col>
            <el-form-item label="Descripción">
              <el-input v-model="manoobra.maob_descripcion" name="descripcion" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="4">
          <el-col :span="24">
            <el-button :disabled="!validate()" title="Guardar Mano de Obra" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-main>
  </el-container>
</template>
<script>
import { saveManoObra } from '@/api/manoobra'

export default {
  data () {
    return {
      manoobra: {
        maob_tipo: null,
        maob_descripcion: null,
        maob_estado: 1,
        empr_id: 0,
        usua_id: 0,
        precios: []
      },
      tiposManoObra: [
        { id: 1, descripcion: 'Mano de Obra' },
        { id: 2, descripcion: 'Herramienta' },
        { id: 3, descripcion: 'Transporte' },
        { id: 4, descripcion: 'Manejo Ambiental' }
      ],
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0
    }
  },
  methods: {
    aplicar () {
      saveManoObra(this.manoobra).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate () {
      if (this.manoobra.maob_descripcion && this.manoobra.maob_tipo) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack () {
      this.manoobra = {
        maob_tipo: null,
        maob_descripcion: null,
        maob_estado: 1,
        empr_id: 0,
        usua_id: 0
      }
      this.$router.push({ path: '/administracion/manoobra' })
    },
    limpiar () {
      this.manoobra = {
        maob_tipo: null,
        maob_descripcion: null,
        maob_estado: 1,
        empr_id: 0,
        usua_id: 0
      }
    },
    success () {
      this.$notify({
        title: this.$i18n.t('manoobra.success'),
        message: this.$i18n.t('manoobra.created') + ' ' + this.manoobra.maob_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('manoobra.error'),
        message: this.$i18n.t('manoobra.notcreated') + ' ' + e,
        onClose: this.limpiar()
      })
    }
  },
  mounted () {
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
