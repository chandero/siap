<template>
    <el-container>
        <el-header>
            <span>{{ $t('reporte.reparar') }}</span>
        </el-header>
        <el-main>
          <el-button type="primary" @click="reparar()">{{ $t('reporte.reparar') }}</el-button>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { actualizarHistoria } from '@/api/postereporte'
export default {
  data() {
    return {
      labelPosition: 'top'
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  methods: {
    reparar() {
      actualizarHistoria().then(response => {
        this.$message({
          message: 'Reparación Finalizada',
          type: 'success'
        })
      })
    }
  }
}
</script>