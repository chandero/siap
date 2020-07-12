<template>
  <div class="barrio-container">
    <code>
      <el-container>
        <el-header class="barrio_header">{{ $t('barrio.tabletitle') }}</el-header>
        <span>{{ $t('barrio.departamento')}}</span>
        <el-select v-model="barrio.depa_id" name="departamento" :placeholder="$t('departamento.select')" v-on:change="actualizarMunicipios()" filterable remote :remote-method="remoteMethodDepartamentos" :loading="loading" focus>
          <el-option v-for="departamento in departamentos" :key="departamento.depa_id" :label="departamento.depa_descripcion" :value="departamento.depa_id" >
          </el-option>       
        </el-select>
        <span>{{ $t('barrio.municipio')}}</span>
        <el-select v-model="barrio.muni_id" name="municipio" :placeholder="$t('municipio.select')" v-on:change="actualizarBarrios()"  filterable remote :remote-method="remoteMethodMunicipios" :loading="loading">
          <el-option v-for="municipio in municipios" :key="municipio.muni_id" :label="municipio.muni_descripcion" :value="municipio.muni_id" >
          </el-option>       
        </el-select>
       <el-container>
          <el-header>
            <el-button :disabled="!canCreate" size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
          </el-header>
          <el-main>
          <el-table
            :data="tableData.filter(data => !search || data.barr_descripcion.toLowerCase().includes(search.toLowerCase()))"
            style="width: 100%">
            <el-table-column
              :label="$t('barrio.description')"
              width="400"
              prop="barr_descripcion"
            >
          </el-table-column>
          <el-table-column
            :label="$t('barrio.code')"
            width="150"
            prop="barr_codigo">
          </el-table-column>
          <el-table-column
            align="right">
            <template slot="header" slot-scope="slot">
              <el-input
                v-model="search"
                size="mini"
                placeholder="Digite para Filtrar">
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
      </el-container>  
    </code>
  </div>
</template>

<script>
import { getBarrios } from '@/api/barrio'
import { deleteBarrio } from '@/api/barrio'
import { getDepartamentos } from '@/api/departamento'
import { getMunicipios } from '@/api/municipio'

export default {
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      search: '',
      departamentos_lista: [],
      departamentos: [],
      departamento: {
        depa_id: '',
        depa_descripcion: ''
      },
      municipios_lista: [],
      municipios: [],
      municipio: {
        muni_id: '',
        muni_descripcion: ''
      },
      barrios: [],
      barrio: {
        barr_id: '',
        barr_descripcion: '',
        barr_codigo: '',
        depa_id: '',
        muni_id: '',
        tiba_id: ''
      },
      message: '',
      loading: false,
      canCreate: false,
      canSearch: false
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({ path: '/administracion/barrio/editar/' + row.barr_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('barrio.confirmationmsg') + ' "' + row.barr_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteBarrio(row.barr_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getBarrios()
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
      this.getBarrios()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getBarrios()
    },
    nuevo() {
      this.$router.push({ path: '/administracion/barrio/crear/' + this.barrio.depa_id + '/' + this.barrio.muni_id })
    },
    getBarrios() {
      getBarrios(this.barrio.muni_id, this.page_size, this.current_page)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.barrios
          console.log('total barrios:' + this.total)
        }).catch(() => {})
    },
    actualizarMunicipios() {
      this.canCreate = false
      if (this.barrio.depa_id) {
        getMunicipios(this.barrio.depa_id).then(response => {
          this.barrio.muni_id = ''
          this.municipios = response.data
          this.municipios_lista = response.data
        }).catch(error => {
          console.log(error)
        })
      }
    },
    actualizarBarrios() {
      if (this.barrio.muni_id) {
        getBarrios(this.barrio.muni_id, this.page_size, this.current_page).then(response => {
          this.total = response.data.total
          this.tableData = response.data.barrios
          this.canCreate = true
          if (this.tableData.length > 0) {
            this.canSearch = true
          } else { this.canSearch = false }
        })
      }
    },
    remoteMethodDepartamentos(query) {
      if (query !== '') {
        this.loading = true
        setTimeout(() => {
          this.loading = false
          this.departamentos = this.departamentos_lista.filter(item => {
            console.log('item =>' + item)
            return item.depa_descripcion.toLowerCase()
              .indexOf(query.toLowerCase()) > -1
          })
        }, 200)
      } else {
        this.empresa.depa_id = ''
      }
    },
    remoteMethodMunicipios(query) {
      if (query !== '') {
        this.loading = true
        setTimeout(() => {
          this.loading = false
          this.municipios = this.municipios_lista.filter(item => {
            console.log('item =>' + item)
            return item.muni_descripcion.toLowerCase()
              .indexOf(query.toLowerCase()) > -1
          })
        }, 200)
      } else {
        this.empresa.muni_id = ''
      }
    }
  },
  mounted() {
    this.departamento.depa_id = parseInt(this.$route.params.did)
    this.municipio.muni_id = parseInt(this.$route.params.mid)
    getDepartamentos().then(response => {
      this.departamentos = response.data
      this.departamentos_lista = response.data
      if (this.departamento.depa_id) {
        this.barrio.depa_id = this.departamento.depa_id
        getMunicipios(this.departamento.depa_id).then(response => {
          this.municipios_lista = response.data
          this.municipios = response.data
          if (this.municipio.muni_id) {
            this.barrio.muni_id = this.municipio.muni_id
            this.actualizarBarrios()
          }
        }).catch(error => {
          console.log(error)
        })
      }
    }).catch(error => {
      console.log(error)
    })
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.header {
  .el-header {
    .barrio_header {
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