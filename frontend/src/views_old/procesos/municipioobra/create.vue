<template>
  <el-container>
      <el-header>
          <span>{{ $t('route.muobcreate') }}</span>
      </el-header>
      <el-main>
          <el-form :disabled="inactivo" ref="muob" :model="muob" :rules="rules" :label-position="labelPosition">
              <el-collapse v-model="activePages" @change="handleActivePagesChange">
                <el-collapse-item name="1" :title="$t('muob.general')">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                            <el-form-item prop="muob_fecharecepcion" :label="$t('muob.muob_fecharecepcion')">
                                <el-date-picker  type="datetime" ref="receptiondate" v-model="muob.muob_fecharecepcion" @change="changeFocus('nombre')"></el-date-picker>
                            </el-form-item>
                        </el-col>
                         <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                          <el-form-item prop="muob_consecutivo" :label="$t('muob.muob_consecutivo')">
                            <el-input type="number" style="font-size: 30px;" v-model="muob.muob_consecutivo" @input="muob.muob_consecutivo = parseInt($event)"></el-input>
                          </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                         <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="muob_direccion" :label="$t('muob.muob_direccion')">
                             <el-input ref="direccion" v-model="muob.muob_direccion" @input="muob.muob_direccion = $event.toUpperCase()" @keyup.enter.native="changeFocus('barrio')">
                             </el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="barr_id" :label="$t('muob.barr_descripcion')">
                             <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="muob.barr_id" name="barrio" :placeholder="$t('barrio.select')"  >
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="muob_descripcion" :label="$t('muob.muob_descripcion')">
                                <el-input ref="descripcion" v-model="muob.muob_descripcion" type="textarea" :rows="2" @input="muob.muob_descripcion = $event.toUpperCase()"  @keyup.enter.native="changeFocus('submit')"></el-input>
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
          <span style="font-size:20px;">Seguro de Guardar la Nueva Municipio Obra?</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="confirmacionGuardar = false">No</el-button>
            <el-button type="primary" @click="aplicar">Sí</el-button>
          </span>
      </el-dialog>
      <el-dialog title="Confirmación" :visible.sync="nuevoObra">
          <span style="font-size:20px;">Proceder con una Nueva Municipio Obra?</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="nuevoObra = false">No</el-button>
            <el-button type="primary" @click="nuevo">Sí</el-button>
          </span>
      </el-dialog>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { getBarriosEmpresa } from '@/api/barrio'
import { saveMuob } from '@/api/muob'

export default {
  data () {
    return {
      labelPosition: 'top',
      guardando: false,
      activePages: ['1', '2', '3', '4'],
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      nuevoObra: false,
      inactivo: false,
      rules: {
        muob_fecharecepcion: [
          { required: true, message: 'Debe diligencia la Fecha de Recepción del Obra', trigger: 'change' }
        ],
        muob_descripcion: [
          { required: false, message: 'Debe Digitar la Descripción de la Obra', trigger: 'blur' }
        ],
        muob_direccion: [
          { required: false, message: 'Debe Digitar la dirección del daño o actividad', trigger: 'blur' }
        ],
        muob_telefono: [
          { required: false, message: 'Debe Digitar el Teléfono de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        barr_id: [
          { required: false, message: 'Debe Seleccionar el Barrio del Daño o Actividad', trigger: 'change' }
        ]
      },
      muob: {
        muob_id: null,
        muob_consecutivo: null,
        muob_direccion: null,
        muob_fecharecepcion: new Date(),
        muob_fechaentrega: null,
        muob_descripcion: null,
        muob_reportetecnico: null,
        muob_radicado: null,
        muob_estado: 1,
        barr_id: null,
        empr_id: null,
        usua_id: null
      },
      barrios: [],
      dialogonuevodanhovisible: false
    }
  },
  computed: {
    ...mapGetters([
      'empresa'
    ])
  },
  methods: {
    changeFocus (next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange (val) {
    },
    validate () {
      if (this.muob.muob_fecharecepcion && this.muob.muob_direccion && this.muob.barr_id && this.muob.muob_descripcion) {
        return true
      } else {
        return false
      }
    },
    aplicar () {
      if (!this.guardando) {
        this.guardando = true
        this.confirmacionGuardar = false
        this.$refs.muob.validate((valid) => {
          if (valid) {
            saveMuob(this.muob)
              .then(response => {
                if (response.status === 201) {
                  this.muob.muob_id = response.data.id
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
    success () {
      this.$notify({
        title: this.$i18n.t('muob.success'),
        message:
          this.$i18n.t('muob.created') +
          ' ' +
          this.muob.muob_consecutivo,
        type: 'success',
        onClose: this.continue()
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('muob.error'),
        message: this.$i18n.t('muob.notcreated') + ' ' + e
      })
    },
    nuevo () {
      this.nuevoObra = false
      this.muob = {
        muob_id: null,
        muob_consecutivo: null,
        muob_direccion: null,
        muob_fecharecepcion: new Date(),
        muob_fechaentrega: null,
        muob_descripcion: null,
        muob_reportetecnico: null,
        muob_radicado: null,
        muob_estado: 1,
        barr_id: null,
        empr_id: null,
        usua_id: null
      }
      this.canPrint = false
      this.canSave = true
      this.inactivo = false
    },
    continue () {
      this.canPrint = true
      this.canSave = false
      this.inactivo = true
    },
    imprimir () {
      // printMuob(this.muob.muob_id, this.empresa.empr_id)
    }
  },
  created () {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
    }).catch(error => {
      console.log(error)
    })
  }
}
</script>
