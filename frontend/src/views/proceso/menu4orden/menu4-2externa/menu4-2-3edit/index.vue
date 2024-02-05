<template>
  <el-container>
      <el-header>
          <span>{{ $t('orhi.orhi_edit') }} - Estado Actual: {{ estado() }}</span>
      </el-header>
      <el-main>
          <el-form ref="orhiForm" :model="obra" :label-position="labelPosition">
              <el-collapse v-model="activePages" @change="handleActivePagesChange">
                <el-collapse-item name="1" :title="$t('obra.general')" style="font-weight: bold;">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                            <el-form-item prop="obra_fecharecepcion" :label="$t('obra.receptiondate')">
                                <span style="font-size: 24px;">{{ obra.obra_fecharecepcion | moment('YYYY/MM/DD HH:MM')}}</span>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                            <el-form-item prop="reti_id" :label="$t('obra.type')">
                                <span style="font-size: 24px;">{{ obra_tipo(orhi.reti_id) }}</span>
                            </el-form-item>
                        </el-col>
                        <!--
                        <el-col v-if="obra.reti_id===2" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                          <el-form-item :label="$t('obra.tipo_expansion.title')">
                            <el-form :model="adicional" :label-position="labelPosition">
                              <el-form-item prop="obra_tipo_expansion">
                                <el-select autofocus clearable :title="$t('obra.tipo_expansion.select')" style="width: 80%" ref="tipo" v-model="obra.adicional.obra_tipo_expansion" name="tipo_expansion" :placeholder="$t('obra.tipo_expansion.select')">
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
                        <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                          <el-form-item prop="muot_id" :label="$t('obra.mot')">
                            <el-input type="number" style="font-size: 30px;" v-model="obra.muot_id" @input="obra.muot_id = parseInt($event, 10)"></el-input>
                          </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                          <template v-if="ortr_id_state">
                            <el-form-item
                              prop="obra.ortr_id"
                              :label="$t('obra.ot')"
                              required
                            >
                              <el-select
                                style="width: 100%"
                                ref="ortr_id"
                                v-model="obra.ortr_id"
                                name="ortr_id"
                                filterable
                                :placeholder="$t('ortr.select')"
                                @change="changeFocus('nombre')"
                              >
                                <el-option
                                  v-for="ot in ordenestrabajo"
                                  :key="ot.ortr_id"
                                  :label="ordenes(ot.ortr_id)"
                                  :value="ot.ortr_id"
                                ></el-option>
                              </el-select>
                              <el-button
                                circle
                                size="mini"
                                icon="el-icon-check"
                                type="success"
                                @click="confirmOrdenTrabajo(); ortr_id_state = false;"
                              />
                              <el-button
                                class="cancel-btn"
                                size="mini"
                                icon="el-icon-close"
                                type="warning"
                                circle
                                @click="obra.ortr_id = ortr_id; ortr_id_state = false;"
                              />
                            </el-form-item>
                          </template>
                          <template v-else>
                            <el-form-item :label="$t('obra.ot')">
                              <span style="400 13.3333px Arial;">{{
                                ordenes(obra.ortr_id)
                              }}</span>
                              <el-button
                                circle
                                size="mini"
                                icon="el-icon-edit"
                                style="border-style: hidden"
                                @click="ortr_id_state = !ortr_id_state"
                              />
                            </el-form-item>
                          </template>
                      </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="obra_nombre" :label="$t('obra.name')">
                                <el-input readonly ref="nombre" v-model="obra.obra_nombre" @input="obra.obra_nombre = $event.toUpperCase()" @keyup.enter.native="changeFocus('direccion')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
                            <el-form-item prop="orig_id" :label="$t('obra.origin')">
                                <el-select style="width:100%;" clearable ref="origin" v-model="obra.orig_id" name="origen" :placeholder="$t('origen.select')"  @change="changeFocus('code')">
                                    <el-option v-for="origen in origenes" :key="origen.orig_id" :label="origen.orig_descripcion" :value="origen.orig_id" :disabled="origen.orig_id != obra.orig_id" >
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
                                <el-input ref="soliticitante" v-model="obra.obra_solicitante" @input="adicional.obra_solicitante = $event.toUpperCase()"></el-input>
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
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="obra_nombre" :label="$t('obra.name')">
                                <el-input readonly ref="nombre" v-model="obra.obra_nombre" @input="obra.obra_nombre = $event.toUpperCase()" @keyup.enter.native="changeFocus('direccion')"></el-input>
                            </el-form-item>
                        </el-col>
                        -->
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="obra_direccion" :label="$t('obra.address')">
                             <el-input readonly ref="direccion" v-model="obra.obra_direccion" @input="obra.obra_direccion = $event.toUpperCase()" @keyup.enter.native="changeFocus('barrio')">
                             </el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="barr_id" :label="$t('obra.neighborhood')">
                             <el-select  style="width:100%;" filterable clearable ref="barrio" v-model="obra.barr_id" name="barrio" :placeholder="$t('barrio.select')"  @change="changeFocus('tiba')">
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id">
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                            <el-form-item prop="tiba_id" :label="$t('obra.sector')">
                             <el-select  style="width:100%;" filterable clearable ref="tiba" v-model="obra.tiba_id" name="tiba" :placeholder="$t('tipobarrio.select')"  @change="changeFocus('telefono')">
                              <el-option v-for="tiba in tiposbarrio" :key="tiba.tiba_id" :label="tiba.tiba_descripcion" :value="tiba.tiba_id">
                              </el-option>
                             </el-select>
                            </el-form-item>
                        </el-col>
                        <!--
                        <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                            <el-form-item prop="obra_telefono" :label="$t('obra.phone')">
                                <el-input readonly ref="telefono" v-model="obra.obra_telefono" @keyup.enter.native="changeFocus('descripcion')"></el-input>
                            </el-form-item>
                        </el-col>
                        -->
                    </el-row>
                    <el-row :gutter="4">
                        <!--
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="acti_id" :label="$t('obra.activity')">
                             <el-select style="width:90%;" filterable clearable ref="tiba" v-model="obra.adicional.acti_id" name="actividad" :placeholder="$t('actividad.select')"  @change="changeFocus('descripcion')">
                              <el-option v-for="acti in actividades" :key="acti.acti_id" :label="acti.acti_descripcion" :value="acti.acti_id">
                              </el-option>
                             </el-select>
                             <el-popover
                              placement="top"
                              width="300"
                              trigger="click"
                              v-model="dialogonuevodanhovisible">
                                <el-form ref="danho" :model="actividad">
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
                        -->
                        <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                            <el-form-item prop="obra_descripcion" :label="$t('obra.description')">
                                <el-input readonly ref="descripcion" v-model="obra.obra_descripcion" type="textarea" :rows="2" @input="obra.obra_descripcion = $event.toUpperCase()"  @keyup.enter.native="changeFocus('submit')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-collapse-item>
                  <el-collapse-item name="2" :title="$t('obra.inform')">
                    <el-row :gutter="4">
                        <el-col :span="8">
                            <el-form-item :label="$t('obra.solutiondate')">
                                <el-date-picker v-model="obra.obra_fechasolucion"></el-date-picker>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item :label="$t('obra.timestart')">
                                <el-time-select v-model="obra.obra_horainicio"
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
                            <el-form-item :label="$t('obra.timeend')">
                                <el-time-select v-model="obra.obra_horafin"
                                   :picker-options="{
                                        start: '07:00',
                                        step: '00:15',
                                        end: '19:00',
                                        minTime: obra.obra_horainicio
                                   }"
                                >
                                </el-time-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :span="24">
                            <el-form-item :label="$t('obra.tecnicalreport')">
                                <el-input type="textarea" :rows="3" ref="tecnico" v-model="obra.obra_obratecnico" @input="obra.obra_obratecnico = $event.toUpperCase()" @keyup.enter.native="changeFocus('evento.aap_id')"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                      <el-col :span="24">
                        <el-form-item :label="$t('obra.environment')">
                          <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">Marcar todos</el-checkbox>
                          <div style="margin: 15px 0;"></div>
                          <el-checkbox-group v-model="obra.meams" @change="handleObraMeamChange">
                            <el-checkbox border v-for="meam in medioambiente" :label="meam.meam_id" :key="meam.meam_id">{{ meam.meam_descripcion }}</el-checkbox>
                          </el-checkbox-group>
                        </el-form-item>
                      </el-col>
                    </el-row>
                    </el-collapse-item>
                    <el-collapse-item v-if="conDirecciones" name="3" title="DATOS LUMINARIAS">
                        <div >
                          <el-form :disabled="obra.rees_id === 0" v-for="(direccion, id) in obra.direcciones" v-bind:key="direccion.even_id" :model="direccion" :ref="'dirform' + direccion.even_id" label-position="left">
                          <el-row :gutter="4">
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                              <span style="font-weight: bold;">No.</span>
                            </el-col>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{ direccion.even_id }}</el-col>
                            <el-col :xs="13" :sm="13" :md="3" :lg="3" :xl="3">
                                <el-form-item prop="aap_id" label="Código Luminaria">
                                  <div style="display: table;">
                                   <el-input :disabled="direccion.even_estado === 2 || direccion.even_estado > 7" type="number" class="sinpadding" style="display: table-cell;" v-model="direccion.aap_id" @input="direccion.aap_id = parseInt($event,10)" @blur="validateAap(direccion, id)">
                                   </el-input>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 3" :xs="16" :sm="16" :md="11" :lg="11" :xl="11">
                             <el-form-item prop="even_direccion" label="Nueva Dirección">
                               <el-input :disabled="direccion.even_estado > 7" v-validate="'required'" :name="'even_direccion_'+id" v-model="direccion.even_direccion" @input="direccion.even_direccion = $event.toUpperCase()" ></el-input>
                               <span>{{ errors.first('even_direccion_'+id)}}</span>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 3" :xs="16" :sm="16" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="barr_id" label="Barrio/Vereda">
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.barr_id" name="barrio" :placeholder="$t('barrio.select')" >
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id">
                              </el-option>
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="dato.aatc_id" label="Tipo Luminaria">
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.dato.aatc_id" name="carcasa" :placeholder="$t('cover.select')" >
                              <el-option v-for="carcasa in carcasas" :key="carcasa.aatc_id" :label="carcasa.aatc_descripcion" :value="carcasa.aatc_id">
                              </el-option>
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="dato.aama_id" label="Marca">
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.dato.aama_id" name="marca" :placeholder="$t('brand.select')" >
                              <el-option v-for="marca in marcas" :key="marca.aama_id" :label="marca.aama_descripcion" :value="marca.aama_id">
                              </el-option>
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="dato.aamo_id" label="Modelo">
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.dato.aamo_id" name="modelo" :placeholder="$t('model.select')" >
                              <el-option v-for="modelo in modelos" :key="modelo.aamo_id" :label="modelo.aamo_descripcion" :value="modelo.aamo_id">
                              </el-option>
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                             <el-form-item prop="dato.aap_tecnologia" label="Tecnología">
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.dato.aap_tecnologia" name="tecnologia" :placeholder="$t('gestion.tecnology.select')" >
                              <el-option v-for="tec in tecnologias" :key="tec" :label="tec" :value="tec">
                              </el-option>
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item :label="$t('gestion.power.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="power" v-model="direccion.dato.aap_potencia" name="power" :placeholder="$t('gestion.power.select')">
                                  <el-option v-for="power in potencias" :key="power" :label="power" :value="parseFloat(power)" >
                                  </el-option>
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 3" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item :label="$t('gestion.connection.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="conexion" v-model="direccion.dato.aaco_id" name="conexion" :placeholder="$t('gestion.connection.select')">
                                  <el-option v-for="conexion in conexiones" :key="conexion.aaco_id" :label="conexion.aaco_descripcion" :value="parseInt(conexion.aaco_id)">
                                  </el-option>
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item :label="$t('gestion.post.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="post" v-model="direccion.dato.tipo_id" name="post" :placeholder="$t('gestion.post.select')">
                                  <el-option v-for="post in postes" :key="post.tipo_id" :label="post.tipo_descripcion" :value="post.tipo_id">
                                  </el-option>
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                              <el-form-item :label="$t('gestion.post.size')">
                                <el-input :disabled="direccion.even_estado > 7" ref="postsize" v-model="direccion.dato.aap_poste_altura" @input="direccion.dato.aap_poste_altura=parseFloat($event)" name="postsize" />
                              </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item :label="$t('gestion.arm')">
                                <el-input :disabled="direccion.even_estado > 7" ref="arm" v-model="direccion.dato.aap_brazo" name="arm" />
                              </el-form-item>
                            </el-col>
                            <el-col v-if="obra.reti_id === 6" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item :label="$t('gestion.collar')">
                                <el-input :disabled="direccion.even_estado > 7" ref="collar" v-model="direccion.dato.aap_collarin" name="collar" />
                              </el-form-item>
                            </el-col>
                          </el-row>
                          <el-row>
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                              <el-button v-if="direccion.even_estado < 8" size="mini" type="danger" circle icon="el-icon-minus" title="Quitar Fila" @click="direccion.even_estado === 1? direccion.even_estado = 8: direccion.even_estado = 9"></el-button>
                              <el-button v-if="direccion.even_estado > 7" size="mini" type="success" circle icon="el-icon-success" title="Restaurar Fila" @click="direccion.even_estado === 9? direccion.even_estado = 2 : direccion.even_estado = 1"></el-button>
                            </el-col>
                         </el-row>
                         <el-row>
                          <el-col style="border-bottom: 1px dotted #000;"></el-col>
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
                       <el-collapse-item name="4" :title="$t('obra.events')">
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
                        <div v-for="(evento, id) in obra.eventos" v-bind:key="evento.even_id">
                          <el-form :model="evento" ref="eventform">
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
                                    <el-input :disabled="evento.even_estado > 7" type="number" class="sinpadding" style="display: table-cell;" v-model="evento.aap_id" @input="evento.aap_id = parseInt($event,10)"  @blur="validateAapEvento(evento.aap_id, id)">
                                    </el-input>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Nombre del Material</span>
                            </el-col>
                            <el-col :xs="2" :sm="2" :md="2" :lg="2" :xl="2">
                                <el-form-item prop="elem_codigo">
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.elem_codigo" @blur="buscarCodigoElemento(evento)"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="15" :sm="15" :md="9" :lg="9" :xl="9">
                             <el-form-item>
                                <el-select :disabled="evento.even_estado > 7" clearable filterable v-model="evento.elem_id" :placeholder="$t('elemento.select')" @change="codigoElemento(evento)" style="width: 100%;"
                                          remote :remote-method="remoteMethodElemento"
                                          :loading="loadingElemento">
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
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.even_codigo_retirado" @blur="validarCodigoElementoRetirado(evento.elem_id, evento.even_codigo_retirado)"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Cantidad Material Retirado</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item>
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.even_cantidad_retirado" @blur="evento.even_cantidad_retirado = parseFloat(evento.even_cantidad_retirado)"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Código Material Instalado</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item>
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.even_codigo_instalado" @blur="validarCodigoElementoInstalado(evento.elem_id, evento.even_codigo_instalado)"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Cantidad Material Instalado</span>
                            </el-col>
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item>
                                    <el-input :disabled="evento.even_estado === 9" class="sinpadding" v-model="evento.even_cantidad_instalado" @blur="evento.even_cantidad_instalado = parseFloat(evento.even_cantidad_instalado)" ></el-input>
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
                             <el-button  style="display: table-cell;" type="info" size="mini" circle icon="el-icon-plus" title="Adicionar Nueva Fila" @click="onAddEvent()" />
                           </el-col>
                         </el-row>
                      </el-collapse-item>
                    </el-collapse>
          </el-form>
      </el-main>
     <el-footer>
      <el-button v-if="canSave" ref="submit" :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="confirmacionGuardar = !confirmacionGuardar">Guardar Obra</el-button>
      <el-button v-if="canPrint" ref="print" size="medium" type="success" icon="el-icon-printer" @click="imprimir">Imprimir</el-button>
     </el-footer>
     <el-dialog
      title="Atención"
      :visible.sync="centerDialogVisible"
      center>
      <span style="font-size: 20px;">El Código de Luminaria <b>{{ aap.aap_id }}</b>, No Existe, Por Favor Verifique.</span>
      <span slot="footer" class="dialog-footer">
        <el-button v-if="obra.reti_id === 2" type="primary" @click="centerDialogVisible = false; showAapModal=!showAapModal">Crear Nuevo Código</el-button>
        <el-button type="primary" @click="centerDialogVisible = false">Cerrar</el-button>
      </span>
     </el-dialog>
      <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
          <span style="font-size:20px;">Seguro de Guardar las Modificaciones a la Obra?</span>
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
import { getObra, updateObra, getEstados, validarCodigo, addObraAOrden } from '@/api/obra'
import { getAcciones } from '@/api/accion'
import { getElementos, getElementoByDescripcion, getElementoByCode } from '@/api/elemento'
import { getAapEdit, getAapValidar } from '@/api/aap'
import { getMedioambiente } from '@/api/medioambiente'
import { getAapTiposCarcasa } from '@/api/aap_tipo_carcasa'
import { getAapMarcas } from '@/api/aap_marca'
import { getAapModelos } from '@/api/aap_modelo'
import { getCaracteristica } from '@/api/caracteristica'
import { getAapConexiones } from '@/api/aap_conexion'
import { getOrdenes } from '@/api/ordentrabajo'

// component

// import { inspect } from 'util'

export default {
  data () {
    return {
      labelPosition: 'top',
      loadingElemento: false,
      showAapModal: false,
      activePages: ['1', '2', '3', '4'],
      activePages2: ['2-1', '2-2'],
      nopopover: false,
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      obra_previo: null,
      evento_siguiente_consecutivo: 1,
      direccion_siguiente_consecutivo: 1,
      guardando: false,
      obra: {
        obra_id: null,
        obra_consecutivo: 0,
        obra_nombre: null,
        obra_fecharecepcion: null,
        obra_direccion: null,
        obra_solicitante: null,
        obra_telefono: null,
        obra_email: null,
        obra_fechasolucion: null,
        obra_horainicio: null,
        obra_horafin: null,
        obra_modificado: null,
        obra_obratecnico: null,
        obra_descripcion: null,
        ortr_id: null,
        muot_id: null,
        rees_id: 3,
        orig_id: null,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        tiba_id: null,
        meams: [],
        eventos: []
      },
      evento: {
        even_codigo_instalado: null,
        even_codigo_retirado: null,
        even_cantidad_instalado: 1,
        even_cantidad_retirado: 1,
        even_estado: 1,
        aap_id: null,
        obra_id: null,
        elem_id: null,
        elem_codigo: null,
        elem_descripcion: null,
        empr_id: 0,
        usua_id: 0,
        even_id: null
      },
      rules: {
        obra_fecharecepcion: [
          { type: 'date', required: true, message: 'Debe diligencia la Fecha de Recepción de la Obra', trigger: 'change' }
        ],
        orig_id: [
          { required: true, message: 'Debe Seleccionar el Origen de la Obra', trigger: 'change' }
        ],
        obra_nombre: [
          { required: true, message: 'Debe Digitar el Nombre de quién reporta el daño o actividad', trigger: 'blur' }
        ],
        obra_direccion: [
          { required: true, message: 'Debe Digitar la dirección del daño o actividad', trigger: 'blur' }
        ],
        barr_id: [
          { required: true, message: 'Debe Seleccionar el Barrio del Daño o Actividad', trigger: 'change' }
        ],
        tiba_id: [
          { required: true, message: 'Debe Seleccionar el Tipo de Sector del Daño o Actividad', trigger: 'blur' }
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
      elementos_list: [],
      medioambiente: [],
      medioambiente_keys: [],
      meams: [],
      estados: [],
      carcasas: [],
      marcas: [],
      modelos: [],
      tecnologias: [],
      potencias: [],
      postes: [],
      conexiones: [],
      checkAll: false,
      isIndeterminate: false,
      aap: { aap_id: null },
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
      ortr_id: null,
      ortr_id_state: false,
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
    autosave: { name: 'autosave', time: 10000, autostart: false, repeat: true },
    pending: { name: 'pending', time: 30000, autostart: false, repeat: true }
  },
  methods: {
    confirmOrdenTrabajo () {
      addObraAOrden(
        this.obra.ortr_id,
        this.obra.obra_id
      )
        .then((response) => {
          if (response.data === 'true') {
            this.$message({
              message: 'Orden de Trabajo Actualizada',
              type: 'success'
            })
          } else {
          }
        })
        .catch((error) => {
          this.$message({
            message: 'Orden de Trabajo NO Actualizada, error:' + error,
            type: 'warning'
          })
        })
    },
    autosave () {
      localStorage.setItem('currEditObra', JSON.stringify(this.obra))
    },
    pending () {
      /* const _ini = localStorage.getItem('currEditRepIni')
      const _curr = localStorage.getItem('currEditRep')
      if (!this.$equals(_ini, _curr)) {
        this.open7()
      } */
    },
    changeFocus (next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange (val) {
    },
    handleDelete (index, row) {
      this.obra.eventos.splice(index, 1)
      this.$refs['evento.aap_id'].focus()
    },
    handleCheckAllChange (val) {
      this.obra.meams = val ? this.medioambiente_keys : []
      this.isIndeterminate = false
    },
    handleObraMeamChange (value) {
      const meamCount = value.length
      this.checkAll = meamCount === this.medioambiente_keys.length
      this.isIndeterminate = meamCount > 0 && meamCount < this.medioambiente_keys.length
    },
    validate () {
      if (!this.guardando && this.obra.muot_id && this.obra.orig_id && this.obra.obra_fecharecepcion && this.obra.obra_nombre && this.obra.obra_direccion && this.obra.barr_id && this.obra.obra_descripcion && this.obra.obra_fechasolucion && this.obra.obra_horainicio && this.obra.obra_horafin) {
        return true
      } else {
        return false
      }
    },
    validateAap (direccion, id) {
      console.log('Direccion: ' + JSON.stringify(direccion))
      if (direccion.aap_id) {
        this.aap.aap_id = direccion.aap_id
        getAapEdit(direccion.aap_id).then(response => {
          const activo = response.data
          console.log('Activo :' + JSON.stringify(activo))
          if (activo.aap === null || activo.aap.aap_id < 1) {
            console.log('Estoy en no existe')
            this.existe = false
            this.centerDialogVisible = true
          } else {
            if (direccion.even_estado === 1) {
              direccion.even_direcion_anterior = activo.aap.aap_direccion
              direccion.barr_id_anterior = activo.aap.barr_id
              direccion.dato.aatc_id_anterior = activo.aap.aatc_id
              direccion.dato.aama_id_anterior = activo.aap.aama_id
              direccion.dato.aamo_id_anterior = activo.aap.aamo_id
              direccion.dato.aaco_id_anterior = activo.aap.aaco_id
              direccion.dato.aap_potencia_anterior = activo.aap_adicional.aap_potencia
              direccion.dato.aap_tecnologia_anterior = activo.aap_adicional.aap_tecnologia
              if (activo.aap_adicional.aap_brazo !== null && activo.aap_adicional.aap_brazo !== undefined) {
                direccion.dato.aap_brazo_anterior = activo.aap_adicional.aap_brazo.toString()
              } else {
                direccion.dato.aap_brazo_anterior = ''
              }
              direccion.dato.aap_collarin_anterior = activo.aap_adicional.aap_collarin
              direccion.dato.tipo_id_anterior = activo.aap_adicional.tipo_id
              direccion.dato.aap_poste_altura_anterior = activo.aap_adicional.aap_poste_altura
              console.log('Direccion2: ' + JSON.stringify(direccion))
            } else {
              console.log('No se puede cambiar la info')
            }
          }
        }).catch(error => {
          console.log('Estoy en Error: ' + error)
          this.existe = false
          this.centerDialogVisible = true
        })
      }
    },
    validateAapEvento (aap_id, id) {
      if (aap_id) {
        this.aap.aap_id = aap_id
        getAapValidar(aap_id).then(response => {
          var result = response.data
          if (result === 'false') {
            this.existe = false
            this.centerDialogVisible = true
          }
        }).catch(() => {
          this.existe = false
          this.centerDialogVisible = true
        })
      }
    },
    validarCodigoElementoRetirado (elem_id, codigo) {
      if (elem_id !== null && elem_id > 0 && codigo !== null && codigo !== '') {
        validarCodigo(elem_id, codigo).then(response => {
          const resultado = response.data
          if (resultado === '10') {
            const msg = 'Código de material ya fue retirado'
            this.alerta(msg)
          } else if (resultado === '11') {
            const msg = 'Código de material ya fue instalado y retirado'
            this.alerta(msg)
          }
        })
      }
    },
    validarCodigoElementoInstalado (elem_id, codigo) {
      if (elem_id !== null && elem_id > 0 && codigo !== null && codigo !== '') {
        validarCodigo(elem_id, codigo).then(response => {
          const resultado = response.data
          if (resultado === '10') {
            const msg = 'Código de material ya fue retirado'
            this.alerta(msg)
          } else if (resultado === '11') {
            const msg = 'Código de material ya fue instalado y retirado'
            this.alerta(msg)
          } else if (resultado === '01') {
            const msg = 'Código de material ya fue instalado'
            this.alerta(msg)
          }
        })
      }
    },
    alerta (msg) {
      this.$alert(msg, 'Atención', {
        confirmButtonText: 'Continuar'
      })
    },
    aplicar () {
      // var valido = true
      this.guardando = true
      this.confirmacionGuardar = false
      // valido = this.validatForm('obraForm')
      for (var i = 0; i < this.obra.eventos.length; i++) {
        if (this.obra.eventos[i].elem_id === '') {
          this.obra.eventos[i].elem_id = null
          this.obra.eventos[i].elem_codigo = null
        }
      }
      this.obra.rees_id = 3
      updateObra(this.obra)
        .then(response => {
          if (response.status === 200) {
            localStorage.setItem('currEditObraIni', this.obra)
            this.success()
          } else {
            this.error('Se presentó un inconveniente al guardar los cambios, por favor reintente')
          }
        })
        .catch(error => {
          this.error(error)
        })
    },
    validatForm (form) {
      var result = false
      this.$refs[form].validate((valid) => {
        if (valid) {
          result = true
        } else {
          result = false
        }
      })
      return result
    },
    imprimir () {
    },
    success () {
      this.$notify({
        title: this.$i18n.t('obra.success'),
        message:
          this.$i18n.t('obra.updated') +
          ' ' +
          this.obra.obra_consecutivo,
        type: 'success'
      })
    },
    error (e) {
      this.$notify.error({
        title: this.$i18n.t('obra.error'),
        message: this.$i18n.t('obra.notupdated') + ' ' + e
      })
    },
    open7 () {
      this.$message({
        showClose: true,
        message: 'Atención, existe información por guardar en el obra.',
        type: 'warning',
        duration: 5000
      })
    },
    codigoElemento (evento) {
      if (evento.elem_id === '' || evento.elem_id === null || evento.elem_id === undefined) {
        return '-'
      } else {
        this.completarMaterial()
        const elemento = this.elementos_list.find(o => o.elem_id === evento.elem_id, { elem_codigo: '-' })
        console.log('Elemento encontrado: ' + JSON.stringify(elemento))
        evento.elem_codigo = elemento.elem_codigo
      }
    },
    buscarCodigoElemento (evento) {
      if (evento.elem_codigo !== undefined && evento.elem_codigo !== null && evento.elem_codigo !== '') {
        const elemento = this.elementos.find(e => parseInt(e.elem_codigo) === parseInt(evento.elem_codigo))
        if (!elemento) {
          getElementoByCode(evento.elem_codigo).then(response => {
            if (response.status === 200) {
              this.elementos = []
              var elemento = response.data
              this.elementos.unshift(elemento)
            } else {
              this.$notify({
                title: 'Atención',
                message: 'No se encontró Material con ese código: (' + response.status + ')',
                type: 'warning'
              })
            }
          }).catch((error) => {
            this.$notify({
              title: 'Atención',
              message: 'No se encontró Material con ese código: (' + error + ')',
              type: 'warning'
            })
          })
        } else {
          this.elementos = []
          this.elementos.unshift(elemento)
        }
      }
    },
    completarMaterial () {
      for (var j = 0; j < this.obra.eventos.length; j++) {
        if (this.obra.eventos[j] !== undefined && this.obra.eventos[j].elem_id !== undefined && this.obra.eventos[j].elem_id > 0) {
          if (this.elementos.find(e => e.elem_id === this.obra.eventos[j].elem_id) === undefined) {
            this.elementos.push({ elem_id: this.obra.eventos[j].elem_id, elem_descripcion: this.elemento(this.obra.eventos[j].elem_id) })
          }
        }
      }
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
        obra_id: this.obra.obra_id,
        elem_id: null,
        elem_codigo: null,
        empr_id: 0,
        usua_id: 0,
        even_id: this.evento_siguiente_consecutivo
      }
      this.obra.eventos.push(evento)
      this.evento_siguiente_consecutivo = this.evento_siguiente_consecutivo + 1
    },
    onAddAddress () {
      var direccion = {
        obra_id: this.obra.obra_id,
        aap_id: null,
        even_id: this.direccion_siguiente_consecutivo,
        even_direccion: null,
        barr_id: null,
        even_direccion_anterior: null,
        barr_id_anterior: null,
        even_estado: 1,
        dato: {
          aatc_id: null,
          aatc_id_anterior: null,
          aama_id: null,
          aama_id_anterior: null,
          aamo_id: null,
          aamo_id_anterior: null,
          aaco_id: null,
          aaco_id_anterior: null,
          aap_potencia: null,
          aap_potencia_anterior: null,
          aap_tecnologia: null,
          aap_tecnologia_anterior: null,
          aap_brazo: null,
          aap_brazo_anterior: null,
          aap_collarin: null,
          aap_collarin_anterior: null,
          tipo_id: null,
          tipo_id_anterior: null,
          aap_poste_altura: null,
          aap_poste_altura_anterior: null
        }
      }
      this.obra.direcciones.push(direccion)
      this.direccion_siguiente_consecutivo = this.direccion_siguiente_consecutivo + 1
    },
    limpiarAndBack () {
      this.obtenerObra()
    },
    elemento (elem_id) {
      if (elem_id === null) {
        return ''
      } else {
        return this.elementos_list.find(o => o.elem_id === elem_id, { elem_descripcion: null }).elem_descripcion
      }
    },
    obra_tipo (reti_id) {
      if (reti_id === null) {
        return ''
      } else {
        return this.tipos.find(o => o.reti_id === reti_id, { reti_descripcion: 'INDEFINIDO' }).reti_descripcion
      }
    },
    estado () {
      if (this.obra && this.obra.rees_id !== null) {
        var rees_id = this.obra.rees_id
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
    tiposector (barr_id) {
      if (barr_id === null) {
        return null
      } else {
        return this.barrios.find(o => o.barr_id === barr_id, { tiba_id: null }).tiba_id
      }
    },
    obtenerObra () {
      getObra(this.$route.params.id).then(response => {
        this.obra_previo = response.data
        this.obra_previo.eventos.forEach(e => {
          e.elem_codigo = null
        })
        localStorage.setItem('currEditObraIni', JSON.stringify(this.obra_previo))
        this.cargarEventos()
        this.validarConsecutivo()
        this.obra = this.obra_previo
        console.log('Obra cargada: ' + JSON.stringify(this.obra))
        this.obra.tiba_id = this.tiposector(this.obra.barr_id)
        if (!this.obra.ortr_id) {
          this.obra.ortr_id = null
        }
        this.$timer.start('autosave')
        this.$timer.start('pending')
        this.pending()
      }).catch(error => {
        console.log('getObra: ' + error)
      })
    },
    ordenes (id) {
      if (id === undefined || id === null) {
        return ''
      } else {
        var orden = this.ordenestrabajo.find((o) => o.ortr_id === id)
        if (orden) {
          return orden.ortr_consecutivo + ' - ' + orden.cuad_descripcion
        } else {
          return ''
        }
      }
    },
    validarConsecutivo () {
      // var consecutivo = 1
      for (var i = 0; i < this.obra_previo.eventos.length; i++) {
        if (this.obra_previo.eventos[i].elem_id !== undefined && this.obra_previo.eventos[i].elem_id > 0) {
          if (this.elementos.find(e => e.elem_id === this.obra_previo.eventos[i].elem_id) === undefined) {
            this.elementos.push({ elem_id: this.obra_previo.eventos[i].elem_id, elem_codigo: this.obra_previo.eventos[i].elem_codigo, elem_descripcion: this.elemento(this.obra_previo.eventos[i].elem_id) })
          }
        }
        // consecutivo++
      }
    },
    cargarEventos () {
      // validar si existe un obra previo
      var stringObraAnterior = localStorage.getItem('currEditObra')
      if (stringObraAnterior !== undefined && stringObraAnterior !== null & stringObraAnterior !== '') {
        var currEditRep = JSON.parse(stringObraAnterior)
        if (currEditRep.obra_id === this.obra_previo.obra_id) {
          this.obra_previo = currEditRep
        }
      }
      var even_length = 0
      this.obra_previo.eventos.forEach(e => {
        if (e.even_id > even_length) {
          even_length = e.even_id
        }
      })
      this.obra_previo.eventos.forEach(e => {
        if (e.even_id === undefined || e.even_id === null || e.even_id < 1) {
          e.even_id = even_length + 1
          even_length = even_length + 1
        }
      })
      /* this.obra_previo.eventos.forEach(e => {
        if (e.elem_id !== undefined && e.elem_id !== null && e.elem_id > 0) {
          e.elem_codigo = this.codigoElemento(e)
        } else {
          e.elem_codigo = null
        }
      }) */
      console.log('Obra previo:' + JSON.stringify(this.obra_previo))
      if (even_length === 0) {
        for (var i = 1; i <= 10; i++) {
          var evento = {
            even_fecha: null,
            even_codigo_instalado: null,
            even_codigo_retirado: null,
            even_cantidad_instalado: 1.00,
            even_cantidad_retirado: 1.00,
            even_estado: 1,
            aap_id: null,
            obra_id: this.obra_previo.obra_id,
            elem_id: null,
            elem_codigo: null,
            empr_id: 0,
            usua_id: 0,
            even_id: even_length + i
          }
          this.obra_previo.eventos.push(evento)
        }
        this.evento_siguiente_consecutivo = even_length + 11
      } else {
        this.evento_siguiente_consecutivo = even_length + 1
      }
    },
    remoteMethodElemento (query) {
      if (query !== '') {
        getElementoByDescripcion(query).then(response => {
          this.elementos = response.data
        })
      }
    }
  },
  beforeMount () {
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
                // getTipos().then(response => {
                // this.tipos = response.data
                // this.tipos_lista = response.data
                getTiposBarrio().then(response => {
                  this.tiposbarrio = response.data
                  getElementos().then(response => {
                    this.elementos_list = response.data
                    getAapTiposCarcasa().then(response => {
                      this.carcasas = response.data
                      getAapMarcas().then(response => {
                        this.marcas = response.data
                        getAapModelos().then(response => {
                          this.modelos = response.data
                          getCaracteristica(7).then(response => {
                            this.tecnologias = response.data.cara_valores.split(',')
                            getCaracteristica(5).then(response => {
                              this.potencias = response.data.cara_valores.split(',')
                              getCaracteristica(8).then(response => {
                                const poste = response.data.cara_valores.split(',')
                                for (var i = 0; i < poste.length; i++) {
                                  this.postes.push({ tipo_id: i, tipo_descripcion: poste[i] })
                                }
                                getAapConexiones().then(response => {
                                  this.conexiones = response.data
                                  getOrdenes().then((response) => {
                                    this.ordenestrabajo = response.data
                                    this.obtenerObra()
                                  }).catch(error => {
                                    console.log('getOrdenes: ', error)
                                  })
                                }).catch(error => {
                                  console.log('getConexiones :' + error)
                                })
                              }).catch(error => {
                                console.log('Caracteristica 8: ' + error)
                              })
                            }).catch(error => {
                              console.log('Caracteristica 5: ' + error)
                            })
                          }).catch(error => {
                            console.log('getCaracteristica 7: ' + error)
                          })
                        }).catch(error => {
                          console.log('getModelos: ' + error)
                        })
                      }).catch(error => {
                        console.log('getMarcas: ' + error)
                      })
                    }).catch(error => {
                      console.log('getCarcasas: ' + error)
                    })
                  }).catch(error => {
                    console.log('getElementos: ' + error)
                  })
                }).catch(error => {
                  console.log('getTiposBarrio: ' + error)
                })
                /* }).catch(error => {
                  console.log('getTipos: ' + error)
                }) */
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
