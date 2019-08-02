<template>
      <el-container>
        <el-header class="elemento_header">{{ $t('elemento.tabletitle') }}</el-header>
        <el-main>
        <el-container>
          <el-header>
            <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" circle disabled></el-button>
            <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          </el-header>
          <el-main>
          <el-table
        :data="tableData"
        width="100%" height="500">
        <el-table-column type="expand">
          <template slot-scope="props">
                <el-table 
                  :data="props.row.caracteristicas"
                  stripe
                  style="width:100%"
                 >
                  <el-table-column
                    :label="$t('elemento.characteristic')"
                    width="250"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ caracteristica(scope.row.cara_id) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="$t('caracteristica.value')"
                    width="250">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.elca_valor }}</span>
                    </template>
                  </el-table-column>             
              </el-table> 
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.description')"
          width="300"
          prop="elem_descripcion"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.elem_descripcion }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.code')"
          width="150"
          prop="elem_codigo">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.elem_codigo }}</span>
          </template>
        </el-table-column>             
        <el-table-column
          :label="$t('elemento.type')"
          width="180">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ getTipoElementoDescripcion(scope.row.tiel_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          align="right"
          width="200"
          >
          <template slot="header" slot-scope="slot">
              <el-input
                v-model="search"
                size="mini"
                placeholder="Filtro"
                @input="buscar(search)">
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
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"        
        :page-size="page_size"
        layout="sizes, prev, pager, next, total"
        :total="total">
      </el-pagination>      
    </el-main>
   </el-container>
  </el-main>
 </el-container>  
</template>

<script>
import { getTodos } from '@/api/elemento'
import { deleteElemento } from '@/api/elemento'
import { getTiposElemento } from '@/api/tipoelemento'
import { getCaracteristicas } from '@/api/caracteristica'

export default {
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      tiposElemento: [],
      caracteristicas: [],
      search: '',
      filter: 'f:'
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({ path: '/administracion/elemento/editar/' + row.elem_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('elemento.confirmationmsg') + ' "' + row.elem_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteElemento(row.elem_id).then(response => {
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
      this.getElementos()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getElementos()
    },
    actualizar() {
      this.getElementos()
    },
    nuevo() {
      this.$router.push({ path: '/administracion/elemento/crear' })
    },
    buscar(query) {
      this.filter = 'f:' + query.toUpperCase()
      this.current_page = 1
      this.getElementos()
    },
    getElementos() {
      getTodos(this.filter, this.page_size, this.current_page)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.elementos
        }).catch(() => {})
    },
    getTipoElementoDescripcion(id) {
      if (id !== undefined & id !== null && id > 0) {
        if (this.tiposElemento.lenght > 0) {
          return this.tiposElemento.find(o => o.tiel_id === id, { tiel_descripcion: '' }).tiel_descripcion
        } else {
          return ''
        }
      } else { return '' }
    },
    caracteristica(cara_id) {
      if (cara_id === null) {
        return ''
      } else {
        return this.caracteristicas.find(o => o.cara_id === cara_id, '').cara_descripcion
      }
    },
    getTipos() {
      getTiposElemento().then(response => {
        this.tiposElemento = response.data
      }).catch(() => {})
    },
    getCaracteristicas() {
      getCaracteristicas().then(response => {
        this.caracteristicas = response.data
      }).catch(() => {})
    }
  },
  mounted() {
    getTiposElemento().then(response => {
      this.tiposElemento = response.data
      this.getElementos()
    }).catch(error => {
      console.log(error)
    })
    this.getCaracteristicas()
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