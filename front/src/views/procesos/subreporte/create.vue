<template>
  <el-container>
      <el-header>
          <span>{{ $t('route.subreportecreate') }}</span>
      </el-header>
      <el-main>
          <span style="font-size: 20px;">{{ $t('reporte.principal')}}</span>
          <el-form ref="principal" :label-position="labelPosition">
              <el-row>
                 <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                   <el-form-item prop="freti_id" :label="$t('reporte.type')">
                     <el-select autofocus clearable :title="$t('reporte.tipo.select')" style="width: 80%" ref="tipo" v-model="freti_id" name="tipo" :placeholder="$t('reporte.tipo.select')">
                        <el-option v-for="tipo in tipos" :key="tipo.reti_id" :label="tipo.reti_descripcion" :value="tipo.reti_id" >
                        </el-option>   
                      </el-select>
                    </el-form-item>   
                  </el-col>
                  <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                    <el-form-item prop="fconsec" :label="$t('reporte.number')">
                      <el-input type="number" style="font-size: 30px;" v-model="fconsec" @input="fconsec = parseInt($event)"></el-input>
                    </el-form-item>                   
                  </el-col>
                  <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                   <el-form-item style="display: inline-block; margin: 0 auto;" label="">
                    <el-button type="warning" icon="el-icon-search" circle @click="buscarReporte" title="Buscar Reporte"></el-button>              
                    <el-button type="danger" icon="el-icon-close" circle @click="freti_id = null; fconsec = null" title="Limpiar"></el-button>
                   </el-form-item>
                  </el-col> 
                </el-row>
                <el-collapse>
                  <el-collapse-item name="0" title="DATOS REPORTE PRINCIPAL">
                    <el-row>
                      <el-col>
                        <span>Dirección:</span><span>{{ reporte_principal.repo_direccion}}</span>
                      </el-col>
                      <el-col>
                        <span>Barrio:</span><span>{{ barrio(reporte_principal.barr_id) }}</span>
                      </el-col>
                    </el-row>
                  </el-collapse-item>
                </el-collapse>      
          </el-form>
          <el-form :disabled="inactivo" ref="reporte" :model="reporte" :rules="rules" :label-position="labelPosition">
              <el-collapse v-model="activePages" @change="handleActivePagesChange">
                <el-collapse-item name="1" :title="$t('reporte.general')">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                            <el-form-item prop="repo_fecharecepcion" :label="$t('reporte.receptiondate')">
                                <el-date-picker  type="datetime" ref="receptiondate" v-model="reporte.repo_fecharecepcion" @change="changeFocus('nombre')"></el-date-picker>
                            </el-form-item>
                        </el-col>                      
                        <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                            <el-form-item prop="reti_id" :label="$t('reporte.type')">
                                <el-select autofocus clearable :title="$t('reporte.tipo.select')" style="width: 80%" ref="tipo" v-model="reporte.reti_id" name="tipo" :placeholder="$t('reporte.tipo.select')"  @change="validarTipo()">
                                    <el-option v-for="tipo in tipos" :key="tipo.reti_id" :label="tipo.reti_descripcion" :value="tipo.reti_id" >
                                    </el-option>   
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col v-if="reporte.reti_id===2" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                          <el-form-item :label="$t('reporte.tipo_expansion.title')">
                            <el-form :model="adicional" :rules="adicional_rules" :label-position="labelPosition">
                              <el-form-item prop="repo_tipo_expansion">
                                <el-select autofocus clearable :title="$t('reporte.tipo_expansion.select')" style="width: 80%" ref="tipo" v-model="adicional.repo_tipo_expansion" name="tipo_expansion" :placeholder="$t('reporte.tipo_expansion.select')">
                                    <el-option v-for="te in tipos_expansion" :key="te.tiex_id" :label="te.tiex_descripcion" :value="te.tiex_id" >
                                    </el-option>   
                                </el-select>
                              </el-form-item>
                            </el-form>
                          </el-form-item>
                        </el-col>                        
                        <el-col :xs="24" :sm="24" :md="3" :lg="3" :xl="3">
                          <el-form-item prop="repo_consecutivo" :label="$t('reporte.number')">
                            <el-input readonly style="font-size: 30px;" v-model="reporte.repo_consecutivo"></el-input>
                          </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                            <el-form-item prop="orig_id" :label="$t('reporte.origin')">
                                <el-select style="width:100%;" clearable ref="origin" v-model="reporte.orig_id" name="origen" :placeholder="$t('origen.select')"  @change="changeFocus('code')">
                                    <el-option v-for="origen in origenes" :key="origen.orig_id" :label="origen.orig_descripcion" :value="origen.orig_id" >
                                    </el-option>   
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col v-if="reporte.orig_id === 6" :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                            <el-form-item prop="repo_email" :label="$t('reporte.email')">
                                <el-input ref="email" v-model="reporte.repo_email" @keyup.enter.native="changeFocus('code')" ></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                            <el-form-item prop="repo_codigo" :label="$t('reporte.code')">
                                <el-input ref="code" type="number" v-model="adicional.repo_codigo" @input="adicional.repo_codigo = $event.toUpperCase()" @blur="buscarAap()"></el-input>
                            </el-form-item>                            
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                            <el-form-item prop="repo_apoyo" :label="$t('reporte.apoyo')">
                                <el-input ref="apoyo" v-model="adicional.repo_apoyo" @input="adicional.repo_apoyo = $event.toUpperCase()" @blur="buscarAapApoyo()"></el-input>
                            </el-form-item>                            
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="repo_nombre" :label="$t('reporte.name')">
                                <el-input ref="nombre" v-model="reporte.repo_nombre" @input="reporte.repo_nombre = $event.toUpperCase()" @keyup.enter.native="changeFocus('direccion')"></el-input>
                            </el-form-item>                            
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="repo_direccion" :label="$t('reporte.address')">
                             <el-input ref="direccion" v-model="reporte.repo_direccion" @input="reporte.repo_direccion = $event.toUpperCase()" @keyup.enter.native="changeFocus('barrio')">
                             </el-input>
                            </el-form-item>
                        </el-col>                        
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="barr_id" :label="$t('reporte.neighborhood')">
                             <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="reporte.barr_id" name="barrio" :placeholder="$t('barrio.select')"  @change="actualizarSector()">
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
                              </el-option>   
                             </el-select>
                            </el-form-item>                        
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                            <el-form-item prop="tiba_id" :label="$t('reporte.sector')">
                             <el-select  style="width:100%;" filterable clearable ref="tiba" v-model="reporte.tiba_id" name="tiba" :placeholder="$t('tipobarrio.select')"  @change="changeFocus('telefono')">
                              <el-option v-for="tiba in tiposbarrio" :key="tiba.tiba_id" :label="tiba.tiba_descripcion" :value="tiba.tiba_id" >
                              </el-option>   
                             </el-select>
                            </el-form-item>                        
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                            <el-form-item prop="repo_telefono" :label="$t('reporte.phone')">
                                <el-input ref="telefono" v-model="reporte.repo_telefono" @keyup.enter.native="changeFocus('descripcion')"></el-input>
                            </el-form-item>
                        </el-col>                        
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="acti_id" :label="$t('reporte.activity')">
                             <el-select style="width:90%;" filterable clearable ref="tiba" v-model="adicional.acti_id" name="actividad" :placeholder="$t('actividad.select')"  @change="changeFocus('descripcion')">
                              <el-option v-for="acti in actividades" :key="acti.acti_id" :label="acti.acti_descripcion" :value="acti.acti_id" >
                              </el-option>   
                             </el-select>
                             <el-popover
                              placement="top"
                              width="300"
                              trigger="click"
                              v-model="dialogonuevodanhovisible">
                                <el-form ref="danho" :rules="danhorules" :model="actividad">
                                  <el-form-item prop="acti_descripcion" label="Descripción del Daño">
                                    <el-input autofocus v-model="actividad.acti_descripcion" @input="actividad.acti_descripcion = $event.toUpperCase()"></el-input>
                                  </el-form-item>
                                  <el-form-item>
                                    <el-button  size="mini" type="primary" icon="el-icon-check" @click="guardarNuevoDanho()"></el-button>
                                    <el-button  size="mini" type="warning" icon="el-icon-close" @click="dialogonuevodanhovisible = false"></el-button>                    
                                  </el-form-item>
                                </el-form>
                                <el-button slot="reference" type="primary" size="mini" circle icon="el-icon-plus" title="Adicionar Nuevo Daño"/>
                             </el-popover>
                            </el-form-item>                        
                        </el-col>                      
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="repo_descripcion" :label="$t('reporte.description')">
                                <el-input ref="descripcion" v-model="reporte.repo_descripcion" type="textarea" :rows="2" @input="reporte.repo_descripcion = $event.toUpperCase()"  @keyup.enter.native="changeFocus('submit')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-collapse-item>
              </el-collapse>
          </el-form>
              <el-row>
                <el-col>
                  <el-button v-if="canSave" ref="submit" size="medium" type="primary" icon="el-icon-check" @click="confirmacionGuardar = true"></el-button>
                  <el-button v-if="canPrint" ref="print" size="medium" type="success" icon="el-icon-printer" title="Imprimir Reporte" @click="imprimir"></el-button>                  
                  <el-button v-if="canPrint" ref="nuevo" size="medium" type="primary" icon="el-icon-plus" @click="nuevoReporte = true">Nuevo Reporte</el-button>                    
                </el-col>
              </el-row>
      </el-main>
      <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
          <span style="font-size:20px;">Seguro de Guardar el Nuevo Reporte?</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="confirmacionGuardar = false">No</el-button>
            <el-button type="primary" @click="aplicar">Sí</el-button>
          </span>
      </el-dialog>
      <el-dialog title="Confirmación" :visible.sync="nuevoReporte">
          <span style="font-size:20px;">Proceder con un Nuevo Reporte?</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="nuevoReporte = false">No</el-button>
            <el-button type="primary" @click="nuevo">Sí</el-button>
          </span>      
      </el-dialog>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getOrigenes } from '@/api/origen'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import { saveReporte, printReporte, getTipos, getReportePorConsecutivo } from '@/api/reporte'
import { getActividades, saveActividad } from '@/api/actividad'
import { getAap, getAapApoyo } from '@/api/aap'

export default {
  data() {
    return {
      labelPosition: 'top',
      guardando: false,
      activePages: ['0', '1', '2', '3', '4'],
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      nuevoReporte: false,
      inactivo: true,
      adicional_rules: {
        repo_tipo_expansion: [
          { required: false, message: 'Debe Seleccionar el Tipo de Expansión', trigger: 'change' }
        ]
      },
      rules: {
        repo_fecharecepcion: [
          { required: true, message: 'Debe diligencia la Fecha de Recepción del Reporte', trigger: 'change' }
        ],
        reti_id: [
          { required: true, message: 'Debe Seleccionar el Tipo de Reporte', trigger: 'change' }
        ],
        orig_id: [
          { required: true, message: 'Debe Seleccionar el Origen del Reporte', trigger: 'change' }
        ],
        acti_id: [
          { required: false, message: 'Debe Seleccionar el Tipo de Daño', trigger: 'change' }
        ],
        repo_nombre: [
          { required: false, message: 'Debe Digitar el Nombre de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        repo_direccion: [
          { required: false, message: 'Debe Digitar la dirección del daño o actividad', trigger: 'blur' }
        ],
        repo_telefono: [
          { required: false, message: 'Debe Digitar el Teléfono de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        barr_id: [
          { required: false, message: 'Debe Seleccionar el Barrio del Daño o Actividad', trigger: 'change' }
        ],
        tiba_id: [
          { required: false, message: 'Debe Seleccionar el Tipo de Sector del Daño o Actividad', trigger: 'blur' }
        ]
      },
      danhorules: {
        acti_descripcion: [
          { required: true, message: 'Debe Diligenciar la Descripción del nuevo Daño', trigger: 'blur' }
        ]
      },
      freti_id: null,
      fconsec: null,
      reporte_principal: {
        repo_id: null,
        reti_id: null,
        repo_consecutivo: null,
        repo_fecharecepcion: null,
        repo_direccion: null,
        repo_nombre: null,
        repo_telefono: null,
        repo_reportetecnico: null,
        repo_descripcion: null,
        rees_id: null,
        orig_id: null,
        barr_id: null,
        tiba_id: null,
        adicional: {}
      },
      reporte: {
        reti_id: 1,
        repo_id: null,
        repo_consecutivo: null,
        tiba_id: null,
        repo_numero: null,
        repo_fecharecepcion: new Date(),
        repo_direccion: null,
        repo_nombre: null,
        repo_telefono: null,
        repo_fechasolucion: null,
        repo_horainicio: null,
        repo_horafin: null,
        repo_reportetecnico: null,
        repo_descripcion: null,
        rees_id: 1,
        orig_id: 1,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        adicional: {
          repo_id: null,
          repo_fechadigitacion: null,
          repo_modificado: null,
          repo_tipo_expansion: null,
          repo_luminaria: null,
          repo_redes: null,
          repo_poste: null,
          repo_email: null,
          acti_id: null,
          repo_codigo: null,
          repo_apoyo: null
        },
        eventos: [],
        direcciones: []
      },
      direccion: {
        repo_id: null,
        aap_id: null,
        even_direccion: null,
        barr_id: null
      },
      adicional: {
        repo_id: null,
        repo_fechadigitacion: null,
        repo_modificado: null,
        repo_tipo_expansion: null,
        repo_luminaria: null,
        repo_redes: null,
        repo_poste: null,
        repo_email: null,
        acti_id: null,
        repo_codigo: null,
        repo_apoyo: null
      },
      aap: null,
      origenes: [],
      barrios: [],
      barrios_lista: [],
      tipos: [],
      tiposbarrio: [],
      actividades: [],
      dialogonuevodanhovisible: false,
      actividad: {
        acti_id: null,
        acti_descripcion: null,
        acti_estado: 1,
        usua_id: 0
      },
      tipos_expansion: [
        {
          tiex_id: 1,
          tiex_descripcion: 'TIPO I',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 2,
          tiex_descripcion: 'TIPO II',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 3,
          tiex_descripcion: 'TIPO III',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 4,
          tiex_descripcion: 'TIPO IV',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        }
      ]
    }
  },
  computed: {
    ...mapGetters([
      'empresa'
    ])
  },
  methods: {
    validarTipo() {
      if (this.reporte.reti_id === 2) {
        this.reporte.orig_id = 5
        this.rules.acti_id[0].required = false
        this.adicional_rules.repo_tipo_expansion[0].required = true
      } else if (this.reporte.reti_id === 1) {
        this.rules.acti_id[0].required = true
        this.adicional_rules.repo_tipo_expansion[0].required = false
      } else {
        this.rules.acti_id[0].required = false
        this.adicional_rules.repo_tipo_expansion[0].required = false
      }
    },
    changeFocus(next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange(val) {
    },
    validate() {
      if (this.reporte.orig_id && this.reporte.repo_fecharecepcion && this.reporte.repo_nombre && this.reporte.repo_direccion && this.reporte.barr_id && this.reporte.repo_telefono && this.reporte.repo_descripcion) {
        return true
      } else {
        return false
      }
    },
    aplicar() {
      if (!this.guardando) {
        this.guardando = true
        this.confirmacionGuardar = false
        this.$refs['reporte'].validate((valid) => {
          if (valid) {
            this.reporte.adicional = this.adicional
            saveReporte(this.reporte)
              .then(response => {
                if (response.status === 201) {
                  this.reporte.repo_id = response.data.id
                  this.reporte.repo_consecutivo = response.data.consec
                  this.guardando = false
                  this.success()
                }
              })
              .catch(error => {
                this.error(error)
                this.guardando = false
              })
          } else {
            this.guardando = false
          }
        })
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('reporte.success'),
        message:
          this.$i18n.t('reporte.created') +
          ' ' +
          this.tiporeporte(this.reporte.reti_id) +
          ' ' +
          this.reporte.repo_consecutivo,
        type: 'success',
        onClose: this.continue()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('reporte.error'),
        message: this.$i18n.t('reporte.notcreated') + ' ' + e
      })
    },
    nuevo() {
      this.nuevoReporte = false
      this.reporte = {
        reti_id: 1,
        repo_id: null,
        repo_consecutivo: null,
        tiba_id: null,
        repo_numero: null,
        repo_fecharecepcion: new Date(),
        repo_direccion: null,
        repo_nombre: null,
        repo_telefono: null,
        repo_fechasolucion: null,
        repo_horainicio: null,
        repo_horafin: null,
        repo_reportetecnico: null,
        repo_descripcion: null,
        rees_id: 1,
        orig_id: 1,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        adicional: {
          repo_id: null,
          repo_fechadigitacion: null,
          repo_modificado: null,
          repo_tipo_expansion: null,
          repo_luminaria: null,
          repo_redes: null,
          repo_poste: null,
          repo_email: null,
          acti_id: null,
          repo_codigo: null,
          repo_apoyo: null
        },
        eventos: [],
        direcciones: []
      }
      this.direccion = {
        repo_id: null,
        aap_id: null,
        even_direccion: null,
        barr_id: null
      }
      this.adicional = {
        repo_id: null,
        repo_fechadigitacion: null,
        repo_modificado: null,
        repo_tipo_expansion: null,
        repo_luminaria: null,
        repo_redes: null,
        repo_poste: null,
        repo_email: null,
        acti_id: null
      }
      this.aap = null
      this.canPrint = false
      this.canSave = true
      this.inactivo = false
    },
    continue() {
      this.canPrint = true
      this.canSave = false
      this.inactivo = true
    },
    guardarNuevoDanho() {
      this.$refs['danho'].validate((valid) => {
        if (valid) {
          this.dialogonuevodanhovisible = false
          saveActividad(this.actividad).then(response => {
            this.limpiarActividad()
            if (response.status === 201) {
              this.getActividades()
              this.actividad.acti_id = response.data
            } else {
              this.$notify.error({
                title: 'Error al Crear Descripción de Daño',
                message: 'No se pudo adicionar la nueva descripción de daño'
              })
            }
          }).catch(error => {
            this.limpiarActividad()
            this.$notify.error({
              title: 'Error al Crear Descripción de Daño',
              message: error
            })
          })
        }
      })
    },
    limpiarActividad() {
      this.actividad = {
        acti_id: null,
        acti_descripcion: null,
        acti_estado: 1,
        usua_id: 0
      }
    },
    imprimir() {
      printReporte(this.reporte.repo_id, this.empresa.empr_id)
    },
    getActividades() {
      getActividades().then(response => {
        this.actividades = response.data
      }).catch(error => {
        console.log('getActividades :' + error)
      })
    },
    buscarAap() {
      if (this.adicional.repo_codigo === null || this.adicional.repo_codigo === '' || this.aap !== null) {
        return
      } else {
        getAap(this.adicional.repo_codigo).then(response => {
          if (response.status === 200) {
            var aap = response.data
            this.reporte.repo_apoyo = aap.aap_apoyo
            this.reporte.repo_direccion = aap.aap_direccion
            this.reporte.barr_id = aap.barr_id
            var barrio = this.barrios.find(b => b.barr_id === aap.barr_id)
            if (barrio != null || barrio !== undefined) {
              this.reporte.tiba_id = barrio.tiba_id
            }
          }
        }).catch(error => {
          console.log('getAap: ' + error)
        })
      }
    },
    buscarAapApoyo() {
      if (this.reporte.repo_apoyo === null || this.reporte.repo_apoyo === '' || this.aap !== null) {
        return
      } else {
        getAapApoyo(this.reporte.repo_apoyo).then(response => {
          if (response.status === 200) {
            var aap = response.data
            this.reporte.repo_codigo = aap.aap_id
            this.reporte.repo_direccion = aap.aap_direccion
            this.reporte.barr_id = aap.barr_id
            var barrio = this.barrios.find(b => b.barr_id === aap.barr_id)
            if (barrio != null || barrio !== undefined) {
              this.reporte.tiba_id = barrio.tiba_id
            }
            this.changeFocus('nombre')
          }
        }).catch(error => {
          console.log('getAap: ' + error)
        })
      }
    },
    tiporeporte(reti_id) {
      if (reti_id === null) {
        return 'INDEFINIDO'
      } else {
        return this.tipos.find(o => o.reti_id === reti_id, { reti_descripcion: 'INDEFINIDO' }).reti_descripcion
      }
    },
    actualizarSector() {
      var barrio = this.barrios.find(b => b.barr_id === this.reporte.barr_id)
      if (barrio != null || barrio !== undefined) {
        this.reporte.tiba_id = barrio.tiba_id
        this.changeFocus('telefono')
      }
    },
    buscarReporte() {
      getReportePorConsecutivo(this.freti_id, this.fconsec).then(response => {
        if (response.status === 200) {
          this.reporte_principal = response.data
          this.existe = true
        } else {
          this.existe = false
        }
      })
    },
    barrio(barr_id) {
      if (barr_id !== null & barr_id !== undefined) {
        return this.barrios.find(b => b.barr_id === barr_id, { barr_descripcion: '' }).barr_descripcion
      }
    }
  },
  created() {
    getOrigenes().then(response => {
      this.origenes = response.data
    }).catch(error => {
      console.log('Origenes: ' + error)
    })
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.barrios_lista = response.data
    }).catch(error => {
      console.log(error)
    })
    getTipos().then(response => {
      this.tipos = response.data
      this.tipos_lista = response.data
    }).catch(error => {
      console.log('getTipos: ' + error)
    })
    getTiposBarrio().then(response => {
      this.tiposbarrio = response.data
    }).catch(error => {
      console.log('getTiposBarrio: ' + error)
    })
    this.getActividades()
  }
}
</script>
