<template>
      <el-container>
        <el-header class="elemento_header">{{ $t('elemento.tabletitle') }}</el-header>
        <el-main>
          <query-builder :labels="qlabels" :rules="qrules" :styled="qstyled" :maxDepth="3" v-model="qbquery"></query-builder>
          <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Actualizar Aplicando el Filtro"></el-button>
          <el-container>
            <el-header>
              <el-button size="mini" type="primary" icon="el-icon-plus" circle @click="nuevo" ></el-button>
              <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
              <el-button size="large" type="primary" icon="el-icon-money" circle @click="showPrecioPeriodoDialog = true" title="Calcular Precio para Nuevo Año"></el-button>
              <el-button size="large" type="primary" icon="el-icon-upload" circle @click="showCotizadoPeriodoDialog = true" title="Subir Valores Cotizados para Nuevo Año"></el-button>
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
              <el-table
                :data="tableData"
                stripe
                @sort-change="handleTableSort"
                width="100%" height="500">
        <el-table-column
          :label="$t('elemento.id')"
          width="100"
          sortable
          prop="elem_id">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row._1 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.description')"
          sortable
          prop="elem_descripcion"
          width="350"
        >
          <template slot-scope="scope">
            <span style="margin-left: 10px" :title="scope.row.elem_descripcion">{{ scope.row._3 | fm_truncate(40) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.code')"
          sortable
          prop="elem_codigo"
          width="100">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row._2 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_anho_anterior')"
          width="100"
          prop="elpr_anho_anterior">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row._4 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_precio_anterior')"
          width="120"
          align="right"
           >
           <template slot-scope="scope">
            <span>{{ scope.row._5 | toThousandslsFilter }}</span>
           </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_ipc')"
          width="120"
          align="right"
           >
           <template slot-scope="scope">
            <span>{{ scope.row._6 }}</span>
           </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_incremento')"
          width="120"
          align="right"
           >
           <template slot-scope="scope">
            <span>{{ scope.row._8 - scope.row._5 | toThousandslsFilter }}</span>
           </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_precio_nuevo')"
          width="120"
          align="right"
           >
           <template slot-scope="scope">
            <span>{{ scope.row._8 }}</span>
           </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_precio_cotizado')"
          width="120"
          align="right"
           >
           <template slot-scope="scope">
            <span>{{ scope.row._9 }}</span>
           </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_precio')"
          width="120"
          align="right"
           >
          <template slot-scope="scope">
            <template v-if="scope.row.edit">
              <el-input v-model="scope.row._10" class="edit-input" size="small" />
              <el-button
                class="cancel-btn"
                size="mini"
                icon="el-icon-close"
                type="warning"
                circle
                @click="cancelEdit(scope.row)"
              />
            </template>
            <span v-else>{{ scope.row._10 | toThousandslsFilter }}</span>
            <el-button
              v-if="scope.row.edit"
              circle
              size="mini"
              icon="el-icon-check"
              @click="confirmEdit(scope.row)"
            />
            <el-button
              v-else
              circle
              size="mini"
              icon="el-icon-edit"
              style="border-style: hidden;"
              @click="scope.row.edit=!scope.row.edit"
            />
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_anho')"
          prop="elpr_anho"
          sortable
          width="120">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row._11 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.date')"
          width="100">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row._12  | moment('YYYY-MM-DD') }}</span>
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
    <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
   </el-container>
  </el-main>
  <el-dialog
    title="Precios Periodo"
    :visible.sync="showPrecioPeriodoDialog"
    width="40%"
    destroy-on-close
    center
    @closed="handlePrecioPeriodoDialogClosed"
  >
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
        <el-button :disabled="!precioAnho || !usarParaIncremento" type="primary" @click="handlePrecioPeriodo()">Calcular y Guardar</el-button>
    </span>
  </el-dialog>
  <el-dialog
    title="Precios Cotizados Periodo"
    :visible.sync="showCotizadoPeriodoDialog"
    width="40%"
    destroy-on-close
    center
    @closed="handleCotizadoPeriodoDialogClosed"
  >
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
                <el-upload
                                class="upload-demo"
                                ref="uploadFile"
                                name="precio_cotizado"
                                :action="uploadFileUrl()"
                                :auto-upload="false"
                                :on-success="uploadOk"
                                :on-error="uploadError"
                                >
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
 </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import VueQueryBuilder from 'vue-query-builder'
import { getTodosPrecio, updatePriceElemento, todosPrecioXls, newPriceElemento, cargarPrecioFijo } from '@/api/elemento'
export default {
  components: {
    'query-builder': VueQueryBuilder
  },
  data () {
    return {
      anho: new Date().getFullYear(),
      tableData: [],
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
        cargarPrecioFijo(this.anho).then(response => {
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
        })
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
    handleFileChange (file, fileList) {
      console.log(file, fileList)
    },
    handleFileRemove (file, fileList) {
      console.log(file, fileList)
    },
    handleFileExceed (files, fileList) {
      console.log(files, fileList)
    },
    uploadFileUrl() {
      return this.baseurl.url + '/elem/upfipr/' + this.cotizadoAnho
    },
    uploadFileAction () {
      this.$refs.uploadFile.submit()
    },
    handleCotizadoPeriodoDialogClosed () {
      this.precioAnho = new Date().getFullYear()
      this.precioIncremento = null
    },
    handleChangeAnho () {
      this.getElementos()
    },
    handlePrecioPeriodoDialogClosed () {
      this.precioAnho = new Date().getFullYear()
      this.precioIncremento = null
    },
    handlePrecioPeriodo () {

    },
    procesarPrecioPeriodo () {
      this.showPrecioPeriodoDialog = false
      const loading = this.$loading({
        lock: true
      })
      newPriceElemento(parseInt(this.precioAnho), this.usarParaIncremento).then(resp => {
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
      })
    },
    handleSizeChange (val) {
      this.page_size = val
      this.getElementos()
    },
    handleCurrentChange (val) {
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
    actualizar () {
      this.getElementos()
    },
    nuevo () {
      this.$router.push({ path: '/administracion/elemento/crear' })
    },
    buscar (query) {
      this.filter = 'f:' + query.toUpperCase()
      this.current_page = 1
      this.getElementos()
    },
    getElementos () {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      const loading = this.$loading({
        lock: true
      })
      getTodosPrecio(this.page_size, this.current_page, this.order, this.qbquery, parseInt(this.anho))
        .then(response => {
          this.total = response.data.total
          if (this.order.includes('desc')) {
            this.tableData = response.data.data.map(v => {
              this.$set(v, 'edit', false) // https://vuejs.org/v2/guide/reactivity.html
              this.$set(v, 'precioOriginal', v._5)
              return v
            }).reverse()
          } else {
            this.tableData = response.data.data.map(v => {
              this.$set(v, 'edit', false) // https://vuejs.org/v2/guide/reactivity.html
              this.$set(v, 'precioOriginal', v._5)
              return v
            })
          }
          loading.close()
        }).catch(() => {
          loading.close()
        })
    },
    cancelEdit (row) {
      row._5 = row.precioOriginal
      row.edit = false
      this.$message({
        message: 'El precio se restauró a su valor original',
        type: 'warning'
      })
    },
    confirmEdit (row) {
      row.edit = false
      const loading = this.$loading({
        lock: true
      })
      updatePriceElemento(this.anho, row._1, parseInt(row._10)).then(response => {
        if (response.data === 'true') {
          loading.close()
          this.getElementos()
          this.$message({
            message: 'El precio ha sido modificado',
            type: 'success'
          })
        } else {
          row._5 = row.precioOriginal
          this.$message({
            message: 'El precio se restauró a su valor original',
            type: 'warning'
          })
        }
      }).catch(() => {
        loading.close()
        row._5 = row.precioOriginal
        this.$message({
          message: 'El precio se restauró a su valor original',
          type: 'warning'
        })
      })
    },
    exportarXls () {
      const loading = this.$loading({
        text: 'Exportando a Excel',
        lock: true
      })
      todosPrecioXls(parseInt(this.anho))
        .then(response => {
          loading.close()
          var blob = response.data
          const d = new Date()
          var _datestring = d.getDate() + '-' + (d.getMonth() + 1) + '-' + d.getFullYear() + '_' + d.getHours() + ':' + d.getMinutes()
          const filename = 'Material_Precio_' + _datestring + '.xlsx'
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
  mounted () {
    this.getElementos()
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
