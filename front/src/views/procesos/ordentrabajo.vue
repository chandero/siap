<template>
    <el-container>
      <el-main>
      <el-container>
        <el-header class="ordentrabajo_header">{{ $t('operativo.workordertitle') }}
        </el-header>
        <el-main>  
          <vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabels" :styled="qstyled" :maxDepth="3"></vue-query-builder>
          <el-button type="warning" icon="el-icon-search" circle @click="actualizar"></el-button>
        </el-main>        
      </el-container>
      <el-container>
          <el-header>
            <el-button type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          </el-header>
          <el-main>
        <el-table
        :data="tableData"
        stripe
        :default-sort = "{prop: 'ortr_id', order: 'descending'}"
        style="width: 100%"
        max-height="600"
        border
        @sort-change="handleSort"
        @filter-change="handleFilter">
        <el-table-column
          :label="$t('ordentrabajo.consecutive')"
          width="120"
          sortable="custom"
          prop="ortr_consecutivo"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.ortr_consecutivo }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('ordentrabajo.date')"
          width="210"
          sortable="custom"
          prop="ortr_fecha"   
          resizable       
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.ortr_fecha }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('ordentrabajo.crew')"
          min-width="200"
          sortable="custom"
          prop="cuad_descripcion"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ cuadrilla(scope.row.cuad_id) }}</span>
          </template>
        </el-table-column>   
        <el-table-column
          :label="$t('barrio.type')"
          min-width="150"
          sortable="custom"
          prop="tiba_descripcion"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ sector(scope.row.tiba_id) }}</span>
          </template>
        </el-table-column>   
        <!---
        <el-table-column
          :label="$t('ordentrabajo.status')"
          width="100"
          prop="ortr_estado"
          sortable="custom"
          resizable
        >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.ortr_estado }}</span>
          </template>
        </el-table-column>
        -->
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
              type="success"
              :title="$t('print')"
              @click="handlePrint(scope.$index, scope.row)"><i class="el-icon-printer"></i></el-button>
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
import VueQueryBuilder from 'vue-query-builder'

import { getTodos, deleteOrden, getEstados } from '@/api/ordentrabajo'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getCuadrillas } from '@/api/cuadrilla'

export default {
  components: {
    VueQueryBuilder
  },
  data() {
    return {
      ordentrabajo: {
        ortr_id: 0,
        ortr_fecha: new Date(),
        ortr_observacion: '',
        ortr_estado: 1,
        cuad_id: null,
        tiba_id: null,
        usua_id: 0,
        empr_id: 0,
        reportes: []
      },
      reporte: {
        repo_id: 0,
        repo_consecutivo: 0,
        repo_numero: null,
        repo_fecharecepcion: null,
        repo_direccion: null,
        repo_nombre: null,
        repo_telefono: null,
        repo_fechasolucion: null,
        repo_horainicio: null,
        repo_horafin: null,
        repo_reportetecnico: null,
        repo_descripcion: null,
        rees_id: 1,
        rees_descripcion: null,
        acti_id: null,
        acti_descripcion: null,
        orig_id: null,
        orig_descripcion: null,
        barr_id: null,
        barr_descripcion: null,
        empr_id: 0,
        usua_id: 0
      },
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      qbquery: {},
      qtiposbarrio: [{ label: this.$i18n.t('tipobarrio.select'), value: '' }],
      qestados: [{ label: this.$i18n.t('estado.select'), value: '' }],
      qrules: [
        {
          type: 'custom',
          id: 'o.ortr_id',
          label: this.$i18n.t('ordentrabajo.consecutive'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'b.tiba_id',
          label: this.$i18n.t('barrio.type'),
          choices: []
        },
        {
          type: 'select',
          id: 'o.ortr_estado',
          label: this.$i18n.t('ordentrabajo.status'),
          choices: []
        }
      ],
      qlabels: {
        matchType: this.$i18n.t('qb.matchType'),
        matchTypeAll: this.$i18n.t('qb.matchTypeAll'),
        matchTypeAny: this.$i18n.t('qb.matchTypeAny'),
        addRule: this.$i18n.t('qb.addRule'),
        removeRule: this.$i18n.t('qb.removeRule'),
        addGroup: this.$i18n.t('qb.addGroup'),
        removeGroup: this.$i18n.t('qb.removeGroup'),
        textInputPlaceholder: this.$i18n.t('qb.textInputPlaceholder')
      },
      qstyled: true,
      order: '',
      cuadrillas: [],
      sectores: [],
      estados: []
    }
  },
  methods: {
    cuadrilla(id) {
      if (id !== undefined && id !== null) {
        return this.cuadrillas.find(o => o.cuad_id === id, { cuad_descripcion: '' }).cuad_descripcion
      }
    },
    sector(id) {
      if (id !== undefined && id !== null) {
        return this.sectores.find(o => o.tiba_id === id, { tiba_descripcion: '' }).tiba_descripcion
      }
    },
    handleFilter(filters) {
      this.filter = JSON.stringify(filters)
      this.getOrdenes()
    },
    handleSort({ column, prop, order }) {
      console.log('column:' + JSON.stringify(column))
      console.log('prop:' + prop)
      console.log('order:' + order)
      if (prop !== null) {
        if (order === 'ascending') {
          this.order = prop + ' asc'
        } else {
          this.order = prop + ' desc'
        }
        this.filter = this.qbquery
        console.log('filter:' + JSON.stringify(this.filter))
        this.getOrdenes()
      }
    },
    handleEdit(index, row) {
      this.$router.push({ path: '/procesos/ordentrabajo/editar/' + row.ortr_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('ordentrabajo.confirmationmsg') + ' "' + row.ortr_id + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteOrden(row.ortr_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getOrdenes()
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
      this.getOrdenes()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getOrdenes()
    },
    getOrdenes() {
      getTodos(this.page_size, this.current_page, this.order, this.qbquery).then(response => {
        this.tableData = response.data.ordenes
        this.total = response.data.total
      }).catch(error => {
        console.log('Reportes: ' + error)
      })
    },
    actualizar() {
      this.getOrdenes()
    },
    nuevo() {
      this.$router.push({ path: '/procesos/ordentrabajo/crear' })
    }
  },
  mounted() {
    this.getOrdenes()
    this.qrules[1].choices = this.qtiposbarrio
    this.qrules[2].choices = this.qestados
  },
  created() {
    getTiposBarrio().then(response => {
      response.data.forEach(b => {
        this.qtiposbarrio.push({ label: b.tiba_descripcion, value: b.tiba_id })
      })
      this.sectores = response.data
    }).catch(error => {
      console.log(error)
    })
    getEstados().then(response => {
      this.estados = response.data
      this.estados.forEach(e => {
        this.qestados.push({ label: e.otes_descripcion, value: e.otes_id })
      })
    }).catch(error => {
      console.log('getEstados: ' + error)
    })
    getCuadrillas().then(response => {
      this.cuadrillas = response.data
    }).catch(error => {
      console.log('getCuadrillas: ' + error)
    })
  }
}
</script>
<style scoped>
td {
  padding: 4px 0;
}
span.el-pagination__total {
  font-size: 16px;
}
div.match-type-container {
    color: #333;
    background-color: #f5f5f5;
    border-color: #ddd;
}
.vue-query-builder >>> .vqb-group {
    color: #333;
    background-color: #f5f5f5;
    border-color: #ddd;
}
.vue-query-builder >>> .vqb-group.depth-2 {
    border-left: 2px solid #8bc34a;
}

.vue-query-builder >>> .vqb-group.depth-3 {
    border-left: 2px solid #ffb94b;
}

.vue-query-builder >>> .panel {
  margin-bottom: 20px;
  background-color: #fff;
  border: 1px solid transparent;
  border-radius: 4px;
  box-shadow: 0 1px 1px rgba(0,0,0,0.05)
}
.vue-query-builder >>> .panel-default {
  border-color: #ddd;
}

.vue-query-builder >>> .pull-right {
  float: right!important;
}

.vue-query-builder >>> .btn {
    display: inline-block;
    line-height: 1;
    white-space: nowrap;
    cursor: pointer;
    background: #fff;
    border: 1px solid #dcdfe6;
    color: #606266;
    -webkit-appearance: none;
    text-align: center;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    outline: 0;
    margin: 0;
    -webkit-transition: .1s;
    transition: .1s;
    font-weight: 500;
    padding: 12px 20px;
    font-size: 14px;
    border-radius: 4px;
}
.vue-query-builder >>> .btn:hover {
    color: #409eff;
    border-color: #c6e2ff;
    background-color: #ecf5ff;
}
.vue-query-builder >>> .btn-default {
  
}
</style>