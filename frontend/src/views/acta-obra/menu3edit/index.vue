<template>
  <el-container>
    <el-header>
      <span>{{ $t('muot.edit') }}</span>
    </el-header>
    <el-main>
      <el-form :disabled="inactivo" ref="muot" :model="muot" :rules="rules" :label-position="labelPosition">
        <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
            <el-form-item prop="muot_fecharecepcion" :label="$t('muot.fecharecepcion')">
              <el-date-picker type="datetime" ref="receptiondate" v-model="muot.muot_fecharecepcion"
                @change="changeFocus('nombre')"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
            <el-form-item prop="muot_acta_numero" :label="$t('muot.acta_numero')">
              <el-input ref="acta_numero" v-model="muot.muot_acta_numero"
                @input="muot.muot_acta_numero = $event.toUpperCase()"
                @keyup.enter.native="changeFocus('receptiondate')"></el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
            <el-form-item prop="muot_acta_anho" :label="$t('muot.acta_anho')">
              <el-input ref="acta_anho" v-model="muot.muot_acta_anho" type="number"
                @input="muot.muot_acta_anho = parseInt($event)"
                @keyup.enter.native="changeFocus('receptiondate')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <el-form-item prop="muot_descripcion" :label="$t('muot.descripcion')">
              <el-input type="textarea" rows="4" ref="muot_descripcion" v-model="muot.muot_descripcion"
                @input="muot.muot_descripcion = $event.toUpperCase()"
                @keyup.enter.native="changeFocus('muot_direccion')"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
            <el-form-item prop="muot_direccion" :label="$t('muot.direccion')">
              <el-input ref="direccion" v-model="muot.muot_direccion" @input="muot.muot_direccion = $event.toUpperCase()"
                @keyup.enter.native="changeFocus('barrio')">
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-form-item prop="barr_id" :label="$t('muot.barr_id')">
              <el-select style="width:100%;" filterable clearable ref="barrio" v-model="muot.barr_id" name="barrio"
                :placeholder="$t('barrio.select')" @change="actualizarSector()">
                <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion"
                  :value="barrio.barr_id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-row>
        <el-col>
          <el-button v-if="canSave" ref="submit" size="medium" type="primary" icon="el-icon-check"
            @click="confirmacionGuardar = true" title="Crear Acta"></el-button>
          <el-button v-if="canPrint" ref="nuevo" size="medium" type="primary" icon="el-icon-plus"
            @click="nuevoActa = true">Nueva Acta</el-button>
        </el-col>
      </el-row>
    </el-main>
    <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
      <span style="font-size:20px;">Seguro de Guardar los Cambios?</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="confirmacionGuardar = false">No</el-button>
        <el-button type="primary" @click="aplicar">Sí</el-button>
      </span>
    </el-dialog>
    <el-dialog title="Confirmación" :visible.sync="nuevoActa">
      <span style="font-size:20px;">Proceder con una Nuevo Acta?</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="nuevoActa = false">No</el-button>
        <el-button type="primary" @click="nuevo">Sí</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getBarriosEmpresa } from '@/api/barrio'
import { getMuot, updateMuot } from '@/api/municipio_orden_trabajo_acta'

export default {
  data() {
    return {
      labelPosition: 'top',
      guardando: false,
      activePages: ['1', '2', '3', '4'],
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      nuevoActa: false,
      inactivo: false,
      adicional_rules: {
        muot_tipo_expansion: [
          { required: false, message: 'Debe Seleccionar el Tipo de Expansión', trigger: 'change' }
        ]
      },
      rules: {
        muot_fecharecepcion: [
          { required: true, message: 'Debe diligencia la Fecha de Recepción del Acta', trigger: 'change' }
        ],
        reti_id: [
          { required: true, message: 'Debe Seleccionar el Tipo de Acta', trigger: 'change' }
        ],
        orig_id: [
          { required: true, message: 'Debe Seleccionar el Origen del Acta', trigger: 'change' }
        ],
        acti_id: [
          { required: false, message: 'Debe Seleccionar el Tipo de Daño', trigger: 'change' }
        ],
        muot_nombre: [
          { required: false, message: 'Debe Digitar el Nombre de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        muot_direccion: [
          { required: false, message: 'Debe Digitar la dirección del daño o actividad', trigger: 'blur' }
        ],
        muot_telefono: [
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
      muot: {
        muot_id: null,
        muot_acta_numero: null,
        muot_acta_anho: new Date().getFullYear(),
        muot_fecharecepcion: new Date(),
        muot_descripcion: null,
        muot_direccion: null,
        barr_id: null,
        muot_fechaentrega: null,
        muot_consecutivo: null,
        muot_reportetecnico: null,
        empr_id: 1,
        usua_id: 0,
        muot_radicado: null,
        muot_estado: 1
      },
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
    obtenerActa() {
      const id = this.$route.params.id
      getMuot(id).then(response => {
        this.muot = response.data
        console.log('Muot:', JSON.stringify(this.muot))
      }).catch(error => {
        console.log('Error:', error)
      })
    },
    changeFocus(next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange(val) {
    },
    validate() {
      if (this.muot.muot_id && this.muot.muot_fecharecepcion && this.muot.muot_descripcion && this.muot.muot_direccion && this.muot.barr_id) {
        return true
      } else {
        return false
      }
    },
    aplicar() {
      if (!this.guardando) {
        this.guardando = true
        this.confirmacionGuardar = false
        this.$refs.muot.validate((valid) => {
          if (valid) {
            updateMuot(this.muot)
              .then(response => {
                if (response.status === 200) {
                  this.muot.muot_id = response.data.id
                  this.muot.muot_consecutivo = response.data.consec
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
        title: this.$i18n.t('muot.success'),
        message:
          this.$i18n.t('muot.updated') +
          ' Acta No. ' +
          this.muot.muot_acta_numero + ' de ' + this.muot.muot_acta_anho,
        type: 'success',
        onClose: this.continue()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('muot.error'),
        message: this.$i18n.t('muot.notcreated') + ' ' + e
      })
    },
    nuevo() {
      this.nuevoActa = false
      this.muot = {
        muot_id: null,
        muot_consecutivo: null,
        muot_nombre: null,
        tiba_id: null,
        muot_fecharecepcion: new Date(),
        muot_direccion: null,
        muot_solicitante: null,
        muot_telefono: null,
        muot_email: null,
        muot_fechasolucion: null,
        muot_horainicio: null,
        muot_horafin: null,
        muot_muottecnico: null,
        muot_descripcion: null,
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
    }
  },
  created() {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.barrios_lista = response.data
      this.obtenerActa()
    }).catch(error => {
      console.log(error)
    })
  }
}
</script>
