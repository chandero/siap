<template>
    <el-container>
        <el-header>
            <span>Nuevo Código de Autorización para Abrir Reporte</span>
        </el-header>
        <el-main>
            <el-row :gutter="4">
                <el-col :xs="24" :sm="12" :md="4" :lg="4" :xl="4">
                  <el-button :disabled="codigo !== null" type="primary" @click="validar()">Generar Código</el-button>
                </el-col>
                <el-col :xs="24" :sm="12" :md="3" :lg="3" :xl="3">
                  <el-input readonly v-model="codigo"></el-input>
                </el-col>
                <el-col :xs="24" :sm="12" :md="3" :lg="3" :xl="3">
                    <el-button :disabled="codigo == null" type="success" icon="el-icon-tickets" circle title="Copiar en el portapapeles"
                        v-clipboard:copy="codigo"
                        v-clipboard:success="onCopy"
                        v-clipboard:error="onError"></el-button>
                </el-col>
            </el-row>
        </el-main>
    </el-container>
</template>
<script>
import { codigo } from '@/api/aap'

export default {
  data () {
    return {
      codigo: null
    }
  },
  methods: {
    onCopy (e) {
      this.$message({
        message: 'Código copiado en el portapapeles.',
        type: 'success'
      })
    },
    onError (e) {
      this.$message.error('Oops, no se pudo copiar en el portapapeles.')
    },
    validar () {
      this.$confirm('Desea generar un nuevo Código de Autorización?', 'Código de Autorización', {
        confirmButtonText: 'Sí',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        codigo(3).then(response => {
          if (response.status === 200) {
            this.codigo = response.data
          } else {
            this.$alert('No fue posible generar el código. Vuelva a intentarlo', 'Error', {
              confirmButtonText: 'Cerrar'
            })
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Generación cancelada'
        })
      })
    }
  }
}
</script>
