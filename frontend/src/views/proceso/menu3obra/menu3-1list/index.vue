<template>
    <el-container>
      <el-main>
      <el-container>
        <el-header class="obra_header">{{ $t('operativo.obratitle') }}
        </el-header>
        <el-main>
          <!--<vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabelsactualizar" :styled="qstyled" :maxDepth="3"></vue-query-builder>-->
          <el-form label-position="top">
          <el-row :gutter="10">
            <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
             <el-form-item prop="fconsec" :label="$t('obra.consecutivo')">
              <el-input type="consecutivo" style="font-size: 30px;" v-model="fconsec" @input="fconsec = parseInt($event)"></el-input>
             </el-form-item>
             </el-col>
             <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
              <el-form-item style="display: inline-block; margin: 0 auto;" label="">
                <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Buscar Obras"></el-button>
                <el-button type="danger" icon="el-icon-close" circle @click="fconsec = null"></el-button>
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
                <el-button type="primary" icon="el-icon-plus" circle @click="nuevo" title="Crear Nueva Obra" ></el-button>
              </el-col>
              <el-col :span="2">
                <el-button type="success" icon="el-icon-refresh" circle @click="actualizar" title="Actualizar listado de Obras"></el-button>
              </el-col>
              <el-col :span="18">
                <el-input style="display: inline-block; position: absolute;" v-model="filtro" @input="filtro=$event.toUpperCase()" placeholder="Buscar por..."></el-input>
              </el-col>
            </el-row>
          </el-header>
          <el-main>
          <el-tabs v-model="activeTab" :tab-position="tabPosition" type="border-card" class="tabs" @tab-click="changeTab" strecth>
            <el-tab-pane
              v-for="(tab, index) in tabsData"
              :key="index"
              :label="tab.tabName"
              :name="tab.tabPeriodo"
            >
              <el-table
                :data="tableData.filter(d => filtrar(d))"
                stripe
                :default-sort = "{prop: 'obra_consecutivo', order: 'descending'}"
                style="width: 100%"
                max-height="600"
                border>
          <el-table-column
          :label="$t('obra.date')"
          width="100"
          sortable="custom"
          prop="obra_fecharecepcion"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.obra_fecharecepcion | moment('YYYY/MM/DD') }}</span>
          </template>
        </el-table-column>
        <!--
        <el-table-column
          :label="$t('obra.type')"
          width="230"
          sortable="custom"
          prop="reti_id"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ tipo(scope.row.reti_id) }}</span>
          </template>
        </el-table-column>
        -->
        <el-table-column
          :label="$t('obra.consecutivo')"
          width="120"
          sortable="custom"
          prop="obra_consecutivo"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.obra_consecutivo }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('obra.mot')"
          width="120"
          sortable="custom"
          prop="muot_id"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.muot_id }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('obra.ot')"
          width="120"
          sortable="custom"
          prop="muot_id"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ ordenes(scope.row.ortr_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('obra.name')"
          width="210"
          sortable="custom"
          prop="obra_nombre"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.obra_nombre }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('obra.origin')"
          width="210"
          sortable="custom"
          prop="orig_id"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ origen(scope.row.orig_id) }}</span>
          </template>
        </el-table-column>
        <!--
        <el-table-column
          :label="$t('obra.phone')"
          width="210"
          sortable="custom"
          prop="obra_telefono"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.obra_telefono }}</span>
          </template>
        </el-table-column>
        -->
        <el-table-column
          :label="$t('obra.address')"
          min-width="250"
          sortable="custom"
          prop="obra_direccion"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ scope.row.obra_direccion }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('obra.neighborhood')"
          min-width="250"
          sortable="custom"
          prop="barr_descripcion"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ barrio(scope.row.barr_id) }}</span>
          </template>
        </el-table-column>
         <el-table-column
          :label="$t('obra.description')"
          width="250"
          prop="obra_descripcion"
          sortable="custom"
          resizable
          >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.obra_descripcion != null ? scope.row.obra_descripcion : ' '  | truncate(truncSize)}}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('obra.status')"
          width="100"
          prop="rees_id"
          sortable="custom"
          resizable
        >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ estado(scope.row.rees_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="93">
          <template slot-scope="scope">
            <el-button
              size="mini"
              circle
              type="warning"
              @click="handleEdit(scope.$index, scope.row)" :title="$t('edit')"><i class="el-icon-edit"></i></el-button>
                  <el-button
                    :disabled='scope.row.rees_id != 1'
                    size="mini"
                    circle
                    type="success"
                    :title="$t('print')"
                    @click="handlePrint(scope.$index, scope.row)"><i class="el-icon-printer"></i></el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
    </el-main>
   </el-container>
  </el-main>
 </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getObrasRango, deleteObra, getEstados, printObra } from '@/api/obra'
import { getBarriosEmpresa } from '@/api/barrio'
import { getOrigenes } from '@/api/origen'
import { getOrdenes } from '@/api/ordentrabajo'

export default {
  data () {
    return {
      tabPosition: 'left',
      filtro: '',
      obra: {
        obra_id: null,
        obra_consecutivo: null,
        obra_nombre: null,
        obra_fecharecepcion: null,
        obra_direccion: null,
        obra_fechasolucion: null,
        obra_reportetecnico: null,
        obra_descripcion: null,
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
      qestados: [{ label: this.$i18n.t('obra.estado.select'), value: '' }],
      qtipos: [{ label: this.$i18n.t('obra.tipo.select'), value: '' }],
      qorigenes: [{ label: this.$i18n.t('obra.origen.select'), value: '' }],
      qrules: [
        /* {
          type: 'select',
          id: 'r.reti_id',
          label: this.$i18n.t('obra.type'),
          choices: []
        }, */
        {
          type: 'custom',
          id: 'r.obra_consecutivo',
          label: this.$i18n.t('obra.consec'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'b.barr_id',
          label: this.$i18n.t('obra.neighborhood'),
          choices: []
        },
        {
          type: 'select',
          id: 'r.rees_id',
          label: this.$i18n.t('obra.status'),
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
      sfilter: {
        logicalOperator: 'AND',
        children: [
          /* {
            type: 'query-builder-rule',
            query: {
              rule: 'r.reti_id',
              selectedOperator: '=',
              selectedOperand: 'Tipo de Reporte',
              value: null
            }
          }, */
          {
            type: 'query-builder-rule',
            query: {
              rule: 'r.obra_consecutivo',
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
    ...mapGetters([
      'empresa'
    ])
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
    handlePrint (index, row) {
      printObra(row.obra_id, this.empresa.empr_id)
    },
    handleFilter (filters) {
      this.filter = JSON.stringify(filters)
      this.getObras()
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
        this.getObras()
      }
    },
    handleHeader (newWidth, oldWidth, column, event) {
      if (column.property === 'obra_descripcion') {
        this.truncSize = Math.round(newWidth / 7.6)
        this.$refs.tableData.doLayout()
      }
    },
    handleEdit (index, row) {
      this.$router.push({ path: '/proceso/menu3obra/menu3-3edit/' + row.obra_id })
      console.log(index, row)
    },
    handleDelete (index, row) {
      this.$confirm(this.$i18n.t('obra.confirmationmsg') + ' "' + row.obra_id + '->' + row.obra_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteObra(row.obra_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getObras()
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
    handleSizeChange (val) {
      this.page_size = val
      this.getObras()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getObras()
    },
    getObras () {
    },
    changeTab (data) {
      console.log('changeTab: ' + JSON.stringify(data.name))
      const mes = data.name.split(':')[0]
      const anho = data.name.split(':')[1]
      const index = data.name.split(':')[2]
      this.getData(anho, mes, index, data.name)
    },
    actualizar () {
      this.filtro = this.fconsec
    },
    nuevo () {
      this.$router.push({ path: '/proceso/menu3obra/menu3-2create' })
    },
    estado (id) {
      console.log('Estado: Ingresado a Buscar Estado: ' + id)
      if (id === null) {
        return ''
      } else {
        console.log('Estados: ', JSON.stringify(this.estados))
        var estado = this.estados.find(e => e.rees_id === id)
        if (estado) {
          return estado.rees_descripcion
        } else {
          return ''
        }
      }
    },
    tipo (id) {
      if (id === null) {
        return ''
      } else {
        return this.tipos.find(e => e.reti_id === id, { reti_descripcion: '' }).reti_descripcion
      }
    },
    origen (id) {
      if (id === null) {
        return ''
      } else {
        return this.origenes.find(e => e.orig_id === id, { orig_descripcion: '' }).orig_descripcion
      }
    },
    barrio (id) {
      if (id === null) {
        return ''
      } else {
        return this.barrios.find(e => e.barr_id === id, { barr_descripcion: '' }).barr_descripcion
      }
    },
    getData (anho, mes, index, name) {
      this.loading = true
      getObrasRango(anho, mes, 1)
        .then((response) => {
          console.log('Periodo: ' + name)
          console.log('Data: ' + JSON.stringify(response.data))
          this.tableData = response.data
          this.loading = false
          // data.tableData = response.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    ordenes (id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var orden = this.ordenestrabajo.find((o) => o.ortr_id === id)
        if (orden) {
          return orden.ortr_consecutivo + ' - ' + orden.cuad_descripcion
        } else {
          return ''
        }
      }
    },
    filtrar (data) {
      if (!this.filtro) {
        return data
      } else {
        if (this.filtro.includes('|')) {
          var obra_consecutivo = parseInt(this.filtro.split('|')[1])
          if (data.obra_consecutivo === obra_consecutivo) {
            return data
          } else {
            return null
          }
        } else if (data.muot_id === parseInt(this.filtro)) {
          return data
        } else if (data.obra_descripcion.trim().toUpperCase().includes(this.filtro.toUpperCase())) {
          return data
        } else {
          return null
        }
      }
    }
  },
  beforeMount () {
    console.log('Entrando a beforeMount')
    this.qrules[0].choices = this.qtipos
    this.qrules[1].choices = this.qbarrios
    this.qrules[2].choices = this.qestados
    const date = new Date()
    this.anho_inicial = date.getFullYear()
    this.mes_inicial = date.getMonth() + 1
    this.createTabs()
  },
  created () {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.barrios.forEach(b => {
        this.qbarrios.push({ label: b.barr_descripcion, value: b.barr_id })
      })
    }).catch(error => {
      console.log(error)
    })
    getEstados().then(response => {
      this.estados = response.data
      this.estados.forEach(e => {
        this.qestados.push({ label: e.rees_descripcion, value: e.rees_id })
      })
    }).catch(error => {
      console.log('getEstados: ' + error)
    })
    /* getTipos().then(response => {
      this.tipos = response.data
      this.tipos.forEach(e => {
        this.qtipos.push({ label: e.reti_descripcion, value: e.reti_id })
      })
    }).catch(error => {
      console.log('getEstados: ' + error)
    }) */
    getOrigenes().then(response => {
      this.origenes = response.data
      this.origenes.forEach(e => {
        this.qorigenes.push({ label: e.orig_descripcion, value: e.orig_id })
      })
    }).catch(error => {
      console.log('getOrigenes: ' + error)
    })
  },
  mounted() {
    const start = async () => {
      this.getData(this.anho, this.mes, 0, this.tabsData[0].tabPeriodo)
      this.activeTab = this.tabsData[0].tabPeriodo
    }
    start()
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
