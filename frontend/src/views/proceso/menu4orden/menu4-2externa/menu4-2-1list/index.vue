<template>
  <el-container>
    <el-header class="medidor_header">{{ $t('orhi.tabletitle') }}</el-header>
    <el-main>
      <el-container>
        <el-header>
          <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo"></el-button>
          <el-button size="mini" type="warning" icon="el-icon-search" circle disabled></el-button>
          <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
        </el-header>
        <el-main>
          <el-table
            :data="tableData.filter(data => !search || data.orhi_consecutivo.toLowerCase().includes(search.toLowerCase()) || (data.orhi_direccion !== null && data.orhi_direccion.toLowerCase().includes(search.toLowerCase())))"
            width="100%" height="500">
            <el-table-column :label="$t('orhi.orhi_consecutivo')" width="100" prop="orhi_consecutivo">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.orhi_consecutivo }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('orhi.orhi_fechasolucion')" width="100" prop="orhi_fechasolucion">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.orhi_fechasolucion }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('orhi.orhi_objeto')" width="350" prop="orhi_objeto">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.orhi_objeto }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('orhi.orhi_direccion')" width="350" prop="orhi_direccion">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.orhi_direccion }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('orhi.barr_descripcion')" width="350" prop="barr_id">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ barrio(scope.row.barr_id) }}</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" align="right" width="200">
              <template slot="header">
                <el-input v-model="search" size="mini" placeholder="Filtro">
                </el-input>
              </template>
              <template slot-scope="scope">
                <el-button size="mini" title="Editar" @click="handleEdit(scope.$index, scope.row)"><i
                    class="el-icon-edit"></i></el-button>
                <el-button size="mini" type="success" title="Informe" @click="handleInforme(scope.$index, scope.row)"><i
                    class="el-icon-date"></i></el-button>
                <el-button size="mini" type="danger" title="Eliminar" @click="handleDelete(scope.$index, scope.row)"><i
                    class="el-icon-delete"></i></el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-main>
      </el-container>
    </el-main>
  </el-container>
</template>

<script>
import { getOrders, deleteOrder } from '@/api/ordentrabajohistoria'
import { getBarriosEmpresa } from '@/api/barrio'

export default {
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      barrios: [],
      search: ''
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({ path: '/proceso/menu4orden/menu4-2externa/menu4-2-3edit/' + row.orhi_id })
      console.log(index, row)
    },
    handleInforme(index, row) {
      this.$router.push({ path: '/proceso/menu4orden/menu4-2externa/menu4-2-4info/' + row.orhi_consecutivo })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('orhi.confirmationmsg') + ' "' + row.orhi_objeto + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteOrder(row.orhi_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getOrdens()
        }).catch((err) => {
          this.$message({
            type: 'error',
            message: this.$i18n.t('general.deletefail') + ' -> ' + err.msg
          })
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$i18n.t('general.deletecancelled')
        })
      })
      console.log(index, row)
    },
    handleSizeChange(val) {
      this.page_size = val
      this.getOrden()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getOrden()
    },
    actualizar() {
      this.getOrden()
    },
    nuevo() {
      this.$router.push({ path: '/proceso/menu4orden/menu4-2externa/menu4-2-3edit' })
    },
    getOrden() {
      getOrders()
        .then(response => {
          this.tableData = response.data
        }).catch(() => { })
    },
    barrio(id) {
      if (id === null) {
        return ''
      } else {
        return this.barrios.find(e => e.barr_id === id, { barr_descripcion: '' }).barr_descripcion
      }
    }
  },
  mounted() {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.getOrden()
    }).catch(error => {
      console.log(error)
    })
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.header {
  .el-header {
    .empresa_header {
      font-size: 24px;
    }
  }
}

td {
  padding: 4px 0;
}

span.el-pagination__total {
  font-size: 16px;
}
</style>
