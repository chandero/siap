<template>
  <el-container>
    <el-header class="elemento_header">{{ $t('mano_herramienta.tabletitle') }}</el-header>
    <el-main>
      <query-builder :labels="qlabels" :rules="qrules" :styled="qstyled" :maxDepth="3" v-model="qbquery"></query-builder>
      <el-button type="warning" icon="el-icon-search" circle @click="actualizar"
        title="Actualizar Aplicando el Filtro"></el-button>
      <el-container>
        <el-header>
          <el-button size="mini" type="primary" icon="el-icon-plus" circle @click="nuevo"></el-button>
          <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          <el-button size="large" type="primary" icon="el-icon-money" circle @click="showPrecioPeriodoDialog = true"
            title="Calcular Precio para Nuevo Año"></el-button>
          <!-- <el-button size="large" type="primary" icon="el-icon-upload" circle @click="showCotizadoPeriodoDialog = true" title="Subir Valores Cotizados para Nuevo Año"></el-button> -->
        </el-header>
        <el-main>
          <el-form>
            <el-row>
              <el-col :span="2">
                <el-form-item label="Año">
                  <el-input type="number" v-model="anho" label="Año" @change="handleChangeAnho"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-table :data="tableData" stripe @sort-change="handleTableSort" width="100%" height="500">
            <el-table-column :label="$t('mano_herramienta.math_descripcion')" sortable prop="_2" width="350">
              <template slot-scope="scope">
                <span style="margin-left: 10px" :title="scope.row._2">{{ scope.row._2 | fm_truncate(40)
                }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.math_codigo')" sortable prop="_3" width="90" align="right" header-align="center">
              <template slot-scope="scope">
                <span>{{ scope.row._3}}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_anho_anterior')" width="100" prop="_12">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row._12 }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_precio_anterior')" width="120" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row._13 | toThousandslsFilter }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_incremento')" width="120" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row._6 - scope.row._13 | toThousandslsFilter }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_precio')" width="120" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row._6 | toThousandslsFilter }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_es_porcentaje')" prop="_7" sortable width="120" align="center">
              <template slot-scope="scope">
                <el-checkbox disabled v-model="scope.row._7" />
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_rendimiento')" prop="_8" sortable width="120" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row._8 }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_cantidad')" prop="_9" sortable width="120" align="center">
              <template slot-scope="scope">
                <span>{{ scope.row._9 }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_anho')" prop="_4" sortable width="120">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row._4 }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.mathpr_fecha')" width="100">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row._5 | moment('YYYY-MM-DD') }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.cotr_tipo_obra')" prop="_10" sortable width="120">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row._10 }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('mano_herramienta.cotr_tipo_obra_tipo')" prop="_11" sortable width="120">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row._11 }}</span>
              </template>
            </el-table-column>
            <el-table-column
              fixed="right"
              align="right"
              width="90"
            >
              <template slot-scope="scope">
                <el-button
                  size="mini"
                  circle
                  type="primary"
                  @click="handleEdit(scope.$index, scope.row)"><i class="el-icon-edit"></i></el-button>
                  <el-popconfirm
                    title="Seguro de eliminar el registro?"
                    @confirm="handleDelete(scope.$index, scope.row)"
                  >
                    <el-button
                      slot="reference"
                      size="mini"
                      circle
                      type="danger"
                    ><i class="el-icon-delete"></i></el-button>
                </el-popconfirm>
              </template>
      </el-table-column>
          </el-table>
          <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :page-size="page_size"
            layout="sizes, prev, pager, next, total" :total="total">
          </el-pagination>
        </el-main>
        <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;"
          :src="require('@/assets/xls.png')" />
      </el-container>
    </el-main>
    <el-dialog title="Precios Periodo" :visible.sync="showPrecioPeriodoDialog" width="40%" destroy-on-close center
      @closed="handlePrecioPeriodoDialogClosed">
      <el-container>
        <el-main>
          <el-form label-position="left" label-width="200px">
            <el-row>
              <el-col :span="24">
                <el-form-item label="Año">
                  <el-input type="number" v-model="precioAnho" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="Incremento">
                  <el-radio v-model="usarParaIncremento" label="ipc">Usar IPC</el-radio>
                  <el-radio v-model="usarParaIncremento" label="ipp">Usar IPP</el-radio>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-main>
      </el-container>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showPrecioPeriodoDialog = false">Cancelar</el-button>
        <el-button :disabled="!precioAnho || !usarParaIncremento" type="primary" @click="handlePrecioPeriodo()">Calcular y
          Guardar</el-button>
      </span>
    </el-dialog>
    <el-dialog title="Precios Cotizados Periodo" :visible.sync="showCotizadoPeriodoDialog" width="40%" destroy-on-close
      center @closed="handleCotizadoPeriodoDialogClosed">
      <el-container>
        <el-main>
          <el-form label-position="left" label-width="200px">
            <el-row>
              <el-col :span="24">
                <el-form-item label="Año">
                  <el-input type="number" v-model="cotizadoAnho" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="Archivo">
                  <el-upload class="upload-demo" ref="uploadFile" name="precio_cotizado" :action="uploadFileUrl()"
                    :auto-upload="false" :on-success="uploadOk" :on-error="uploadError">
                    <el-button size="small" type="warning">Clic para subir archivo</el-button>
                    <div slot="tip" class="el-upload__tip">Solo archivo excel xlsx</div>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-main>
      </el-container>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showCotizadoPeriodoDialog = false">Cancelar</el-button>
        <el-button :disabled="!cotizadoAnho" type="primary" @click="uploadFileAction()">Subir y Actualizar</el-button>
      </span>
    </el-dialog>
    <el-dialog title="Gestionar Precio" :visible.sync="showManoHerramientaDialog">
      <el-form label-position="left" label-width="200px">
        <el-form-item label="Transporte y Herramienta">
          <el-select :disabled="enEdicion" v-model="manoherramientaprecio.math_id">
            <el-option
              v-for="item in listaManoHerramienta"
              :key="item.math_id"
              :label="item.math_descripcion"
              :value="item.math_id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Tipo Obra">
          <el-select :disabled="enEdicion" v-model="manoherramientaprecio.cotr_tipo_obra">
            <el-option
              v-for="item in listaTipoObra"
              :key="item.reti_id"
              :label="item.reti_descripcion"
              :value="item.reti_id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Nivel">
          <el-input :disabled="enEdicion" v-model="manoherramientaprecio.cotr_tipo_obra_tipo" />
        </el-form-item>
        <el-form-item label="Precio">
          <el-input type="number" v-model="manoherramientaprecio.mathpr_precio" />
        </el-form-item>
        <el-form-item label="Es Porcentaje">
          <el-checkbox v-model="manoherramientaprecio.mathpr_es_porcentaje" />
        </el-form-item>
        <el-form-item label="Rendimiento">
          <el-input type="number" v-model="manoherramientaprecio.mathpr_rendimiento" />
        </el-form-item>
        <el-form-item label="Cantidad">
          <el-input type="number" v-model="manoherramientaprecio.mathpr_cantidad" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleManoHerramientaPrecioSave()">Guardar</el-button>
          <el-button @click="handleManoHerramientaPrecioCancel()">Cancelar</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import VueQueryBuilder from 'vue-query-builder'
import { getManoHerramientaOrdenPrecio, getManoHerramientaOrden, getManoHerramientaOrdenPrecioXlsx, newPriceElemento, cargarPrecioFijo, saveManoHerramientaOrdenPrecio, deleteManoHerramientaOrdenPrecio } from '@/api/manoherramienta'
export default {
  components: {
    'query-builder': VueQueryBuilder
  },
  data() {
    return {
      showManoHerramientaDialog: false,
      enEdicion: false,
      manoherramientaprecio: {
        mathpr_id: null,
        math_id: null,
        cotr_tipo_obra: null,
        cotr_tipo_obra_tipo: null,
        mathpr_precio: null,
        mathpr_anho: null,
        mathpr_fecha: null,
        mathpr_rendimiento: 1,
        mathpr_cantidad: 1,
        mathpr_es_porcentaje: false
      },
      anho: new Date().getFullYear(),
      tableData: [],
      listaManoHerramienta: [],
      listaTipoObra: [{
        reti_id: 2,
        reti_descripcion: 'EXPANSION'
      },
      {
        reti_id: 6,
        reti_descripcion: 'MODERNIZACION'
      }],
      page_size: 10,
      current_page: 1,
      precioAnho: new Date().getFullYear(),
      cotizadoAnho: new Date().getFullYear(),
      precioIncremento: null,
      usarParaIncremento: 'ipc',
      total: 0,
      order: '',
      showPrecioPeriodoDialog: false,
      showCotizadoPeriodoDialog: false,
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
          type: 'text',
          id: 'o.elem_descripcion',
          label: this.$i18n.t('elemento.elem_descripcion'),
          operators: ['igual a', 'no igual a', 'contiene a', 'comienza con', 'termina con']
        },
        {
          type: 'text',
          id: 'cast(o.elem_codigo as int)',
          label: this.$i18n.t('elemento.elem_codigo'),
          operators: ['=', '<>', '<=', '>=']
        }
      ]
    }
  },
  computed: {
    ...mapGetters([
      'baseurl',
      'empresa',
      'sessionUUID',
      'months'
    ])
  },
  methods: {
    getManoHerramientaOrden() {
      getManoHerramientaOrden()
        .then(response => {
          this.listaManoHerramienta = response.data
        }).catch((err) => { console.err('Error al obtener los datos de mano de obra -> ' + err) })
    },
    uploadOk(response, file, list) {
      this.uno_ok = true
      console.log('respons: ' + JSON.stringify(response))
      console.log('file: ' + JSON.stringify(file))
      console.log('list: ' + JSON.stringify(list))
      if (response && file) {
        this.nofile = false
        this.$message({
          message: 'Archivo subido al servidor',
          type: 'success'
        })
        this.showCotizadoPeriodoDialog = false
        /* cargarPrecioFijo(this.anho).then(response => {
          if (response.data._1 === true) {
            this.$message({
              message: 'Precios cargados',
              type: 'success'
            })
          } else {
            this.$message({
              message: 'Error al cargar precios',
              type: 'warning'
            })
          }
          this.$message({
            message: 'Precios cargados',
            type: 'success'
          })
        }).catch(error => {
          this.$message({
            message: 'Error al cargar precios: ' + error,
            type: 'warning'
          })
        }) */
      }
    },
    uploadError(err, file) {
      this.uno_ok = false
      if (err && file) {
        this.$message({
          message: 'Error al cargar el archivo al servidor.',
          type: 'warning'
        })
      }
    },
    handleFileChange(file, fileList) {
      console.log(file, fileList)
    },
    handleFileRemove(file, fileList) {
      console.log(file, fileList)
    },
    handleFileExceed(files, fileList) {
      console.log(files, fileList)
    },
    uploadFileUrl() {
      return this.baseurl.url + '/elem/upfipr/' + this.cotizadoAnho
    },
    uploadFileAction() {
      this.$refs.uploadFile.submit()
    },
    handleEdit(index, row) {
      console.log('Row Selected: ' + JSON.stringify(row))
      this.enEdicion = true
      this.manoherramientaprecio = {
        math_id: row._1,
        math_codigo: row._3,
        mathpr_anho: row._4,
        mathpr_fecha: row._5,
        mathpr_precio: row._6,
        mathpr_es_porcentaje: row._7,
        mathpr_rendimiento: row._8,
        mathpr_cantidad: row._9,
        cotr_tipo_obra: row._10,
        cotr_tipo_obra_tipo: row._11
      }
      this.showManoHerramientaDialog = true
    },
    handleDelete(index, row) {
      this.manoherramientaprecio = {
        math_id: row._1,
        math_codigo: row._3,
        mathpr_anho: row._4,
        mathpr_fecha: row._5,
        mathpr_precio: row._6,
        mathpr_es_porcentaje: row._7,
        mathpr_rendimiento: row._8,
        mathpr_cantidad: row._9,
        cotr_tipo_obra: row._10,
        cotr_tipo_obra_tipo: row._11
      }
      deleteManoHerramientaOrdenPrecio(this.manoherramientaprecio).then(response => {
        if (response.data === true) {
          this.$message({
            message: 'Precio eliminado',
            type: 'success'
          })
          this.getElementos()
        } else {
          this.$message({
            message: 'Error al eliminar el precio',
            type: 'warning'
          })
        }
      }).catch(error => {
        this.$message({
          message: 'Error al eliminar el precio: ' + error,
          type: 'warning'
        })
      })
    },
    handleCotizadoPeriodoDialogClosed() {
      this.precioAnho = new Date().getFullYear()
      this.precioIncremento = null
    },
    handleChangeAnho() {
      this.getElementos()
    },
    handlePrecioPeriodoDialogClosed() {
      this.precioAnho = new Date().getFullYear()
      this.precioIncremento = null
    },
    handlePrecioPeriodo() {
      this.showPrecioPeriodoDialog = false
      this.procesarPrecioPeriodo()
    },
    handleManoHerramientaPrecioSave() {
      this.manoherramientaprecio.mathpr_precio = parseFloat(this.manoherramientaprecio.mathpr_precio)
      this.manoherramientaprecio.mathpr_rendimiento = parseFloat(this.manoherramientaprecio.mathpr_rendimiento)
      this.manoherramientaprecio.mathpr_cantidad = parseFloat(this.manoherramientaprecio.mathpr_cantidad)
      saveManoHerramientaOrdenPrecio(this.manoherramientaprecio).then(response => {
        if (response.data === true) {
          this.$message({
            message: 'Precio Guardado',
            type: 'success'
          })
          this.showManoHerramientaDialog = false
          this.getElementos()
        } else {
          this.$message({
            message: 'Error al guardar el precio',
            type: 'warning'
          })
        }
      }).catch(error => {
        this.$message({
          message: 'Error al guardar el precio: ' + error,
          type: 'warning'
        })
        this.showManoHerramientaDialog = false
      })
    },
    handleManoHerramientaPrecioCancel() {
      this.manoherramientaprecio = {
        mathpr_id: null,
        math_id: null,
        cotr_tipo_obra: null,
        cotr_tipo_obra_tipo: null,
        mathpr_precio: null,
        mathpr_anho: null,
        mathpr_fecha: null,
        mathpr_rendimiento: 1,
        mathpr_cantidad: 1,
        mathpr_es_porcentaje: false
      }
      this.showManoHerramientaDialog = false
    },
    procesarPrecioPeriodo() {
      this.showPrecioPeriodoDialog = false
      const loading = this.$loading({
        lock: true
      })
      /* newPriceElemento(parseInt(this.precioAnho), this.usarParaIncremento).then(resp => {
        loading.close()
        this.getElementos()
        if (resp.data === 'true') {
          this.$message({
            message: 'Precios Calculados con Exito',
            type: 'success'
          })
        } else {
          this.$alert('No se pudo calcular los nuevos precios, revise los valores de IPP e IPC del Año a Procesar',
            'Atención'
          )
        }
      }).catch(err => {
        loading.close()
        this.getElementos()
        this.$message.error('Al procesar nuevos precios, Error al conectar con el servidor:' + err)
      }) */
    },
    handleSizeChange(val) {
      this.page_size = val
      this.getElementos()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getElementos()
    },
    handleTableSort({ col, prop, order }) {
      console.log('Column:', col)
      console.log('Prop:', prop)
      console.log('Order:', order)
      switch (order) {
        case 'ascending':
          order = 'asc'
          break
        case 'descending':
          order = 'desc'
          break
        case null:
          order = ''
          break
      }
      this.order = prop + ' ' + order
      this.actualizar()
    },
    actualizar() {
      this.getElementos()
    },
    nuevo() {
      this.enEdicion = false
      this.manoherramientaprecio = {
        mathpr_id: null,
        math_id: null,
        cotr_tipo_obra: null,
        cotr_tipo_obra_tipo: null,
        mathpr_precio: null,
        mathpr_anho: this.anho,
        mathpr_fecha: (new Date()).getTime(),
        mathpr_rendimiento: 1,
        mathpr_cantidad: 1,
        mathpr_es_porcentaje: false
      }
      this.showManoHerramientaDialog = true
    },
    buscar(query) {
      this.filter = 'f:' + query.toUpperCase()
      this.current_page = 1
      this.getElementos()
    },
    getElementos() {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      const loading = this.$loading({
        lock: true
      })
      getManoHerramientaOrdenPrecio(this.page_size, this.current_page, this.order, this.qbquery, parseInt(this.anho))
        .then(response => {
          loading.close()
          this.total = response.data.total
          this.tableData = response.data.data
        }).catch(() => {
          loading.close()
        })
    },
    exportarXls() {
      const loading = this.$loading({
        text: 'Exportando a Excel',
        lock: true
      })
      getManoHerramientaOrdenPrecioXlsx(parseInt(this.anho))
        .then(response => {
          loading.close()
          var blob = response.data
          const d = new Date()
          var _datestring = d.getDate() + '-' + (d.getMonth() + 1) + '-' + d.getFullYear() + '_' + d.getHours() + ':' + d.getMinutes()
          const filename = 'ManoHerramienta_Precio_' + _datestring + '.xlsx'
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
        .catch(() => { loading.close() })
    }
  },
  mounted() {
    this.getElementos()
    this.getManoHerramientaOrden()
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
