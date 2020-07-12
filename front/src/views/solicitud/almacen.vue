<template>
  <el-container>
    <el-header>
      <span>{{ $t('route.solicitudedit') }}</span>
    </el-header>
    <el-main>
     <el-form v-model="solicitud">
       <el-row :gutter="4">
          <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item :label="$t('solicitud.radicado')">
              <el-input ref="radicado" style="font-size: 32px;" name="radicado" v-model="solicitud.a.soli_radicado" @blur="getData()"></el-input>
            </el-form-item>
          </el-col>
          <el-col el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <el-form-item :label="$t('solicitud.estado')">
              <span :class="estadoClass()" name="estado">{{ solicitud.b.soli_estado }}.{{ estado(solicitud.b.soli_estado) }}</span>
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
              <el-input readonly name="nombre" v-model="solicitud.a.soli_nombre" ></el-input>
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
              <el-select disabled style="width:100%;" filterable clearable ref="barrio" v-model="solicitud.a.barr_id" name="barrio" :placeholder="$t('barrio.select')">
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
            <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
              <el-form-item :label="$t('solicitud.fechaalmacen')">
                <el-date-picker type="datetime" name="fechaalmacen" v-model="solicitud.b.soli_fechaalmacen"></el-date-picker>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
              <el-form-item prop="b.puntos" :label="$t('solicitud.puntos')">
                <el-input type="number" v-model="solicitud.b.soli_puntos" />
              </el-form-item>
            </el-col>            
            <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
              <el-form-item prop="b.numerorte" :label="$t('solicitud.numerorte')">
                <el-input ref="numerorte" name="numerorte" v-model="solicitud.b.soli_numerorte" @input="solicitud.b.numerorte = parseInt($event)"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
              <el-form-item :label="$t('solicitud.aprobado')">
                <el-checkbox v-model="solicitud.b.soli_aprobada" />
              </el-form-item>
            </el-col>            
            <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
              <el-form-item v-if="solicitud.b.soli_aprobada" :label="$t('solicitud.tipo_expansion.title')">
                <el-select clearable :title="$t('solicitud.tipo_expansion.select')" style="width: 80%" ref="tipo" v-model="solicitud.b.soli_tipoexpansion" name="tipo_expansion" :placeholder="$t('solicitud.tipo_expansion.select')">
                  <el-option v-for="te in tipos_expansion" :key="te.tiex_id" :label="te.tiex_descripcion" :value="te.tiex_id" >
                  </el-option>   
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
              <el-form-item v-if="solicitud.b.soli_aprobada" :label="$t('solicitud.numero_luminarias')">
                <el-input type="number" v-model="solicitud.b.soli_luminarias" @input="solicitud.b.soli_luminarias = parseInt($event)" />
              </el-form-item>
            </el-col>
          </el-row>
         </el-collapse-item>                 
         <el-collapse-item v-if="solicitud.b.soli_estado >= 4" name="2" title="Informe">
          <el-row :gutter="4">
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
         <el-collapse-item v-if="solicitud.b.soli_estado >= 5 || solicitud.b.soli_estado === 3" name="3" title="Respuesta">
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
       <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="confirmacionGuardar = true">Guardar</el-button>
      </el-form>
      <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
        <span style="font-size:20px;">Seguro de Guardar los Cambios?</span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="confirmacionGuardar = false">No</el-button>
          <el-button type="primary" @click="aplicar">Sí</el-button>
        </span>
      </el-dialog>      
    </el-main>
  </el-container>
</template>
<script>
import { asignarRteSolicitud, getSolicitudPorRadicado } from '@/api/solicitud'
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
      this.limpiar()
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
    getData() {
      getSolicitudPorRadicado(this.solicitud.a.soli_radicado).then(response => {
        if (response.status === 200) {
          this.solicitud = response.data
          if (this.solicitud.b.soli_estado !== 3) {
            this.$alert('No Puede Modificar Esta Solicitud. Radicado: ' + this.solicitud.a.soli_radicado, 'Editar Solicitud', {
              confirmButtonText: 'Continuar',
              callback: action => {
                this.limpiar()
              }
            })
          } else {
            this.solicitud.b.soli_fechaalmacen = new Date()
            // this.$refs['numerorte'].focus()
          }
        } else {
          this.$alert('Solicitud No Encontrada', 'Editar Solicitud', {
            confirmButtonText: 'Continuar',
            callback: action => {
              this.limpiar()
            }
          })
        }
      }).catch(() => {
        this.$alert('Solicitud No Encontrada', 'Editar Solicitud', {
          confirmButtonText: 'Continuar',
          callback: action => {
            this.limpiar()
          }
        })
      })
    },
    aplicar() {
      this.confirmacionGuardar = false
      asignarRteSolicitud(this.solicitud.a.soli_id, this.solicitud.b.soli_fechaalmacen.getTime(), this.solicitud.b.soli_numerorte).then(response => {
        if (response.status === 200) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.solicitud.b.soli_fechaalmacen &&
          this.solicitud.b.soli_numerorte) {
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
          soli_puntos: null,
          soli_tipoexpansion: null,
          soli_aprobada: true,
          soli_codigorespuesta: null,
          soli_luminarias: null,
          soli_estado: null,
          empr_id: null,
          usua_id: null
        }
      }
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
          soli_informe: null,
          soli_consecutivo: null
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
          soli_puntos: null,
          soli_tipoexpansion: null,
          soli_aprobada: true,
          soli_codigorespuesta: null,
          soli_luminarias: null,
          soli_estado: null,
          empr_id: null,
          usua_id: null
        }
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('solicitud.success'),
        message: this.$i18n.t('solicitud.updated') + ' ' + this.solicitud.a.soli_radicado,
        type: 'success'
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('solicitud.error'),
        message: this.$i18n.t('solicitud.notupdated') + ' ' + e
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
  background: #e6c23c;
}

.estado-5 {
  background: #89d34d;
}

.estado-6 {
  background: #12c23a;
}

.estado-n {
  background: #f56c6c;
}
</style>