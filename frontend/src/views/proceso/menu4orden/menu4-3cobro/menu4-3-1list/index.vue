<template>
  <el-container>
    <el-main>
      <el-container>
        <el-header class="ordentrabajo_header">{{ $t('operativo.workordercobrotitle') }}
        </el-header>
        <el-main>
          <!-- <vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabels" :styled="qstyled" :maxDepth="3"></vue-query-builder> -->
          <!-- <el-button type="warning" icon="el-icon-search" circle @click="actualizar"></el-button> -->
        </el-main>
      </el-container>
      <el-container>
          <el-header>
            <el-button type="primary" icon="el-icon-circle-plus" circle @click="showDialog = true" ></el-button>
            <el-button type="success" icon="el-icon-refresh" circle @click="obtener()"></el-button>
          </el-header>
          <el-main>
            <el-table
            :data="tableData"
            stripe
            show-summary
            :summary-method="getSummaries"
            :default-sort = "{prop: 'ortr_id', order: 'descending'}"
            style="width: 100%"
            max-height="600"
            border
            @sort-change="handleSort"
            @filter-change="handleFilter">
            <el-table-column
              :label="$t('cotr.anho')"
              width="60"
              sortable="custom"
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
              sortable="custom"
              prop="cotr_periodo"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_periodo }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.consecutivo')"
              width="100"
              sortable="custom"
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
              sortable="custom"
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
              sortable="custom"
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
              sortable="custom"
              prop="cotr_luminaria_nueva"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_luminaria_nueva }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.tecnologia_anterior')"
              width="200"
              sortable="custom"
              prop="cotr_tecnologia_anterior"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_tecnologia_anterior }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.tecnologia_nueva')"
              width="200"
              sortable="custom"
              prop="cotr_tecnologia_nueva"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_tecnologia_nueva }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.potencia_anterior')"
              width="200"
              sortable="custom"
              prop="cotr_potencia_anterior"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_potencia_anterior }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.potencia_nueva')"
              width="200"
              sortable="custom"
              prop="cotr_potencia_nueva"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_potencia_nueva }}</span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('cotr.direccion')"
              width="200"
              sortable="custom"
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
              sortable="custom"
              prop="cotr_cantidad"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.cotr_cantidad }}</span>
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
    </el-main>
  </el-container>
        <el-row>
        <el-col :span='24'>
          <img
            :title="$t('xls')"
            @click='exportarXls()'
            style='width: 32px; height: 36px; cursor: pointer;'
            :src="require('@/assets/xls.png')"
          />
        </el-col>
      </el-row>

  </el-main>
  <el-dialog
    title="Generar Orden de Trabajo"
    :visible.sync="showDialog"
    width="50%"
    :before-close="handleClose"
  >
    <el-container>
      <el-main>
        <el-form>
          <el-row>
            <el-col>
              <el-form-item label="Año">
                <el-input type="number" v-model="anho" />
              </el-form-item>
            </el-col>
            <el-col>
              <el-form-item label="Periodo">
                <el-select v-model="mes">
                  <el-option v-for="m in months" :key="m.id" :value="m.id" :label="$t(`months.${m.label}`)"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item>
                <span>MODERNIZACION</span>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-main>
    </el-container>
    <span slot="footer" class="dialog-footer">
        <el-button @click="showDialog = false">Cancelar</el-button>
        <el-button type="primary" @click="generar()">Confirmar</el-button>
    </span>
  </el-dialog>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getTipos } from '@/api/reporte'
import { obtener, generar } from '@/api/cobro'
import { parseTime } from '@/utils'
export default {
  data () {
    return {
      anho: null,
      mes: null,
      tireuc_id: 1,
      reti_id: 6,
      reporte_tipo: null,
      tableData: [],
      showDialog: false,
      total: 0,
      page_size: 50,
      current_page: 1
    }
  },
  computed: {
    ...mapGetters(['months'])
  },
  mounted () {
    getTipos().then(response => {
      this.reporte_tipo = response.data
      this.obtener()
    })
    const today = new Date()
    this.anho = today.getFullYear()
    this.mes = today.getMonth()
  },
  methods: {
    obtener () {
      this.tableData = []
      obtener(this.reti_id).then(response => {
        this.tableData = response.data
      })
    },
    generar () {
      this.showDialog = false
      generar(this.anho, this.mes, this.tireuc_id, this.reti_id).then(response => {
        if (response.data === true) {
          this.$message({
            showClose: true,
            message: 'Generación Finalizada...',
            type: 'success',
            duration: 5000
          })
          this.obtener()
        }
      })
    },
    exportarXls () {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then((excel) => {
        const tHeader = [
          'Año',
          'Periodo',
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
        if (index === 10) {
          sums[index] = 'Total Luminarias'
          return
        }
        const values = data.map(item => Number(item[column.property]))
        if (index === 11 && !values.every(value => isNaN(value))) {
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
    handleSort () {},
    handleFilter () {},
    handleSizeChange () {},
    handleCurrentChange () {},
    handleClose () {},
    actualizar () {}
  }
}
</script>
