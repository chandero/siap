<template>
  <el-container>
    <el-main>
      <el-container>
        <el-header class="muot_header">{{ $t('muot.title') }}
        </el-header>
        <el-main>
          <!--<vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabelsactualizar" :styled="qstyled" :maxDepth="3"></vue-query-builder>-->
          <el-form label-position="top">
            <el-row :gutter="10">
              <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                <el-form-item prop="fconsec" :label="$t('muot.consecutivo')">
                  <el-input type="consecutivo" style="font-size: 30px;" v-model="fconsec"
                    @input="fconsec = parseInt($event)"></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <el-form-item style="display: inline-block; margin: 0 auto;" label="">
                  <el-button type="warning" icon="el-icon-search" circle @click="actualizar"
                    title="Buscar Actas"></el-button>
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
              <el-button type="primary" icon="el-icon-plus" circle @click="nuevo" title="Crear Nueva Acta"></el-button>
            </el-col>
            <el-col :span="2">
              <el-button type="success" icon="el-icon-refresh" circle @click="actualizar"
                title="Actualizar listado de Actas"></el-button>
            </el-col>
            <el-col :span="18">
              <el-input style="display: inline-block; position: absolute;" v-model="filtro"
                @input="filtro = $event.toUpperCase()" placeholder="Buscar por..."></el-input>
            </el-col>
          </el-row>
        </el-header>
        <el-main>
          <el-table :data="tableData" stripe :default-sort="{ prop: 'muot_fecharecepcion', order: 'descending' }"
            style="width: 100%" max-height="600" border>
            <el-table-column :label="$t('muot.acta_anho')">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.muot_acta_anho }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('muot.acta_numero')">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.muot_acta_numero }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('muot.fecharecepcion')" width="100" sortable="custom"
              resizable>
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.muot_fecharecepcion | moment('YYYY/MM/DD') }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('muot.consecutivo')" width="120" sortable="custom"
              resizable>
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.muot_consecutivo }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('muot.descripcion')" width="210" sortable="custom" resizable>
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.muot_descripcion }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('muot.direccion')" min-width="250" sortable="custom"
              resizable>
              <template slot-scope="scope">
                <span>{{ scope.row.muot_direccion }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('obra.neighborhood')" min-width="250" sortable="custom" prop="barr_descripcion"
              resizable>
              <template slot-scope="scope">
                <span>{{ barrio(scope.row.barr_id) }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('muot.estado')" width="100" sortable="custom" resizable>
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ estado(scope.row.muot_estado) }}</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" :label="$t('table.accion')" width="93">
              <template slot-scope="scope">
                <el-button size="mini" circle type="warning" @click="handleEdit(scope.$index, scope.row)"
                  :title="$t('edit')"><i class="el-icon-edit"></i></el-button>
                <el-button :disabled='scope.row.rees_id != 1' size="mini" circle type="success" :title="$t('print')"
                  @click="handlePrint(scope.$index, scope.row)"><i class="el-icon-printer"></i></el-button>
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
import { getBarriosEmpresa } from '@/api/barrio'
import { getTodos, deleteMuot } from '@/api/municipio_orden_trabajo_acta'

export default {
  data() {
    return {
      tabPosition: 'left',
      filtro: '',
      tableData: [],
      columnDefs: null,
      page_size: 10,
      current_page: 1,
      total: 0,
      barrios: [],
      estados: [],
      tipos: [],
      origenes: [],
      ordenestrabajo: [],
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
    handlePrint(index, row) {
    },
    handleFilter(filters) {
      this.filter = JSON.stringify(filters)
      this.getData()
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
        this.getData()
      }
    },
    handleHeader(newWidth, oldWidth, column, event) {
      if (column.property === 'muot_descripcion') {
        this.truncSize = Math.round(newWidth / 7.6)
        this.$refs.tableData.doLayout()
      }
    },
    handleEdit(index, row) {
      this.$router.push({ path: '/ao/menu3edit/' + row.muot_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('muot.confirmationmsg') + ' "' + row.muot_id + '->' + row.muot_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteMuot(row.muot_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getActas()
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
      this.getData()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getData()
    },
    actualizar() {
      this.getData()
    },
    nuevo() {
      this.$router.push({ path: '/ao/menu2create' })
    },
    estado(id) {
      console.log('Estado: Ingresado a Buscar Estado: ' + id)
      if (id === null) {
        return ''
      } else {
        switch (id) {
          case 1:
            return 'Activa'
          case 4:
            return 'Entregada'
          case 6:
            return 'Cancelada'
          case 9:
            return 'Anulada'
          default:
            return 'No Definido'
        }
      }
    },
    tipo(id) {
      if (id === null) {
        return ''
      } else {
        return this.tipos.find(e => e.reti_id === id, { reti_descripcion: '' }).reti_descripcion
      }
    },
    origen(id) {
      if (id === null) {
        return ''
      } else {
        return this.origenes.find(e => e.orig_id === id, { orig_descripcion: '' }).orig_descripcion
      }
    },
    barrio(id) {
      if (id === null) {
        return ''
      } else {
        const barrio = this.barrios.find(e => e.barr_id === id)
        if (barrio) {
          return barrio.barr_descripcion
        } else {
          return ''
        }
      }
    },
    getData() {
      this.loading = true
      getTodos(this.page_size, this.current_page, this.filter, this.order)
        .then((response) => {
          this.tableData = response.data.data
          this.total = response.data.total
          this.loading = false
          // data.tableData = response.data
        })
        .catch(() => {
          this.loading = false
        })
    },
    ordenes(id) {
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
    filtrar(data) {
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
  beforeMount() {
    console.log('Entrando a beforeMount')
    this.qrules[0].choices = this.qtipos
    this.qrules[1].choices = this.qbarrios
    this.qrules[2].choices = this.qestados
  },
  created() {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.barrios.forEach(b => {
        this.qbarrios.push({ label: b.barr_descripcion, value: b.barr_id })
      })
    }).catch(error => {
      console.log(error)
    })
  },
  mounted() {
    const start = async () => {
      this.getData()
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

.vue-query-builder>>>.vqb-group {
  color: #333;
  background-color: #f5f5f5;
  border-color: #ddd;
}

.vue-query-builder>>>.vqb-group.depth-2 {
  border-left: 2px solid #8bc34a;
}

.vue-query-builder>>>.vqb-group.depth-3 {
  border-left: 2px solid #ffb94b;
}

.vue-query-builder>>>.panel {
  margin-bottom: 20px;
  background-color: #fff;
  border: 1px solid transparent;
  border-radius: 4px;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.05)
}

.vue-query-builder>>>.panel-default {
  border-color: #ddd;
}

.vue-query-builder>>>.pull-right {
  float: right !important;
}

.vue-query-builder>>>.btn {
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

.vue-query-builder>>>.btn:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}
</style>
