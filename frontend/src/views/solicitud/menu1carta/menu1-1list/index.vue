<template>
    <el-container>
      <el-main>
      <el-container>
        <el-header class="solicitud_header">{{ $t('solicitud.title') }}
        </el-header>
        <el-main>
          <!--<vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabelsactualizar" :styled="qstyled" :maxDepth="3"></vue-query-builder>-->
          <el-form label-position="top">
          <el-row :gutter="10">
             <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
             <el-form-item prop="fconsec" :label="$t('solicitud.radicado')">
              <el-input type="number" style="font-size: 30px;" v-model="fconsec" @input="fconsec = parseInt($event)"></el-input>
             </el-form-item>
             </el-col>
             <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
              <el-form-item style="display: inline-block; margin: 0 auto;" label="">
                <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Buscar Derecho de Petición"></el-button>
                <el-button type="danger" icon="el-icon-close" circle @click="freti_id = null; fconsec = null"></el-button>
              </el-form-item>
             </el-col>
           </el-row>
          </el-form>
        </el-main>
      </el-container>
      <el-container>
          <el-header>
            <el-row :gutter="4">
              <el-col :span="2">
                <el-button type="primary" icon="el-icon-plus" circle @click="nuevo" title="Crear Nueva Solicitud" ></el-button>
              </el-col>
              <el-col :span="2">
                <el-button type="success" icon="el-icon-refresh" circle @click="actualizar" title="Actualizar listado de Solicitudes"></el-button>
              </el-col>
              <el-col :span="18">
                <el-input style="display: inline-block; position: absolute;" v-model="filtro" @input="filtro=$event.toUpperCase()" placeholder="Buscar por..."></el-input>
              </el-col>
            </el-row>
          </el-header>
          <el-main>
            <el-row>
              <el-col>
                <el-button type="primary" circle icon="el-icon-top" title="Subir Mes" @click="handleUp"/>
              </el-col>
            </el-row>
          <el-row>
            <el-col>
              <el-container>
                <el-main style="height:500px; max-height: 500px;">
              <el-tabs v-model="activeTab" :tab-position="tabPosition" type="border-card" class="tabs" @tab-click="changeTab" strecth>
            <el-tab-pane
              v-for="(tab, index) in tabsData"
              :key="index"
              :label="tab.tabName"
              :name="tab.tabPeriodo"
              :lazy="true"
            >
              <el-table
                v-loading="loading"
                :data="tableData.filter(data => filtrar(data))"
                stripe
                :default-sort = "{prop: 'a.soli_radicado', order: 'descending'}"
                style="width: 100%"
                max-height="600"
                border>
          <el-table-column
          :label="$t('solicitud.fecha')"
          width="100"
          sortable="custom"
          prop="a.soli_fecha"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_fecha | moment('YYYY/MM/DD') }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('solicitud.radicado')"
          width="100"
          sortable="custom"
          prop="a.soli_radicado"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_radicado }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('solicitud.tipo')"
          width="100"
          sortable="custom"
          prop="a.soti_descripcion"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ soti_descripcion(scope.row.a.soti_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('solicitud.estado')"
          width="130"
          sortable="custom"
          prop="b.soli_estado"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ estado(scope.row.b.soli_estado) }}</span>
          </template>
        </el-table-column>
        <el-table-column
         :label="$t('solicitud.codigorespuesta')"
         width="150"
         resizable
         >
           <template slot-scope="scope">
             <span style="margin-left: 2px;">{{ scope.row.b.soli_codigorespuesta }}</span>
           </template>
        </el-table-column>
        <el-table-column
          :label="$t('solicitud.nombre')"
          width="210"
          sortable="custom"
          prop="a.soli_nombre"
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
          prop="a.soli_direccion"
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
          prop="a.soli_telefono"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.a.soli_telefono }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="230">
          <template slot-scope="scope">
            <el-button
              size="mini"
              circle
              @click="handleVer(scope.$index, scope.row)"
              :title="$t('solicitud.ver')">
              <i class="el-icon-view"></i>
            </el-button>
            <el-button
              :disabled="scope.row.b.soli_estado >= 2"
              size="mini"
              circle
              type="primary"
              @click="handleClickSupervisor(scope.$index, scope.row)"
              :title="$t('solicitud.asupervisor')">
              <i class="el-icon-s-custom"></i>
            </el-button>
            <el-button
              :disabled="scope.row.b.soli_estado < 2 || scope.row.b.soli_estado === 6 || scope.row.b.soli_estado === 7"
              size="mini"
              circle
              type="primary"
              @click="handleFormatoRTE(scope.$index, scope.row)"
              :title="$t('solicitud.aformatorte')">
              <i class="el-icon-s-order"></i>
            </el-button>
            <el-button
              :disabled="scope.row.b.soli_estado < 2 || scope.row.b.soli_estado === 6 || scope.row.b.soli_estado === 7"
              size="mini"
              circle
              type="warning"
              @click="handleEdit(scope.$index, scope.row)" :title="$t('solicitud.dinforme')">
              <i class="el-icon-edit"></i>
            </el-button>
            <el-popover
              placement="bottom"
              width="160">
              <el-menu @select="handleMenuRespuesta" class="el-menu-respuesta">
                <el-menu-item :index="scope.row.a.soli_id + '-1'">Para Imprimir</el-menu-item>
                <el-menu-item :index="scope.row.a.soli_id + '-2'">Para Modificar</el-menu-item>
              </el-menu>
              <el-button
                slot="reference"
                :disabled="scope.row.b.soli_estado === 1 || scope.row.b.soli_estado === 7"
                size="mini"
                circle
                type="success"
                :title="$t('solicitud.drespuesta')"
              ><i class="el-icon-document"></i>
              </el-button>
            </el-popover>
            <el-button
              :disabled="scope.row.b.soli_estado !== 6"
              size="mini"
              circle
              type="warning"
              @click="handleClickFechaEntregaRespuesta(scope.$index, scope.row)" :title="$t('solicitud.dentrega')">
              <i class="el-icon-date"></i>
            </el-button>
           </template>
          </el-table-column>
              </el-table>
            </el-tab-pane>
              </el-tabs>
              </el-main>
              </el-container>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-button type="primary" circle icon="el-icon-bottom" title="Bajar Mes" @click="handleDown"/>
            </el-col>
          </el-row>
         </el-main>
       </el-container>
  </el-main>
  <el-dialog :title="'Seguro de entregar la solicitud Radicado No.' + registro.a.soli_radicado + ' al Supervisor?'" :visible.sync="dialogEntregaSupervisorVisible">
    <el-form :model="formSupervisor">
      <el-form-item label="Fecha y Hora de Entrega" label-width="150">
        <el-date-picker v-model="formSupervisor.date" type="datetime"></el-date-picker>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
     <el-button @click="dialogEntregaSupervisorVisible = false">No</el-button>
     <el-button type="primary" @click="handleSupervisor()">Sí</el-button>
    </span>
  </el-dialog>
  <el-dialog :title="'Actualizar Fecha de Entrega de Respuesta Solicitud Radicado No.' + registro.a.soli_radicado" :visible.sync="dialogFechaEntregaRespuestaVisible">
    <el-form :model="formEntregaRespuesta">
      <el-form-item label="Fecha y Hora de Entrega" label-width="150">
        <el-date-picker v-model="formEntregaRespuesta.date" type="datetime"></el-date-picker>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
     <el-button @click="dialogFechaEntregaRespuestaVisible = false">No</el-button>
     <el-button type="primary" @click="handleFechaEntregaRespuesta()">Sí</el-button>
    </span>
  </el-dialog>
 </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getSolicitudesRango, deleteSolicitud, entregarSupervisor, fechaEntregaRespuesta, entregarFormatoRTE, imprimirFormatoRTE, imprimirRespuestaSolicitud, getTipos } from '@/api/solicitud'
import { getBarriosEmpresa } from '@/api/barrio'

export default {
  data () {
    return {
      formSupervisor: {
        date: new Date()
      },
      formEntregaRespuesta: {
        date: new Date()
      },
      dialogEntregaSupervisorVisible: false,
      dialogFechaEntregaRespuestaVisible: false,
      tabPosition: 'left',
      filtro: '',
      loading: false,
      solicitud: {
        a: {
          soli_id: null,
          soti_id: null,
          soli_fecha: null,
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
      registro: {
        a: {
          soli_radicado: null
        },
        b: {}
      },
      index: null,
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
      truncSize: 32,
      anho_inicial: null,
      mes_inicial: null
    }
  },
  computed: {
    ...mapGetters([
      'empresa'
    ])
  },
  methods: {
    handleUp () {
      this.mes_inicial += 1
      if (this.mes_inicial > 12) {
        this.mes_inicial = 1
        this.anho_inicial += 1
      }
      this.createTabs()
      var data = { name: this.tabsData[0].tabPeriodo }
      this.changeTab(data)
      this.activeTab = this.tabsData[0].tabPeriodo
    },
    handleDown () {
      this.mes_inicial -= 1
      if (this.mes_inicial < 1) {
        this.mes_inicial = 12
        this.anho_inicial -= 1
      }
      this.createTabs()
      var data = { name: this.tabsData[11].tabPeriodo }
      this.changeTab(data)
      this.activeTab = this.tabsData[11].tabPeriodo
    },
    createTabs () {
      var year = this.anho_inicial
      var month = this.mes_inicial
      this.anho = year
      this.mes = month
      this.tabsData = []
      console.log('Mes Inicial : ' + month)
      for (var i = 12; i >= 1; i--) {
        var data = {
          tabName: this.$i18n.t('months.m' + month) + year,
          tabRange: {
            anho: year,
            mes: month
          },
          tabPeriodo: month + ':' + year + ':' + (12 - i),
          tableData: []
        }
        this.tabsData.push(data)
        // this.getData(data.tabRange.anho, data.tabRange.mes, data)
        month--
        if (month < 1) {
          month = 12
          year--
        }
      }
    },
    handlePrint (index, row) {
    },
    handleFilter (filters) {
      this.filter = JSON.stringify(filters)
      this.getSolicitudes()
    },
    handleSort ({ column, prop, order }) {
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
    handleHeader (newWidth, oldWidth, column, event) {
      if (column.property === 'soli_direccion') {
        this.truncSize = Math.round(newWidth / 7.6)
        this.$refs.tableData.doLayout()
      }
    },
    handleVer (index, row) {
      this.$router.push({ path: '/solicitud/menu1carta/menu1-3view/' + row.a.soli_id })
    },
    handleClickSupervisor (index, row) {
      this.registro = row
      this.index = index
      this.dialogEntregaSupervisorVisible = true
    },
    handleSupervisor () {
      console.log('enviar a supervisor')
      this.dialogEntregaSupervisorVisible = false
      entregarSupervisor(this.registro.a.soli_id).then(response => {
        if (response.status === 200) {
          this.registro.b.soli_estado = 2
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
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Entrega Cancelada'
        })
      })
    },
    handleFormatoRTE (index, row) {
      console.log('Entregar Formato RTE')
      this.$confirm('Seguro de entregar el formato reporte técnico a la solicitud Radicado No.' + row.a.soli_radicado + ' al Supervisor?', 'Atención', {
        confirmButtonText: 'Sí',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        entregarFormatoRTE(row.a.soli_id).then(response => {
          if (response.status === 200) {
            row.b.soli_estado = 3
            imprimirFormatoRTE(row.a.soli_id, this.empresa.empr_id)
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
    handleEdit (index, row) {
      this.$confirm('Continuar con el Informe Técnico Radicado No.' + row.a.soli_radicado, 'Informe', {
        confirmButtonText: 'Continuar',
        cancelButtonText: 'Cancelar',
        customClass: 'infoclass'
      }).then(() => {
        this.$router.push({ path: '/solicitud/menu1carta/menu1-4edit/' + row.a.soli_id })
        /* ingresarInforme(row.soli_id, info.value).then(response => {
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
        }) */
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Informe Cancelado'
        })
      })
    },
    handleClickFechaEntregaRespuesta (index, row) {
      this.registro = row
      this.index = index
      this.dialogFechaEntregaRespuestaVisible = true
    },
    handleFechaEntregaRespuesta () {
      this.dialogFechaEntregaRespuestaVisible = false
      fechaEntregaRespuesta(this.registro.a.soli_id, this.formEntregaRespuesta.date.getTime()).then(response => {
        if (response.status === 200) {
          this.registro.b.soli_estado = 7
          this.$message({
            type: 'info',
            message: 'Fecha Entrega Respuesta Confirmada'
          })
        } else {
          this.$message({
            type: 'warning',
            message: 'No se pudo Confirmar la fecha de entrega de la respuesta'
          })
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Entrega Cancelada'
        })
      })
    },
    handleRespuesta (soli_id, editable) {
      console.log('ingresar respuesta')
      this.$confirm('Desea Incluir la Firma de Gerencia?', 'Respuesta', {
        confirmButtonText: 'No',
        cancelButtonText: 'Sí',
        customClass: 'infoclass'
      }).then(() => {
        imprimirRespuestaSolicitud(soli_id, this.empresa.empr_id, 0, editable)
      }).catch(() => {
        imprimirRespuestaSolicitud(soli_id, this.empresa.empr_id, 1, editable)
      })
    },
    handleDelete (index, row) {
      this.$confirm(this.$i18n.t('solicitud.confirmationmsg') + ' "' + row.soli_id + '->' + row.soli_direccion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteSolicitud(row.soli_id).then(response => {
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
    handleMenuRespuesta (key, keyPath) {
      console.log('key:' + key)
      const res = key.split('-')
      const soli_id = res[0]
      const editable = res[1]
      if (editable === '2') {
        console.log('Es Editable')
        this.handleRespuesta(soli_id, true)
      } else {
        this.handleRespuesta(soli_id, false)
      }
    },
    handleSizeChange (val) {
      this.page_size = val
      this.getReportes()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getReportes()
    },
    getReportes () {
    },
    changeTab (data) {
      const mes = data.name.split(':')[0]
      const anho = data.name.split(':')[1]
      const index = data.name.split(':')[2]
      this.getData(anho, mes, index, data.name)
    },
    actualizar () {
      this.filtro = this.fconsec ? this.fconsec.toString() : ''
    },
    nuevo () {
      this.$router.push({ path: '/solicitud/menu1carta/menu1-2create' })
    },
    barrio (id) {
      if (id === null || id === undefined) {
        return ''
      } else {
        return this.barrios.find(e => e.barr_id === id, { barr_descripcion: '' }).barr_descripcion
      }
    },
    getData (anho, mes, index, name) {
      this.loading = true
      getSolicitudesRango(anho, mes).then(response => {
        console.log('Periodo: ' + name)
        this.tableData = response.data
        // this.tabsData[index].tableData = response.data
        this.loading = false
        // data.tableData = response.data
      }).catch(() => {
        this.loading = false
      })
    },
    filtrar (data) {
      if (!this.filtro) {
        return data
      } else {
        if (data.a.soli_radicado.toString() === this.filtro) {
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
    soti_descripcion (id) {
      if (id === undefined || id === null || id < 1) {
        return ''
      } else {
        return this.tipos.find(t => t.soti_id === id, { soti_descripcion: '' }).soti_descripcion
      }
    },
    estado (soli_estado) {
      if (soli_estado === 1) {
        return 'PENDIENTE'
      } else if (soli_estado === 2) {
        return 'EN SUPERVISOR'
      } else if (soli_estado === 3) {
        return 'FORMATO REPORTE TECNICO'
      } else if (soli_estado === 4) {
        return 'EN ALMACEN'
      } else if (soli_estado === 5) {
        return 'EN INFORME TECNICO'
      } else if (soli_estado === 6) {
        return 'RESPONDIDA'
      } else if (soli_estado === 7) {
        return 'RESPUESTA ENTREGADA'
      } else {
        return 'NO ASIGNADO'
      }
    },
    estadoClass () {
      if (this.solicitud.b.soli_estado === 1) {
        return 'estado-1'
      } else if (this.solicitud.b.soli_estado === 2) {
        return 'estado-2'
      } else if (this.solicitud.b.soli_estado === 3) {
        return 'estado-3'
      } else if (this.solicitud.b.soli_estado === 4) {
        return 'estado-4'
      } else if (this.solicitud.b.soli_estado === 5) {
        return 'estado-5'
      } else if (this.solicitud.b.soli_estado === 6) {
        return 'estado-6'
      } else {
        return 'estado-n'
      }
    }
  },
  beforeMount () {
    const date = new Date()
    this.anho_inicial = date.getFullYear()
    this.mes_inicial = date.getMonth() + 1
    this.createTabs()
  },
  mounted () {
    const start = async () => {
      getBarriosEmpresa().then(response => {
        this.barrios = response.data
        getTipos().then(response => {
          this.tipos = response.data
          this.getData(this.anho, this.mes, 0, this.tabsData[0].tabPeriodo)
          this.activeTab = this.tabsData[0].tabPeriodo
        }).catch(error => {
          console.log('getTipos Error:' + error)
        })
      }).catch(error => {
        console.log(error)
      })
    }
    start()
  },
  created () {

  }
}
</script>
<style scoped>
.estado-1 {
  background: #fff;
}

.estado-2 {
  background: #53a8ff;
}

.estado-3 {
  background: #e6a23c;
}

.estado-4 {
  background: #e6c23c;
}

.estado-5 {
  background: #89d34d;
}

.estado-6 {
  background: #12c23a;
}

.estado-n {
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
