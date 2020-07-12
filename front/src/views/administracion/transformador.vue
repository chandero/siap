<template>
      <el-container>
        <el-header class="tranformador_header">{{ $t('gestion.transformador.tabletitle') }}</el-header>
        <el-main>
        <el-container>
          <el-header>
            <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" circle disabled></el-button>
            <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          </el-header>
          <el-main>
          <el-table
        :data="tableData.filter(data => !search || data.tran_numero.toLowerCase().includes(search.toLowerCase()) || (data.tran_direccion !== null && data.tran_direccion.toLowerCase().includes(search.toLowerCase())) || (data.barr_descripcion !== null && data.barr_descripcion.toLowerCase().includes(search.toLowerCase())))"
        width="100%" height="500">
        <el-table-column
          :label="$t('gestion.transformador.numero')"
          width="100"
          prop="tran_numero"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.tran_numero }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('gestion.transformador.direccion')"
          width="350"
          prop="tran_direccion">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.tran_direccion }}</span>
          </template>
        </el-table-column>             
        <el-table-column
          :label="$t('gestion.transformador.barr_descripcion')"
          width="150"
          prop="barr_descripcion">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ barrio(scope.row.barr_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          align="right"
          width="200"
          >
          <template slot="header" slot-scope="scope">
              <el-input
                v-model="search"
                size="mini"
                placeholder="Filtro">
              </el-input>
          </template>
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
    </el-main>
   </el-container>
  </el-main>
 </el-container>  
</template>

<script>
import { getTransformadors } from '@/api/transformador'
import { deleteTransformador } from '@/api/transformador'
import { getBarriosEmpresa } from '@/api/barrio'

export default {
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      search: ''
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({ path: '/administracion/transformador/editar/' + row.tran_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('gestion.transformador.confirmationmsg') + ' "' + row.tran_numero + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteTransformador(row.tran_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getTransformadores()
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
      this.getTransformadores()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getTransformadores()
    },
    actualizar() {
      this.getTransformadores()
    },
    nuevo() {
      this.$router.push({ path: '/administracion/transformador/crear' })
    },
    barrio(id) {
      if (id === null) {
        return ''
      } else {
        return this.barrios.find(e => e.barr_id === id, { barr_descripcion: '' }).barr_descripcion
      }
    },
    getTransformadores() {
      getTransformadors()
        .then(response => {
          this.tableData = response.data
        }).catch(() => {})
    },
    getBarrios() {
      getBarriosEmpresa().then(response => {
        this.barrios = response.data
        this.getTransformadores()
      }).catch(error => {
        console.log(error)
      })
    }
  },
  mounted() {
    this.getBarrios()
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