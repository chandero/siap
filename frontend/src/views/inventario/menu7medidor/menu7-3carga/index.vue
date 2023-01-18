<template>
    <el-container>
        <el-header>
            <el-row>
                <el-col :span="24">
                    <span>Carga de Lecturas Medidor</span>
                </el-col>
            </el-row>
        </el-header>
        <el-main>
            <el-container>
            <el-form size="medium">
                <el-row :gutter="4">
                    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
                        <el-form-item label="Año">
                            <el-input v-model="anho" type="number" />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
                        <el-form-item label="Mes">
                          <el-select style="width:100%;" ref="mes" v-model="mes" name="meses" :placeholder="$t('mes_select')">
                            <el-option v-for="m in months" :key="m.id" :label="$t('months.'+m.label)" :value="m.id" >
                            </el-option>
                          </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
                        <el-form-item label="Archivo Lecturas">
                            <el-upload
                                class="upload-demo"
                                ref="upload1"
                                name="file_medidor_lectura"
                                :action="action_one()"
                                :auto-upload="true"
                                :on-success="one_ok"
                                :on-error="one_error"
                                >
                                <el-button size="small" type="primary">Clic para subir archivo</el-button>
                                <div slot="tip" class="el-upload__tip">Solo archivo excel xlsx</div>
                            </el-upload>
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="24" :md="12" :lg="6" :xl="6">
                      <el-form-item v-show="inProgress" label="Progreso">
                        <el-progress :text-inside="true" :stroke-width="24" :percentage="porcentaje"></el-progress>
                      </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
                        <el-button :disabled="anho === null || mes === null || nofile" style="margin-left: 10px;" size="small" type="success" @click="procesarXlsx">Cargar Datos</el-button>
                    </el-col>
                </el-row>
            </el-form>
            </el-container>
            <el-row>
                <el-col>
                  <el-table
                    :data="tableData"
                    stripe
                    :row-class-name="tachado"
                    style="width: 100%">
                    <el-table-column
                      :label="$t('anho')"
                      width="150">
                      <template slot-scope="scope">
                        <span style="margin-left: 10px">{{ scope.row.controlcarga_anho }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      :label="$t('mes')"
                      width="120">
                      <template slot-scope="scope">
                        <span style="margin-left: 10px">{{ scope.row.controlcarga_mes }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      :label="$t('fecha')"
                      width="180">
                      <template slot-scope="scope">
                        <span style="margin-left: 10px">{{ scope.row.controlcarga_fecha | moment("YYYY-MM-DD HH:mm") }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      :label="$t('registros')"
                      width="100">
                      <template slot-scope="scope">
                        <span style="margin-left: 10px">{{ scope.row.controlcarga_registros }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      :label="$t('estado.title')"
                      width="100">
                      <template slot-scope="scope">
                        <i style="font-size: 20px;" :class="scope.row.controlcarga_exito ? 'el-icon-circle-check': 'el-icon-circle-close'" />
                      </template>
                    </el-table-column>
                    <el-table-column
                      :label="$t('mensaje')"
                      width="200">
                      <template slot-scope="scope">
                        <span style="margin-left: 10px" :title="scope.row.controlcarga_mensaje">{{ scope.row.controlcarga_mensaje | fm_truncate(40) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column
                      fixed="right"
                      :label="$t('table.accion')"
                      width="140">
                      <template slot-scope="scope">
                        <el-button
                          :disabled="scope.row.controlcarga_estado === 9"
                          size="mini"
                          circle
                          type="success"
                          title="Ver"
                          @click="handleView(scope.$index, scope.row)"><i class="el-icon-view"></i></el-button>
                        <el-button
                          :disabled="scope.row.controlcarga_procesado || scope.row.controlcarga_estado === 9"
                          size="mini"
                          circle
                          type="danger"
                          title="Borrar"
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
                </el-col>
            </el-row>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { eliminar, getTodos, procesar_xlsx } from '@/api/carga'

let msgServer

export default {
  data () {
    return {
      porcentaje: 0,
      inProgress: false,
      comenergia_id: null,
      anho: new Date().getFullYear(),
      mes: new Date().getMonth() + 1,
      uno_ok: false,
      nofile: true,
      comenergias: [],
      tableData: [],
      page_size: 10,
      current_page: 1,
      filter: '',
      order_by: '',
      total: 0
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
    handleView (index, row) {
      this.$router.push({ path: '/inventario/menu7medidor/menu7-4lectura/' + row.controlcarga_anho + '/' + row.controlcarga_mes })
    },
    handleDelete (index, row) {
      this.$confirm('Se borraran de forma permanente los datos. Continuar?', 'Advertencia', {
        confirmButtonText: 'Si',
        cancelButtonText: 'No',
        type: 'warning'
      }).then(() => {
        eliminar(row.controlcarga_anho, row.controlcarga_mes, row.comenergia_id, 1).then(response => {
          if (response.data === true) {
            this.$notify({
              title: 'Exito!',
              message: 'Datos eliminados',
              type: 'success'
            })
            this.getCargas()
          } else {
            this.$notify.error({
              title: 'Error',
              message: 'Error al eliminar los datos'
            })
          }
        }).catch((e) => {
          this.$notify.error({
            title: 'Error',
            message: 'Error en el proceso: ' + e
          })
        })
      })
    },
    handleSizeChange (val) {
      this.page_size = val
      this.getCargas()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getCargas()
    },
    handleSse () {
      this.$sse('/api/progressStatuses/' + this.sessionUUID, { format: 'json', withCredentials: true })
        .then(sse => {
          msgServer = sse
          sse.onError((e) => {
            console.error('lost connection; giving up!', e)
            sse.close()
          })
          sse.subscribe('', (message, rawEvent) => {
            const event = message.name
            switch (event) {
              case 'medidorPreparingEvent':
                this.inProgress = true
                this.porcentaje = 0
                break
              case 'medidorParsingEvent':
                this.inProgress = true
                this.porcentaje = (parseInt(message.data) / parseInt(message.size)) * 100
                break
              case 'medidorDoneEvent':
                this.inProgress = false
                this.porcentaje = 0
                this.getCargas()
                break
            }
            console.info('Received message', JSON.stringify(message))
          })
          /* setTimeout(() => {
            sse.unsubscribe('')
            console.log('Stopped listening to event-less messages!')
          }, 30000) */
        }).catch((err) => {
          console.error('Failed to connect to server', err)
        })
    },
    tachado ({ row, index }) {
      if (row.controlcarga_estado === 9) {
        return 'tachado-row'
      }
      return ''
    },
    action_one () {
      return this.baseurl.url + '/mdone/' + this.anho + '/' + this.mes + '/' + this.empresa.empr_id
    },
    one_ok (response, file, list) {
      this.uno_ok = true
      console.log('respons: ' + JSON.stringify(response))
      console.log('file: ' + JSON.stringify(file))
      console.log('list: ' + JSON.stringify(list))
      if (response && file) {
        this.nofile = false
        this.$message({
          message: 'Archivo Lectura subido al servidor',
          type: 'success'
        })
      }
    },
    one_error (err, file) {
      this.uno_ok = false
      if (err && file) {
        this.$message({
          message: 'Error al cargar el archivo RecCar.',
          type: 'warning'
        })
      }
    },
    procesarXlsx () {
      procesar_xlsx(this.anho, this.mes, 1, this.sessionUUID).then(response => {
        this.$notify({
          title: 'En Proceso',
          message: 'El archivo está siendo procesados, esto puede tomar algunos minútos.',
          type: 'info'
        })
      })
    },
    getCargas () {
      getTodos(this.page_size, this.current_page, this.filter, this.order_by, 1).then(response => {
        this.total = response.data.total
        this.tableData = response.data.data
        this.handleSse()
      })
    }
  },
  mounted () {
    this.getCargas()
  },
  beforeDestroy () {
    msgServer.close()
  }
}
</script>
<style>
  .el-table .tachado-row {
    text-decoration: line-through red solid;
  }

  .el-progress-bar .el-progress-bar__outer {
    background-color: #8a834a;
  }
</style>
