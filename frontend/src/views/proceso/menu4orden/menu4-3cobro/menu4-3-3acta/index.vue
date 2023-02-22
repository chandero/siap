<template>
  <el-container>
    <el-header class="factura_header">{{ $t('factura.tabletitle') }}</el-header>
    <el-main>
      <query-builder :labels="qlabels" :rules="qrules" :styled="qstyled" :maxDepth="3" v-model="qbquery"></query-builder>
      <el-button type="warning" icon="el-icon-search" circle @click="actualizar"
        title="Actualizar Aplicando el Filtro"></el-button>
      <el-container>
        <el-header>
          <el-button size="mini" type="primary" icon="el-icon-plus" circle @click="nuevo"></el-button>
          <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
        </el-header>
        <el-main>
          <el-table :data="tableData" stripe width="100%" height="500">
            <el-table-column type="expand">
              <template slot-scope="props">
                <el-table :data="props.row.ordenes" stripe style="width:100%">
                  <el-table-column :label="$t('factura.orden')" width="250">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.reti_descripcion }} {{ scope.row.cotr_tipo_obra_tipo |
                        arabicToRoman }} </span>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('factura.consecutivo')" width="250">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.cotr_consecutivo }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </template>
            </el-table-column> -->
            <el-table-column :label="$t('factura.numero')" width="120" prop="cofa_factura">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cofa_factura }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('factura.anho')" width="100" prop="cofa_anho">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cofa_anho }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('factura.periodo')" width="180">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cofa_periodo }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('factura.fecha')" width="180">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cofa_fecha | moment('YYYY-MM-DD') }}</span>
              </template>
            </el-table-column>
            <el-table-column align="right" width="90">
              <template slot-scope="scope">
                <el-button size="mini" circle type="primary" @click="handleEdit(scope.$index, scope.row)"><i
                    class="el-icon-edit"></i></el-button>
                <el-button size="mini" circle type="danger" @click="handleDelete(scope.$index, scope.row)"><i
                    class="el-icon-delete"></i></el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :page-size="page_size"
            layout="sizes, prev, pager, next, total" :total="total">
          </el-pagination>
        </el-main>
      </el-container>
    </el-main>
    <img :title="$t('xls')" @click="handleRelacion()" style="width:32px; height: 36px; cursor: pointer;"
      :src="require('@/assets/xls.png')" />
    <el-dialog title="Factura" :visible.sync="showFacturaDialog" width="50%" destroy-on-close center
      @closed="handleFacturaDialogClosed">
      <el-container>
        <el-main>
          <el-form v-model="factura" label-position="top" label-width="200px">
            <el-row>
              <el-col :span="8">
                <el-form-item label="Prefijo Factura">
                  <el-input type="text" v-model="factura.cofa_prefijo" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Factura">
                  <el-input type="number" v-model="factura.cofa_factura" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Sufijo Factura">
                  <el-input type="text" v-model="factura.cofa_sufijo" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="8">
                <el-form-item label="Año">
                  <el-input type="number" v-model="factura.cofa_anho" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Periodo">
                  <el-select v-model="factura.cofa_periodo">
                    <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="Fecha">
                  <el-date-picker type="date" v-model="factura.cofa_fecha" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <DoubleListBox :source="ordenes_disponibles" :destination="ordenes_seleccionadas"
                  @selected-data="handleDoubleListBoxSelection">
                </DoubleListBox>
              </el-col>
            </el-row>
          </el-form>
        </el-main>
      </el-container>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showFacturaDialog = false">Cancelar</el-button>
        <el-button :disabled="!factura.cofa_factura || !factura.cofa_anho || !factura.cofa_periodo || !factura.cofa_fecha"
          type="primary" @click="guardar()">Confirmar</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import VueQueryBuilder from 'vue-query-builder'
import { facturaCrear, facturaActualizar, facturaTodas, facturaEliminar, ordenesSinFactura, relacion2 } from '@/api/cobro'
import { mapGetters } from 'vuex'
import DoubleListBox from '@/components/DoubleListBox'

export default {
  components: {
    'query-builder': VueQueryBuilder,
    DoubleListBox
  },
  data() {
    return {
      showFacturaDialog: false,
      ordenes_disponibles: [],
      ordenes_seleccionadas: [],
      isEdition: false,
      factura: {
        cofa_factura: null,
        cofa_anho: new Date().getFullYear(),
        cofa_periodo: new Date().getMonth() + 1,
        cofa_fecha: new Date(),
        cofa_prefijo: null,
        cofa_sufijo: null,
        cofa_estado: 1,
        ordenes: []
      },
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      tiposElemento: [],
      caracteristicas: [],
      order: '',
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
      qbquery_ant: {},
      qbquery: {},
      qrules: [
        {
          type: 'select',
          id: 'e.tiel_tipo',
          label: this.$i18n.t('elemento.type'),
          choices: [],
          operators: ['=']
        },
        {
          type: 'text',
          id: 'e.elem_descripcion',
          label: this.$i18n.t('elemento.elem_descripcion'),
          operators: ['igual a', 'no igual a', 'contiene a', 'comienza con', 'termina con']
        },
        {
          type: 'text',
          id: 'e.elem_codigo',
          label: this.$i18n.t('elemento.elem_codigo'),
          operators: ['=', '<>', '<=', '>=']
        }
      ]
    }
  },
  computed: {
    ...mapGetters(['months'])
  },
  methods: {
    guardar() {
      this.factura.cofa_factura = parseInt(this.factura.cofa_factura)
      this.factura.cofa_anho = parseInt(this.factura.cofa_anho)
      this.factura.cofa_periodo = parseInt(this.factura.cofa_periodo)
      this.factura.ordenes = this.ordenes_seleccionadas
      if (this.isEdition) {
        facturaActualizar(this.factura).then(response => {
          this.showFacturaDialog = false
          this.$message({
            type: 'success',
            message: 'Factura Actualizada con Exito...!'
          })
          this.getFacturas()
        }).catch(e => {
          this.showFacturaDialog = false
          this.$message({
            type: 'error',
            message: 'No Fué Posible Actualizar la Factura...!' + e
          })
          this.getFacturas()
        })
      } else {
        facturaCrear(this.factura).then(response => {
          this.showFacturaDialog = false
          this.$message({
            type: 'success',
            message: 'Factura Creada con Exito...!'
          })
          this.getFacturas()
        }).catch(e => {
          this.showFacturaDialog = false
          this.$message({
            type: 'error',
            message: 'No Fué Posible Crear la Factura...!' + e
          })
          this.getFacturas()
        })
      }
    },
    handleEdit(index, row) {
      this.isEdition = true
      row.ordenes.forEach(item => {
        this.ordenes_seleccionadas = row.ordenes.map(item => {
          return {
            cotr_id: item.cotr_id,
            reti_descripcion: item.reti_descripcion,
            cotr_tipo_obra_tipo: item.cotr_tipo_obra_tipo,
            cotr_consecutivo: item.cotr_consecutivo,
            name: item.reti_descripcion + ' ' + this.$options.filters.arabicToRoman(item.cotr_tipo_obra_tipo) + ' ' + item.cotr_consecutivo
          }
        })
      })
      this.factura = row
      this.getOrdenesSinFactura()
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('factura.confirmationmsg') + ' "' + row.cofa_factura + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        facturaEliminar(row.cofa_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getFacturas()
        }).catch((err) => {
          this.$message({
            type: 'error',
            message: this.$i18n.t('general.deletefail') + ' -> ' + err.msg
          })
          this.getFacturas()
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
      this.getFacturas()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getFacturas()
    },
    handleFacturaDialogIsOpen() {
      this.getOrdenesSinFactura()
    },
    handleFacturaDialogClosed() {
      this.ordenes_disponibles = []
      this.ordenes_seleccionadas = []
    },
    handleDoubleListBoxSelection(selection) {
      this.ordenes_seleccionadas = selection
    },
    handleRelacion() {
      const loading = this.$loading({
        lock: true,
        text: 'Generando Reporte...'
      })
      relacion2().then(resp => {
        loading.close()
        var blob = resp.data
        const filename = 'Relacion_Orden_Trabajo_ITAF_Factura' + '.xlsx'
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
      }).catch(() => {
        loading.close()
      })
    },
    actualizar() {
      this.getFacturas()
    },
    nuevo() {
      this.isEdition = false
      this.getOrdenesSinFactura()
    },
    buscar(query) {
      this.filter = 'f:' + query.toUpperCase()
      this.current_page = 1
      this.getFacturas()
    },
    getOrdenesSinFactura() {
      ordenesSinFactura().then(response => {
        this.ordenes_disponibles = response.data.map(item => {
          return {
            cotr_id: item._1,
            reti_descripcion: item._2,
            cotr_tipo_obra_tipo: item._3,
            cotr_consecutivo: item._4,
            name: item._2 + ' ' + this.$options.filters.arabicToRoman(item._3) + ' ' + item._4
          }
        })
        this.showFacturaDialog = true
      }).catch((err) => {
        this.$message({
          type: 'error',
          message: this.$i18n.t('general.loadfail') + ' -> ' + err.msg
        })
      })
    },
    getFacturas() {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      facturaTodas(this.page_size, this.current_page, this.order, this.qbquery)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.data
        }).catch(() => { })
    }
    /* exportarXls () {
      todosXls(this.qbquery)
        .then(response => {
          var blob = response.data
          const d = new Date()
          var _datestring = d.getDate() + '-' + (d.getMonth() + 1) + '-' + d.getFullYear() + '_' + d.getHours() + ':' + d.getMinutes()
          const filename = 'Material_' + _datestring + '.xlsx'
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
        .catch(() => {})
    } */
  },
  mounted() {
    this.getFacturas()
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
