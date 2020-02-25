<template>
    <el-dropdown trigger="click" class='alert-container' @command="listaReporte" title="Alerta de Reportes Vencidos">
      <div class="alert-wrapper">
       <el-badge class='alert-container' :max="99" :value="conteo">
        <i style="font-size: 32px; width: 32px; height: 32px;" class="el-icon-bell"></i>
       </el-badge>
       <i class="el-icon-caret-bottom"></i>
      </div>
     <el-dropdown-menu slot="dropdown">
        <el-dropdown-item v-for="(item) in listData" :key="item.reti_descripcion">
          <span @click="listaReporte(item.reti_id)" style="display:block;">{{item.reti_descripcion}} - {{item.pendiente}}</span>
        </el-dropdown-item>
      </el-dropdown-menu> 
    </el-dropdown>
</template>

<script>
import { siap_grafica_reporte_vencido } from '@/api/grafica'

export default {
  data() {
    return {
      listData: [],
      conteo: 0
    }
  },
  methods: {
    listaReporte(reti_id) {
      this.$router.push({ path: '/procesos/vencido' })
    }
  },
  beforeMount() {
    siap_grafica_reporte_vencido().then(response => {
      this.listData = response.data
      this.listData.forEach(o => {
        this.conteo = this.conteo + o.pendiente
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


