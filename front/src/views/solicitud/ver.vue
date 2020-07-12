<template>
  <el-container>
    <el-header>
      <span>{{ $t('route.solicitudver') }}</span>
    </el-header>
    <el-main>
     <el-form v-model="solicitud">
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item :label="$t('solicitud.radicado')">
              <span style="font-size: 32px;" name="radicado">{{ solicitud.a.soli_radicado }}</span>
            </el-form-item>
          </el-col>
          <el-col el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item :label="$t('solicitud.estado')">
              <span :class="estadoClass()" name="estado">{{ estado(solicitud.b.soli_estado) }}</span>
            </el-form-item>
         </el-col>       
       </el-row>
       <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
              <el-form-item :label="$t('solicitud.fecha')">
                <el-date-picker readonly type="datetime" name="fecha" v-model="solicitud.a.soli_fecha"></el-date-picker>
              </el-form-item>
            </el-col>         
          <el-col :xs="24" :sm="24" :md="19" :lg="19" :xl="19">
            <el-form-item :label="$t('solicitud.nombre')">
              <el-input readonly name="nombre" v-model="solicitud.a.soli_nombre" autofocus @input="solicitud.a.soli_nombre = $event.toUpperCase()"></el-input>
            </el-form-item>
         </el-col>
       </el-row>
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
            <el-form-item :label="$t('solicitud.direccion')">
              <el-input readonly name="direccion" v-model="solicitud.a.soli_direccion"></el-input>
            </el-form-item>
         </el-col>
          <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
            <el-form-item :label="$t('solicitud.barrio')">
              <el-select readonly style="width:100%;" filterable clearable ref="barrio" v-model="solicitud.a.barr_id" name="barrio" :placeholder="$t('barrio.select')">
                <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
                </el-option>   
              </el-select>
            </el-form-item>
         </el-col>
       </el-row>
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item :label="$t('solicitud.telefono')">
              <el-input readonly name="telefono" v-model="solicitud.a.soli_telefono"></el-input>
            </el-form-item>
         </el-col>
          <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item :label="$t('solicitud.email')">
              <el-input readonly name="email" v-model="solicitud.a.soli_email"></el-input>
            </el-form-item>
         </el-col>
       </el-row>
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
            <el-form-item :label="$t('solicitud.descripcion')">
              <el-input readonly type="textarea" :rows="5" name="telefono" v-model="solicitud.a.soli_solicitud"></el-input>
            </el-form-item>
         </el-col>
       </el-row>
       <el-collapse v-model="activeNames">
         <el-collapse-item v-if="solicitud.b.soli_estado >= 2" name="1" title="Supervisor">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fechasupervisor')">
                <el-date-picker readonly type="datetime" name="fechasupervisor" v-model="solicitud.b.soli_fechasupervisor"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
         </el-collapse-item>
         <el-collapse-item v-if="solicitud.b.soli_estado >= 3" name="1" title="Fecha Formato Reporte Técnico">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fecharte')">
                <el-date-picker readonly type="datetime" name="fecharte" v-model="solicitud.b.soli_fecharte"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
         </el-collapse-item>
         <el-collapse-item v-if="solicitud.b.soli_estado >= 3" name="1" title="Recibido En Almacén">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fechaalmacen')">
                <el-date-picker readonly type="datetime" name="fechaalmacen" v-model="solicitud.b.soli_fechaalmacen"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item prop="b.numerorte" :label="$t('solicitud.numerorte')">
                <el-input readonly ref="numerorte" name="numerorte" v-model="solicitud.b.soli_numerorte" @input="solicitud.b.numerorte = parseInt($event)"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
         </el-collapse-item>          
         <el-collapse-item v-if="solicitud.b.soli_estado >= 4" name="2" title="Informe">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fechavisita')">
                <el-date-picker readonly type="datetime" name="fechavisita" v-model="solicitud.b.soli_fechavisita"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fechainforme')">
                <el-date-picker readonly type="datetime" name="fechainforme" v-model="solicitud.b.soli_fechainforme"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-form-item :label="$t('solicitud.informe')">
                <el-input readonly type="textarea" :rows="5" name="informe" v-model="solicitud.a.soli_informe"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-collapse-item>  
         <el-collapse-item v-if="solicitud.b.soli_estado >= 5" name="3" title="Respuesta">
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
              <el-form-item :label="$t('solicitud.fecharespuesta')">
                <el-date-picker readonly type="datetime" name="fecharespuesta" v-model="solicitud.b.soli_fecharespuesta"></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="4">
            <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="24">
              <el-form-item :label="$t('solicitud.respuesta')">
                <el-input readonly type="textarea" :rows="5" name="respuesta" v-model="solicitud.a.soli_respuesta"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-collapse-item>         
       </el-collapse>
      </el-form>
    </el-main>
  </el-container>
</template>
<script>
import { saveSolicitud, getSolicitud } from '@/api/solicitud'
import { getBarriosEmpresa } from '@/api/barrio'

export default {
  data() {
    return {
      activeNames: ['1', '2', '3'],
      confirmacionGuardar: false,
      solicitud: {
        a: {
          soli_id: null,
          soti_id: null,
          soli_fecha: null,
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
      barrios: []
    }
  },
  mounted() {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.getData(this.$route.params.id)
    }).catch(error => {
      console.log(error)
    })
  },
  methods: {
    estado(soli_estado) {
      if (soli_estado === 1) {
        return 'PENDIENTE'
      } else if (soli_estado === 2) {
        return 'EN SUPERVISOR'
      } else if (soli_estado === 3) {
        return 'FORMATO REPORTE TECNICO'
      } else if (soli_estado === 4) {
        return 'EN ALMACEN'
      } else if (soli_estado === 5) {
        return 'EN INFORME TECNICO'
      } else if (soli_estado === 6) {
        return 'RESPONDIDA'
      } else {
        return 'NO ASIGNADO'
      }
    },
    estadoClass() {
      if (this.solicitud.b.soli_estado === 1) {
        return 'estado-1'
      } else if (this.solicitud.b.soli_estado === 2) {
        return 'estado-2'
      } else if (this.solicitud.b.soli_estado === 3) {
        return 'estado-3'
      } else if (this.solicitud.b.soli_estado === 4) {
        return 'estado-4'
      } else if (this.solicitud.b.soli_estado === 5) {
        return 'estado-5'
      } else if (this.solicitud.b.soli_estado === 6) {
        return 'estado-6'
      } else {
        return 'estado-n'
      }
    },
    getData(id) {
      getSolicitud(id).then(response => {
        if (response.status === 200) {
          this.solicitud = response.data
        } else {
          this.$alert('Solicitud No Encontrada', 'Ver Solicitud', {
            confirmButtonText: 'Continuar',
            callback: action => {
              this.limpiarAndBack()
            }
          })
        }
      }).catch(error => {
        this.$alert('Error Buscando la Solicitud: ' + error, 'Ver Solicitud', {
          confirmButtonText: 'Continuar',
          callback: action => {
            this.limpiarAndBack()
          }
        })
      })
    },
    aplicar() {
      this.confirmacionGuardar = false
      saveSolicitud(this.solicitud).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.solicitud.a.soli_fecha &&
          this.solicitud.a.soli_nombre &&
          this.solicitud.a.soli_direccion &&
          this.solicitud.a.barr_id &&
          this.solicitud.a.soli_telefono &&
          this.solicitud.a.soli_solicitud) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.solicitud = {
        a: {
          soli_id: null,
          soti_id: null,
          soli_fecha: null,
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
      }
      this.$router.push({ path: '/solicitud/solicitud' })
    },
    limpiar() {
      this.solicitud = {
        a: {
          soli_id: null,
          soti_id: null,
          soli_fecha: null,
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
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('solicitud.success'),
        message: this.$i18n.t('solicitud.created') + ' ' + this.solicitud.a.soli_radicado,
        type: 'success',
        onClose: this.limpiar()
      })
    },
    error(e) {
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