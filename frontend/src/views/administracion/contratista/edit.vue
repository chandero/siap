<template>
  <el-container class="box-card">
    <el-header>
      <span>Editar Contratista</span>
    </el-header>
    <el-main>
      <el-form style="display: flex;">
        <el-form-item label="Nombre" style="margin: auto; margin-right: 10px;">
          <el-input name="Nombre" v-model="contratista.cont_nombre" v-validate="'required'"></el-input>
        </el-form-item>
        <el-form-item label="Documento" style="margin: auto; margin-right: 10px;">
          <el-input name="Documento" v-model="contratista.cont_documento" v-validate="'required'"></el-input>
        </el-form-item>
        <el-form-item label="Dirección" style="margin: auto; margin-right: 10px;">
          <el-input name="Direccion" v-model="contratista.cont_direccion" v-validate="'required'"></el-input>
        </el-form-item>
        <el-form-item label="Teléfono" style="margin: auto; margin-right: 10px;">
          <el-input name="Telefono" v-model="contratista.cont_telefono" v-validate="'required'"></el-input>
        </el-form-item>
        <el-form-item label="Email" style="margin: auto; margin-right: 10px;">
          <el-input name="Email" v-model="contratista.cont_email" v-validate="'required'"></el-input>
        </el-form-item>
        <el-form-item label="Estado" style="margin: auto; margin-right: 10px;">
          <el-select name="estado" v-model="contratista.cont_estado" v-validate="'required'">
            <el-option v-for="item in options_cont_estado" :key="item.value" :label="item.label" :value="item.value">
            </el-option></el-select>
        </el-form-item>
      </el-form>
      <section style="padding-top: 50px;">
        <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
      </section>
    </el-main>
  </el-container>
</template>
<script>
import { getContratista, updateContratista } from '@/api/contratista'

export default {
  data() {
    return {
      contratista: {
        cont_nombre: '',
        cont_documento: '',
        cont_direccion: '',
        cont_telefono: '',
        cont_email: '',
        cont_estado: true
      },
      options_cont_estado: [
        { value: true, label: 'Activo' },
        { value: false, label: 'Inactivo' }
      ],
      selectables: [],
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1
    }
  },
  methods: {
    aplicar() {
      updateContratista(this.contratista)
        .then(response => {
          this.success()
        })
        .catch(error => {
          this.error(error)
        })
    },
    validate() {
      if (this.contratista.cont_nombre) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.limpiar()
      this.$router.push({ path: '/administracion/contratista' })
    },
    limpiar() {
      this.contratista = {
        cont_nombre: '',
        cont_documento: '',
        cont_direccion: '',
        cont_telefono: '',
        cont_email: '',
        cont_estado: 1
      }
    },
    success() {
      this.$notify({
        title: 'Se ha editado el contratista correctamente',
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: 'Error al editar el Contratista',
        type: 'error',
        onClose: this.limpiar()
      })
    }
  },
  mounted() {
    getContratista(this.$route.params.id)
      .then(response => {
        this.contratista = response.data
      })
      .catch(() => { })
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 100%;
}

.footfont {
  font-size: 11px;
}
</style>
