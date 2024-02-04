<template>
  <el-container>
    <el-main>
      <el-tabs v-model="activeName" type="border-card">
        <el-tab-pane label="Por Potencia" name="uno">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataPotencia" stripe :default-sort="{ prop: 'label', order: 'ascending' }"
                style="width: 100%" max-height="600" border>
                <el-table-column v-for="(item, index) in labels" :key="index" :label=item.toString() width="100">
                  <template slot-scope="scope">
                    <div style="display: inline;">
                      <span style="margin-left: 10px">{{ scope.row.value[index] }}</span>
                      <img :title="$t('ver')" @click="verDatos(labels[index])"
                        style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_luminaria | toThousandslsFilter }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <bar-chart :chart-data="potenciaData" :options="potenciaOptions"></bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Potencia y Tecnología" name="dos">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataPotenciaTecnologia" stripe :default-sort="{ prop: 'label', order: 'ascending' }"
                style="width: 100%" max-height="600" border>
                <el-table-column v-for="(item, index) in labels" :key="index" :label=item.toString() width="150">
                  <el-table-column v-for="(tecno, index2) in tecnologias" :key="index2" :label=tecno width="70">
                    <template slot-scope="scope">
                      <div style="display: inline;">
                        <span style="margin-left: 10px">{{ scope.row[(index * 3) + index2].value }}</span>
                        <img :title="$t('ver')" @click="verDatosPotenciaTecnologia(tecno, item)"
                          style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                      </div>
                    </template>
                  </el-table-column>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_potenciaytecnologia | toThousandslsFilter }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <bar-chart :chart-data="pottecData" :options="potenciaOptions"></bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Potencia y Medida" name="tres">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataPotenciaMedida" stripe :default-sort="{ prop: 'label', order: 'ascending' }"
                style="width: 100%" max-height="600" border>
                <el-table-column v-for="(item, index) in labels" :key="index" :label=item.toString() width="150">
                  <el-table-column v-for="(medida, index2) in medidas" :key="index2" :label=medida width="70">
                    <template slot-scope="scope">
                      <div style="display: inline;">
                        <span style="margin-left: 10px">{{ scope.row[(index * 3) + index2].value }}</span>
                        <img :title="$t('ver')" @click="verDatosPotenciaMedida(medida, item)"
                          style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                      </div>
                    </template>
                  </el-table-column>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_potenciaymedida | toThousandslsFilter  }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <bar-chart :chart-data="potmedData" :options="potenciaOptions"></bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Sector" name="cuatro">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataSector" stripe :default-sort="{ prop: 'label', order: 'ascending' }" style="width: 100%"
                max-height="600" border>
                <el-table-column v-for="(item, index) in sectores" :key="index" :label=item.toString() width="100">
                  <template slot-scope="scope">
                    <div style="display: inline;">
                      <span style="margin-left: 10px">{{ scope.row[index] }}</span>
                      <img :title="$t('ver')" @click="verDatosSector(item)"
                        style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_porsector | toThousandslsFilter }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <bar-chart :chart-data="sectorData" :options="sectorOptions"></bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Uso" name="cinco">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataUso" stripe :default-sort="{ prop: 'label', order: 'ascending' }" style="width: 100%"
                max-height="600" border>
                <el-table-column v-for="(item, index) in usos" :key="index" :label=item.toString() width="100">
                  <template slot-scope="scope">
                    <div style="display: inline;">
                      <span style="margin-left: 10px">{{ scope.row[index] }}</span>
                      <img :title="$t('ver')" @click="verDatosUso(item)"
                        style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_poruso | toThousandslsFilter }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <bar-chart :chart-data="usoData" :options="usoOptions"></bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Tecnología" name="seis">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataTecnologia" stripe :default-sort="{ prop: 'label', order: 'ascending' }"
                style="width: 100%" max-height="600" border>
                <el-table-column v-for="(item, index) in tecnologias" :key="index" :label=item.toString() width="100">
                  <template slot-scope="scope">
                    <div style="display: inline;">
                      <span style="margin-left: 10px">{{ scope.row[index] }}</span>
                      <img :title="$t('ver')" @click="verDatosTecnologia(item)"
                        style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_portecnologia | toThousandslsFilter }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <bar-chart :chart-data="tecnologiaData" :options="tecnologiaOptions"></bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Medida" name="siete">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataMedida" stripe :default-sort="{ prop: 'label', order: 'ascending' }" style="width: 100%"
                max-height="600" border>
                <el-table-column v-for="(item, index) in medidas" :key="index" :label=item.toString() width="100">
                  <template slot-scope="scope">
                    <div style="display: inline;">
                      <span style="margin-left: 10px">{{ scope.row[index] }}</span>
                      <img :title="$t('ver')" @click="verDatosMedida(item)"
                        style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_pormedida | toThousandslsFilter }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <bar-chart :chart-data="medidaData" :options="medidaOptions"></bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Barrio" name="ocho">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataBarrio" stripe style="width: 100%" max-height="600" border>
                <el-table-column v-for="(item, index) in barrios" :key="index" :label=item.toString() width="140">
                  <template slot-scope="scope">
                    <div style="display: inline;">
                      <span style="margin-left: 10px">{{ scope.row[index] }}</span>
                      <img :title="$t('ver')" @click="verDatosBarrio(item)"
                        style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_porbarrio | toThousandslsFilter }}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <horizontal-bar-chart :chart-data="barrioData" :options="barrioOptions"></horizontal-bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Por Vereda" name="nueve">
          <el-row>
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-table :data="dataVereda" stripe style="width: 100%" max-height="600" border>
                <el-table-column v-for="(item, index) in veredas" :key="index" :label=item.toString() width="140">
                  <template slot-scope="scope">
                    <div style="display: inline;">
                      <span style="margin-left: 10px">{{ scope.row[index] }}</span>
                      <img :title="$t('ver')" @click="verDatosVereda(item)"
                        style="width:16px; height: 16px; cursor: pointer;" :src="require('@/assets/ojo.png')" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Total Luminarias" width="130">
                  {{ total_porvereda | toThousandslsFilter}}
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
          <el-row style="height: auto;">
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <div>
                <horizontal-bar-chart :chart-data="veredaData" :options="veredaOptions"></horizontal-bar-chart>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
      <el-dialog title="Información de Luminarias" :visible.sync="centerDialogVisible" width="90%" center>
        <span>Filtro: {{ titulo }}</span>
        <el-table :data="aaps" stripe :default-sort="{ prop: 'aap_id', order: 'ascending' }" style="width: 100%"
          max-height="500" border>
          <el-table-column :label="$t('gestion.code')" width="150" prop="aap_id" resizable>
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.aap_id }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.support')" width="150" prop="aap_apoyo" resizable>
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.aap_apoyo }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.address')" min-width="250" prop="aap_direccion" resizable>
            <template slot-scope="scope">
              <span>{{ scope.row.aap_direccion }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.neighborhood')" min-width="150" prop="barr_descripcion" resizable>
            <template slot-scope="scope">
              <span>{{ scope.row.barr_descripcion }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.vereda')" width="120" prop="vereda">
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.vereda }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.neighborhoodtype')" width="100" prop="tiba_descripcion">
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.tiba_descripcion }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.connection.title')" width="120" prop="aaco_descripcion" resizable>
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.aaco_descripcion }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.tecnology.title')" width="100" prop="aap_tecnologia" resizable>
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.aap_tecnologia }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.power.title')" width="80" prop="aap_potencia" resizable>
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.aap_potencia }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('gestion.use')" width="150" prop="aaus_descripcion" resizable>
            <template slot-scope="scope">
              <span style="margin-left: 10px">{{ scope.row.aaus_descripcion }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div slot="footer" class="dialog-footer">
          <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;"
            :src="require('@/assets/xls.png')" />
          <el-button style="float: right;" type="primary"
            @click="aaps = [], centerDialogVisible = false">Cerrar</el-button>
        </div>
      </el-dialog>
    </el-main>
  </el-container>
</template>
<script>
import BarChart from '@/chart/bar'
import HorizontalBarChart from '@/chart/hbar'

import {
  siap_lista_potencias, siap_lista_tecnologias, siap_lista_medidas, siap_lista_sectores, siap_lista_usos, siap_lista_barrios, siap_lista_veredas,
  siap_grafica_reporte_potencia, siap_lista_reporte_potencia,
  siap_grafica_reporte_potencia_tecnologia, siap_lista_reporte_potencia_tecnologia, siap_grafica_reporte_potencia_medida,
  siap_lista_reporte_potencia_medida, siap_grafica_reporte_sector, siap_lista_reporte_sector, siap_grafica_reporte_uso, siap_lista_reporte_uso,
  siap_grafica_reporte_tecnologia, siap_lista_reporte_tecnologia, siap_grafica_reporte_medida, siap_lista_reporte_medida,
  siap_grafica_reporte_barrio, siap_lista_reporte_barrio, siap_grafica_reporte_vereda
} from '@/api/grafica'
import { getColor } from '@/utils/color'
import { parseTime } from '@/utils'

export default {
  components: {
    BarChart,
    HorizontalBarChart
  },
  data() {
    return {
      centerDialogVisible: false,
      activeName: 'uno',
      labels: [],
      tecnologias: [],
      medidas: [],
      sectores: [],
      usos: [],
      barrios: [],
      veredas: [],
      dataPotencia: [],
      dataPotenciaTecnologia: [],
      dataPotenciaMedida: [],
      dataSector: [],
      dataUso: [],
      dataTecnologia: [],
      dataMedida: [],
      dataBarrio: [],
      dataVereda: [],
      total_luminaria: 0,
      total_potenciaytecnologia: 0,
      total_potenciaymedida: 0,
      total_porsector: 0,
      total_poruso: 0,
      total_portecnologia: 0,
      total_pormedida: 0,
      total_porbarrio: 0,
      total_porvereda: 0,
      aaps: [],
      titulo: '',
      potenciaData: null,
      potenciaOptions: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        },
        tooltips: {
          mode: 'nearest'
        }
      },
      pottecData: {
        labels: null,
        datasets: null
      },
      potmedData: {
        labels: null,
        datasets: null
      },
      sectorData: null,
      sectorOptions: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        },
        tooltips: {
          mode: 'nearest'
        }
      },
      usoData: null,
      usoOptions: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        },
        tooltips: {
          mode: 'nearest'
        }
      },
      tecnologiaData: null,
      tecnologiaOptions: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        },
        tooltips: {
          mode: 'nearest'
        }
      },
      medidaData: null,
      medidaOptions: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        },
        tooltips: {
          mode: 'nearest'
        }
      },
      barrioData: null,
      barrioOptions: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        },
        tooltips: {
          mode: 'nearest'
        }
      },
      veredaData: null,
      veredaOptions: {
        scales: {
          yAxes: [{
            ticks: {
              beginAtZero: true
            }
          }]
        },
        tooltips: {
          mode: 'nearest'
        }
      }
    }
  },
  mounted() {
    this.getDataPotencia()
    this.getDataPotenciaTecnologia()
    this.getDataPotenciaMedida()
    this.getDataSector()
    this.getDataUso()
    this.getDataTecnologia()
    this.getDataMedida()
    this.getDataBarrio()
    this.getDataVereda()
  },
  methods: {
    verDatos(aap_potencia) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_potencia(aap_potencia).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'POTENCIA_' + aap_potencia + '_w'
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosPotenciaTecnologia(aap_tecnologia, aap_potencia) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_potencia_tecnologia(aap_tecnologia, aap_potencia).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'POTENCIA_' + aap_potencia + '_w_TECNOLOGIA_' + aap_tecnologia
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosPotenciaMedida(aaco_descripcion, aap_potencia) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_potencia_medida(aaco_descripcion, aap_potencia).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'POTENCIA_' + aap_potencia + '_w_MEDIDA_' + aaco_descripcion
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosSector(tiba_descripcion) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_sector(tiba_descripcion).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'SECTOR_' + tiba_descripcion
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosUso(aaus_descripcion) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_uso(aaus_descripcion).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'USO_' + aaus_descripcion
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosTecnologia(aap_tecnologia) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_tecnologia(aap_tecnologia).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'TECNOLOGIA_' + aap_tecnologia
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosMedida(aaco_descripcion) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_medida(aaco_descripcion).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'MEDIDA_' + aaco_descripcion
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosBarrio(barr_descripcion) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_barrio(barr_descripcion).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'BARRIO_' + barr_descripcion
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    verDatosVereda(barr_descripcion) {
      const loading = this.$loading({
        text: 'Cargando Datos...'
      })
      siap_lista_reporte_barrio(barr_descripcion).then(response => {
        loading.close()
        this.aaps = response.data
        this.centerDialogVisible = true
        this.titulo = 'VEREDA_' + barr_descripcion
      }).catch(error => {
        loading.close()
        console.log('Error leyendo datos: ' + error)
      })
    },
    exportarXls() {
      this.downloadLoading = true
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['Código', 'Apoyo', 'Dirección', 'Barrio', 'Es Vereda', 'Sector', 'Tipo Medida', 'Tecnología', 'Potencia', 'Uso']
        const filterVal = ['aap_id', 'aap_apoyo', 'aap_direccion', 'barr_descripcion', 'vereda', 'tiba_descripcion', 'aaco_descripcion', 'aap_tecnologia', 'aap_potencia', 'aaus_descripcion']
        const list = this.aaps
        const data = this.formatJson(filterVal, list)
        excel.export_json_to_excel(tHeader, data, 'informe_luminarias_filtrado_por_' + this.titulo + '_a_fecha_' + this.$moment(new Date()).format('YYYYMMDD'))
        this.downloadLoading = false
      })
    },
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => {
        if (j === 'timestamp') {
          return parseTime(v[j])
        } else {
          return v[j]
        }
      }))
    },
    getDataPotencia() {
      siap_grafica_reporte_potencia().then(response => {
        const data = response.data
        var _vvalues = []
        var _vvaluest = []
        var _vlabels = []
        var _vcolor = []
        data.forEach(item => {
          _vlabels.push(item.label)
          _vvalues.push(item.value)
          this.total_luminaria = this.total_luminaria + item.value
          _vvaluest.push(item.value)
          const color = getColor()
          _vcolor.push(color)
        })
        this.potenciaData = {
          labels: _vlabels,
          datasets: [{
            label: 'Luminarias',
            data: _vvalues,
            backgroundColor: _vcolor,
            borderWidth: 1
          }]
        }
        this.dataPotencia.push({ value: _vvaluest })
      }).catch(error => {
        console.log('getPotencia =>' + error)
      })
    },
    getDataPotenciaTecnologia() {
      siap_lista_potencias().then(response => {
        const data = response.data
        var labels = data
        var datasets = []
        var values = []
        siap_grafica_reporte_potencia_tecnologia().then(response => {
          const data = response.data
          for (var i = 0; i < this.labels.length; i++) {
            this.tecnologias.forEach(tecno => {
              values.push({ value: data[tecno][i] })
              this.total_potenciaytecnologia = this.total_potenciaytecnologia + data[tecno][i]
            })
          }
          this.dataPotenciaTecnologia.push(values)
          Object.keys(data).map((key, index) => {
            const color = getColor()
            datasets.push({
              label: key,
              data: data[key],
              backgroundColor: color,
              borderWidth: 5
            })
          })
          this.pottecData = {
            labels: labels,
            datasets: datasets
          }
        })
      }).catch(error => {
        console.log('get Potencias: ' + error)
      })
    },
    getDataPotenciaMedida() {
      siap_lista_potencias().then(response => {
        const data = response.data
        var labels = data
        var datasets = []
        var values = []
        siap_grafica_reporte_potencia_medida().then(response => {
          const data = response.data
          for (var i = 0; i < this.labels.length; i++) {
            this.medidas.forEach(medida => {
              values.push({ value: data[medida][i] })
              this.total_potenciaymedida = this.total_potenciaymedida + data[medida][i]
            })
          }
          this.dataPotenciaMedida.push(values)
          Object.keys(data).map((key, index) => {
            const color = getColor()
            datasets.push({
              label: key,
              data: data[key],
              backgroundColor: color,
              borderWidth: 5
            })
          })
          this.potmedData = {
            labels: labels,
            datasets: datasets
          }
        })
      }).catch(error => {
        console.log('get Medida: ' + error)
      })
    },
    getDataSector() {
      siap_grafica_reporte_sector().then(response => {
        const data = response.data
        var _vlabels = []
        var _vvalues = []
        var _vcolor = []
        var values = []
        data.forEach(item => {
          _vlabels.push(item.label)
          _vvalues.push(item.value)
          values.push(item.value)
          const color = getColor()
          _vcolor.push(color)
          this.total_porsector = this.total_porsector + (item.value)
        })
        this.dataSector.push(values)
        this.sectorData = {
          labels: _vlabels,
          datasets: [{
            label: 'Luminarias',
            data: _vvalues,
            backgroundColor: _vcolor,
            borderWidth: 1
          }]
        }
      }).catch(error => {
        console.log('getSector =>' + error)
      })
    },
    getDataUso() {
      siap_grafica_reporte_uso().then(response => {
        const data = response.data
        var _vlabels = []
        var _vvalues = []
        var _vcolor = []
        var values = []
        data.forEach(item => {
          _vlabels.push(item.label)
          _vvalues.push(item.value)
          values.push(item.value)
          const color = getColor()
          _vcolor.push(color)
          this.total_poruso = this.total_poruso + (item.value)
        })
        this.dataUso.push(values)
        this.usoData = {
          labels: _vlabels,
          datasets: [{
            label: 'Luminarias',
            data: _vvalues,
            backgroundColor: _vcolor,
            borderWidth: 1
          }]
        }
      }).catch(error => {
        console.log('getUso =>' + error)
      })
    },
    getDataTecnologia() {
      siap_grafica_reporte_tecnologia().then(response => {
        const data = response.data
        var _vlabels = []
        var _vvalues = []
        var _vcolor = []
        var values = []
        data.forEach(item => {
          _vlabels.push(item.label)
          _vvalues.push(item.value)
          values.push(item.value)
          const color = getColor()
          _vcolor.push(color)
          this.total_portecnologia = this.total_portecnologia + (item.value)
        })
        this.dataTecnologia.push(values)
        this.tecnologiaData = {
          labels: _vlabels,
          datasets: [{
            label: 'Luminarias',
            data: _vvalues,
            backgroundColor: _vcolor,
            borderWidth: 1
          }]
        }
      }).catch(error => {
        console.log('getTecnologia =>' + error)
      })
    },
    getDataMedida() {
      siap_grafica_reporte_medida().then(response => {
        const data = response.data
        var _vlabels = []
        var _vvalues = []
        var _vcolor = []
        var values = []
        data.forEach(item => {
          _vlabels.push(item.label)
          _vvalues.push(item.value)
          values.push(item.value)
          const color = getColor()
          _vcolor.push(color)
          this.total_pormedida = this.total_pormedida + (item.value)
        })
        this.dataMedida.push(values)
        this.medidaData = {
          labels: _vlabels,
          datasets: [{
            label: 'Luminarias',
            data: _vvalues,
            backgroundColor: _vcolor,
            borderWidth: 1
          }]
        }
      }).catch(error => {
        console.log('getMedida =>' + error)
      })
    },
    getDataBarrio() {
      siap_grafica_reporte_barrio().then(response => {
        const data = response.data
        var _vlabels = []
        var _vvalues = []
        var _vcolor = []
        var values = []
        data.forEach(item => {
          _vlabels.push(item.label)
          _vvalues.push(item.value)
          const color = getColor()
          values.push(item.value)
          _vcolor.push(color)
          this.total_porbarrio = this.total_porbarrio + (item.value)
        })
        this.dataBarrio.push(values)
        this.barrioData = {
          labels: _vlabels,
          datasets: [{
            label: 'Luminarias',
            data: _vvalues,
            backgroundColor: _vcolor,
            borderWidth: 1
          }]
        }
      }).catch(error => {
        console.log('getBarrio =>' + error)
      })
    },
    getDataVereda() {
      siap_grafica_reporte_vereda().then(response => {
        const data = response.data
        var _vlabels = []
        var _vvalues = []
        var _vcolor = []
        var values = []
        data.forEach(item => {
          _vlabels.push(item.label)
          _vvalues.push(item.value)
          const color = getColor()
          values.push(item.value)
          _vcolor.push(color)
          this.total_porvereda = this.total_porvereda + (item.value)
        })
        this.dataVereda.push(values)
        this.veredaData = {
          labels: _vlabels,
          datasets: [{
            label: 'Luminarias',
            data: _vvalues,
            backgroundColor: _vcolor,
            borderWidth: 1
          }]
        }
      }).catch(error => {
        console.log('getVereda =>' + error)
      })
    }
  },
  beforeCreate() {
    siap_lista_potencias().then(response => {
      const data = response.data
      this.labels = data
      siap_lista_tecnologias().then(response => {
        const data2 = response.data
        this.tecnologias = data2
        siap_lista_medidas().then(response => {
          const data3 = response.data
          this.medidas = data3
          siap_lista_sectores().then(response => {
            const data4 = response.data
            this.sectores = data4
            siap_lista_usos().then(response => {
              const data5 = response.data
              this.usos = data5
              siap_lista_barrios().then(response => {
                const data6 = response.data
                this.barrios = data6
                siap_lista_veredas().then(response => {
                  const data7 = response.data
                  this.veredas = data7
                })
              })
            })
          })
        })
      })
    })
  }
}
</script>
<style>
.small {
  max-width: 100%;
  margin: 10px auto;
  height: 400px;
}

.dialog-footer {
  text-align: left !important;
}
</style>
