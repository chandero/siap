<template>
  <el-container class="historia-container">
    <el-main>
      <el-container>
        <el-header class="historia_header">{{ $t('historia.title') }}
        </el-header>
        <el-main>  
          <vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabels" :styled="qstyled" :maxDepth="3"></vue-query-builder>
          <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Actualizar Aplicando el Filtro"></el-button>
        </el-main>        
      </el-container>
      <el-container>
          <el-header>
            <!-- <el-button type="primary" icon="el-icon-plus" circle @click="nuevo" title="Crear Nueva Luminaria"></el-button> -->
            <el-button type="success" icon="el-icon-refresh" circle @click="actualizar" title="Refrescar Lista de Luminarias"></el-button>
          </el-header>
          <el-main>
          <el-table
        :data="tableData"
        stripe
        :default-sort = "{prop: 'aap_id', order: 'ascending'}"        
        style="width: 100%"
        max-height="600"
        border
        @sort-change="handleSort"
        @filter-change="handleFilter">
        <el-table-column type="expand">
          <template slot-scope="props">
                <el-table 
                  :data="props.row.historia"
                  stripe
                  border
                  style="width:100%"
                 >
                  <el-table-column
                    :label="$t('elemento.fecha')"
                    width="110"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aael_fecha | moment('YYYY/MM/DD') }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="$t('elemento.reporte_tipo')"
                    width="150"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ tipo_reporte(scope.row.reti_id) }}</span>
                    </template>
                  </el-table-column>                  
                  <el-table-column
                    :label="$t('elemento.reporte')"
                    width="110"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.repo_consecutivo }}</span>
                    </template>
                  </el-table-column>                  
                  <el-table-column
                    :label="$t('elemento.bombillo')">
                   <el-table-column
                    :label="$t('elemento.retirado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_bombillo_retirado }}</span>
                    </template>
                   </el-table-column>             
                   <el-table-column
                    :label="$t('elemento.instalado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_bombillo_instalado }}</span>
                    </template>
                   </el-table-column>
                  </el-table-column>
                  <el-table-column
                    :label="$t('elemento.balasto')">
                   <el-table-column
                    :label="$t('elemento.retirado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_balasto_retirado }}</span>
                    </template>
                   </el-table-column>             
                   <el-table-column
                    :label="$t('elemento.instalado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_balasto_instalado }}</span>
                    </template>
                   </el-table-column>
                  </el-table-column>           
                  <el-table-column
                    :label="$t('elemento.arrancador')">
                   <el-table-column
                    :label="$t('elemento.retirado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_arrancador_retirado }}</span>
                    </template>
                   </el-table-column>             
                   <el-table-column
                    :label="$t('elemento.instalado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_arrancador_instalado }}</span>
                    </template>
                   </el-table-column>
                  </el-table-column>                  
                  <el-table-column
                    :label="$t('elemento.condensador')">
                   <el-table-column
                    :label="$t('elemento.retirado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_condensador_retirado }}</span>
                    </template>
                   </el-table-column>             
                   <el-table-column
                    :label="$t('elemento.instalado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_condensador_instalado }}</span>
                    </template>
                   </el-table-column>
                  </el-table-column>                  
                  <el-table-column
                    :label="$t('elemento.fotocelda')">
                   <el-table-column
                    :label="$t('elemento.retirado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_fotocelda_retirado }}</span>
                    </template>
                   </el-table-column>             
                   <el-table-column
                    :label="$t('elemento.instalado')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_fotocelda_instalado }}</span>
                    </template>
                   </el-table-column>
                  </el-table-column>                  
              </el-table> 
          </template>
        </el-table-column>        
        <el-table-column
          :label="$t('historia.code')"
          width="150"
          sortable="custom"
          prop="aap_id"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_id }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('historia.support')"
          width="150"
          sortable="custom"
          prop="aap_apoyo"   
          resizable       
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_apoyo }}</span>
          </template>
        </el-table-column>
        <!--
        <el-table-column
          :label="$t('historia.description')"
          width="200"
          sortable="custom"
          prop="aap_descripcion"  
          resizable        
           >
          <template slot-scope="scope">
            <span >{{ scope.row.aap_descripcion }}</span>
          </template>
        </el-table-column>
        -->
        <el-table-column
          :label="$t('historia.address')"
          min-width="250"
          sortable="custom"
          prop="aap_direccion" 
          resizable
          >
          <template slot-scope="scope">
            <span >{{ scope.row.aap_direccion }}</span>
          </template>
        </el-table-column>   
        <el-table-column
          :label="$t('historia.neighborhood')"
          min-width="250"
          sortable="custom"
          prop="barr_id"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ barrio(scope.row.barr_id) }}</span>
          </template>
        </el-table-column>   
        <el-table-column
          :label="$t('historia.neighborhoodtype')"
          width="200"
          sortable="custom"
          prop="tiba_id">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ sector(scope.row.tiba_id) }}</span>
          </template>
        </el-table-column>                     
        <el-table-column :label="$t('historia.georeference')">
        <el-table-column
          :label="$t('historia.lat')"
          width="120">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_lat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('historia.lng')"
          width="120">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_lng }}</span>
          </template>
        </el-table-column>
        </el-table-column>
        <el-table-column 
          :label="$t('historia.connection')"
          width="120"
          prop="aaco_id"
          resizable
          >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ aap_conexion(scope.row.aaco_id) }}</span>
          </template>
        </el-table-column>
        <!--
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="93">
          <template slot-scope="scope">
            <el-button
              size="mini"
              circle
              type="warning"
              @click="handleEdit(scope.$index, scope.row)"
              title="Modificar Luminaria"><i class="el-icon-edit"></i></el-button>
            <el-button
              size="mini"
              circle
              type="danger"
              title="Borrar Luminaria"
              @click="handleDelete(scope.$index, scope.row)"><i class="el-icon-delete"></i></el-button>
          </template>
        </el-table-column>
        -->
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

import { getTodos } from '@/api/aap'
import { deleteAap } from '@/api/aap'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getAapConexiones } from '@/api/aap_conexion'
import { getAapUsos } from '@/api/aap_uso'
import { getElementos } from '@/api/elemento'
import { getTipos } from '@/api/reporte'

export default {
  components: {
    VueQueryBuilder
  },
  data() {
    return {
      tableData: [],
      barrios: [],
      sectores: [],
      aap_conexiones: [],
      aap_usos: [],
      elementos: [],
      tiposreporte: [],
      qbquery: {},
      qbarrios: [{ label: this.$i18n.t('barrio.select'), value: '' }],
      qtiposbarrio: [{ label: this.$i18n.t('barrio.type'), value: '' }],
      qconnections: [{ label: this.$i18n.t('connection.select'), value: '' }],
      quses: [{ label: this.$i18n.t('use.select'), value: '' }],
      qelements: [{ label: this.$i18n.t('element.select'), value: '' }],
      tipobarrio_filters: [],
      barrios_filters: [],
      qrules: [
        {
          type: 'custom',
          id: 'a.aap_id',
          label: this.$i18n.t('historia.code'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'text',
          id: 'a.aap_apoyo',
          label: this.$i18n.t('historia.support'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'b.barr_id',
          label: this.$i18n.t('historia.neighborhood'),
          choices: []
        },
        {
          type: 'select',
          id: 't.tiba_id',
          label: this.$i18n.t('historia.neighborhoodtype'),
          choices: []
        },
        {
          type: 'select',
          id: 'a.aaco_id',
          label: this.$i18n.t('historia.connection'),
          choices: []
        },
        {
          type: 'select',
          id: 'a.aaus_id',
          label: this.$i18n.t('historia.use'),
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
      page_size: 10,
      current_page: 1,
      total: 0,
      order: '',
      filter: ''
    }
  },
  methods: {
    handleFilter(filters) {
      this.filter = JSON.stringify(filters)
      this.getAaps()
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
        this.getAaps()
      }
    },
    handleEdit(index, row) {
      this.$router.push({ path: '/inventario/historia/editar/' + row.aap_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('historia.confirmationmsg') + ' "' + row.aap_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteAap(row.aap_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getAaps()
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
      this.getAaps()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getAaps()
    },
    nuevo() {
      this.$router.push({ path: '/inventario/historia/crear' })
    },
    actualizar() {
      this.getAaps()
    },
    getAaps() {
      getTodos(this.page_size, this.current_page, this.order, this.qbquery)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.aaps
        }).catch(() => {})
    },
    barrio(barr_id) {
      if (barr_id === null || barr_id === undefined || barr_id === 0) {
        return ''
      } else {
        if (this.barrios.length > 0) {
          return this.barrios.find(o => o.barr_id === barr_id, { barr_descripcion: '' }).barr_descripcion
        } else { return '' }
      }
    },
    sector(tiba_id) {
      if (tiba_id === null || tiba_id === undefined || tiba_id === 0) {
        return ''
      } else {
        if (this.sectores.length > 0) {
          return this.sectores.find(o => o.tiba_id === tiba_id, { tiba_descripcion: '' }).tiba_descripcion
        }
      }
    },
    elemento(elem_id) {
      if (elem_id === null || elem_id === undefined || elem_id === 0) {
        return ''
      } else {
        return this.elementos.find(o => o.elem_id === elem_id, { elem_descripcion: '' }).elem_descripcion
      }
    },
    aap_conexion(aaco_id) {
      if (aaco_id === null || aaco_id === undefined || aaco_id === 0) {
        return ''
      } else {
        if (this.aap_conexiones.length > 0) {
          return this.aap_conexiones.find(o => o.aaco_id === aaco_id, { aaco_descripcion: '' }).aaco_descripcion
        }
      }
    },
    tipo_reporte(reti_id) {
      if (reti_id === null || reti_id === undefined || reti_id === 0) {
        return ''
      } else {
        if (this.tiposreporte.length > 0) {
          return this.tiposreporte.find(o => o.reti_id === reti_id, { reti_descripcion: '' }).reti_descripcion
        }
      }
    }
  },
  mounted() {
    this.qrules[2].choices = this.qbarrios
    this.qrules[3].choices = this.qtiposbarrio
    this.qrules[4].choices = this.qconnections
    this.qrules[5].choices = this.quses
  },
  created() {
    getTiposBarrio().then(response => {
      this.sectores = response.data
      response.data.forEach(b => {
        this.qtiposbarrio.push({ label: b.tiba_descripcion, value: b.tiba_id })
      })
      getElementos().then(response => {
        response.data.forEach(b => {
          this.qelements.push({ label: b.elem_descripcion, value: b.elem_id })
          this.elementos.push(b)
        })
        getAapConexiones().then(response => {
          response.data.forEach(b => {
            this.qconnections.push({ label: b.aaco_descripcion, value: b.aaco_id })
            this.aap_conexiones.push(b)
          })
          getAapUsos().then(response => {
            response.data.forEach(b => {
              this.quses.push({ label: b.aaus_descripcion, value: b.aaus_id })
              this.aap_usos.push(b)
            })
            getBarriosEmpresa().then(response => {
              this.barrios = response.data
              this.barrios.forEach(b => {
                this.qbarrios.push({ label: b.barr_descripcion, value: b.barr_id })
              })
              getTipos().then(response => {
                this.tiposreporte = response.data
                this.getAaps()
              }).catch(error => {
                console.log('tipo reporte error: ' + error)
              })
            }).catch(error => {
              console.log(error)
            })
          })
        }).catch(error => {
          console.log(error)
        })
      }).catch(error => {
        console.log(error)
      })
    }).catch(error => {
      console.log(error)
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