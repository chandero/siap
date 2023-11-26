<template>
  <el-container>
    <el-main>
      <el-container>
        <el-header class="reporte_header"
          >{{ $t('operativo.reporttitle') }}
        </el-header>
        <el-main>
          <!--<vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabelsactualizar" :styled="qstyled" :maxDepth="3"></vue-query-builder>-->
          <el-form label-position="top">
            <el-row :gutter="10">
              <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                <el-form-item prop="freti_id" :label="$t('reporte.type')">
                  <el-select
                    autofocus
                    clearable
                    :title="$t('reporte.tipo.select')"
                    style="width: 80%"
                    ref="tipo"
                    v-model="freti_id"
                    name="tipo"
                    :placeholder="$t('reporte.tipo.select')"
                  >
                    <el-option
                      v-for="tipo in tipos"
                      :key="tipo.reti_id"
                      :label="tipo.reti_descripcion"
                      :value="tipo.reti_id"
                    >
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                <el-form-item prop="fconsec" :label="$t('reporte.number')">
                  <el-input
                    type="number"
                    style="font-size: 30px"
                    v-model="fconsec"
                    @input="fconsec = parseInt($event)"
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <el-form-item
                  style="display: inline-block; margin: 0 auto"
                  label=""
                >
                  <el-button
                    type="warning"
                    icon="el-icon-search"
                    circle
                    @click="actualizar"
                    title="Buscar Reportes"
                  ></el-button>
                  <el-button
                    type="danger"
                    icon="el-icon-close"
                    circle
                    @click="
                      freti_id = null
                      fconsec = null
                    "
                  ></el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-main>
      </el-container>
      <el-container>
        <el-header>
          <el-row :gutter="4">
            <el-col :xs="4" :sm="4" :md="2" :lg="2" :xl="2">
              <el-button
                type="primary"
                icon="el-icon-plus"
                circle
                @click="nuevo"
                title="Crear Nuevo Reporte"
              ></el-button>
              <el-button
                type="success"
                icon="el-icon-refresh"
                circle
                @click="actualizar"
                title="Actualizar listado de Reportes"
              ></el-button>
            </el-col>
            <el-col :xs="16" :sm="16" :md="10" :lg="10" :xl="10">
              <el-input
                v-model="filtro"
                placeholder="Buscar por..."
              ></el-input>
            </el-col>
            <el-col :xs="4" :sm="4" :md="2" :lg="2" :xl="2">
              <el-button type="warning" icon="el-icon-search" circle @click="filtered" title="Buscar por filtro"></el-button>
            </el-col>
          </el-row>
        </el-header>
        <el-main>
          <el-row>
            <el-col>
              <el-button
                type="primary"
                circle
                icon="el-icon-top"
                title="Subir Mes"
                @click="handleUp"
              />
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-container>
                <el-main style="height: 500px; max-height: 500px">
                  <el-tabs
                    v-model="activeTab"
                    :tab-position="tabPosition"
                    type="border-card"
                    class="tabs"
                    @tab-click="changeTab"
                    strecth
                  >
                    <el-tab-pane
                      v-for="(tab, index) in tabsData"
                      :key="index"
                      :label="tab.tabName"
                      :name="tab.tabPeriodo"
                      :lazy="true"
                    >
                      <el-table
                        v-loading="loading"
                        :data="tableData"
                        stripe
                        :default-sort="{
                          prop: 'repo_consecutivo',
                          order: 'descending'
                        }"
                        style="width: 100%"
                        max-height="600"
                        border
                      >
                        <el-table-column
                          :label="$t('reporte.date')"
                          width="100"
                          sortable="custom"
                          prop="repo_fecharecepcion"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row.repo_fecharecepcion
                                | moment('YYYY/MM/DD')
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.operacion')"
                          width="230"
                          sortable="custom"
                          prop="tireuc_id"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              operacion(scope.row.tireuc_id)
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.type')"
                          width="230"
                          sortable="custom"
                          prop="reti_id"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              tipo(scope.row.reti_id)
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.number')"
                          width="120"
                          sortable="custom"
                          prop="repo_consecutivo"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row.repo_consecutivo
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.origin')"
                          width="210"
                          sortable="custom"
                          prop="orig_id"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              origen(scope.row.orig_id)
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.name')"
                          width="210"
                          sortable="custom"
                          prop="repo_nombre"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row.repo_nombre
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.phone')"
                          width="210"
                          sortable="custom"
                          prop="repo_telefono"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row.repo_telefono
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.address')"
                          min-width="250"
                          sortable="custom"
                          prop="repo_direccion"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span>{{ scope.row.repo_direccion }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.neighborhood')"
                          min-width="250"
                          sortable="custom"
                          prop="barr_descripcion"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span>{{ barrio(scope.row.barr_id) }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.description')"
                          width="250"
                          prop="repo_descripcion"
                          sortable="custom"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row.repo_descripcion != null
                                ? scope.row.repo_descripcion
                                : ' ' | truncate(truncSize)
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.status')"
                          width="100"
                          prop="rees_id"
                          sortable="custom"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              estado(scope.row.rees_id)
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          fixed="right"
                          :label="$t('table.accion')"
                          width="150"
                        >
                          <template slot-scope="scope">
                            <el-button
                              size="mini"
                              circle
                              type="info"
                              v-if="scope.row.rees_id == 3"
                              @click="
                                handleEditElemento(scope.$index, scope.row)
                              "
                              :title="$t('edit_element')"
                              ><i class="el-icon-edit-outline"></i>
                            </el-button>
                            <el-button
                              size="mini"
                              circle
                              type="warning"
                              @click="handleEdit2(scope.$index, scope.row)"
                              :title="$t('edit')"
                              ><i class="el-icon-edit"></i>
                            </el-button>
                            <el-button
                              :disabled="scope.row.rees_id > 2"
                              size="mini"
                              circle
                              type="success"
                              :title="$t('print')"
                              @click="handlePrint(scope.$index, scope.row)"
                              ><i class="el-icon-printer"></i>
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
              <el-button
                type="primary"
                circle
                icon="el-icon-bottom"
                title="Bajar Mes"
                @click="handleDown"
              />
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import {
  getReportesRango,
  deleteReporte,
  getEstados,
  getTipos,
  printReporte,
  buscarReportePorTipoConsectivo,
  buscarReportePorVarios
} from '@/api/reporte'
import { getBarriosEmpresa } from '@/api/barrio'
import { getOrigenes } from '@/api/origen'
// import { inspect } from 'util'

export default {
  data() {
    return {
      tabPosition: 'left',
      filtro: '',
      loading: false,
      reporte: {
        reti_id: null,
        tireuc_id: 1,
        repo_id: null,
        repo_consecutivo: null,
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
      qrules: [
        {
          type: 'select',
          id: 'r.reti_id',
          label: this.$i18n.t('reporte.type'),
          choices: []
        },
        {
          type: 'custom',
          id: 'r.repo_consecutivo',
          label: this.$i18n.t('reporte.consec'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'b.barr_id',
          label: this.$i18n.t('reporte.neighborhood'),
          choices: []
        },
        {
          type: 'select',
          id: 'r.rees_id',
          label: this.$i18n.t('reporte.status'),
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
      freti_id: null,
      fconsec: null,
      qstyled: true,
      order: '',
      truncSize: 32,
      anho_inicial: null,
      mes_inicial: null,
      sfilter: {
        logicalOperator: 'AND',
        children: [
          {
            type: 'query-builder-rule',
            query: {
              rule: 'r.reti_id',
              selectedOperator: '=',
              selectedOperand: 'Tipo de Reporte',
              value: null
            }
          },
          {
            type: 'query-builder-rule',
            query: {
              rule: 'r.repo_consecutivo',
              selectedOperator: '=',
              selectedOperand: 'Consecutivo',
              value: null
            }
          }
        ]
      },
      tfilter: {
        logicalOperator: 'AND',
        children: [
          {
            type: 'query-builder-rule',
            query: {
              rule: 'r.reti_id',
              selectedOperator: '=',
              selectedOperand: 'Tipo de Reporte',
              value: null
            }
          }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['empresa'])
  },
  methods: {
    handleUp() {
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
    handleDown() {
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
    createTabs() {
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
    handlePrint(index, row) {
      printReporte(row.repo_id, this.empresa.empr_id)
    },
    handleFilter(filters) {
      this.filter = JSON.stringify(filters)
      this.getReportes()
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
        this.getReportes()
      }
    },
    handleHeader(newWidth, oldWidth, column, event) {
      if (column.property === 'repo_descripcion') {
        this.truncSize = Math.round(newWidth / 7.6)
        this.$refs.tableData.doLayout()
      }
    },
    handleEdit2(index, row) {
      this.$router.push({
        path:
          '/proceso/menu1reporte/menu1-1luminaria/menu1-1-3edit/' + row.repo_id
      })
      console.log(index, row)
    },
    handleEditElemento(index, row) {
      this.$router.push({
        path:
          '/proceso/menu1reporte/menu1-1luminaria/menu1-1-4elemento/' +
          row.repo_id
      })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(
        this.$i18n.t('reporte.confirmationmsg') +
          ' "' +
          row.repo_id +
          '->' +
          row.repo_descripcion +
          '"',
        this.$i18n.t('general.warning'),
        {
          confirmButtonText: this.$i18n.t('general.ok'),
          cancelButtonText: this.$i18n.t('general.cancel'),
          type: 'warning'
        }
      )
        .then(() => {
          deleteReporte(row.repo_id)
            .then((response) => {
              this.$message({
                type: 'success',
                message: this.$i18n.t('general.deletesuccessful')
              })
              this.getReportes()
            })
            .catch((err) => {
              this.$message({
                type: 'error',
                message: this.$i18n.t('general.deletefail') + ' -> ' + err.msg
              })
            })
        })
        .catch(() => {
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
    getReportes() {},
    changeTab(data) {
      const mes = data.name.split(':')[0]
      const anho = data.name.split(':')[1]
      const index = data.name.split(':')[2]
      this.getData(anho, mes, index, data.name)
    },
    actualizar() {
      this.filtro = this.freti_id + '|' + this.fconsec
      this.filtered()
    },
    nuevo() {
      this.$router.push({
        path: '/proceso/menu1reporte/menu1-1luminaria/menu1-1-2create/1'
      })
    },
    estado(id) {
      if (id === undefined || id === null || id <= 0) {
        return ''
      } else {
        return this.estados.find((e) => e.rees_id === id, {
          rees_descripcion: ''
        }).rees_descripcion
      }
    },
    tipo(reti_id) {
      if (reti_id === null) {
        return ''
      } else {
        const reporte_tipo = this.tipos.find((o) => o.reti_id === reti_id)
        if (reporte_tipo) {
          return reporte_tipo.reti_descripcion
        } else {
          return ''
        }
      }
    },
    operacion(tireuc_id) {
      switch (tireuc_id) {
        case 1:
          return 'LUMINARIA'
        case 2:
          return 'CONTROL'
        case 3:
          return 'TRANSFORMADOR'
        case 4:
          return 'MEDIDOR'
      }
    },
    origen(id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        const origen = this.origenes.find((e) => e.orig_id === id)
        if (origen) {
          return origen.orig_descripcion
        } else {
          return ''
        }
      }
    },
    barrio(id) {
      if (id === null) {
        return ''
      } else {
        const barrio = this.barrios.find((e) => e.barr_id === id, {
          barr_descripcion: ''
        })
        if (barrio) {
          return barrio.barr_descripcion
        } else {
          return ''
        }
      }
    },
    getData(anho, mes, index, name) {
      this.loading = true
      getReportesRango(anho, mes, 1)
        .then((response) => {
          console.log('Periodo: ' + name)
          this.tableData = response.data
          this.loading = false
          // data.tableData = response.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    filtrar(data) {
      console.log('Filtrando....')
      if (!this.filtro) {
        return data
      } else {
        if (this.filtro.includes('|')) {
          var reti_id = parseInt(this.filtro.split('|')[0])
          var repo_consecutivo = parseInt(this.filtro.split('|')[1])
          if (
            data.reti_id === reti_id &&
            data.repo_consecutivo === repo_consecutivo
          ) {
            return data
          } else {
            return null
          }
        } else if (data.repo_consecutivo === parseInt(this.filtro)) {
          return data
        } else if (
          data.repo_nombre != null &&
          data.repo_nombre.toLowerCase().includes(this.filtro.toLowerCase())
        ) {
          return data
        } else if (
          data.repo_telefono != null &&
          data.repo_telefono.toLowerCase().includes(this.filtro.toLowerCase())
        ) {
          return data
        } else if (
          this.tipo(data.reti_id)
            .toLowerCase()
            .includes(this.filtro.toLowerCase())
        ) {
          return data
        } else if (
          this.origen(data.orig_id)
            .toLowerCase()
            .includes(this.filtro.toLowerCase())
        ) {
          return data
        } else if (
          this.barrio(data.barr_id)
            .toLowerCase()
            .includes(this.filtro.toLowerCase())
        ) {
          return data
        } else if (
          data.repo_direccion != null &&
          data.repo_direccion.toLowerCase().includes(this.filtro.toLowerCase())
        ) {
          return data
        } else {
          return null
        }
      }
    },
    filtered() {
      this.filtro = this.filtro.toUpperCase()
      if (this.filtro.includes('|')) {
        var reti_id = parseInt(this.filtro.split('|')[0])
        var repo_consecutivo = parseInt(this.filtro.split('|')[1])
        this.loading = true
        buscarReportePorTipoConsectivo(1, reti_id, repo_consecutivo).then(response => {
          this.tableData = response.data
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else {
        this.loading = true
        buscarReportePorVarios(this.filtro, 1).then(response => {
          this.tableData = response.data
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      }
    }
  },
  beforeMount() {
    this.qrules[0].choices = this.qtipos
    this.qrules[2].choices = this.qbarrios
    this.qrules[3].choices = this.qestados
    const date = new Date()
    this.anho_inicial = date.getFullYear()
    this.mes_inicial = date.getMonth() + 1
    this.createTabs()
  },
  mounted() {
    const start = async () => {
      this.getData(this.anho, this.mes, 0, this.tabsData[0].tabPeriodo)
      this.activeTab = this.tabsData[0].tabPeriodo
    }
    start()
  },
  created() {
    getBarriosEmpresa()
      .then((response) => {
        this.barrios = response.data
        this.barrios.forEach((b) => {
          this.qbarrios.push({ label: b.barr_descripcion, value: b.barr_id })
        })
      })
      .catch((error) => {
        console.log(error)
      })
    getEstados()
      .then((response) => {
        this.estados = response.data
        this.estados.forEach((e) => {
          this.qestados.push({ label: e.rees_descripcion, value: e.rees_id })
        })
      })
      .catch((error) => {
        console.log('getEstados: ' + error)
      })
    getTipos()
      .then((response) => {
        this.tipos = response.data
        this.tipos.forEach((e) => {
          this.qtipos.push({ label: e.reti_descripcion, value: e.reti_id })
        })
      })
      .catch((error) => {
        console.log('getEstados: ' + error)
      })
    getOrigenes()
      .then((response) => {
        this.origenes = response.data
        this.origenes.forEach((e) => {
          this.qorigenes.push({ label: e.orig_descripcion, value: e.orig_id })
        })
      })
      .catch((error) => {
        console.log('getEstados: ' + error)
      })
  }
}
</script>
<style scoped>
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
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
}
.vue-query-builder >>> .panel-default {
  border-color: #ddd;
}

.vue-query-builder >>> .pull-right {
  float: right !important;
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
  -webkit-transition: 0.1s;
  transition: 0.1s;
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
