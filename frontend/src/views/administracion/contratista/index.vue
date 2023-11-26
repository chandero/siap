<template>
  <div class="cuadrilla-container">
    <code>
        <el-container>
          <el-header class="cuadrilla_header">
            <span>Contratista</span>
          </el-header>
          <el-container>
            <el-header>
              <el-button
                size="mini"
                type="primary"
                icon="el-icon-circle-plus"
                circle
                @click="nuevo"
              ></el-button>
              <el-button size="mini" icon="el-icon-search" circle></el-button>
            </el-header>
            <el-main>
              <el-table :data="tableData" style="width: 100%">
                <el-table-column label="Contratista" width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{
                      scope.row.cont_nombre
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Documento" width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{
                      scope.row.cont_documento
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Dirección" width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{
                      scope.row.cont_direccion
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Teléfono" width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{
                      scope.row.cont_telefono
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Correo" width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{
                      scope.row.cont_email
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('cuadrilla.state')" width="200">
                  <template slot-scope="scope">
                    <span style="margin-left: 10px">{{
                      scope.row.cont_estado == 1 ? 'Activo' : 'Inactivo'
                    }}</span>
                  </template>
                </el-table-column>
                <el-table-column
                  fixed="right"
                  :label="$t('table.accion')"
                  width="140"
                >
                  <template slot-scope="scope">
                    <el-button
                      size="mini"
                      @click="handleEdit(scope.$index, scope.row)"
                      ><i class="el-icon-edit"></i
                    ></el-button>
                    <el-button
                      size="mini"
                      type="danger"
                      @click="handleDelete(scope.$index, scope.row)"
                      ><i class="el-icon-delete"></i
                    ></el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :page-size="page_size"
                layout="sizes, prev, pager, next, total"
                :total="total"
              >
              </el-pagination>
            </el-main>
          </el-container>
        </el-container>
      </code>
  </div>
</template>

<script>
import { getTodos, deleteContratista } from '@/api/contratista'

export default {
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      unidades: []
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({
        path: '/administracion/contratista/editar/' + row.cont_id
      })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(
        'Se procederá a borrar la cuadrilla seleccionada ¿Desea continuar?' +
        ' "' +
        row.cont_nombre +
        '"',
        this.$i18n.t('general.warning'),
        {
          confirmButtonText: this.$i18n.t('general.ok'),
          cancelButtonText: this.$i18n.t('general.cancel'),
          type: 'warning'
        }
      )
        .then(() => {
          deleteContratista(row.cont_id)
            .then(response => {
              this.$message({
                type: 'success',
                message: this.$i18n.t('general.deletesuccessful')
              })
              this.getContratista()
            })
            .catch(err => {
              this.$message({
                type: 'error',
                message: this.$i18n.t('general.deletefail') + ' -> ' + err.msg
              })
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: this.$i18n.t('general.deletecancelled')
          })
        })
      console.log(index, row)
    },
    handleSizeChange(val) {
      this.page_size = val
      this.getContratista()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getContratista()
    },
    nuevo() {
      this.$router.push({ path: '/administracion/contratista/crear' })
    },
    getContratista() {
      getTodos(this.page_size, this.current_page)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.contratista
          console.log('total cuadrillas:' + this.total)
        })
        .catch(() => { })
    }
  },
  mounted() {
    this.getContratista()
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
