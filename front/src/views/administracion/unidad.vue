<template>
  <div class="unidad-container">
    <code>
      <el-container>
        <el-header class="unidad_header">{{ $t('unidad.tabletitle') }}</el-header>
        <el-container>
          <el-header>
            <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button size="mini" icon="el-icon-search" circle></el-button>
          </el-header>
          <el-main>
          <el-table
        :data="tableData"
        style="width: 100%">
        <el-table-column
          :label="$t('unidad.description')"
          width="400"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.unid_descripcion }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('unidad.simbol')"
          width="100">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.unid_abreviatura }}</span>
          </template>
        </el-table-column>             
        <el-table-column
          :label="$t('unidad.type')"
          width="100">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ $t(scope.row.unid_tipo) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('unidad.state')"
          width="100">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.unid_estado }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="140">
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
        :page-size="page_size"
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
import { getTodos } from '@/api/unidad'
import { deleteUnidad } from '@/api/unidad'

export default {
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({ path: '/administracion/unidad/editar/' + row.unid_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('unidad.confirmationmsg') + ' "' + row.unid_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteUnidad(row.unid_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getUnidades()
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
      this.getUnidades()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getUnidades()
    },
    nuevo() {
      this.$router.push({ path: '/administracion/unidad/crear' })
    },
    getUnidades() {
      getTodos(this.page_size, this.current_page)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.unidades
          console.log('total unidades:' + this.total)
        }).catch(() => {})
    }
  },
  mounted() {
    this.getUnidades()
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