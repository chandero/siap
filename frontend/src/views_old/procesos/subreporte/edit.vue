<template>
  <el-container>
      <el-header>
          <span>{{ $t('route.reporteedit') }} - Estado Actual: {{ estado() }}</span>
      </el-header>
      <el-main>
          <el-form ref="reporteForm" :model="reporte" :label-position="labelPosition">
              <el-collapse v-model="activePages" @change="handleActivePagesChange">
                <el-collapse-item name="1" :title="$t('reporte.general')" style="font-weight: bold;">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                            <el-form-item prop="repo_fecharecepcion" :label="$t('reporte.receptiondate')">
                                <span style="font-size: 24px;">{{ reporte.repo_fecharecepcion | moment('YYYY/MM/DD')}}</span>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                            <el-form-item prop="reti_id" :label="$t('reporte.type')">
                                <span style="font-size: 24px;">{{ reporte_tipo(reporte.reti_id) }}</span>
                            </el-form-item>
                        </el-col>
                        <el-col v-if="reporte.reti_id===2" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                          <el-form-item :label="$t('reporte.tipo_expansion.title')">
                            <el-form :model="adicional" :rules="adicional_rules" :label-position="labelPosition">
                              <el-form-item prop="repo_tipo_expansion">
                                <el-select autofocus clearable :title="$t('reporte.tipo_expansion.select')" style="width: 80%" ref="tipo" v-model="reporte.adicional.repo_tipo_expansion" name="tipo_expansion" :placeholder="$t('reporte.tipo_expansion.select')">
                                    <el-option v-for="te in tipos_expansion" :key="te.tiex_id" :label="te.tiex_descripcion" :value="te.tiex_id" >
                                    </el-option>
                                </el-select>
                              </el-form-item>
                            </el-form>
                          </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                          <el-form-item prop="repo_consecutivo" :label="$t('reporte.number')">
                            <span style="font-size: 30px;">{{ reporte.repo_consecutivo | fillZeros(6) }}</span>
                          </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                            <el-form-item prop="orig_id" :label="$t('reporte.origin')">
                                <el-select style="width:100%;" clearable ref="origin" v-model="reporte.orig_id" name="origen" :placeholder="$t('origen.select')"  @change="changeFocus('code')">
                                    <el-option v-for="origen in origenes" :key="origen.orig_id" :label="origen.orig_descripcion" :value="origen.orig_id" :disabled="origen.orig_id != reporte.orig_id" >
                                    </el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                            <el-form-item prop="repo_codigo" :label="$t('reporte.code')">
                                <el-input readonly ref="code" v-model="reporte.adicional.repo_codigo" @input="reporte.adicional.repo_codigo = $event.toUpperCase()" @keyup.enter.native="changeFocus('apoyo')"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                            <el-form-item prop="repo_apoyo" :label="$t('reporte.apoyo')">
                                <el-input readonly ref="apoyo" v-model="reporte.adicional.repo_apoyo" @input="reporte.adicional.repo_apoyo = $event.toUpperCase()" @keyup.enter.native="changeFocus('nombre')" ></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="repo_nombre" :label="$t('reporte.name')">
                                <el-input readonly ref="nombre" v-model="reporte.repo_nombre" @input="reporte.repo_nombre = $event.toUpperCase()" @keyup.enter.native="changeFocus('direccion')"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="repo_direccion" :label="$t('reporte.address')">
                             <el-input readonly ref="direccion" v-model="reporte.repo_direccion" @input="reporte.repo_direccion = $event.toUpperCase()" @keyup.enter.native="changeFocus('barrio')">
                             </el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="barr_id" :label="$t('reporte.neighborhood')">
                             <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="reporte.barr_id" name="barrio" :placeholder="$t('barrio.select')"  @change="changeFocus('tiba')">
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id">
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                            <el-form-item prop="tiba_id" :label="$t('reporte.sector')">
                             <el-select  style="width:100%;" filterable clearable ref="tiba" v-model="reporte.tiba_id" name="tiba" :placeholder="$t('tipobarrio.select')"  @change="changeFocus('telefono')">
                              <el-option v-for="tiba in tiposbarrio" :key="tiba.tiba_id" :label="tiba.tiba_descripcion" :value="tiba.tiba_id">
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                            <el-form-item prop="repo_telefono" :label="$t('reporte.phone')">
                                <el-input readonly ref="telefono" v-model="reporte.repo_telefono" @keyup.enter.native="changeFocus('descripcion')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="acti_id" :label="$t('reporte.activity')">
                             <el-select style="width:90%;" filterable clearable ref="tiba" v-model="reporte.adicional.acti_id" name="actividad" :placeholder="$t('actividad.select')"  @change="changeFocus('descripcion')">
                              <el-option v-for="acti in actividades" :key="acti.acti_id" :label="acti.acti_descripcion" :value="acti.acti_id">
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
                                <el-button disabled slot="reference" type="primary" size="mini" circle icon="el-icon-plus" title="Adicionar Nuevo Daño"/>
                             </el-popover>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="repo_descripcion" :label="$t('reporte.description')">
                                <el-input readonly ref="descripcion" v-model="reporte.repo_descripcion" type="textarea" :rows="2" @input="reporte.repo_descripcion = $event.toUpperCase()"  @keyup.enter.native="changeFocus('submit')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-collapse-item>
                  <el-collapse-item name="2" :title="$t('reporte.inform')">
                    <el-row :gutter="4">
                        <el-col :span="8">
                            <el-form-item :label="$t('reporte.solutiondate')">
                                <el-date-picker v-model="reporte.repo_fechasolucion"></el-date-picker>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item :label="$t('reporte.timestart')">
                                <el-time-select v-model="reporte.repo_horainicio"
                                   :picker-options="{
                                        start: '07:00',
                                        step: '00:15',
                                        end: '19:00',
                                   }"
                                >
                                </el-time-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item :label="$t('reporte.timeend')">
                                <el-time-select v-model="reporte.repo_horafin"
                                   :picker-options="{
                                        start: '07:00',
                                        step: '00:15',
                                        end: '19:00',
                                        minTime: reporte.repo_horainicio
                                   }"
                                >
                                </el-time-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :span="24">
                            <el-form-item :label="$t('reporte.tecnicalreport')">
                                <el-input type="textarea" :rows="3" ref="tecnico" v-model="reporte.repo_reportetecnico" @input="reporte.repo_reportetecnico = $event.toUpperCase()" @keyup.enter.native="changeFocus('evento.aap_id')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                      <el-col :span="24">
                        <el-form-item :label="$t('reporte.environment')">
                          <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">Marcar todos</el-checkbox>
                          <div style="margin: 15px 0;"></div>
                          <el-checkbox-group v-model="reporte.meams" @change="handleReporteMeamChange">
                            <el-checkbox border v-for="meam in medioambiente" :label="meam.meam_id" :key="meam.meam_id">{{ meam.meam_descripcion }}</el-checkbox>
                          </el-checkbox-group>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-row>
                     <el-col :span="24">
                      <el-collapse  v-model="activePages2">
                       <el-collapse-item v-if="conDirecciones" name="2-1" :title="$t('reporte.addresses')">
                        <el-row :gutter="4" class="hidden-sm-and-down">
                          <el-col :md="1" :lg="1" :xl="1">
                            <span style="font-weight: bold;">No.</span>
                          </el-col>
                          <el-col :md="3" :lg="3" :xl="3">
                            <span style="font-weight: bold;">Código de la Luminaria</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="11" :lg="11" :xl="11">
                            <span style="font-weight: bold;">Dirección de la Luminaria Expandida o Reubicada</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
                            <span style="font-weight: bold;">Barrio o Vereda</span>
                          </el-col>
                        </el-row>
                        <div >
                          <el-form :disabled="reporte.rees_id === 0" v-for="(direccion, id) in reporte.direcciones" v-bind:key="direccion.even_id" :model="direccion" :ref="'dirform' + direccion.even_id">
                          <el-row :gutter="4">
                            <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                              <span style="font-weight: bold;">No.</span>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{ direccion.even_id }}</el-col>
                            <el-col class="hidden-md-and-up" :xs="9" :sm="9">
                              <span style="font-weight: bold;">Código de la Luminaria</span>
                            </el-col>
                            <el-col :xs="13" :sm="13" :md="3" :lg="3" :xl="3">
                                <el-form-item prop="aap_id">
                                  <div style="display: table;">
                                   <el-input type="number" class="sinpadding" style="display: table-cell;" v-model="direccion.aap_id" @input="direccion.aap_id = parseInt($event,10)" @blur="validateAap(direccion.aap_id, id)">
                                   </el-input>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Dirección de la Luminaria Expandida o Reubicada</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="11" :lg="11" :xl="11">
                             <el-form-item prop="even_direccion">
                               <el-input v-validate="'required'" :name="'even_direccion_'+id" v-model="direccion.even_direccion" @input="direccion.even_direccion = $event.toUpperCase()" ></el-input>
                               <span>{{ errors.first('even_direccion_'+id)}}</span>
                             </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Barrio o Vereda</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="barr_id">
                             <el-select style="width:100%;" filterable clearable v-model="direccion.barr_id" name="barrio" :placeholder="$t('barrio.select')" >
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id">
                              </el-option>
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                              <el-button size="mini" type="danger" circle icon="el-icon-minus" title="Quitar Fila" @click="reporte.direcciones.splice(id, 1)"></el-button>
                            </el-col>
                         </el-row>
                         </el-form>
                         <el-row class="hidden-md-and-up">
                          <el-col style="border-bottom: 1px dotted #000;"></el-col>
                         </el-row>
                        </div>
                         <el-row>
                           <el-col :span="24">
                             <el-button  style="display: table-cell;" type="info" size="mini" circle icon="el-icon-plus" title="Adicionar Nueva Dirección" @click="onAddAddress()" />
                           </el-col>
                         </el-row>
                      </el-collapse-item>
                       <el-collapse-item name="2-2" :title="$t('reporte.events')">
                        <el-row :gutter="4" class="hidden-sm-and-down">
                          <el-col :md="1" :lg="1" :xl="1">
                            <span style="font-weight: bold;">No.</span>
                          </el-col>
                          <el-col :md="3" :lg="3" :xl="3">
                            <span style="font-weight: bold;">Código de la Luminaria</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="11" :lg="11" :xl="11">
                            <span style="font-weight: bold;">Nombre del Material</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                            <span style="font-weight: bold;">Código Material Retirado</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                            <span style="font-weight: bold;">Cantidad Material Retirado</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                            <span style="font-weight: bold;">Código Material Instalado</span>
                          </el-col>
                          <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                            <span style="font-weight: bold;">Cantidad Material Instalado</span>
                          </el-col>
                        </el-row>
                        <div v-for="(evento, id) in reporte.eventos" v-bind:key="evento.even_id">
                          <el-form :disabled="reporte.rees_id === 0" :model="evento" ref="eventform">
                          <el-row :gutter="4">
                            <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                              <span style="font-weight: bold;">No.</span>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{ evento.even_id }}</el-col>
                            <el-col class="hidden-md-and-up" :xs="9" :sm="9">
                              <span style="font-weight: bold;">Código de la Luminaria</span>
                            </el-col>
                            <el-col :xs="13" :sm="13" :md="3" :lg="3" :xl="3">
                                <el-form-item>
                                  <div style="display: table;">
                                    <el-input type="number" class="sinpadding" style="display: table-cell;" v-model="evento.aap_id" @input="evento.aap_id = parseInt($event,10)"  @blur="validateAapEvento(evento.aap_id, id)">
                                    </el-input>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Nombre del Material</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="11" :lg="11" :xl="11">
                             <el-form-item>
                                <el-select clearable filterable v-model="evento.elem_id" :placeholder="$t('elemento.select')" style="width: 100%;">
                                    <el-option v-for="elemento in elementos" :key="elemento.elem_id" :label="elemento.elem_descripcion" :value="elemento.elem_id" >
                                    </el-option>
                                </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Código Material Retirado</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item>
                                    <el-input class="sinpadding" v-model="evento.even_codigo_retirado"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Cantidad Material Retirado</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item>
                                    <el-input class="sinpadding" v-model="evento.even_cantidad_retirado" @input="evento.even_cantidad_retirado = parseFloat($event)"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Código Material Instalado</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item>
                                    <el-input class="sinpadding" v-model="evento.even_codigo_instalado"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Cantidad Material Instalado</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item>
                                    <el-input class="sinpadding" v-model="evento.even_cantidad_instalado" @input="evento.even_cantidad_instalado = parseFloat($event)" ></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                              <el-button size="mini" type="danger" circle icon="el-icon-minus" title="Quitar Fila" @click="reporte.eventos.splice(id, 1)"></el-button>
                            </el-col>
                         </el-row>
                         </el-form>
                         <el-row class="hidden-md-and-up">
                          <el-col style="border-bottom: 1px dotted #000;"></el-col>
                         </el-row>
                        </div>
                         <el-row>
                           <el-col :span="24">
                             <el-button  style="display: table-cell;" type="info" size="mini" circle icon="el-icon-plus" title="Adicionar Nueva Fila" @click="onAddEvent()" />
                           </el-col>
                         </el-row>
                      </el-collapse-item>
                    </el-collapse>
                   </el-col>
                  </el-row>
                </el-collapse-item>
              </el-collapse>
          </el-form>
      </el-main>
     <el-footer>
      <el-button v-if="canSave" ref="submit" :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="confirmacionGuardar = !confirmacionGuardar">Guardar Reporte</el-button>
      <el-button v-if="canPrint" ref="print" size="medium" type="success" icon="el-icon-printer" @click="imprimir">Imprimir</el-button>
     </el-footer>
     <el-dialog
      title="Atención"
      :visible.sync="centerDialogVisible"
      center>
      <span style="font-size: 20px;">El Código de Luminaria <b>{{ aap.aap_id }}</b>, No Existe, Por Favor Verifique.</span>
      <span slot="footer" class="dialog-footer">
        <el-button v-if="reporte.reti_id === 2" type="primary" @click="centerDialogVisible = false; showAapModal=!showAapModal">Crear Nuevo Código</el-button>
        <el-button type="primary" @click="centerDialogVisible = false">Cerrar</el-button>
      </span>
     </el-dialog>
      <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
          <span style="font-size:20px;">Seguro de Guardar las Modificaciones al Reporte?</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="confirmacionGuardar = false">No</el-button>
            <el-button type="primary" @click="aplicar">Sí</el-button>
          </span>
      </el-dialog>
  </el-container>
</template>
<script>
import { getActividades } from '@/api/actividad'
import { getOrigenes } from '@/api/origen'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getReporte, updateReporte, getTipos, getEstados } from '@/api/reporte'
import { getAcciones } from '@/api/accion'
import { getElementos } from '@/api/elemento'
import { getAap } from '@/api/aap'
import { getMedioambiente } from '@/api/medioambiente'
// component

// import { inspect } from 'util'

export default {
  data () {
    return {
      labelPosition: 'top',
      showAapModal: false,
      activePages: ['1', '2'],
      activePages2: ['2-1', '2-2'],
      nopopover: false,
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      reporte_previo: null,
      reporte: {
        reti_id: null,
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
        rees_id: 3,
        orig_id: null,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        meams: [],
        eventos: [],
        direcciones: [],
        adicional: []
      },
      evento: {
        even_codigo_instalado: null,
        even_codigo_retirado: null,
        even_cantidad_instalado: 1,
        even_cantidad_retirado: 1,
        even_estado: 1,
        aap_id: null,
        repo_id: null,
        elem_id: null,
        elem_descripcion: null,
        empr_id: 0,
        usua_id: 0,
        even_id: null
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
        repo_subreporte: null,
        repo_subid: null,
        repo_email: null,
        acti_id: 0,
        repo_codigo: null,
        repo_apoyo: null
      },
      adicional_rules: {
        repo_tipo_expansion: [
          { required: false, message: 'Debe Seleccionar el Tipo de Expansión', trigger: 'change' }
        ]
      },
      rules: {
        repo_fecharecepcion: [
          { type: 'date', required: true, message: 'Debe diligencia la Fecha de Recepción del Reporte', trigger: 'change' }
        ],
        reti_id: [
          { required: true, message: 'Debe Seleccionar el Tipo de Reporte', trigger: 'change' }
        ],
        orig_id: [
          { required: true, message: 'Debe Seleccionar el Origen del Reporte', trigger: 'change' }
        ],
        acti_id: [
          { required: true, message: 'Debe Seleccionar el Tipo de Daño', trigger: 'change' }
        ],
        repo_nombre: [
          { required: true, message: 'Debe Digitar el Nombre de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        repo_direccion: [
          { required: true, message: 'Debe Digitar la dirección del daño o actividad', trigger: 'blur' }
        ],
        repo_telefono: [
          { required: true, message: 'Debe Digitar el Teléfono de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        barr_id: [
          { required: true, message: 'Debe Seleccionar el Barrio del Daño o Actividad', trigger: 'change' }
        ],
        tiba_id: [
          { required: true, message: 'Debe Seleccionar el Tipo de Sector del Daño o Actividad', trigger: 'blur' }
        ]
      },
      danhorules: {
        acti_descripcion: [
          { required: true, message: 'Debe Diligenciar la Descripción del nuevo Daño', trigger: 'blur' }
        ]
      },
      timeOptions: {
        start: '07:00',
        step: '00:15',
        end: '23:59'
      },
      origenes: [],
      barrios: [],
      barrios_lista: [],
      acciones: [],
      elementos: [],
      medioambiente: [],
      medioambiente_keys: [],
      meams: [],
      estados: [],
      checkAll: false,
      isIndeterminate: false,
      aap: { aap_id: null },
      tipos: [],
      tiposbarrio: [],
      actividades: [],
      dialogonuevodanhovisible: false,
      actividad: {
        acti_id: 0,
        acti_descripcion: null,
        acti_estado: 1,
        usua_id: 0
      },
      centerDialogVisible: false,
      dirrules: [],
      eventrules: [],
      conDirecciones: false,
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
  timers: {
    autosave: { name: 'autosave', time: 10000, autostart: true, repeat: true }
  },
  methods: {
    autosave () {
      /*
      var valido = true
      valido = this.validatForm('reporteForm')
      if (!valido) {
        return false
      }
      this.reporte.rees_id = 1
      updateReporte(this.reporte)
        .then(response => {
          if (response.status === 200) {
          }
        })
        .catch(error => {
          this.error(error)
        })
      */
      localStorage.setItem('currEditRep', JSON.stringify(this.reporte))
    },
    changeFocus (next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange (val) {
    },
    handleDelete (index, row) {
      this.reporte.eventos.splice(index, 1)
      this.$refs['evento.aap_id'].focus()
    },
    handleCheckAllChange (val) {
      console.log('checkAll: ' + val)
      this.reporte.meams = val ? this.medioambiente_keys : []
      this.isIndeterminate = false
    },
    handleReporteMeamChange (value) {
      console.log('value: ' + value)
      const meamCount = value.length
      this.checkAll = meamCount === this.medioambiente_keys.length
      this.isIndeterminate = meamCount > 0 && meamCount < this.medioambiente_keys.length
    },
    validate () {
      if (this.reporte.repo_fechasolucion && this.reporte.repo_horainicio && this.reporte.repo_horafin) {
        return true
      } else {
        return false
      }
    },
    validateAap (aap_id, id) {
      if (aap_id) {
        this.aap.aap_id = aap_id
        getAap(aap_id).then(response => {
          this.aap = response.data
          if (!this.aap) {
            this.existe = false
            this.centerDialogVisible = true
          }
        }).catch(() => {
          this.existe = false
          this.centerDialogVisible = true
        })
      }
    },
    validateAapEvento (aap_id, id) {
      if (aap_id) {
        this.aap.aap_id = aap_id
        getAap(aap_id).then(response => {
          this.aap = response.data
          if (!this.aap) {
            this.existe = false
            this.centerDialogVisible = true
          }
        }).catch(() => {
          this.existe = false
          this.centerDialogVisible = true
        })
      }
    },
    aplicar () {
      var valido = true
      this.confirmacionGuardar = false
      valido = this.validatForm('reporteForm')
      if (!valido) {
        return false
      }
      for (var i = 0; i < this.reporte.direcciones.length; i++) {
        if (!valido) {
          return false
        }
      }
      for (i = 0; i < this.reporte.eventos.length; i++) {
        if (!valido) {
          return false
        }
      }
      this.reporte.rees_id = 3
      updateReporte(this.reporte)
        .then(response => {
          if (response.status === 200) {
            this.success()
          }
        })
        .catch(error => {
          this.error(error)
        })
    },
    validatForm (form) {
      console.log('validando form: ' + form)
      var result = false
      this.$refs[form].validate((valid) => {
        if (valid) {
          result = true
        } else {
          result = false
        }
      })
      console.log('resultado validacion: ' + result)
      return result
    },
    imprimir () {
    },
    success () {
      this.$notify({
        title: this.$i18n.t('reporte.success'),
        message:
          this.$i18n.t('reporte.updated') +
          ' ' +
          this.reporte.repo_consecutivo,
        type: 'success'
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('reporte.error'),
        message: this.$i18n.t('reporte.notupdated') + ' ' + e
      })
    },
    onAddEvent () {
      var evento = {
        even_fecha: null,
        even_codigo_instalado: null,
        even_codigo_retirado: null,
        even_cantidad_instalado: 1.00,
        even_cantidad_retirado: 1.00,
        even_estado: 1,
        aap_id: null,
        repo_id: this.reporte.repo_id,
        elem_id: null,
        empr_id: 0,
        usua_id: 0,
        even_id: null
      }
      this.reporte.eventos.push(evento)
    },
    onAddAddress () {
      var direccion = {
        repo_id: this.reporte.repo_id,
        aap_id: null,
        even_direccion: null,
        barr_id: null,
        even_id: null,
        even_direccion_anterior: null,
        barr_id_anterior: null
      }
      this.reporte.direcciones.push(direccion)
    },
    limpiarAndBack () {
      this.obtenerReporte()
    },
    elemento (elem_id) {
      if (elem_id === null) {
        return ''
      } else {
        return this.elementos.find(o => o.elem_id === elem_id, { elem_descripcion: null }).elem_descripcion
      }
    },
    reporte_tipo (reti_id) {
      if (reti_id === null) {
        return ''
      } else {
        return this.tipos.find(o => o.reti_id === reti_id, { reti_descripcion: 'INDEFINIDO' }).reti_descripcion
      }
    },
    estado () {
      if (this.reporte && this.reporte.rees_id !== null) {
        var rees_id = this.reporte.rees_id
        if (rees_id === null || rees_id === undefined) {
          return ''
        } else {
          if (this.estados && this.estados.length > 0) {
            return this.estados.find(o => o.rees_id === rees_id, { rees_descripcion: 'INDEFINIDO' }).rees_descripcion
          } else {
            return 'INDEFINIDO'
          }
        }
      }
    },
    accion (acci_id) {
      if (acci_id === null) {
        return ''
      } else {
        return this.acciones.find(o => o.acci_id === acci_id, { acci_descripcion: null }).acci_descripcion
      }
    },
    obtenerReporte () {
      getReporte(this.$route.params.id).then(response => {
        this.reporte_previo = response.data
        if (this.reporte_previo.adicional === null) {
          this.adicional = {
            repo_id: this.reporte_previo.repo_id,
            repo_fechadigitacion: new Date(),
            repo_modificado: new Date(),
            repo_tipo_expansion: null,
            repo_luminaria: null,
            repo_redes: null,
            repo_poste: null,
            repo_subreporte: null,
            repo_subid: null,
            acti_id: 0,
            repo_codigo: null,
            repo_apoyo: null
          }
        } else {
          this.reporte_previo.adicional.repo_modificado = new Date()
          this.adicional = this.reporte_previo.adicional
          this.actividad.acti_id = this.adicional.acti_id
        }
        if (this.reporte_previo.reti_id === 2 || this.reporte_previo.reti_id === 3) {
          this.conDirecciones = true
        } else {
          this.conDirecciones = false
        }
        this.validarConsecutivo()
        this.cargarEventos()
      }).catch(error => {
        console.log('getReporte: ' + error)
      })
    },
    validarConsecutivo () {
      var consecutivo = 1
      for (var i = 0; i < this.reporte_previo.eventos.length; i++) {
        this.reporte_previo.eventos[i].even_id = consecutivo
        consecutivo++
      }
    },
    cargarEventos () {
      var even_length = this.reporte_previo.eventos.length
      var dire_length = this.reporte_previo.direcciones.length
      for (var i = 1; i <= 10; i++) {
        var evento = {
          even_fecha: null,
          even_codigo_instalado: null,
          even_codigo_retirado: null,
          even_cantidad_instalado: 1.00,
          even_cantidad_retirado: 1.00,
          even_estado: 1,
          aap_id: null,
          repo_id: this.reporte_previo.repo_id,
          elem_id: null,
          empr_id: 0,
          usua_id: 0,
          even_id: even_length + i
        }
        var direccion = {
          repo_id: this.reporte_previo.repo_id,
          aap_id: null,
          even_direccion: null,
          barr_id: null,
          even_id: dire_length + i
        }
        this.reporte_previo.eventos.push(evento)
        this.reporte_previo.direcciones.push(direccion)
        this.dirrules.push({
          even_direccion: [
            { required: false, message: 'Debe Diligenciar la Direccion de la Luminaria', trigger: 'blur' }
          ],
          barr_id: [
            { required: false, message: 'Debe Seleccionar el Barrio de la Dirección de la Luminaria', trigger: 'change' }
          ]
        }
        )
        this.eventrules.push({
          elem_id: [
            { required: false, message: 'Debe Seleccionar el Material', trigger: 'change' }
          ]
        })
      }
      // validar si existe un reporte previo
      var stringReporteAnterior = localStorage.getItem('currEditRep')
      if (stringReporteAnterior !== undefined && stringReporteAnterior !== '') {
        var currEditRep = JSON.parse(stringReporteAnterior)
        if (currEditRep.repo_id === this.reporte_previo.repo_id) {
          this.reporte_previo = currEditRep
        }
      }
      this.reporte = this.reporte_previo
    }
  },
  created () {
    getOrigenes().then(response => {
      this.origenes = response.data
      getBarriosEmpresa().then(response => {
        this.barrios = response.data
        this.barrios_lista = response.data
        getActividades().then(response => {
          this.actividades = response.data
          getAcciones().then(response => {
            this.acciones = response.data
            getMedioambiente().then(response => {
              this.medioambiente = response.data
              this.medioambiente.forEach((o) => {
                this.medioambiente_keys.push(o.meam_id)
              })
              getEstados().then(response => {
                this.estados = response.data
                getTipos().then(response => {
                  this.tipos = response.data
                  this.tipos_lista = response.data
                  getTiposBarrio().then(response => {
                    this.tiposbarrio = response.data
                    getElementos().then(response => {
                      this.elementos = response.data
                      this.obtenerReporte()
                    }).catch(error => {
                      console.log('getElementos: ' + error)
                    })
                  }).catch(error => {
                    console.log('getTiposBarrio: ' + error)
                  })
                }).catch(error => {
                  console.log('getTipos: ' + error)
                })
              }).catch(error => {
                console.log('getEstados: ' + error)
              })
            }).catch(error => {
              console.log('getMedioambiente: ' + error)
            })
          }).catch(error => {
            console.log('getAcciones: ' + error)
          })
        }).catch(error => {
          console.log('Actividades: ' + error)
        })
      }).catch(error => {
        console.log(error)
      })
    }).catch(error => {
      console.log('Origenes: ' + error)
    })
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
div.el-input input {
  padding: 0 5px;
}
.el-collapse-item__header {
  font-weight: bold;
}
</style>
