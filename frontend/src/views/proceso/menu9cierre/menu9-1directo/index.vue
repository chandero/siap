<template>
  <el-container>
    <el-header>
      Cierre Directo de Reportes
    </el-header>
    <el-main>
      <el-row :gutter="4">
        <el-form>
                    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
                        <el-form-item :label="$t('informe.initialDate')">
                            <el-date-picker v-model="fecha_inicial" format="yyyy/MM/dd"></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
                        <el-form-item :label="$t('informe.endDate')">
                            <el-date-picker v-model="fecha_final" format="yyyy/MM/dd"></el-date-picker>
                        </el-form-item>
                    </el-col>
        </el-form>
      </el-row>
      <el-row>
        <el-button type="primary" @click="getData()">Obtener Reportes</el-button>
      </el-row>
      <el-row><el-col><br/></el-col></el-row>
        <el-row>
          <el-col :span="24">
                      <el-table
                        :data="tableData"
                        stripe
                        :default-sort="{
                          prop: '_5',
                          order: 'ascending'
                        }"
                        style="width: 100%"
                        max-height="600"

                        @selection-change="handleSelectionChange">
                        <el-table-column
                          type="selection"
                          width="55">
                        </el-table-column>
                      <el-table-column
                        type="index"
                        width="50">
                      </el-table-column>
                        <el-table-column
                          :label="$t('reporte.date')"
                          width="100"
                          prop="repo_fecharecepcion"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row._6
                                | moment('YYYY/MM/DD')
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.type')"
                          width="300"
                          prop="_4"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row._4
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.number')"
                          width="120"
                          prop="_5"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row._5
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          :label="$t('reporte.date_solve')"
                          width="130"
                          prop="_7"
                          resizable
                        >
                          <template slot-scope="scope">
                            <span style="margin-left: 10px">{{
                              scope.row._7 | moment('YYYY/MM/DD')
                            }}</span>
                          </template>
                        </el-table-column>
                        <el-table-column
                          fixed="right"
                          :label="$t('table.accion')"
                          width="150"
                        >
                          <template slot-scope="scope">
                            <el-button
                              size="mini"
                              circle
                              type="info"
                              @click="
                                handleEdit(scope.$index, scope.row)
                              "
                              :title="$t('edit_element')"
                              ><i class="el-icon-edit-outline"></i>
                            </el-button>
                            <el-button
                              size="mini"
                              circle
                              type="success"
                              @click="handleClose(scope.$index, scope.row)"
                              :title="$t('close')"
                              ><i class="el-icon-check"></i>
                            </el-button>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-col>
                  </el-row>
                  <el-row><el-col><br/></el-col></el-row>
                  <el-row>
                    <el-col :span="4">
                      <el-button :disabled="multipleSelection.length == 0" type="primary" icon="el-icon-check" @click="handleCloseSelected()">Cerrar Selección</el-button>
                    </el-col>
                    <el-col :span="4">
                      <el-button :disabled="tableData.length == 0" type="success" icon="el-icon-check" @click="handleCloseAll()">Cerrar Todos</el-button>
                    </el-col>
                  </el-row>
    </el-main>
  </el-container>
</template>
<script>
import { getReportesParaCierreDirecto, cerrarReporte, cerrarReportes } from '@/api/reporte'
export default ({
  data() {
    return {
      tableData: [],
      fecha_inicial: null,
      fecha_final: null,
      multipleSelection: []
    }
  },
  beforeMount() {
    this.fecha_inicial = new Date()
    this.fecha_final = new Date()
  },
  methods: {
    getData() {
      const loading = this.$loading({
        lock: true,
        text: 'Cargando...'
      })
      getReportesParaCierreDirecto(this.fecha_inicial.getTime(), this.fecha_final.getTime()).then(response => {
        this.tableData = response.data
        loading.close()
      }).catch(error => {
        loading.close()
        this.$message.error(error)
      })
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
      console.log('Seleccionados: ' + JSON.stringify(this.multipleSelection))
    },
    handleCloseSelected() {
      this.$confirm('Seguro de Cerrar los Reportes Seleccionados?', 'Confirmar', {
        confirmButtonText: 'Sí',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        this.closeSelected()
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Cierre Cancelado'
        })
      })
    },
    handleCloseAll() {
      this.$confirm('Seguro de Cerrar TODOS Reportes?', 'Confirmar', {
        confirmButtonText: 'Sí',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        this.closeAll()
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Cierre Cancelado'
        })
      })
    },
    handleClose(index, item) {
      this.$confirm(`Seguro de Cerrar el Reporte ${item._4} ${item._5}?`, 'Confirmar', {
        confirmButtonText: 'Sí',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        this.closeReporte(item)
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Cierre Cancelado'
        })
      })
    },
    closeReporte(item) {
      cerrarReporte(item._1, item._2).then(response => {
        this.$message.success('Reportes cerrado correctamente')
        this.$nextTick(() => {
          this.getData()
        })
      }).catch(error => {
        this.$message.error(error)
      })
    },
    closeSelected() {
      var lista = []
      this.multipleSelection.forEach(element => {
        lista.push([element._1, element._2])
      })
      cerrarReportes(lista).then(response => {
        this.$message.success('Reportes cerrados correctamente')
        this.$nextTick(() => {
          this.getData()
        })
      }).catch(error => {
        this.$message.error(error)
      })
    },
    closeAll() {
      var lista = []
      this.tableData.forEach(element => {
        lista.push([element._1, element._2])
      })
      cerrarReportes(lista).then(response => {
        this.$message.success('Reportes cerrados correctamente')
        this.$nextTick(() => {
          this.getData()
        })
      }).catch(error => {
        this.$message.error(error)
      })
    }
  }
})
</script>
