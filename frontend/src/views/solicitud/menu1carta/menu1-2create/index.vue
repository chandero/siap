<template>
  <el-container>
    <el-header>
      <span>{{ $t('route.solicitudcreate') }}</span>
    </el-header>
    <el-main>
     <el-form ref="formSolicitud" :model="solicitud" :rules="rules" label-position="top">
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
            <el-form-item prop="a.soli_radicado" :label="$t('solicitud.radicado')">
              <el-input readonly name="radicado" v-model="solicitud.a.soli_radicado"></el-input>
            </el-form-item>
         </el-col>
            <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
              <el-form-item prop="a.soli_fecha" :label="$t('solicitud.fecha')">
                <el-date-picker type="datetime" name="fecha" v-model="solicitud.a.soli_fecha"></el-date-picker>
              </el-form-item>
            </el-col>
          <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
            <el-form-item prop="a.soti_id" :label="$t('solicitud.tipo')">
              <el-select autofocus v-model="solicitud.a.soti_id">
                <el-option v-for="s in solicitudtipos" :key="s.soti_id" :label="s.soti_descripcion" :value="s.soti_id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-form-item prop="a.soli_nombre" :label="$t('solicitud.nombre')">
              <el-input name="nombre" v-model="solicitud.a.soli_nombre" @input="solicitud.a.soli_nombre = $event.toUpperCase()"></el-input>
            </el-form-item>
         </el-col>
       </el-row>
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
            <el-form-item prop="a.soli_direccion" :label="$t('solicitud.direccion')">
              <el-input name="direccion" v-model="solicitud.a.soli_direccion"></el-input>
            </el-form-item>
         </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-form-item prop="a.barr_id" :label="$t('solicitud.barrio')">
              <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="solicitud.a.barr_id" name="barrio" :placeholder="$t('barrio.select')">
                <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
                </el-option>
              </el-select>
            </el-form-item>
         </el-col>
       </el-row>
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item prop="a.soli_telefono" :label="$t('solicitud.telefono')">
              <el-input name="telefono" v-model="solicitud.a.soli_telefono"></el-input>
            </el-form-item>
         </el-col>
          <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item prop="a.soli_email" :label="$t('solicitud.email')">
              <el-input name="email" v-model="solicitud.a.soli_email"></el-input>
            </el-form-item>
         </el-col>
       </el-row>
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <el-form-item prop="a.soli_solicitud" :label="$t('solicitud.descripcion')">
              <el-input type="textarea" :rows="5" name="telefono" v-model="solicitud.a.soli_solicitud"></el-input>
            </el-form-item>
         </el-col>
       </el-row>
       <el-collapse v-model="activeNames">
         <el-collapse-item v-if="state === 1" name="1" title="Supervisor">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fechasupervisor')">
                <el-date-picker type="datetime" name="fechasupervisor" v-model="solicitud.soli_fechasupervisor"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
         </el-collapse-item>
         <el-collapse-item v-if="state === 2" name="2" title="Informe">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fechainforme')">
                <el-date-picker type="datetime" name="fechainforme" v-model="solicitud.soli_fechainforme"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-form-item :label="$t('solicitud.informe')">
                <el-input type="textarea" :rows="5" name="informe" v-model="solicitud.soli_informe"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-collapse-item>
         <el-collapse-item v-if="state === 3" name="3" title="Respuesta">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fecharespuesta')">
                <el-date-picker type="datetime" name="fecharespuesta" v-model="solicitud.soli_fecharespuesta"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-form-item :label="$t('solicitud.respuesta')">
                <el-input type="textarea" :rows="5" name="respuesta" v-model="solicitud.soli_respuesta"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-collapse-item>
       </el-collapse>
       <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="confirmacionGuardar = true">Guardar</el-button>
      </el-form>
      <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
        <span style="font-size:20px;">Seguro de Guardar la Nueva Solicitud?</span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="confirmacionGuardar = false">No</el-button>
          <el-button type="primary" @click="aplicar">Sí</el-button>
        </span>
      </el-dialog>
    </el-main>
  </el-container>
</template>
<script>
import { saveSolicitud } from '@/api/solicitud'
import { getBarriosEmpresa } from '@/api/barrio'
import { getSolicitudTipos } from '@/api/solicitudtipo'

export default {
  data () {
    return {
      activeNames: [],
      state: 0,
      confirmacionGuardar: false,
      puedeGuardar: true,
      solicitud: {
        a: {
          soli_id: null,
          soti_id: null,
          soli_fecha: new Date(),
          soli_nombre: null,
          soli_radicado: null,
          soli_direccion: null,
          barr_id: null,
          soli_telefono: null,
          soli_email: null,
          soli_solicitud: null,
          soli_respuesta: null,
          soli_informe: null
        },
        b: {
          soli_fecharespuesta: null,
          soli_fechadigitado: null,
          soli_fechalimite: null,
          soli_fechasupervisor: null,
          soli_fechainforme: null,
          soli_fechavisita: null,
          soli_fecharte: null,
          soli_fechaalmacen: null,
          soli_numerorte: null,
          soli_estado: null,
          empr_id: null,
          usua_id: null
        }
      },
      barrios: [],
      solicitudtipos: [],
      rules: {
        'a.soli_fecha': [
          { required: true, message: 'Seleccione la Fecha de la Solicitud', trigger: 'blur' }
        ],
        'a.soti_id': [
          { required: true, message: 'Seleccione el tipo de Solicitud', trigger: 'blur' }
        ],
        'a.soli_nombre': [
          { required: true, message: 'Digite el Nombre Completo del Solicitante', trigger: 'blur' }
        ],
        'a.soli_direccion': [
          { required: true, message: 'Digite la Dirección Completa de la Solicitud', trigger: 'blur' }
        ],
        'a.barr_id': [
          { required: true, message: 'Seleccione el Barrio', trigger: 'blur' }
        ],
        'a.soli_telefono': [
          { required: true, message: 'Digite el Teléfono del Solicitante', trigger: 'blur' }
        ],
        'a.soli_solicitud': [
          { required: true, message: 'Digite el Detalle de la Solicitud', trigger: 'blur' }
        ]
      }
    }
  },
  mounted () {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      getSolicitudTipos().then(response => {
        this.solicitudtipos = response.data
      }).catch(error => {
        console.log('solicitud tipos: ' + error)
      })
    }).catch(error => {
      console.log('barrios: ' + error)
    })
  },
  methods: {
    aplicar () {
      this.confirmacionGuardar = false
      this.$refs.formSolicitud.validate((valid) => {
        saveSolicitud(this.solicitud).then(response => {
          if (response.status === 201) {
            this.solicitud.a.soli_radicado = response.data
            this.success()
          }
        }).catch(error => {
          this.error(error)
        })
      })
    },
    validate () {
      if (this.solicitud.a.soli_fecha &&
          this.solicitud.a.soti_id &&
          this.solicitud.a.soli_nombre &&
          this.solicitud.a.soli_direccion &&
          this.solicitud.a.barr_id &&
          this.solicitud.a.soli_telefono &&
          this.solicitud.a.soli_solicitud &&
          this.puedeGuardar) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack () {
      this.solicitud = {
        soli_id: null,
        soli_fecha: null,
        soli_nombre: null,
        soli_radicado: null,
        soli_direccion: null,
        soli_telefono: null,
        soli_email: null,
        soli_solicitud: null,
        soli_respuesta: null,
        soli_informe: null,
        soli_fecharespuesta: null,
        soli_fechadigitado: null,
        soli_fechalimite: null,
        soli_fechasupervisor: null,
        soli_fechainforme: null,
        soli_estado: 1,
        empr_id: null,
        usua_id: null
      }
      this.$router.push({ path: '/solicitud/solicitud' })
    },
    limpiar () {
      this.solicitud = {
        soli_id: null,
        soli_fecha: null,
        soli_nombre: null,
        soli_radicado: null,
        soli_direccion: null,
        soli_telefono: null,
        soli_email: null,
        soli_solicitud: null,
        soli_respuesta: null,
        soli_informe: null,
        soli_fecharespuesta: null,
        soli_fechadigitado: null,
        soli_fechalimite: null,
        soli_fechasupervisor: null,
        soli_fechainforme: null,
        soli_estado: 1,
        empr_id: null,
        usua_id: null
      }
    },
    success () {
      this.$notify({
        title: this.$i18n.t('solicitud.success'),
        message: this.$i18n.t('solicitud.created') + ' ' + this.solicitud.a.soli_radicado,
        type: 'success'
      })
      this.puedeGuardar = false
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('solicitud.error'),
        message: this.$i18n.t('solicitud.notcreated') + ' ' + e
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 480px;
}

.estado-1 {
  background: #fff;
}

.estado-2 {
  background: #53a8ff;
}

.estado-3 {
  background: #e6a23c;
}

.estado-4 {
  background: #67c23a;
}

.estado-n {
  background: #f56c6c;
}
</style>
