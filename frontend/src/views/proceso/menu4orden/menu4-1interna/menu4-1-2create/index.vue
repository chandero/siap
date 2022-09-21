<template>
  <el-container>
      <el-header>
          <span>{{ $t('route.ordentrabajocreate') }}</span>
      </el-header>
      <el-main>
          <el-form :label-position="labelPosition">
              <el-collapse v-model="activePages" @change="handleActivePagesChange">
                <el-collapse-item name="1" :title="$t('ordentrabajo.general')">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item :label="$t('ordentrabajo.consecutive')">
                                <el-input readonly v-model="ordentrabajo.ortr_id" ref="consecutive" @keyup.enter.native="changeFocus('date')"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item :label="$t('ordentrabajo.date')">
                                <el-date-picker ref="date" v-model="ordentrabajo.ortr_fecha" @change="changeFocus('nombre')"></el-date-picker>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item :label="$t('ordentrabajo.crew')">
                             <el-select filterable clearable ref="crew" v-model="ordentrabajo.cuad_id" name="crew" :placeholder="$t('cuadrilla.select')"  style="width:250px;" @change="validarCuadrilla(ordentrabajo.cuad_id)">
                              <el-option v-for="cuadrilla in cuadrillas" :key="cuadrilla.cuad_id" :label="cuadrilla.cuad_descripcion" :value="cuadrilla.cuad_id" >
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item :label="$t('ordentrabajo.type')">
                             <el-select filterable clearable ref="type" v-model="ordentrabajo.tiba_id" name="crew" :placeholder="$t('tipobarrio.select')"  style="width:250px;" @change="changeFocus('report')">
                              <el-option v-for="tipobarrio in tiposbarrio" :key="tipobarrio.tiba_id" :label="tipobarrio.tiba_descripcion" :value="tipobarrio.tiba_id" >
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-collapse-item>
                <el-collapse-item name="2" :title="$t('ordentrabajo.reports')">
                        <el-row :gutter="4" class="hidden-sm-and-down">
                          <el-col :md="1" :lg="1" :xl="1">
                            <span style="font-weight: bold;">No.</span>
                          </el-col>
                          <el-col :md="6" :lg="6" :xl="6">
                            <span style="font-weight: bold;">Tipo de Reporte</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                            <span style="font-weight: bold;">Número de Reporte</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="11" :lg="11" :xl="11">
                            <span style="font-weight: bold;">Descripción del Reporte</span>
                          </el-col>
                        </el-row>
                        <div v-for="(evento, id, index) in ordentrabajo.reportes" v-bind:key="id">
                          <el-form :model="evento" ref="reporteform">
                          <el-row :gutter="4">
                            <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                              <span style="font-weight: bold;">No.</span>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{ id + 1 }}</el-col>
                            <el-col class="hidden-md-and-up" :xs="9" :sm="9">
                              <span style="font-weight: bold;">Tipo de Reporte</span>
                            </el-col>
                            <el-col :xs="13" :sm="13" :md="6" :lg="6" :xl="6">
                                <el-form-item>
                                  <el-select filterable clearable ref="type" v-model="evento.reti_id" name="reti" :placeholder="$t('reporte.tipo.select')"  style="width:250px;" @change="changeFocus('report')">
                                    <el-option v-for="reti in tipos" :key="reti.reti_id" :label="reti.reti_descripcion" :value="reti.reti_id" >
                                    </el-option>
                                  </el-select>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                              <span style="font-weight: bold;">Número de Reporte</span>
                            </el-col>
                            <el-col :xs="13" :sm="13" :md="5" :lg="5" :xl="5">
                                <el-form-item>
                                  <div style="display: table;">
                                    <el-input :disabled="evento.even_estado === 2 || evento.even_estado > 7" type="number" class="sinpadding" style="display: table-cell;" v-model="evento.repo_consecutivo" @input="evento.repo_consecutivo = parseInt($event,10)" @blur="validateRepoEvento(evento, id)">
                                    </el-input>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Descripción de Reporte</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="11" :lg="11" :xl="11">
                                <el-form-item>
                                    <el-input readonly :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.repo_descripcion"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                              <el-button v-if="evento.even_estado < 8" size="mini" type="danger" circle icon="el-icon-minus" title="Quitar Fila" @click="handleDelete(index, evento)"></el-button>
                            </el-col>
                         </el-row>
                         </el-form>
                         <el-row class="hidden-md-and-up">
                          <el-col style="border-bottom: 1px dotted #000;"></el-col>
                         </el-row>
                        </div>
                         <el-row>
                           <el-col :span="24">
                             <el-button  style="display: table-cell;" type="info" size="mini" circle icon="el-icon-plus" title="Adicionar Nueva Fila" @click="onAddReport()" />
                           </el-col>
                         </el-row>
                </el-collapse-item>
                <el-collapse-item name="3" :title="$t('ordentrabajo.obras')">
                        <el-row :gutter="4" class="hidden-sm-and-down">
                          <el-col :md="1" :lg="1" :xl="1">
                            <span style="font-weight: bold;">No.</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                            <span style="font-weight: bold;">Número de Obra</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="11" :lg="11" :xl="11">
                            <span style="font-weight: bold;">Nombre de la Obra</span>
                          </el-col>
                        </el-row>
                        <div v-for="(evento, id) in ordentrabajo.obras" v-bind:key="id">
                          <el-form :model="evento" ref="obraform">
                          <el-row :gutter="4">
                            <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                              <span style="font-weight: bold;">No.</span>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{ evento.even_id }}</el-col>
                            <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                              <span style="font-weight: bold;">Número de Obra</span>
                            </el-col>
                            <el-col :xs="13" :sm="13" :md="5" :lg="5" :xl="5">
                                <el-form-item>
                                  <div style="display: table;">
                                    <el-input :disabled="evento.even_estado === 2 || evento.even_estado > 7" type="number" class="sinpadding" style="display: table-cell;" v-model="evento.obra_consecutivo" @input="evento.obra_consecutivo = parseInt($event,10)"  @blur="validateObraEvento(evento, id)">
                                    </el-input>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Nombre de la Obra</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="17" :lg="17" :xl="17">
                                <el-form-item>
                                    <el-input readonly :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.obra_nombre"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                              <el-button v-if="evento.even_estado < 8" size="mini" type="danger" circle icon="el-icon-minus" title="Quitar Fila" @click="evento.even_estado === 1? evento.even_estado = 8 : evento.even_estado = 9"></el-button>
                              <el-button v-if="evento.even_estado > 7" size="mini" type="success" circle icon="el-icon-success" title="Restaurar Fila" @click="evento.even_estado === 9? evento.even_estado = 2 : evento.even_estado = 1"></el-button>
                            </el-col>
                         </el-row>
                         </el-form>
                         <el-row class="hidden-md-and-up">
                          <el-col style="border-bottom: 1px dotted #000;"></el-col>
                         </el-row>
                        </div>
                         <el-row>
                           <el-col :span="24">
                             <el-button  style="display: table-cell;" type="info" size="mini" circle icon="el-icon-plus" title="Adicionar Nueva Fila" @click="onAddObra()" />
                           </el-col>
                         </el-row>
                </el-collapse-item>
                <el-collapse-item name="4" :title="$t('ordentrabajo.novedades')">
                        <el-row :gutter="4" class="hidden-sm-and-down">
                          <el-col :md="1" :lg="1" :xl="1">
                            <span style="font-weight: bold;">No.</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                            <span style="font-weight: bold;">Novedad</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="3" :lg="3" :xl="3">
                            <span style="font-weight: bold;">Hora Inicio</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="3" :lg="3" :xl="3">
                            <span style="font-weight: bold;">Hora Terminación</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
                            <span style="font-weight: bold;">Observación</span>
                          </el-col>
                        </el-row>
                        <div v-for="(evento, id) in ordentrabajo.novedades" v-bind:key="id">
                          <el-form :model="evento" ref="novedadform">
                          <el-row :gutter="4">
                            <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                              <span style="font-weight: bold;">No.</span>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{ evento.even_id }}</el-col>
                            <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                              <span style="font-weight: bold;">Novedad</span>
                            </el-col>
                            <el-col :xs="13" :sm="13" :md="6" :lg="6" :xl="6">
                                <el-form-item>
                                  <el-select :disabled="evento.even_estado > 7" filterable clearable ref="type" v-model="evento.nove_id" name="nove" :placeholder="$t('ordentrabajo.novedad.select')"  style="width:95%;">
                                    <el-option v-for="nove in novedades" :key="nove.nove_id" :label="nove.nove_descripcion" :value="nove.nove_id" >
                                    </el-option>
                                  </el-select>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                              <span style="font-weight: bold;">Hora Inicio</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="3" :lg="3" :xl="3">
                              <el-form-item
                                prop="ortrno_horaini"
                              >
                                <el-time-select
                                  :disabled="evento.even_estado > 7"
                                  v-model="evento.ortrno_horaini"
                                  style="width:90%"
                                  :picker-options="{
                                    start: '07:00',
                                    step: '00:15',
                                    end: '19:00',
                                  }"
                                />
                            </el-form-item>
                          </el-col>
                          <el-col class="hidden-md-and-up" :xs="5" :sm="5">
                            <span style="font-weight: bold;">Hora Terminacion</span>
                          </el-col>
                          <el-col :xs="16" :sm="16" :md="3" :lg="3" :xl="3">
                              <el-form-item
                              prop="ortrno_horafin"
                              >
                              <el-time-select
                                :disabled="evento.even_estado > 7"
                                v-model="evento.ortrno_horafin"
                                style="width:90%"
                                :picker-options="{
                                  start: '07:00',
                                  step: '00:15',
                                  end: '23:45',
                                  minTime: evento.ortrno_horaini
                                }"
                              />
                            </el-form-item>
                          </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Observaciön</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="9" :lg="9" :xl="9">
                                <el-form-item>
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.ortrno_observacion"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                              <el-button v-if="evento.even_estado < 8" size="mini" type="danger" circle icon="el-icon-minus" title="Quitar Fila" @click="evento.even_estado === 1? evento.even_estado = 8 : evento.even_estado = 9"></el-button>
                              <el-button v-if="evento.even_estado > 7" size="mini" type="success" circle icon="el-icon-success" title="Restaurar Fila" @click="evento.even_estado === 9? evento.even_estado = 2 : evento.even_estado = 1"></el-button>
                            </el-col>
                         </el-row>
                         </el-form>
                         <el-row class="hidden-md-and-up">
                          <el-col style="border-bottom: 1px dotted #000;"></el-col>
                         </el-row>
                        </div>
                         <el-row>
                           <el-col :span="24">
                             <el-button  style="display: table-cell;" type="info" size="mini" circle icon="el-icon-plus" title="Adicionar Nueva Fila" @click="onAddNovedad()" />
                           </el-col>
                         </el-row>
                </el-collapse-item>
              </el-collapse>
          </el-form>
      </el-main>
     <el-footer>
      <el-button v-if="canSave" ref="submit" :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
      <el-button v-if="canPrint" ref="print" size="medium" type="success" icon="el-icon-printer" @click="imprimir"></el-button>
     </el-footer>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getCuadrillas } from '@/api/cuadrilla'
import { saveOrden, printOrden, getOrdenByCuadrillaFecha } from '@/api/ordentrabajo'
import { getReporte, getTipos, getReportePorConsecutivo } from '@/api/reporte'
import { getObra, getObraPorConsecutivo } from '@/api/obra'
import { getNovedades } from '@/api/novedad'

export default {
  data () {
    return {
      labelPosition: 'top',
      activePages: ['1', '2', '3', '4'],
      canSave: true,
      canPrint: false,
      ordentrabajo: {
        ortr_id: null,
        ortr_fecha: new Date(),
        ortr_observacion: '',
        ortr_consecutivo: null,
        otes_id: 1,
        cuad_id: null,
        tiba_id: null,
        usua_id: 0,
        empr_id: 0,
        reportes: [],
        obras: [],
        novedades: []
      },
      reporte: {
        repo_id: null,
        repo_consecutivo: 0,
        repo_numero: null,
        repo_fecharecepcion: null,
        repo_direccion: null,
        repo_nombre: null,
        repo_telefono: null,
        repo_fechasolucion: null,
        repo_horainicio: null,
        repo_horafin: null,
        repo_reportetecnico: null,
        repo_descripcion: null,
        rees_id: 1,
        rees_descripcion: null,
        acti_id: null,
        acti_descripcion: null,
        orig_id: null,
        orig_descripcion: null,
        barr_id: null,
        barr_descripcion: null,
        empr_id: 0,
        usua_id: 0,
        es_valido: false
      },
      obra: {
        obra_id: null,
        obra_consecutivo: null,
        obra_nombre: null,
        even_id: null,
        even_estado: null,
        es_valido: false
      },
      novedad: {
        nove_id: null,
        ortrno_horaini: null,
        ortrno_horafin: null,
        ortrno_observacion: null,
        even_id: null,
        even_estado: null
      },
      tipos: [],
      tiposbarrio: [],
      cuadrillas: [],
      novedades: [],
      reporte_siguiente_consecutivo: 0,
      obra_siguiente_consecutivo: 0,
      novedad_siguiente_consecutivo: 0,
      isNotSaving: true,
      noValid: false
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario',
      'tipo_inventario'
    ])
  },
  methods: {
    changeFocus (next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange (val) {
    },
    onAddReport () {
      this.reporte_siguiente_consecutivo = this.reporte_siguiente_consecutivo + 1
      var reporte = {
        even_id: this.reporte_siguiente_consecutivo,
        even_estado: 1,
        repo_id: null,
        reti_id: null,
        reti_descripcion: null,
        repo_consecutivo: null,
        acti_id: null,
        acti_descripcion: null,
        repo_descripcion: null,
        tireuc_id: 1
      }
      this.ordentrabajo.reportes.push(reporte)
    },
    onAddObra () {
      this.obra_siguiente_consecutivo = this.obra_siguiente_consecutivo + 1
      var obra = {
        obra_id: null,
        obra_consecutivo: null,
        obra_nombre: null,
        even_id: this.obra_siguiente_consecutivo,
        even_estado: 1
      }
      this.ordentrabajo.obras.push(obra)
    },
    onAddNovedad () {
      this.novedad_siguiente_consecutivo = this.novedad_siguiente_consecutivo + 1
      var novedad = {
        nove_id: null,
        ortrno_horaini: null,
        ortrno_horafin: null,
        ortrno_observacion: null,
        even_id: this.novedad_siguiente_consecutivo,
        even_estado: 1
      }
      this.ordentrabajo.novedades.push(novedad)
    },
    validateRepoEvento (evento, id) {
      if (evento !== undefined && evento !== null) {
        if (evento.reti_id !== undefined && evento.reti_id !== null && evento.repo_consecutivo !== undefined && evento.repo_consecutivo !== null) {
          getReportePorConsecutivo(evento.reti_id, evento.repo_consecutivo).then(response => {
            if (response.status === 200) {
              evento.repo_descripcion = response.data.repo_descripcion
              evento.repo_id = response.data.repo_id
              evento.es_valido = true
            } else {
              evento.es_valido = false
              this.reportdoesnotexist()
            }
          }).catch(error => {
            evento.es_valido = false
            this.reportdoesnotexist(error)
          })
        }
      }
    },
    validateObraEvento (evento, id) {
      if (evento !== undefined && evento !== null) {
        if (evento.obra_consecutivo !== undefined && evento.obra_consecutivo !== null) {
          getObraPorConsecutivo(evento.obra_consecutivo).then(response => {
            if (response.status === 200) {
              evento.obra_nombre = response.data.obra_nombre
              evento.obra_id = response.data.obra_id
            } else {
              this.obradoesnotexist()
            }
          }).catch(error => {
            this.obradoesnotexist(error)
          })
        }
      }
    },
    validate () {
      if (!this.noValid && this.isNotSaving && this.ordentrabajo.ortr_fecha && this.ordentrabajo.cuad_id && this.ordentrabajo.tiba_id && this.ordentrabajo.reportes.length > 0) {
        return true
      } else {
        return false
      }
    },
    validarCuadrilla(cuad_id) {
      getOrdenByCuadrillaFecha(cuad_id, this.ordentrabajo.ortr_fecha.getTime()).then(response => {
        const orden = response.data
        if (orden) {
          this.$message('Ya existe una orden para la cuadrilla con esta fecha')
          this.noValid = true
        } else {
          this.noValid = false
        }
      }).catch(e => {
        this.noValid = false
        console.log('Error buscando orden: ', e)
      })
    },
    buscarReporte () {
      if (this.reporte.repo_id) {
        getReporte(this.reporte.repo_id).then(response => {
          this.reporte = response.data
        }).catch(error => {
          console.log('Error buscando reporte :' + error)
        })
      }
    },
    buscarObra () {
      if (this.obra.obra_id) {
        getObra(this.obra.obra_id).then(response => {
          this.obra = response.data
        }).catch(error => {
          console.log('Error buscando obra :' + error)
        })
      }
    },
    handleDelete (index, row) {
      this.ordentrabajo.reportes.splice(index, 1)
      this.$refs.report.focus()
    },
    limpiar() {
      this.ordentrabajo = {
        ortr_id: null,
        ortr_fecha: new Date(),
        ortr_observacion: '',
        ortr_consecutivo: null,
        otes_id: 1,
        cuad_id: null,
        tiba_id: null,
        usua_id: 0,
        empr_id: 0,
        reportes: [],
        obras: [],
        novedades: []
      }
    },
    aplicar () {
      this.isNotSaving = false
      saveOrden(this.ordentrabajo)
        .then(response => {
          this.isNotSaving = true
          if (response.status === 201) {
            this.ordentrabajo.ortr_id = response.data
            this.success()
          }
        })
        .catch(error => {
          this.isNotSaving = true
          this.error(error)
        })
    },
    success () {
      this.limpiar()
      this.$notify({
        title: this.$i18n.t('ordentrabajo.success'),
        message:
          this.$i18n.t('ordentrabajo.created') +
          ' ' +
          this.ordentrabajo.ortr_id,
        type: 'success',
        onClose: this.continue()
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('ordentrabajo.error'),
        message: this.$i18n.t('ordentrabajo.notcreated') + ' ' + e,
        onClose: this.limpiar()
      })
    },
    reportdoesnotexist (e) {
      this.$notify.error({
        title: 'No Existe',
        message: 'El reporte no existe, por favor verifique'
      })
    },
    obradoesnotexist (e) {
      this.$notify.error({
        title: 'No Existe',
        message: 'La obra no existe, por favor verifique'
      })
    },
    continue () {
      this.canPrint = true
      this.canSave = false
    },
    imprimir () {
      printOrden(this.ordentrabajo.ortr_id, this.$store.state.empresa.empresa.empr_id)
    }
  },
  mounted () {
    getTiposBarrio().then(response => {
      this.tiposbarrio = response.data
    }).catch(error => {
      console.log('getTiposBarrio:' + error)
    })
    getCuadrillas().then(response => {
      this.cuadrillas = response.data
    }).catch(error => {
      console.log('getCuadrillas:' + error)
    })
    getTipos().then(response => {
      this.tipos = response.data
    }).catch(error => {
      console.log('getTipos:' + error)
    })
    this.changeFocus('date')
    getNovedades(3).then(response => {
      this.novedades = response.data
    }).catch(error => {
      console.log('getNovedades:' + error)
    })
    for (var i = 0; i < 5; i++) {
      this.onAddReport()
      this.onAddObra()
    }
  }
}
</script>
