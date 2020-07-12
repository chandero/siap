<template>
    <el-dropdown trigger="click" class='alert-container' @command="listaSolicitud" title="Alerta de Solicitudes Por Vencer">
      <el-dropdown-menu slot="dropdown" />
      <div class="alert-wrapper" @click="listaSolicitud()">
       <el-badge class='alert-container' :max="99" :value="conteo">
        <i style="font-size: 32px; width: 32px; height: 32px;" class="el-icon-postcard"></i>
       </el-badge>
       <!-- <i class="el-icon-caret-bottom"></i> -->
      </div>
    </el-dropdown>
</template>

<script>
import { buscarPorVencer } from '@/api/solicitud'

export default {
  data () {
    return {
      listData: [],
      conteo: 0
    }
  },
  methods: {
    listaSolicitud () {
      this.$router.push({ path: '/solicitud/porvencer' })
    }
  },
  beforeMount () {
    buscarPorVencer().then(response => {
      this.listData = response.data
      this.listData.forEach(o => {
        this.conteo = this.conteo + 1
      })
    })
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.alert-container {
      height: 50px;
      margin-right: 30px;
      .alert-wrapper {
        cursor: pointer;
        position: relative;
        .alert-avatar {
          width: 32px;
          height: 32px;
        }
        .el-icon-caret-bottom {
          position: absolute;
          right: -20px;
          top: 15px;
          font-size: 12px;
        }
      }
}

</style>
