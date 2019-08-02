<template>
      <el-container>
        <el-header class="medidor_header">{{ $t('gestion.medidor.tabletitle') }}</el-header>
        <el-main>
        <el-container>
          <el-header>
            <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" circle disabled></el-button>
            <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          </el-header>
          <el-main>
          <el-table
        :data="tableData.filter(data => !search || data.medi_numero.toLowerCase().includes(search.toLowerCase()) || (data.medi_direccion !== null && data.medi_direccion.toLowerCase().includes(search.toLowerCase())))"
        width="100%" height="500">
        <el-table-column
          :label="$t('gestion.medidor.numero')"
          width="100"
          prop="medi_numero"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.medi_numero }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('gestion.medidor.direccion')"
          width="350"
          prop="medi_direccion">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.medi_direccion }}</span>
          </template>
        </el-table-column>             
        <el-table-column
          :label="$t('gestion.medidor.tipo.title')"
          width="80"
          prop="amet_id">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ getTipoMedidorDescripcion(scope.row.amet_id) }}</span>
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
import { getMedidors } from '@/api/medidor'
import { deleteMedidor } from '@/api/medidor'
import { getTiposMedidor } from '@/api/tipomedidor'

export default {
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      tiposMedidor: [],
      search: ''
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({ path: '/administracion/medidor/editar/' + row.medi_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('medidor.confirmationmsg') + ' "' + row.medi_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteMedidor(row.medi_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getElementos()
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
      this.getMedidores()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getMedidores()
    },
    actualizar() {
      this.getMedidores()
    },
    nuevo() {
      this.$router.push({ path: '/administracion/medidor/crear' })
    },
    getMedidores() {
      getMedidors()
        .then(response => {
          this.tableData = response.data
        }).catch(() => {})
    },
    getTipoMedidorDescripcion(id) {
      if (id === null || id === undefined || id === 0) {
        return ''
      } else {
        return this.tiposMedidor.find(o => o.amet_id === id, { amet_descripcion: '' }).amet_descripcion
      }
    }
  },
  mounted() {
    getTiposMedidor().then(response => {
      console.log('tipos medidor: ' + JSON.stringify(response.data))
      this.tiposMedidor = response.data
      this.getMedidores()
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