<template>
  <el-container>
      <el-header>
          <span>{{ $t('route.obracreate') }}</span>
      </el-header>
      <el-main>
          <el-form :disabled="inactivo" ref="obra" :model="obra" :rules="rules" :label-position="labelPosition">
              <el-collapse v-model="activePages" @change="handleActivePagesChange">
                <el-collapse-item name="1" :title="$t('obra.general')">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                            <el-form-item prop="obra_fecharecepcion" :label="$t('obra.receptiondate')">
                                <el-date-picker  type="datetime" ref="receptiondate" v-model="obra.obra_fecharecepcion" @change="changeFocus('nombre')"></el-date-picker>
                            </el-form-item>
                        </el-col>                      
                        <!--
                        <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                            <el-form-item prop="reti_id" :label="$t('obra.type')">
                                <el-select autofocus clearable :title="$t('obra.tipo.select')" style="width: 80%" ref="tipo" v-model="obra.reti_id" name="tipo" :placeholder="$t('obra.tipo.select')"  @change="validarTipo()">
                                    <el-option v-for="tipo in tipos" :key="tipo.reti_id" :label="tipo.reti_descripcion" :value="tipo.reti_id" >
                                    </el-option>   
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col v-if="obra.reti_id===2" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                          <el-form-item :label="$t('obra.tipo_expansion.title')">
                            <el-form :model="adicional" :rules="adicional_rules" :label-position="labelPosition">
                              <el-form-item prop="obra_tipo_expansion">
                                <el-select autofocus clearable :title="$t('obra.tipo_expansion.select')" style="width: 80%" ref="tipo" v-model="adicional.obra_tipo_expansion" name="tipo_expansion" :placeholder="$t('obra.tipo_expansion.select')">
                                    <el-option v-for="te in tipos_expansion" :key="te.tiex_id" :label="te.tiex_descripcion" :value="te.tiex_id" >
                                    </el-option>   
                                </el-select>
                              </el-form-item>
                            </el-form>
                          </el-form-item>
                        </el-col>
                        -->
                        <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                          <el-form-item prop="obra_consecutivo" :label="$t('obra.consecutivo')">
                            <el-input readonly style="font-size: 30px;" v-model="obra.obra_consecutivo"></el-input>
                          </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
                          <el-form-item prop="muot_id" :label="$t('obra.ot')">
                            <el-input type="number" style="font-size: 30px;" v-model="obra.muot_id" @input="obra.muot_id = parseInt($event)"></el-input>
                          </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row>
                        <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
                            <el-form-item prop="obra_nombre" :label="$t('obra.name')">
                                <el-input ref="nombre" v-model="obra.obra_nombre" @input="obra.obra_nombre = $event.toUpperCase()" @keyup.enter.native="changeFocus('direccion')"></el-input>
                            </el-form-item>                            
                        </el-col>                        
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                            <el-form-item prop="orig_id" :label="$t('obra.origin')">
                                <el-select style="width:100%;" clearable ref="origin" v-model="obra.orig_id" name="origen" :placeholder="$t('origen.select')"  @change="changeFocus('code')">
                                    <el-option v-for="origen in origenes" :key="origen.orig_id" :label="origen.orig_descripcion" :value="origen.orig_id" >
                                    </el-option>   
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col v-if="obra.orig_id === 6" :xs="24" :sm="24" :md="24" :lg="5" :xl="5">
                            <el-form-item prop="obra_email" :label="$t('obra.email')">
                                <el-input ref="email" v-model="obra.obra_email" @keyup.enter.native="changeFocus('code')" ></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="24" :lg="6" :xl="6">
                            <el-form-item prop="obra_solicitante" :label="$t('obra.solicitante')">
                                <el-input ref="solicitante" v-model="obra.obra_solicitante" @input="obra.obra_solicitante = $event.toUpperCase()"></el-input>
                            </el-form-item>                            
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="24" :lg="5" :xl="5">
                            <el-form-item prop="obra_telefono" :label="$t('obra.telefono')">
                                <el-input ref="telefono" v-model="obra.obra_telefono"></el-input>
                            </el-form-item>                            
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <!--

                        -->
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="obra_direccion" :label="$t('obra.address')">
                             <el-input ref="direccion" v-model="obra.obra_direccion" @input="obra.obra_direccion = $event.toUpperCase()" @keyup.enter.native="changeFocus('barrio')">
                             </el-input>
                            </el-form-item>
                        </el-col>                        
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="barr_id" :label="$t('obra.neighborhood')">
                             <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="obra.barr_id" name="barrio" :placeholder="$t('barrio.select')"  @change="actualizarSector()">
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
                              </el-option>   
                             </el-select>
                            </el-form-item>                        
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                            <el-form-item prop="tiba_id" :label="$t('obra.sector')">
                             <el-select  style="width:100%;" filterable clearable ref="tiba" v-model="obra.tiba_id" name="tiba" :placeholder="$t('tipobarrio.select')"  @change="changeFocus('telefono')">
                              <el-option v-for="tiba in tiposbarrio" :key="tiba.tiba_id" :label="tiba.tiba_descripcion" :value="tiba.tiba_id" >
                              </el-option>   
                             </el-select>
                            </el-form-item>                        
                        </el-col>
                        <!--
                        <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                            <el-form-item prop="obra_telefono" :label="$t('obra.phone')">
                                <el-input ref="telefono" v-model="obra.obra_telefono" @keyup.enter.native="changeFocus('descripcion')"></el-input>
                            </el-form-item>
                        </el-col>                        
                        -->
                    </el-row>
                    <el-row :gutter="4">
                        <!-- 
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="acti_id" :label="$t('obra.activity')">
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
                        -->                      
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="obra_descripcion" :label="$t('obra.description')">
                                <el-input ref="descripcion" v-model="obra.obra_descripcion" type="textarea" :rows="2" @input="obra.obra_descripcion = $event.toUpperCase()"  @keyup.enter.native="changeFocus('submit')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-collapse-item>
              </el-collapse>
          </el-form>
              <el-row>
                <el-col>
                  <el-button v-if="canSave" ref="submit" size="medium" type="primary" icon="el-icon-check" @click="confirmacionGuardar = true"></el-button>
                  <el-button v-if="canPrint" ref="print" size="medium" type="success" icon="el-icon-printer" title="Imprimir Obra" @click="imprimir"></el-button>                  
                  <el-button v-if="canPrint" ref="nuevo" size="medium" type="primary" icon="el-icon-plus" @click="nuevoObra = true">Nueva Obra</el-button>                    
                </el-col>
              </el-row>
      </el-main>
      <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
          <span style="font-size:20px;">Seguro de Guardar la Nueva Obra?</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="confirmacionGuardar = false">No</el-button>
            <el-button type="primary" @click="aplicar">Sí</el-button>
          </span>
      </el-dialog>
      <el-dialog title="Confirmación" :visible.sync="nuevoObra">
          <span style="font-size:20px;">Proceder con una Nuevo Obra?</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="nuevoObra = false">No</el-button>
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
import { saveObra, printObra } from '@/api/obra'
/* import { getActividades, saveActividad } from '@/api/actividad' */
import { getAap, getAapApoyo } from '@/api/aap'

export default {
  data() {
    return {
      labelPosition: 'top',
      guardando: false,
      activePages: ['1', '2', '3', '4'],
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      nuevoObra: false,
      inactivo: false,
      adicional_rules: {
        obra_tipo_expansion: [
          { required: false, message: 'Debe Seleccionar el Tipo de Expansión', trigger: 'change' }
        ]
      },
      rules: {
        obra_fecharecepcion: [
          { required: true, message: 'Debe diligencia la Fecha de Recepción del Obra', trigger: 'change' }
        ],
        reti_id: [
          { required: true, message: 'Debe Seleccionar el Tipo de Obra', trigger: 'change' }
        ],
        orig_id: [
          { required: true, message: 'Debe Seleccionar el Origen del Obra', trigger: 'change' }
        ],
        acti_id: [
          { required: false, message: 'Debe Seleccionar el Tipo de Daño', trigger: 'change' }
        ],
        obra_nombre: [
          { required: false, message: 'Debe Digitar el Nombre de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        obra_direccion: [
          { required: false, message: 'Debe Digitar la dirección del daño o actividad', trigger: 'blur' }
        ],
        obra_telefono: [
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
      obra: {
        obra_id: null,
        obra_consecutivo: null,
        obra_nombre: null,
        obra_fecharecepcion: new Date(),
        obra_direccion: null,
        obra_solicitante: null,
        obra_telefono: null,
        obra_email: null,
        obra_fechasolucion: null,
        obra_horainicio: null,
        obra_horafin: null,
        obra_obratecnico: null,
        obra_descripcion: null,
        muot_id: null,
        rees_id: 1,
        orig_id: 3,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        eventos: []
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
      if (this.obra.reti_id === 2) {
        this.obra.orig_id = 5
        this.rules.acti_id[0].required = false
        this.adicional_rules.obra_tipo_expansion[0].required = true
      } else if (this.obra.reti_id === 1) {
        this.rules.acti_id[0].required = true
        this.adicional_rules.obra_tipo_expansion[0].required = false
      } else {
        this.rules.acti_id[0].required = false
        this.adicional_rules.obra_tipo_expansion[0].required = false
      }
    },
    changeFocus(next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange(val) {
    },
    validate() {
      if (this.obra.orig_id && this.obra.obra_fecharecepcion && this.obra.obra_nombre && this.obra.obra_direccion && this.obra.barr_id && this.obra.obra_descripcion) {
        return true
      } else {
        return false
      }
    },
    aplicar() {
      if (!this.guardando) {
        this.guardando = true
        this.confirmacionGuardar = false
        this.$refs['obra'].validate((valid) => {
          if (valid) {
            this.obra.adicional = this.adicional
            saveObra(this.obra)
              .then(response => {
                if (response.status === 201) {
                  this.obra.obra_id = response.data.id
                  this.obra.obra_consecutivo = response.data.consec
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
        title: this.$i18n.t('obra.success'),
        message:
          this.$i18n.t('obra.created') +
          ' ' +
          this.obra.obra_consecutivo,
        type: 'success',
        onClose: this.continue()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('obra.error'),
        message: this.$i18n.t('obra.notcreated') + ' ' + e
      })
    },
    nuevo() {
      this.nuevoObra = false
      this.obra = {
        obra_id: null,
        obra_consecutivo: null,
        obra_nombre: null,
        tiba_id: null,
        obra_fecharecepcion: new Date(),
        obra_direccion: null,
        obra_solicitante: null,
        obra_telefono: null,
        obra_email: null,
        obra_fechasolucion: null,
        obra_horainicio: null,
        obra_horafin: null,
        obra_obratecnico: null,
        obra_descripcion: null,
        rees_id: 1,
        orig_id: 1,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        eventos: []
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
    /*
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
    */
    limpiarActividad() {
      this.actividad = {
        acti_id: null,
        acti_descripcion: null,
        acti_estado: 1,
        usua_id: 0
      }
    },
    imprimir() {
      printObra(this.obra.obra_id, this.empresa.empr_id)
    },
    /*
    getActividades() {
      getActividades().then(response => {
        this.actividades = response.data
      }).catch(error => {
        console.log('getActividades :' + error)
      })
    },
    */
    buscarAap() {
      if (this.adicional.obra_codigo === null || this.adicional.obra_codigo === '' || this.aap !== null) {
        return
      } else {
        getAap(this.adicional.obra_codigo).then(response => {
          if (response.status === 200) {
            var aap = response.data
            this.obra.obra_apoyo = aap.aap_apoyo
            this.obra.obra_direccion = aap.aap_direccion
            this.obra.barr_id = aap.barr_id
            var barrio = this.barrios.find(b => b.barr_id === aap.barr_id)
            if (barrio != null || barrio !== undefined) {
              this.obra.tiba_id = barrio.tiba_id
            }
          }
        }).catch(error => {
          console.log('getAap: ' + error)
        })
      }
    },
    buscarAapApoyo() {
      if (this.obra.obra_apoyo === null || this.obra.obra_apoyo === '' || this.aap !== null) {
        return
      } else {
        getAapApoyo(this.obra.obra_apoyo).then(response => {
          if (response.status === 200) {
            var aap = response.data
            this.obra.obra_codigo = aap.aap_id
            this.obra.obra_direccion = aap.aap_direccion
            this.obra.barr_id = aap.barr_id
            var barrio = this.barrios.find(b => b.barr_id === aap.barr_id)
            if (barrio != null || barrio !== undefined) {
              this.obra.tiba_id = barrio.tiba_id
            }
            this.changeFocus('nombre')
          }
        }).catch(error => {
          console.log('getAap: ' + error)
        })
      }
    },
    tipoobra(reti_id) {
      if (reti_id === null) {
        return 'INDEFINIDO'
      } else {
        return this.tipos.find(o => o.reti_id === reti_id, { reti_descripcion: 'INDEFINIDO' }).reti_descripcion
      }
    },
    actualizarSector() {
      var barrio = this.barrios.find(b => b.barr_id === this.obra.barr_id)
      if (barrio != null || barrio !== undefined) {
        this.obra.tiba_id = barrio.tiba_id
        this.changeFocus('telefono')
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
    /*
    getTipos().then(response => {
      this.tipos = response.data
      this.tipos_lista = response.data
    }).catch(error => {
      console.log('getTipos: ' + error)
    })
    */
    getTiposBarrio().then(response => {
      this.tiposbarrio = response.data
    }).catch(error => {
      console.log('getTiposBarrio: ' + error)
    })
    /*
    this.getActividades()
    */
  }
}
</script>
