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
            <el-button type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          </el-header>
          <el-main>
            <el-table
            :data="tableData"
            stripe
            :default-sort = "{prop: 'ortr_id', order: 'descending'}"
            style="width: 100%"
            max-height="600"
            border
            @sort-change="handleSort"
            @filter-change="handleFilter">
            <el-table-column
              :label="$t('ordentrabajo.consecutive')"
              width="120"
              sortable="custom"
              prop="ortr_consecutivo"
              resizable
               >
              <template slot-scope="scope">
                <span style="margin-left: 10px">{{ scope.row.ortr_consecutivo }}</span>
              </template>
            </el-table-column>
            <el-table-column
          :label="$t('ordentrabajo.date')"
          width="210"
          sortable="custom"
          prop="ortr_fecha"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.ortr_fecha | moment('YYYY-MM-DD') }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('ordentrabajo.crew')"
          min-width="200"
          sortable="custom"
          prop="cuad_descripcion"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ cuadrilla(scope.row.cuad_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('barrio.type')"
          min-width="150"
          sortable="custom"
          prop="tiba_descripcion"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ sector(scope.row.tiba_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="140">
          <template slot-scope="scope">
            <el-button
              size="mini"
              circle
              @click="handleEdit(scope.$index, scope.row)"><i class="el-icon-edit"></i></el-button>
            <el-button
              size="mini"
              circle
              type="success"
              :title="$t('print')"
              @click="handlePrint(scope.$index, scope.row)"><i class="el-icon-printer"></i></el-button>
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
  </el-container>
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
                <el-label>MODERNIZACION</el-label>
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
import { generar } from '@/api/cobro'
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
    })
    const today = new Date()
    this.anho = today.getFullYear()
    this.mes = today.getMonth()
  },
  methods: {
    generar () {
      generar(this.anho, this.mes, this.tireuc_id, this.reti_id)
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
