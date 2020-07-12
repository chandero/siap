<template>
  <div class="usuario-container">
    <code>
      <el-container>
        <el-header class="usuario_header">{{ $t('usuario.tabletitle') }}</el-header>
        <el-container>
          <el-header>
            <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button size="mini" icon="el-icon-search" circle></el-button>
          </el-header>
          <el-main>
          <el-table
            :data="tableData"
            stripe
            style="width: 100%">
            <el-table-column
              :label="$t('usuario.lastname')"
              width="100">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.usua_apellido }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('usuario.name')"
              width="100">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.usua_nombre }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('usuario.email')"
              width="200">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.usua_email }}</span>
              </template>
            </el-table-column>
            <el-table-column
              fixed="right"
              :label="$t('table.accion')"
              width="180">
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  @click="handleEdit(scope.$index, scope.row)"><i class="el-icon-edit"></i></el-button>
                <el-button
                  size="mini"
                  type="danger"
                  @click="handleDelete(scope.$index, scope.row)"><i class="el-icon-delete"></i></el-button>
              </template>
            </el-table-column>
        </el-table>
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :page-size="10"
          layout="sizes, prev, pager, next, total"
          :total="total">
        </el-pagination>
      </el-main>
    </el-container>
  </el-container>
 </code>
 </div>
</template>

<script>
import { getTodos, deleteUsuario } from '@/api/usuario'

export default {
  data () {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0
    }
  },
  methods: {
    handleEdit (index, row) {
      this.$router.push({ path: '/administracion/usuario/editar/' + row.usua_id })
      console.log(index, row)
    },
    handleDelete (index, row) {
      this.$confirm(this.$i18n.t('usuario.confirmationmsg') + ' "' + row.usua_apellido + ' ' + row.usua_nombre + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteUsuario(row.usua_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getUsuarios()
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
    handleSizeChange (val) {
      this.page_size = val
      this.getUsuarios()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getUsuarios()
    },
    nuevo () {
      this.$router.push({ path: '/administracion/usuario/crear' })
    },
    getUsuarios () {
      getTodos(this.page_size, this.current_page)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.usuarios
        }).catch(() => {})
    }
  },
  mounted () {
    this.getUsuarios()
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
