<template>
  <el-container>
    <el-header class="acta_header">{{ $t('acre.tabletitle') }}</el-header>
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
            <el-table-column :label="$t('acre.acre_numero')" width="120" prop="acre_numero">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.acre_numero }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('acre.acre_anho')" width="100" prop="acre_anho">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.acre_anho }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('acre.acre_periodo')" width="180">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.acre_periodo }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('acre.acre_fecha')" width="180">
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.acre_fecha | moment('YYYY-MM-DD') }}</span>
              </template>
            </el-table-column>
            <el-table-column align="right" width="90">
              <template slot-scope="scope">
                <el-button size="mini" circle type="primary" title="Descargar" @click="handlePrint(scope.row)"><i
                    class="el-icon-printer"></i></el-button>
<!--                 <el-button size="mini" circle type="success" title="Reprocesar" @click="handleDelete(scope.$index, scope.row)"><i
                    class="el-icon-refresh"></i></el-button>
 -->              </template>
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
    <el-dialog title="Acta de Redimensionamiento" :visible.sync="showActaRedimensionamientoDialog" width="50%" destroy-on-close center
      @closed="handleActaRedimensionamientoDialogClosed">
      <el-container>
        <el-main>
          <el-form v-model="acre" label-position="top" label-width="200px">
            <el-row>
              <el-col :span="24">
                <el-form-item label="Año">
                  <el-input type="number" v-model="acre.acre_anho" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="Periodo">
                  <el-select v-model="acre.acre_periodo">
                    <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="Fecha">
                  <el-date-picker type="date" v-model="acre.acre_fecha" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-main>
      </el-container>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showActaRedimensionamientoDialog = false">Cancelar</el-button>
        <el-button :disabled="!acre.acre_anho || !acre.acre_periodo"
          type="primary" @click="guardar()">Confirmar</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import VueQueryBuilder from 'vue-query-builder'
import { acreCrear, acreAnexo, acreTodas, acreReprocesar } from '@/api/acta_redimensionamiento'
import { mapGetters } from 'vuex'

export default {
  components: {
    'query-builder': VueQueryBuilder
  },
  data() {
    return {
      showActaRedimensionamientoDialog: false,
      isEdition: false,
      acre: {
        acre_anho: new Date().getFullYear(),
        acre_periodo: new Date().getMonth() + 1,
        acre_fecha: new Date(),
        acre_estado: 1
      },
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
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
    nuevo() {
      this.showActaRedimensionamientoDialog = true
    },
    handlePrint(row) {
      const loading = this.$loading({
        lock: true,
        text: 'Generando Acta de Redimensionamiento...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      acreCrear(row.acre_anho, row.acre_periodo).then(resp => {
        loading.close()
        this.showActaRedimensionamientoDialog = false
        this.$message({
          type: 'success',
          message: 'Acta de Redimensionamiento Creada con Exito...!'
        })
        this.getActas()
        var blob = resp.data
        const filename = resp.headers['content-disposition'].split('filename=')[1]
        if (window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveBlob(blob, filename)
        } else {
          var downloadLink = window.document.createElement('a')
          downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' }))
          downloadLink.download = filename
          document.body.appendChild(downloadLink)
          downloadLink.click()
          document.body.removeChild(downloadLink)
        }
        acreAnexo(row.acre_anho, row.acre_periodo).then(resp => {
          var blob = resp.data
          const filename = resp.headers['content-disposition'].split('filename=')[1]
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
        }).catch(e => {
          this.$message({
            type: 'error',
            message: 'No Fué Posible Crear el Anexo al Acta de Redimensionamiento...!' + e
          })
        })
      }).catch(e => {
        loading.close()
        this.showActaRedimensionamientoDialog = false
        this.$message({
          type: 'error',
          message: 'No Fué Posible Crear el Acta de Redimensionamiento...!' + e
        })
      })
    },
    guardar() {
      this.acre.acre_anho = parseInt(this.acre.acre_anho)
      this.acre.acre_periodo = parseInt(this.acre.acre_periodo)
      const loading = this.$loading({
        lock: true,
        text: 'Generando Acta...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      acreCrear(this.acre.acre_anho, this.acre.acre_periodo).then(resp => {
        loading.close()
        this.showActaRedimensionamientoDialog = false
        this.$message({
          type: 'success',
          message: 'Acta de Redimensionamiento Creada con Exito...!'
        })
        this.getActas()
        var blob = resp.data
        const filename = resp.headers['content-disposition'].split('filename=')[1]
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
        acreAnexo(this.acre.acre_anho, this.acre.acre_periodo).then(resp => {
          var blob = resp.data
          const filename = resp.headers['content-disposition'].split('filename=')[1]
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
        }).catch(e => {
          this.$message({
            type: 'error',
            message: 'No Fué Posible Crear el Anexo al Acta de Redimensionamiento...!' + e
          })
        })
      }).catch(e => {
        loading.close()
        this.showActaRedimensionamientoDialog = false
        this.$message({
          type: 'error',
          message: 'No Fué Posible Crear el Acta de Redimensionamiento...!' + e
        })
        this.getActas()
      })
    },
    handleEdit(index, row) {
      this.isEdition = true
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('acta.confirmationmsg') + ' "' + row.acta_numero + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        acreReprocesar(row.acta_id).then(response => {
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
          this.getActas()
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$i18n.t('general.deletecancelled')
        })
      })
    },
    handleSizeChange(val) {
      this.page_size = val
      this.getActas()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getActas()
    },
    handleRelacion() {
    },
    actualizar() {
      this.getActas()
    },
    buscar(query) {
      this.filter = 'f:' + query.toUpperCase()
      this.current_page = 1
      this.getActas()
    },
    getActas() {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      acreTodas(this.page_size, this.current_page, this.order, this.qbquery)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.data
        }).catch(() => { })
    },
    handleActaRedimensionamientoDialogClosed() {
      this.isEdition = false
      this.getActas()
    }
  },
  mounted() {
    this.getActas()
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
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

.el-icon-docx {
  background-image: url('~@/assets/icons8-docx-64.png')
}

.el-icon-xlsx {
  background-image: url('~@/assets/icons8-xlsx-64.png')
}
</style>
