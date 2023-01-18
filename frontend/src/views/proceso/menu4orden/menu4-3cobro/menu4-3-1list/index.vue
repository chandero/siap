<template>
  <el-container>
    <el-main>
      <el-container>
        <el-header class="ordentrabajo_header">{{ $t('operativo.workordercobrotitle') }}
        </el-header>
        <el-main>
          <query-builder :labels="qlabels" :rules="qrules" :styled="qstyled" :maxDepth="3" v-model="qbquery"></query-builder>
          <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Actualizar Aplicando el Filtro"></el-button>
        </el-main>
      </el-container>
      <el-container>
          <el-main>
            <el-form>
              <el-row>
                <el-col :span="1">
                  <el-button type="primary" icon="el-icon-plus" circle @click="showDialog = true" ></el-button>
                </el-col>
                <el-col :span="1">
                  <el-button type="success" icon="el-icon-refresh" circle @click="obtener()"></el-button>
                </el-col>
              </el-row>
            </el-form>
            <el-row>
            <el-col>
            <el-table
            :data="tableData"
            stripe
            show-summary
            :summary-method="getSummaries"
            style="width: 100%"
            max-height="600"
            border
            >
            <el-table-column
              :label="$t('cotr.anho')"
              width="60"
              prop="cotr_anho"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_anho }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.mes')"
              width="80"
              prop="cotr_periodo"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_periodo }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.tipo_obra')"
              width="150"
              prop="cotr_tipo_obra"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ tipo_obra(scope.row.cotr_tipo_obra) }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.tipo_obra_tipo')"
              width="80"
              prop="cotr_tipo_obra_tipo"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ parseInt(scope.row.cotr_tipo_obra_tipo) | arabicToRoman }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.consecutivo')"
              width="100"
              prop="cotr_consecutivo"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_consecutivo }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.fecha')"
              width="100"
              prop="cotr_fecha"
              resizable
            >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_fecha | moment('YYYY-MM-DD') }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.luminaria_anterior')"
              width="200"
              prop="cotr_luminaria_anterior"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_luminaria_anterior }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.luminaria_nueva')"
              width="200"
              prop="cotr_luminaria_nueva"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_luminaria_nueva }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.tecnologia_anterior')"
              width="135"
              prop="cotr_tecnologia_anterior"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_tecnologia_anterior }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.tecnologia_nueva')"
              width="130"
              prop="cotr_tecnologia_nueva"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_tecnologia_nueva }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.potencia_anterior')"
              width="125"
              prop="cotr_potencia_anterior"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_potencia_anterior }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.potencia_nueva')"
              width="120"
              prop="cotr_potencia_nueva"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_potencia_nueva }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.direccion')"
              width="250"
              prop="cotr_direccion"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_direccion }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.cantidad')"
              width="200"
              prop="cotr_cantidad"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_cantidad }}</span>
              </template>
            </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="100">
          <template slot-scope="scope">
            <!-- <el-button
              size="mini"
              circle
              type="warning"
              @click="handleEdit2(scope.$index, scope.row)" :title="$t('edit')"><i class="el-icon-edit"></i>
            </el-button> -->
            <el-button
              size="mini"
              circle
              type="success"
              :title="$t('xls')"
              @click="handleXls(scope.$index, scope.row)"><i class="el-icon-download"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-size="page_size"
        layout="sizes, prev, pager, next, total"
        :total="total">
      </el-pagination> -->
        </el-col>
      </el-row>
    </el-main>
  </el-container>
        <el-row>
        <el-col :span="1">
          <img
            :title="$t('xls')"
            @click='exportarXls()'
            style='width: 32px; height: 36px; cursor: pointer;'
            :src="require('@/assets/xls.png')"
          />
        </el-col>
        <el-col :span="1">
          <img
            :title="$t('relacion')"
            @click='showRelacionDialog = true'
            style='width: 32px; height: 36px; cursor: pointer;'
            :src="require('@/assets/prnt.png')"
          />
        </el-col>
        <el-col :span="1">
          <img
            :title="$t('cobro.acta_redimensionamiento')"
            @click='showActaDialog = true'
            style='width: 32px; height: 36px; cursor: pointer;'
            :src="require('@/assets/pdf.png')"
          />
        </el-col>
        <el-col :span="1">
          <img
            :title="$t('cobro.anexo_redimensionamiento')"
            @click='showAnexoDialog = true'
            style='width: 32px; height: 36px; cursor: pointer;'
            :src="require('@/assets/xls.png')"
          />
        </el-col>
      </el-row>
  </el-main>
  <el-dialog
    title="Generar Orden de Trabajo"
    :visible.sync="showDialog"
    width="40%"
    destroy-on-close
    center
    @closed="handleDialogClosed"
  >
    <el-container>
      <el-main>
        <el-form label-position="left" label-width="200px">
          <el-row>
            <el-col :span="24">
              <el-form-item label="Año">
                <el-input type="number" v-model="anho" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="Periodo">
                <el-select v-model="mes">
                  <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item label="Tipo de Obra">
                <el-select v-model="reti_id_gen">
                  <el-option v-for="r in tipos_obra" :key="r.reti_id" :value="r.reti_id" :label="r.reti_descripcion"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="Consecutivo ITAF Siguente">
                <el-input :disabled="validCsc" v-model="cotr_consecutivo" style="font-weight: bolder;" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-main>
    </el-container>
    <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false">Cancelar</el-button>
        <el-button :disabled="!reti_id_gen || !cotr_consecutivo || esGenerando" type="primary" @click="generar()">Confirmar</el-button>
    </span>
  </el-dialog>
  <el-dialog
    title="Generar Relación"
    :visible.sync="showRelacionDialog"
    width="40%"
    destroy-on-close
    center
    @closed="handleRelacionDialogClosed"
  >
    <el-container>
      <el-main>
        <el-form label-position="left" label-width="200px">
          <el-row>
            <el-col :span="24">
              <el-form-item label="Año">
                <el-input type="number" v-model="anho" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="Periodo">
                <el-select v-model="mes">
                  <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-main>
    </el-container>
    <span slot="footer" class="dialog-footer">
        <el-button @click="showRelacionDialog = false">Cancelar</el-button>
        <el-button :disabled="!anho || !mes" type="primary" @click="handleRelacion()">Imprimir</el-button>
    </span>
  </el-dialog>
  <el-dialog
    title="Generar Acta Redimensionamiento"
    :visible.sync="showActaDialog"
    width="40%"
    destroy-on-close
    center
    @closed="handleActaDialogClosed"
  >
    <el-container>
      <el-main>
        <el-form label-position="left" label-width="200px">
          <el-row>
            <el-col :span="24">
              <el-form-item label="Año">
                <el-input type="number" v-model="anho" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="Periodo">
                <el-select v-model="mes">
                  <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-main>
    </el-container>
    <span slot="footer" class="dialog-footer">
        <el-button @click="showActaDialog = false">Cancelar</el-button>
        <el-button :disabled="!anho || !mes" type="primary" @click="handleActa()">Generar</el-button>
    </span>
  </el-dialog>
  <el-dialog
    title="Generar Anexo Redimensionamiento"
    :visible.sync="showAnexoDialog"
    width="40%"
    destroy-on-close
    center
    @closed="handleAnexoDialogClosed"
  >
    <el-container>
      <el-main>
        <el-form label-position="left" label-width="200px">
          <el-row>
            <el-col :span="24">
              <el-form-item label="Año">
                <el-input type="number" v-model="anho" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="Periodo">
                <el-select v-model="mes">
                  <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-main>
    </el-container>
    <span slot="footer" class="dialog-footer">
        <el-button @click="showAnexoDialog = false">Cancelar</el-button>
        <el-button :disabled="!anho || !mes" type="primary" @click="handleAnexo()">Generar</el-button>
    </span>
  </el-dialog>
  </el-container>
</template>
<script>
import VueQueryBuilder from 'vue-query-builder'
import { mapGetters } from 'vuex'
import { getTipos } from '@/api/reporte'
import { obtener, generar, xls, verificar, consecutivo, relacion, actaRedimensionamiento, anexoRedimensionamiento } from '@/api/cobro'
import { getCaracteristica } from '@/api/caracteristica'
import { parseTime } from '@/utils'
export default {
  components: {
    'query-builder': VueQueryBuilder
  },
  data () {
    return {
      qbquery: {},
      qrules: [
        {
          type: 'select',
          id: 'co1.cotr_tipo_obra',
          label: this.$i18n.t('cotr.tipo_obra'),
          choices: [
            { label: 'EXPANSION', value: 2 },
            { label: 'MODERNIZACION', value: 6 }
          ],
          operators: ['=']
        },
        {
          type: 'custom',
          id: 'co1.cotr_anho',
          label: this.$i18n.t('cotr.anho'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'co1.cotr_periodo',
          label: this.$i18n.t('cotr.mes'),
          choices: [],
          operators: ['=']
        },
        {
          type: 'select',
          id: 'cotr.cotr_direccion',
          label: this.$i18n.t('cotr.direccion'),
          choices: [],
          operators: ['=']
        },
        {
          type: 'select',
          id: 'co1.cotr_tecnologia_nueva',
          label: this.$i18n.t('cotr.tecnologia_nueva'),
          choices: [],
          operators: ['=']
        },
        {
          type: 'custom',
          id: 'co1.cotr_potencia_nueva',
          label: this.$i18n.t('cotr.potencia_nueva'),
          choices: [],
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
      esGenerando: false,
      validCsc: false,
      anho: null,
      mes: null,
      tireuc_id: 1,
      reti_id: 6,
      reti_id_gen: null,
      cotr_consecutivo: null,
      reporte_tipo: null,
      tableData: [],
      showDialog: false,
      showRelacionDialog: false,
      showActaDialog: false,
      showAnexoDialog: false,
      total: 0,
      page_size: 50,
      current_page: 1,
      tipos_obra: [
        {
          reti_id: 2,
          reti_descripcion: 'EXPANSION'
        },
        {
          reti_id: 6,
          reti_descripcion: 'MODERNIZACION'
        }
      ],
      order: 'co1.cotr_anho, co1.cotr_periodo, co1.cotr_tipo_obra, co1.cotr_consecutivo'
    }
  },
  watch: {
    qbquery () {
      this.obtener()
    },
    reti_id_gen () {
      if (this.reti_id_gen) {
        consecutivo(this.reti_id_gen).then(res => {
          if (res.data) {
            this.cotr_consecutivo = res.data + 1
            this.validCsc = true
          } else {
            this.cotr_consecutivo = null
            this.validCsc = false
          }
        })
      }
    }
  },
  computed: {
    ...mapGetters(['months'])
  },
  mounted () {
    getTipos().then(response => {
      this.reporte_tipo = response.data
      this.months.forEach(m => {
        this.qrules[2].choices.push({ label: this.$i18n.t(`months.${m.label}`), value: m.id })
      })
      getCaracteristica(7).then(response => {
        const tecnologias = response.data.cara_valores.split(',')
        const qtecnologias = []
        tecnologias.forEach(t => {
          qtecnologias.push({ label: t, value: t })
        })
        this.qrules[4].choices = qtecnologias
        this.obtener()
      })
    })
    const today = new Date()
    this.anho = today.getFullYear()
    this.mes = today.getMonth()
  },
  methods: {
    tipo_obra (tipo) {
      return this.tipos_obra.find(t => t.reti_id === tipo).reti_descripcion
    },
    handleDialogClosed () {
      this.reti_id_gen = null
      this.cotr_consecutivo = null
    },
    obtener () {
      const loading = this.$loading({
        lock: true,
        text: 'Cargando...'
      })
      this.tableData = []
      obtener(this.order, this.qbquery).then(response => {
        this.tableData = response.data
        loading.close()
      }).catch(() => {
        loading.close()
      })
    },
    generar () {
      verificar(this.reti_id_gen, this.anho, this.mes).then(response => {
        if (response.data === true) {
          this.$alert('Ya existen ordenes de trabajo para este periodo', 'Atención', {
            confirmButtonText: 'Cerrar'
          })
        } else {
          this.esGenerando = true
          generar(this.anho, this.mes, this.tireuc_id, this.reti_id_gen, this.cotr_consecutivo).then(response => {
            this.esGenerando = false
            this.showDialog = false
            if (response.data === true) {
              this.$message({
                showClose: true,
                message: 'Generación Finalizada...',
                type: 'success',
                duration: 5000
              })
              this.obtener()
            }
          }).catch(() => {
            this.esGenerando = false
            this.showDialog = false
          })
        }
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then((excel) => {
        const tHeader = [
          'Año',
          'Periodo',
          'Tipo Obra',
          'Tipo',
          'Consecutivo',
          'Fecha',
          'Luminaria Anterior',
          'Luminaria Nueva',
          'Tecnología Anterior',
          'Tecnología Nueva',
          'Potencia Anterior',
          'Potencia Nueva',
          'Dirección',
          'Cantidad'
        ]
        const filterVal = [
          'cotr_anho',
          'cotr_periodo',
          'cotr_tipo_obra',
          'cotr_tipo_obra_tipo',
          'cotr_consecutivo',
          'cotr_fecha',
          'cotr_luminaria_anterior',
          'cotr_luminaria_nueva',
          'cotr_tecnologia_anterior',
          'cotr_tecnologia_nueva',
          'cotr_potencia_anterior',
          'cotr_potencia_nueva',
          'cotr_direccion',
          'cotr_cantidad'
        ]
        const list = this.tableData
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(
          tHeader,
          data,
          'general_orden_de_trabajo_itaf_año_' +
            this.anho +
            '_periodo_' +
            this.$i18n.t(`months.m${this.mes}`)
        )
        this.downloadLoading = false
      })
    },
    formatJson (filterVal, jsonData) {
      return jsonData.map((v) =>
        filterVal.map((j) => {
          if (j === 'cotr_fecha') {
            return parseTime(v[j], '{y}-{m}-{d}')
          } else if (j === 'cotr_tipo_obra') {
            return this.tipo_obra(v[j])
          } else if (j === 'cotr_tipo_obra_tipo') {
            return this.$options.filters.arabicToRoman(v[j])
          } else {
            return v[j]
          }
        })
      )
    },
    getSummaries (param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 12) {
          sums[index] = 'Total Luminarias'
          return
        }
        const values = data.map(item => Number(item[column.property]))
        if ((index === 13) && !values.every(value => isNaN(value))) {
          sums[index] = '' + values.reduce((prev, curr) => {
            const value = Number(curr)
            if (!isNaN(value)) {
              return prev + curr
            } else {
              return prev
            }
          }, 0)
        } else {
          sums[index] = ''
        }
      })
      return sums
    },
    handleXls (idx, orden) {
      console.log('_idx: ', idx, ', row: ', JSON.stringify(orden))
      xls(orden.cotr_id, orden.cotr_tipo_obra).then(resp => {
        var blob = resp.data
        const filename = 'Informe_Orden_Trabajo_ITAF_' + orden.cotr_consecutivo + '.xlsx'
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
    },
    handleRelacion () {
      this.showRelacionDialog = false
      relacion(this.anho, this.mes).then(resp => {
        var blob = resp.data
        const filename = 'Relacion_Orden_Trabajo_ITAF_' + this.anho + '_' + this.$i18n.t(this.mes) + '.xlsx'
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
    },
    handleActa () {
      this.showActaDialog = false
      const loading = this.$loading({
        lock: true,
        text: 'Generando Acta...'
      })
      actaRedimensionamiento(this.anho, this.mes).then(resp => {
        var blob = resp.data
        const filename = resp.headers['content-disposition'].split(';')[1].split('=')[1]
        if (window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveBlob(blob, filename)
        } else {
          var downloadLink = window.document.createElement('a')
          downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
          downloadLink.download = filename
          document.body.appendChild(downloadLink)
          loading.close()
          downloadLink.click()
          document.body.removeChild(downloadLink)
        }
        this.handleAnexo(this.anho, this.mes)
      }).catch((err) => {
        loading.close()
        this.$message.error(err.message)
      })
    },
    handleAnexo () {
      this.showAnexoDialog = false
      const loading = this.$loading({
        lock: true,
        text: 'Generando Anexo...'
      })
      anexoRedimensionamiento(this.anho, this.mes).then(resp => {
        var blob = resp.data
        const filename = resp.headers['content-disposition'].split(';')[1].split('=')[1]
        if (window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveBlob(blob, filename)
        } else {
          var downloadLink = window.document.createElement('a')
          downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
          downloadLink.download = filename
          document.body.appendChild(downloadLink)
          loading.close()
          downloadLink.click()
          document.body.removeChild(downloadLink)
        }
      }).catch((err) => {
        loading.close()
        this.$message.error(err.message)
      })
    },
    handleSort () {},
    handleFilter () {},
    handleSizeChange () {},
    handleCurrentChange () {},
    handleClose () {},
    handleRelacionDialogClosed () {},
    handleActaDialogClosed () {},
    actualizar () {}
  }
}
</script>
