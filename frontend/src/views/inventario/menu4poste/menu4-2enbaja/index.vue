<template>
  <el-container class="gestion-container">
    <el-main>
      <el-container>
        <el-header class="gestion_header">{{ $t('poste.eliminada.tabletitle') }}
        </el-header>
        <el-main>
          <query-builder :labels="qlabels" :rules="qrules" :styled="qstyled" :maxDepth="3" v-model="qbquery"></query-builder>
          <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Actualizar Aplicando el Filtro"></el-button>
        </el-main>
      </el-container>
      <el-container>
          <el-header>
            <el-button type="success" icon="el-icon-refresh" circle @click="actualizar" title="Refrescar Lista de Controles"></el-button>
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
        <el-table-column
          :label="$t('poste.gestion.code')"
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
          :label="$t('poste.gestion.address')"
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
          :label="$t('poste.gestion.neighborhood')"
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
          fixed="right"
          :label="$t('table.accion')"
          width="93">
          <template slot-scope="scope">
            <el-button
              size="mini"
              circle
              type="warning"
              @click="handleEdit(scope.$index, scope.row)"
              title="Modificar Poste"><i class="el-icon-edit"></i></el-button>
            <el-button
              size="mini"
              circle
              type="danger"
              title="Borrar Poste"
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
      <!--
      <el-row>
        <el-col :span="24">
          <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
        </el-col>
      </el-row>
      -->
    </el-main>
   </el-container>
  </el-main>
 </el-container>
</template>

<script>
import VueQueryBuilder from 'vue-query-builder'
import { mapGetters } from 'vuex'
import { getTodosEliminados, recuperarAap, validar } from '@/api/poste'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getAapConexiones } from '@/api/aap_conexion'
import { getAapUsos } from '@/api/aap_uso'
import { getElementos } from '@/api/elemento'
import { getAapTiposCarcasa } from '@/api/aap_tipo_carcasa'
import { getCaracteristica } from '@/api/caracteristica'
import { getAapModelos } from '@/api/aap_modelo'
import { informe_siap_inventario_filtro_xls } from '@/api/informe'

export default {
  data () {
    return {
      tableData: [],
      barrios: [],
      sectores: [],
      aap_conexiones: [],
      aap_usos: [],
      elementos: [],
      qbquery: {},
      qbarrios: [{ label: this.$i18n.t('barrio.select'), value: '' }],
      qtiposbarrio: [{ label: this.$i18n.t('barrio.type'), value: '' }],
      qconnections: [{ label: this.$i18n.t('connection.select'), value: '' }],
      quses: [{ label: this.$i18n.t('use.select'), value: '' }],
      qelements: [{ label: this.$i18n.t('element.select'), value: '' }],
      qpotencias: [{ label: this.$i18n.t('gestion.power.select'), value: '' }],
      qtecnologias: [{ label: this.$i18n.t('gestion.tecnology.select'), value: '' }],
      qtiposluminaria: [{ label: this.$i18n.t('cover.select'), value: '' }],
      qmodelos: [{ label: this.$i18n.t('model.select'), value: '' }],
      qestado: [{ label: 'Seleccione el Estado', value: '' }],
      tipobarrio_filters: [],
      barrios_filters: [],
      potencias: [],
      tecnologias: [],
      aap_modelos: [],
      aap_tipos_carcasa: [],
      qrules: [
        {
          type: 'custom',
          id: 'a.aap_id',
          label: this.$i18n.t('poste.gestion.code'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'b.barr_id',
          label: this.$i18n.t('control.gestion.neighborhood'),
          choices: [],
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 't.tiba_id',
          label: this.$i18n.t('control.gestion.neighborhoodtype'),
          choices: [],
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'a.esta_id',
          label: 'Estado Actual',
          choices: [
            { label: 'Activa', value: 1 },
            { label: 'Retirada', value: 2 },
            { label: 'En Baja', value: 9 }
          ],
          operators: ['=', '<>', '<', '<=', '>', '>=']
        }
      ],
      qlabels: {
        matchType: this.$i18n.t('qb.matchType'),
        matchTypes: [
          {
            id: 'all',
            label: this.$i18n.t('qb.matchTypeAll')
          },
          {
            id: 'any',
            label: this.$i18n.t('qb.matchTypeAny')
          }
        ],
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
  components: {
    'query-builder': VueQueryBuilder
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  methods: {
    handleFilter (filters) {
      this.filter = JSON.stringify(filters)
      this.getAaps()
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
        this.getAaps()
      }
    },
    handleRecover (index, row) {
      this.$prompt(this.$i18n.t('eliminada.confirmationmsg') + ' "' + row.aap_id + ' ' + row.aap_direccion + '" Ingrese el Código:', 'Recuperar Luminaria Seleccionada', {
        confirmButtonText: 'Confirmar',
        cancelButtonText: 'Cancelar'
      }).then(({ value }) => {
        validar(4, value).then(response => {
          if (response.data === true) {
            recuperarAap(row.aap_id).then(response => {
              this.$message({
                type: 'success',
                message: this.$i18n.t('eliminada.recoveredsuccessful')
              })
              this.getAaps()
            }).catch((err) => {
              this.$message({
                type: 'error',
                message: this.$i18n.t('eliminada.recoveredfail') + ' -> ' + err.msg
              })
            })
          } else {
            this.$alert('El código ingresado no es válido, por favor confirmelo', 'Error', {
              confirmButtonText: 'Cerrar'
            })
          }
        }).catch(error => {
          this.$message({
            type: 'error',
            message: 'Se presentó error al válidar el código (' + error + ')',
            duration: 5000
          })
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$i18n.t('eliminada.recovercancelled')
        })
      })
    },
    handleSizeChange (val) {
      this.page_size = val
      this.getAaps()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getAaps()
    },
    nuevo () {
      this.$router.push({ path: '/inventario/gestion/crear' })
    },
    actualizar () {
      this.getAaps()
    },
    getAaps () {
      getTodosEliminados(this.page_size, this.current_page, this.order, this.qbquery)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.aaps
        }).catch(() => {})
    },
    barrio (barr_id) {
      if (barr_id === null || barr_id === undefined || barr_id === 0) {
        return ''
      } else {
        if (this.barrios.length > 0) {
          return this.barrios.find(o => o.barr_id === barr_id, { barr_descripcion: '' }).barr_descripcion
        } else { return '' }
      }
    },
    sector (tiba_id) {
      if (tiba_id === null || tiba_id === undefined || tiba_id === 0) {
        return ''
      } else {
        if (this.sectores.length > 0) {
          return this.sectores.find(o => o.tiba_id === tiba_id, { tiba_descripcion: '' }).tiba_descripcion
        }
      }
    },
    elemento (elem_id) {
      if (elem_id === null || elem_id === undefined || elem_id === 0) {
        return ''
      } else {
        return this.elementos.find(o => o.elem_id === elem_id, { elem_descripcion: '' }).elem_descripcion
      }
    },
    aap_conexion (aaco_id) {
      if (aaco_id === null || aaco_id === undefined || aaco_id === 0) {
        return ''
      } else {
        if (this.aap_conexiones.length > 0) {
          return this.aap_conexiones.find(o => o.aaco_id === aaco_id, { aaco_descripcion: '' }).aaco_descripcion
        }
      }
    },
    exportarXls () {
      informe_siap_inventario_filtro_xls(new Date().getTime(), this.order, this.qbquery, 9).then(response => {
        var blob = response.data
        const filename = 'Informe_Poste_EnBaja.xlsx'
        if (window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveBlob(blob, filename)
        } else {
          var downloadLink = window.document.createElement('a')
          downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
          downloadLink.download = filename
          document.body.appendChild(downloadLink)
          downloadLink.click()
          document.body.removeChild(downloadLink)
        }
      })
    }
  },
  mounted () {
    this.qrules[2].choices = this.qbarrios
    this.qrules[3].choices = this.qtiposbarrio
    this.qrules[4].choices = this.qconnections
    this.qrules[5].choices = this.quses
    this.qrules[6].choices = this.qtiposluminaria
    this.qrules[7].choices = this.qtecnologias
    this.qrules[8].choices = this.qpotencias
    this.qrules[9].choices = this.qmodelos
  },
  created () {
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
              getCaracteristica(7).then(response => {
                this.tecnologias = response.data.cara_valores.split(',')
                this.tecnologias.forEach(t => {
                  this.qtecnologias.push({ label: t, value: t })
                })
                getCaracteristica(5).then(response => {
                  this.potencias = response.data.cara_valores.split(',')
                  this.potencias.forEach(p => {
                    this.qpotencias.push({ label: p, value: p })
                  })
                  getAapTiposCarcasa().then(response => {
                    this.aap_tipos_carcasa = response.data
                    this.aap_tipos_carcasa.forEach(c => {
                      this.qtiposluminaria.push({ label: c.aatc_descripcion, value: c.aatc_id })
                    })
                    getAapModelos().then(response => {
                      this.aap_modelos = response.data
                      this.aap_modelos.forEach(m => {
                        this.qmodelos.push({ label: m.aamo_descripcion, value: m.aamo_id })
                      })
                      this.getAaps()
                    }).catch(error => {
                      console.log(error)
                    })
                  }).catch(error => {
                    console.log(error)
                  })
                }).catch(error => {
                  console.log('Caracteristica 5: ' + error)
                })
              }).catch(error => {
                console.log('Caracteristica 7: ' + error)
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
</style>
