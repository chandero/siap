<template>
    <el-container>
      <el-main>
      <el-container>
        <el-header class="solicitud_header">{{ $t('solicitud.porvencer') }}
        </el-header>
      </el-container>
      <el-container>
          <el-main>
            <el-table
              v-loading="loading"
              :data="tableData"
              :default-sort = "{prop: 'soli_radicado', order: 'descending'}"        
              style="width: 100%"
              max-height="600"
              border
              :row-class-name="tableRowClassName">
          <el-table-column
          :label="$t('solicitud.fecha')"
          width="150"
          sortable="custom"
          prop="soli_fecha"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_fecha | moment('YYYY/MM/DD HH:MM:SS') }}</span>
          </template>
        </el-table-column>       
          <el-table-column
          :label="$t('solicitud.fechalimite')"
          width="150"
          sortable="custom"
          prop="soli_fechalimite"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.b.soli_fechalimite | moment('YYYY/MM/DD HH:MM:SS') }}</span>
          </template>
        </el-table-column>          
        <el-table-column
          :label="$t('solicitud.radicado')"
          width="100"
          sortable="custom"
          prop="soli_radicado"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_radicado }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('solicitud.nombre')"
          width="210"
          sortable="custom"
          prop="soli_nombre"   
          resizable       
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_nombre }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('solicitud.direccion')"
          width="350"
          sortable="custom"
          prop="soli_direccion"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_direccion }}</span>
          </template>
        </el-table-column>        
        <el-table-column
          :label="$t('solicitud.barrio')"
          min-width="250"
          sortable="custom"
          prop="barr_descripcion"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ barrio(scope.row.a.barr_id) }}</span>
          </template>
        </el-table-column>  
        <el-table-column
          :label="$t('solicitud.telefono')"
          width="150"
          sortable="custom"
          prop="soli_telefono"   
          resizable       
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_telefono }}</span>
          </template>
        </el-table-column>         
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="180">
          <template slot-scope="scope">
            <el-button
              size="mini"
              circle
              @click="handleVer(scope.$index, scope.row)"
              :title="$t('solicitud.ver')">
              <i class="el-icon-view"></i>
            </el-button>            
            <el-button
              :disabled="scope.row.b.soli_estado != 1"
              size="mini"
              circle
              type="primary"
              @click="handleSupervisor(scope.$index, scope.row)"
              :title="$t('solicitud.asupervisor')">
              <i class="el-icon-s-custom"></i>
            </el-button>
            <el-button
              :disabled="scope.row.b.soli_estado != 4"
              size="mini"
              circle
              type="warning"
              @click="handleEdit(scope.$index, scope.row)" :title="$t('solicitud.dinforme')">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-button
              :disabled="scope.row.soli_estado != 5"
              size="mini"
              circle
              type="success"
              :title="$t('solicitud.drespuesta')"
              @click="handleRespuesta(scope.$index, scope.row)"><i class="el-icon-document"></i>
            </el-button>
           </template>
          </el-table-column>
        </el-table> 
    </el-main>
   </el-container>
  </el-main>
 </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { deleteSolicitud, entregarSupervisor, ingresarInforme, ingresarRespuesta, buscarPorVencer } from '@/api/solicitud'
import { getBarriosEmpresa } from '@/api/barrio'

export default {
  data() {
    return {
      tabPosition: 'left',
      filtro: '',
      loading: false,
      solicitud: {
        a: {
          soli_id: null,
          soti_id: null,
          soli_fecha: new Date(),
          soli_nombre: null,
          soli_radicado: null,
          soli_direccion: null,
          barr_id: null,
          soli_telefono: null,
          soli_email: null,
          soli_solicitud: null,
          soli_respuesta: null,
          soli_informe: null
        },
        b: {
          soli_fecharespuesta: null,
          soli_fechadigitado: null,
          soli_fechalimite: null,
          soli_fechasupervisor: null,
          soli_fechainforme: null,
          soli_fechavisita: null,
          soli_fecharte: null,
          soli_fechaalmacen: null,
          soli_numerorte: null,
          soli_estado: null,
          empr_id: null,
          usua_id: null
        }
      },
      tableData: [],
      activeTab: null,
      tabsData: [],
      columnDefs: null,
      page_size: 10,
      current_page: 1,
      total: 0,
      barrios: [],
      estados: [],
      tipos: [],
      origenes: [],
      qbquery: {},
      qbarrios: [{ label: this.$i18n.t('barrio.select'), value: '' }],
      qestados: [{ label: this.$i18n.t('reporte.estado.select'), value: '' }],
      qtipos: [{ label: this.$i18n.t('reporte.tipo.select'), value: '' }],
      qorigenes: [{ label: this.$i18n.t('reporte.origen.select'), value: '' }],
      fconsec: null,
      qstyled: true,
      order: '',
      truncSize: 32
    }
  },
  computed: {
    ...mapGetters([
      'empresa'
    ])
  },
  methods: {
    handlePrint(index, row) {
    },
    handleFilter(filters) {
      this.filter = JSON.stringify(filters)
      this.getSolicitudes()
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
        this.getSolicitudes()
      }
    },
    handleHeader(newWidth, oldWidth, column, event) {
      if (column.property === 'soli_direccion') {
        this.truncSize = Math.round(newWidth / 7.6)
        this.$refs['tableData'].doLayout()
      }
    },
    handleVer(index, row) {
      this.$router.push({ path: '/solicitud/ver/' + row.a.soli_id })
    },
    handleSupervisor(index, row) {
      console.log('enviar a suervisor')
      this.$confirm('Seguro de entregar la solicitud Radicado No.' + row.a.soli_radicado + ' al Supervisor?', 'Atención', {
        confirmButtonText: 'Sí',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        entregarSupervisor(row.a.soli_id).then(response => {
          if (response.status === 200) {
            row.soli_estado = 2
            this.$message({
              type: 'info',
              message: 'Entrega Confirmada'
            })
          } else {
            this.$message({
              type: 'warning',
              message: 'No se pudo Confirmar la entrega'
            })
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Entrega Cancelada'
        })
      })
    },
    handleEdit(index, row) {
      console.log('ingresar informe')
      this.$prompt('Informe Técnico Solicitud Radicado No.' + row.a.soli_radicado, 'Informe', {
        confirmButtonText: 'Guardar',
        cancelButtonText: 'Cancelar',
        inputType: 'textarea',
        customClass: 'infoclass'
      }).then((info) => {
        ingresarInforme(row.a.soli_id, info.value).then(response => {
          if (response.status === 200) {
            row.soli_estado = 3
            this.$message({
              type: 'info',
              message: 'Informe Guardado'
            })
          } else {
            this.$message({
              type: 'warning',
              message: 'No se pudo Guardar el Informe'
            })
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Informe Cancelado'
        })
      })
    },
    handleRespuesta(index, row) {
      console.log('ingresar respuesta')
      this.$prompt('Respuesta Solicitud Radicado No.' + row.a.soli_radicado, 'Respuesta', {
        confirmButtonText: 'Guardar',
        cancelButtonText: 'Cancelar',
        inputType: 'textarea',
        customClass: 'infoclass'
      }).then((info) => {
        ingresarRespuesta(row.a.soli_id, info.value).then(response => {
          if (response.status === 200) {
            row.a.soli_estado = 4
            this.$message({
              type: 'info',
              message: 'Respuesta Guardada'
            })
          } else {
            this.$message({
              type: 'warning',
              message: 'No se pudo Guardar la Respuesta'
            })
          }
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Respuesta Cancelada'
        })
      })
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('solicitud.confirmationmsg') + ' "' + row.a.soli_id + '->' + row.a.soli_direccion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteSolicitud(row.a.soli_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getReportes()
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
      this.getReportes()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getReportes()
    },
    actualizar() {
      this.filtro = this.fconsec ? this.fconsec : ''
    },
    nuevo() {
      this.$router.push({ path: '/solicitud/create' })
    },
    barrio(id) {
      if (id === null || id === undefined) {
        return ''
      } else {
        return this.barrios.find(e => e.barr_id === id, { barr_descripcion: '' }).barr_descripcion
      }
    },
    getData() {
      this.loading = true
      buscarPorVencer().then(response => {
        this.tableData = response.data
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    filtrar(data) {
      if (!this.filtro) {
        return data
      } else {
        if (data.a.soli_radicado === this.filtro) {
          return data
        } else if (data.a.soli_nombre != null && data.a.soli_nombre.toLowerCase().includes(this.filtro.toLowerCase())) {
          return data
        } else if (data.a.soli_telefono != null && data.a.soli_telefono.toLowerCase().includes(this.filtro.toLowerCase())) {
          return data
        } else if (this.barrio(data.a.barr_id).toLowerCase().includes(this.filtro.toLowerCase())) {
          return data
        } else if (data.a.soli_direccion != null && data.a.soli_direccion.toLowerCase().includes(this.filtro.toLowerCase())) {
          return data
        } else if (data.a.soli_solicitud != null && data.a.soli_solicitud.toLowerCase().includes(this.filtro.toLowerCase())) {
          return data
        } else {
          return null
        }
      }
    },
    tableRowClassName({ row, rowIndex }) {
      if (row.b.soli_estado === 1) {
        return 'estado-1'
      } else if (row.b.soli_estado === 2) {
        return 'estado-2'
      } else if (row.b.soli_estado === 3) {
        return 'estado-3'
      } else if (row.b.soli_estado === 4) {
        return 'estado-4'
      } else if (row.b.soli_estado === 5) {
        return 'estado-5'
      } else if (row.b.soli_estado === 6) {
        return 'estado-6'
      } else {
        return 'estado-n'
      }
    }
  },
  mounted() {
    const start = async() => {
      getBarriosEmpresa().then(response => {
        this.barrios = response.data
        this.getData()
      }).catch(error => {
        console.log(error)
      })
    }
    start()
  },
  created() {

  }
}
</script>
<style>
.el-table .estado-1 {
  background: #fff;
}

.el-table .estado-2 {
  background: #53a8ff;
}

.el-table .estado-3 {
  background: #e6a23c;
}

.el-table .estado-4 {
  background: #e6c23c;
}

.el-table .estado-5 {
  background: #89d34d;
}

.el-table .estado-6 {
  background: #12c23a;
}

.el-table .estado-n {
  background: #f56c6c;
}

.el-message-box.infoclass {
  width: 70%
}
.tabs {
  height: 800px;
}
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
</style>