<template>
      <el-container>
        <el-header class="medidor_header">{{ $t('muob.tabletitle') }}</el-header>
        <el-main>
        <el-container>
          <el-header>
            <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" circle disabled></el-button>
            <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          </el-header>
          <el-main>
          <el-table
        :data="tableData.filter(data => !search || data.muob_consecutivo.toLowerCase().includes(search.toLowerCase()) || (data.muob_direccion !== null && data.muob_direccion.toLowerCase().includes(search.toLowerCase())))"
        width="100%" height="500">
        <el-table-column
          :label="$t('muob.muob_consecutivo')"
          width="100"
          prop="muob_consecutivo"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.muob_consecutivo }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('muob.muob_fecharecepcion')"
          width="100"
          prop="muob_fecharecepcion"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.muob_fecharecepcion }}</span>
          </template>
        </el-table-column>   
        <el-table-column
          :label="$t('muob.muob_descripcion')"
          width="350"
          prop="muob_descripcion">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.muob_descripcion }}</span>
          </template>
        </el-table-column>             
        <el-table-column
          :label="$t('muob.muob_direccion')"
          width="350"
          prop="muob_direccion">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.muob_direccion }}</span>
          </template>
        </el-table-column> 
        <el-table-column
          :label="$t('muob.barr_descripcion')"
          width="350"
          prop="barr_id">
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
              title="Editar"
              @click="handleEdit(scope.$index, scope.row)"><i class="el-icon-edit"></i></el-button>
            <el-button
              size="mini"
              type="success"
              title="Informe"
              @click="handleInforme(scope.$index, scope.row)"><i class="el-icon-date"></i></el-button>
            <el-button
              size="mini"
              type="danger"
              title="Eliminar"
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
import { getMuobs, deleteMuob } from '@/api/muob'
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
      this.$router.push({ path: '/procesos/muob/editar/' + row.muob_id })
      console.log(index, row)
    },
    handleInforme(index, row) {
      this.$router.push({ path: '/procesos/muob/info/' + row.muob_consecutivo })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('muob.confirmationmsg') + ' "' + row.muob_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteMuob(row.muob_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getMuobs()
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
      this.getMuob()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getMuob()
    },
    actualizar() {
      this.getMuob()
    },
    nuevo() {
      this.$router.push({ path: '/procesos/muob/crear' })
    },
    getMuob() {
      getMuobs()
        .then(response => {
          this.tableData = response.data
        }).catch(() => {})
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
      this.getMuob()
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