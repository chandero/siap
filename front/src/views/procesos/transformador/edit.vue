<template>
  <el-container>
      <el-header>
          <span>{{ $t('route.controlreporteedit') }} - Estado Actual: {{ estado() }}</span>
      </el-header>
      <el-main>
          <el-form ref="reporteForm" :model="reporte" :rules="rules" :label-position="labelPosition">
              <el-collapse v-model="activePages" @change="handleActivePagesChange">
                <el-collapse-item name="1" :title="$t('reporte.general')" style="font-weight: bold;">
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                            <el-form-item prop="repo_fecharecepcion" :label="$t('reporte.receptiondate')">
                                <span style="font-size: 24px;">{{ reporte.repo_fecharecepcion | moment('YYYY/MM/DD HH:MM')}}</span>
                            </el-form-item>
                        </el-col>                      
                        <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
                            <el-form-item prop="reti_id" :label="$t('reporte.type')">
                                <span style="font-size: 24px;">{{ reporte_tipo(reporte.reti_id) }}</span>
                            </el-form-item>
                        </el-col>
                        <el-col v-if="reporte.reti_id===2" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="adicional.repo_tipo_expansion" :label="$t('reporte.tipo_expansion.title')">
                                <el-select :disabled="reporte.rees_id == 3" clearable :title="$t('reporte.tipo_expansion.select')" style="width: 80%" ref="tipo" v-model="reporte.adicional.repo_tipo_expansion" name="tipo_expansion" :placeholder="$t('reporte.tipo_expansion.select')" @change="validarExpansion()">
                                    <el-option v-for="te in tipos_expansion" :key="te.tiex_id" :label="te.tiex_descripcion" :value="te.tiex_id" >
                                    </el-option>   
                                </el-select>
                              </el-form-item>
                        </el-col>
                        <el-col v-if="reporte.reti_id===2 && reporte.adicional.repo_tipo_expansion < 4" :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                          <el-form-item prop="muot_id" :label="$t('reporte.ot')">
                            <el-input type="number" style="font-size: 30px;" v-model="reporte.adicional.muot_id" @input="reporte.adicional.muot_id = parseInt($event)"></el-input>
                          </el-form-item>
                        </el-col>                        
                        <el-col v-if="reporte.adicional.repo_tipo_expansion === 5" :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                              <el-form-item :label="$t('reporte.urba.title')" prop="adicional.urba_id">
                                <el-select :disabled="reporte.rees_id == 3" clearable :title="$t('reporte.urba.select')" style="width: 80%" ref="tipo" v-model="reporte.adicional.urba_id" name="urbanizadora" :placeholder="$t('reporte.urba.select')">
                                    <el-option v-for="u in urbanizadoras" :key="u.urba_id" :label="u.urba_descripcion" :value="u.urba_id" >
                                    </el-option>   
                                </el-select>
                              </el-form-item>
                        </el-col>
                    </el-row>
                    <el-row :gutter="4">
                        <el-col :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                          <el-form-item prop="repo_consecutivo" :label="$t('reporte.number')">
                            <span style="font-size: 30px;">{{ reporte.repo_consecutivo | fillZeros(6) }}</span>
                          </el-form-item>
                        </el-col>                      
                        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                            <el-form-item prop="orig_id" :label="$t('reporte.origin')" :read-only="reporte.rees_id == 3">
                                <el-select :disabled="reporte.rees_id == 3" style="width:100%;" ref="origin" v-model="reporte.orig_id" name="origen" :placeholder="$t('origen.select')"  @change="changeFocus('code')">
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
                            <el-form-item prop="barr_id" :label="$t('reporte.neighborhood')" :read-only="reporte.rees_id == 3">
                             <el-select :disabled="reporte.rees_id == 3" style="width:100%;" filterable ref="barrio" v-model="reporte.barr_id" name="barrio" :placeholder="$t('barrio.select')"  @change="changeFocus('tiba')">
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id">
                              </el-option>   
                             </el-select>
                            </el-form-item>                        
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                            <el-form-item prop="tiba_id" :label="$t('reporte.sector')" :read-only="reporte.rees_id == 3">
                             <el-select :disabled="reporte.rees_id == 3" style="width:100%;" filterable ref="tiba" v-model="reporte.tiba_id" name="tiba" :placeholder="$t('tipobarrio.select')"  @change="changeFocus('telefono')">
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
                            <el-form-item prop="adicional.acti_id" :label="$t('reporte.activity')" :read-only="reporte.rees_id == 3">
                             <el-select :disabled="reporte.rees_id == 3" style="width:90%;" filterable ref="tiba" v-model="reporte.adicional.acti_id" name="actividad" :placeholder="$t('actividad.select')"  @change="changeFocus('descripcion')">
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
                                    <el-input :disabled="reporte.rees_id == 3" autofocus v-model="actividad.acti_descripcion" @input="actividad.acti_descripcion = $event.toUpperCase()"></el-input>
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
                            <el-form-item prop="repo_fechasolucion" :label="$t('reporte.solutiondate')" :read-only="reporte.rees_id == 3">
                                <el-date-picker :disabled="reporte.rees_id == 3" v-model="reporte.repo_fechasolucion"
                                 :picker-options="datePickerOptions"
                                ></el-date-picker>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item prop="repo_horainicio" :label="$t('reporte.timestart')" :read-only="reporte.rees_id == 3">
                                <el-time-select :disabled="reporte.rees_id == 3" v-model="reporte.repo_horainicio"
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
                            <el-form-item prop="repo_horafin" :label="$t('reporte.timeend')" :read-only="reporte.rees_id == 3">
                                <el-time-select :disabled="reporte.rees_id == 3" v-model="reporte.repo_horafin"
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
                            <el-form-item prop="repo_reportetecnico" :label="$t('reporte.tecnicalreport')">
                                <el-input :disabled="reporte.rees_id == 3" type="textarea" :rows="3" ref="tecnico" v-model="reporte.repo_reportetecnico" @input="reporte.repo_reportetecnico = $event.toUpperCase()" @keyup.enter.native="changeFocus('evento.aap_id')"></el-input>
                            </el-form-item>
                        </el-col>                        
                    </el-row>
                    <el-row :gutter="4">
                      <el-col :span="24">
                        <el-form-item prop="meams" :label="$t('reporte.environment')">
                          <el-checkbox :disabled="reporte.rees_id == 3" :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">Marcar todos</el-checkbox>
                          <div style="margin: 15px 0;"></div>                        
                          <el-checkbox-group v-model="reporte.meams" @change="handleReporteMeamChange">
                            <el-checkbox :disabled="reporte.rees_id == 3" border v-for="meam in medioambiente" :label="meam.meam_id" :key="meam.meam_id">{{ meam.meam_descripcion }}</el-checkbox>
                          </el-checkbox-group>                   
                        </el-form-item>
                      </el-col>
                    </el-row>                    
                    </el-collapse-item>
                    <el-collapse-item v-if="conDirecciones" name="3" title="DATOS LUMINARIAS">
                        <div>
                          <el-form :disabled="reporte.rees_id == 3" v-for="(direccion, id) in reporte.direcciones" v-bind:key="direccion.even_id" :model="direccion" :ref="'dirform_'+direccion.even_id" label-position="left" :rules="dirrules">
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
                            <el-col v-if="reporte.reti_id !== 1" :xs="16" :sm="16" :md="11" :lg="11" :xl="11">
                             <el-form-item prop="even_direccion" label="Nueva Dirección">
                               <el-input :disabled="direccion.even_estado > 7" :name="'even_direccion_'+id" v-model="direccion.even_direccion" @input="direccion.even_direccion = $event.toUpperCase()" ></el-input>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="16" :sm="16" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="barr_id" label="Barrio/Vereda"> 
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.barr_id" name="barrio" :placeholder="$t('barrio.select')" >
                              <el-option v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id">
                              </el-option>   
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="dato.aatc_id" label="Tipo Luminaria"> 
                             <el-select :disabled="direccion.even_estado > 7" v-validate="'required|excluded:0'" style="width:100%;" filterable clearable v-model="direccion.dato.aatc_id" :name="'aatc_id_'+id" :placeholder="$t('cover.select')" >
                              <el-option v-for="carcasa in carcasas" :key="carcasa.aatc_id" :label="carcasa.aatc_descripcion" :value="parseInt(carcasa.aatc_id)">
                              </el-option>   
                             </el-select>
                             <span>{{ errors.first('aatc_id_'+id) }}</span>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="dato.aama_id" label="Marca"> 
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.dato.aama_id" name="marca" :placeholder="$t('brand.select')" >
                              <el-option v-for="marca in marcas" :key="marca.aama_id" :label="marca.aama_descripcion" :value="marca.aama_id">
                              </el-option>   
                             </el-select>
                             </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
                             <el-form-item prop="dato.aamo_id" label="Modelo"> 
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.dato.aamo_id" name="modelo" :placeholder="$t('model.select')" >
                              <el-option v-for="modelo in modelos" :key="modelo.aamo_id" :label="modelo.aamo_descripcion" :value="modelo.aamo_id">
                              </el-option>   
                             </el-select>
                             </el-form-item>
                            </el-col>
                          </el-row>
                          <el-row :gutter="4">
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                             <el-form-item prop="dato.aap_tecnologia" label="Tecnología"> 
                             <el-select :disabled="direccion.even_estado > 7" style="width:100%;" filterable clearable v-model="direccion.dato.aap_tecnologia" name="tecnologia" :placeholder="$t('gestion.tecnology.select')" >
                              <el-option v-for="tec in tecnologias" :key="tec" :label="tec" :value="tec">
                              </el-option>   
                             </el-select>
                             </el-form-item>
                            </el-col>   
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato.aap_potencia" :label="$t('gestion.power.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="power" v-model="direccion.dato.aap_potencia" name="potencia" :placeholder="$t('gestion.power.select')">
                                  <el-option v-for="power in potencias" :key="power" :label="power" :value="parseFloat(power)" >
                                  </el-option>       
                                </el-select>
                              </el-form-item>
                            </el-col> 
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato.aaco_id" :label="$t('gestion.connection.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="conexion" v-model="direccion.dato.aaco_id" name="conexion" :placeholder="$t('gestion.connection.select')">
                                  <el-option v-for="conexion in conexiones" :key="conexion.aaco_id" :label="conexion.aaco_descripcion" :value="parseInt(conexion.aaco_id)">
                                  </el-option>       
                                </el-select>
                              </el-form-item>
                            </el-col>   
                            <el-col v-if="reporte.reti_id !== 1 & direccion.dato.aaco_id == 2" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato_adicional.medi_id" :label="$t('gestion.medidor.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="medidor" v-model="direccion.dato_adicional.medi_id" name="medidor" :placeholder="$t('gestion.medidor.select')">
                                  <el-option v-for="m in medidores" :key="m.medi_id" :label="m.medi_numero" :value="m.medi_id">
                                  </el-option>       
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato_adicional.tran_id" :label="$t('gestion.transformador.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="transformador" v-model="direccion.dato_adicional.tran_id" name="transformador" :placeholder="$t('gestion.transformador.select')">
                                  <el-option v-for="t in transformadores" :key="t.tran_id" :label="t.tran_numero" :value="t.tran_id">
                                  </el-option>       
                                </el-select>
                              </el-form-item>
                            </el-col>  
                          </el-row>  
                          <el-row>                                                                               
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato.tipo_id" :label="$t('gestion.post.title')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="post" v-model="direccion.dato.tipo_id" name="post" :placeholder="$t('gestion.post.select')">
                                  <el-option v-for="post in postes" :key="post.tipo_id" :label="post.tipo_descripcion" :value="post.tipo_id">
                                  </el-option>       
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato.aap_poste_altura" :label="$t('gestion.post.size')">
                                <el-input :disabled="direccion.even_estado > 7" ref="postsize" v-model="direccion.dato.aap_poste_altura" @input="direccion.dato.aap_poste_altura=parseFloat($event)" name="postsize" />
                              </el-form-item>
                            </el-col>  
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato.aap_poste_propietario" :label="$t('gestion.post.own')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="postowner" v-model="direccion.dato.aap_poste_propietario" name="postowner" :placeholder="$t('gestion.post.selectown')">
                                  <el-option v-for="own in owns" :key="own" :label="own" :value="own" >
                                  </el-option>       
                              </el-select>
                              </el-form-item>
                            </el-col>                            
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato.aap_brazo" :label="$t('gestion.arm')">
                                <el-input :disabled="direccion.even_estado > 7" ref="arm" v-model="direccion.dato.aap_brazo" name="arm" />    
                              </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato.aap_collarin" :label="$t('gestion.collar')">
                                <el-input :disabled="direccion.even_estado > 7" ref="collar" v-model="direccion.dato.aap_collarin" name="collar" />
                              </el-form-item>
                            </el-col>   
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato_adicional.aaus_id" :label="$t('gestion.use')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="use" v-model="direccion.dato_adicional.aaus_id" name="use" :placeholder="$t('use.select')">
                                  <el-option v-for="aapuso in aap_usos" :key="aapuso.aaus_id" :label="aapuso.aaus_descripcion" :value="aapuso.aaus_id" >
                                  </el-option>       
                                </el-select>
                              </el-form-item>
                            </el-col>
                            <el-col v-if="reporte.reti_id !== 1" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="dato_adicional.aacu_id" :label="$t('gestion.account')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="account" v-model="direccion.dato_adicional.aacu_id" name="account" :placeholder="$t('account.select')">
                                  <el-option v-for="aapcuentaap in aap_cuentasap" :key="aapcuentaap.aacu_id" :label="aapcuentaap.aacu_descripcion" :value="aapcuentaap.aacu_id" >
                                  </el-option>       
                                </el-select>
                              </el-form-item>                              
                            </el-col>                                                                                                                                                                                           
                          </el-row>
                          <el-row>
                            <el-col v-if="reporte.reti_id == 8" :xs="24" :sm="6" :md="4" :lg="4" :xl="4">
                              <el-form-item prop="tire_id" :label="$t('gestion.tiporetiro')">
                                <el-select :disabled="direccion.even_estado > 7" clearable filterable ref="tiporetiro" v-model="direccion.tire_id" name="tiporetiro" :placeholder="$t('tiporetiro.select')">
                                  <el-option v-for="tire in tiposretiro" :key="tire.tire_id" :label="tire.tire_descripcion" :value="tire.tire_id" >
                                  </el-option>       
                                </el-select>
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
                       <el-collapse-item name="4" :title="$t('reporte.events')">
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
                        <div style="max-height: 600px; overflow: auto;">
                          <el-form :disabled="reporte.rees_id == 3" :model="evento" :ref="'matform_' + evento.even_id" :rules="matrules">
                          <el-row :gutter="4" v-for="evento in reporte.eventos" v-bind:key="evento.even_id">
                            <el-col class="hidden-md-and-up" :xs="1" :sm="1">
                              <span style="font-weight: bold;">No.</span>
                            </el-col>                            
                            <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{ evento.even_id }}</el-col>
                            <el-col class="hidden-md-and-up" :xs="9" :sm="9">
                              <span style="font-weight: bold;">Código de la Luminaria</span>
                            </el-col>
                            <el-col :xs="13" :sm="13" :md="3" :lg="3" :xl="3">
                                <el-form-item prop="aap_id">
                                  <div style="display: table;">
                                    <el-input :disabled="evento.even_estado === 2 || evento.even_estado > 7" class="sinpadding" style="display: table-cell;" type="number" v-model="evento.aap_id" @input="evento.aap_id = parseInt($event,10)" >
                                    </el-input>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="7" :sm="7">
                              <span style="font-weight: bold;">Nombre del Material</span>
                            </el-col>
                            <el-col :xs="2" :sm="2" :md="2" :lg="2" :xl="2">
                              <span style="width: 100%;">{{ codigoElemento(evento.elem_id) }}</span>
                            </el-col>
                            <el-col :xs="15" :sm="15" :md="9" :lg="9" :xl="9">
                             <el-form-item prop="elem_id">
                                <el-select :disabled="evento.even_estado > 7" filterable clearable v-model="evento.elem_id" :placeholder="$t('elemento.select')" style="width: 100%;" @focus="limpiarElemento()" 
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
                                <el-form-item prop="even_codigo_retirado">
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.even_codigo_retirado" @blur="validarCodigoElementoRetirado(evento.elem_id, evento.even_codigo_retirado)"></el-input>
                                </el-form-item>
                            </el-col>    
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Cantidad Material Retirado</span>
                            </el-col>                             
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item prop="even_cantidad_retirado">
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.even_cantidad_retirado" @blur="evento.even_cantidad_retirado = parseFloat(evento.even_cantidad_retirado)"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Código Material Instalado</span>
                            </el-col>                             
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item prop="even_codigo_instalado">
                                    <el-input :disabled="evento.even_estado > 7" class="sinpadding" v-model="evento.even_codigo_instalado" @blur="validarCodigoElementoInstalado(evento.elem_id, evento.even_codigo_instalado)"></el-input>
                                </el-form-item>
                            </el-col>
                            <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                              <span style="font-weight: bold;">Cantidad Material Instalado</span>
                            </el-col>                             
                            <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                                <el-form-item prop="even_cantidad_instalado">
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
                           <el-col :span="1">
                            <el-input type="number" v-model="addinputevent"></el-input>
                           </el-col> 
                           <el-col :span="23">
                             <el-button  style="display: table-cell;" type="info" size="mini" circle icon="el-icon-plus" title="Adicionar Nueva Fila" @click="onAddEvent()" />
                           </el-col>
                         </el-row>                        
                      </el-collapse-item>
                    </el-collapse>
          </el-form>
      </el-main>
     <el-footer>
      <el-button v-if="canSave" ref="submit" :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="confirmacionGuardar = !confirmacionGuardar">Guardar Reporte</el-button>
      <el-button v-if="canPrint" ref="print" size="medium" type="success" icon="el-icon-printer" @click="imprimir">Imprimir</el-button>      
      <el-button v-if="reporte.rees_id === 3" ref="abrir" size="medium" type="success" icon="el-icon-edit-outline" @click="abrirReporte()">Abrir Reporte</el-button>
     </el-footer>
     <el-dialog  width="80%" :visible.sync="showAapModal">
      <aap-create/>
     </el-dialog>
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
     <el-dialog
      title="Atención"
      :visible.sync="retiradoDialogVisible"
      center>
      <span style="font-size: 20px;">El Código de Luminaria <b>{{ aap.aap_id }}</b>, No Esta en Estado RETIRADO, Por Favor Verifique.</span>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="retiradoDialogVisible = false">Cerrar</el-button>
      </span>
     </el-dialog>
     <el-dialog
      title="Atención"
      :visible.sync="yaretiradoDialogVisible"
      center>
      <span style="font-size: 20px;">El Código de Luminaria <b>{{ aap.aap_id }}</b>, Se encuentra en Estado RETIRADO, Por Favor Verifique.</span>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="yaretiradoDialogVisible = false">Cerrar</el-button>
      </span>
     </el-dialog>     
     <el-dialog
      title="Recuperar Información"
      :visible.sync="recoveryVisible"
      width="30%"
     >
     <span>Existe información de Recuperación para el Reporte {{ reporte_previo.repo_consecutivo }}</span>
     <span>Desea recuperarla ?</span>
     <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">No</el-button>
      <el-button type="primary" @click="recuperarReporte()">Si</el-button>
     </span>
</el-dialog>           
  </el-container>
</template>
<script>
import { getActividades } from '@/api/actividad'
import { getOrigenes } from '@/api/origen'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getReporte, updateReporte, getTipos, getEstados, validarCodigo, validarReporteDiligenciado } from '@/api/controlreporte'
import { getAcciones } from '@/api/accion'
import { getElementos, getElementoByDescripcion } from '@/api/elemento'
import { getAapEdit, getAapValidar, validar } from '@/api/control'
import { getMedioambiente } from '@/api/medioambiente'
import { getAapTiposCarcasa } from '@/api/aap_tipo_carcasa'
import { getAapMarcas } from '@/api/aap_marca'
import { getAapModelos } from '@/api/aap_modelo'
import { getCaracteristica } from '@/api/caracteristica'
import { getAapConexiones } from '@/api/aap_conexion'
import { getAapUsos } from '@/api/aap_uso'
import { getAapCuentasAp } from '@/api/aap_cuentaap'
import { getTiposRetiro } from '@/api/tiporetiro'
import { getUrbanizadoraTodas } from '@/api/urbanizadora'
import { getMedidors } from '@/api/medidor'
import { getTransformadors } from '@/api/transformador'
// component
import AapCreate from '@/views/inventario/gestion/create'

import Popper from 'vue-popperjs'
import 'vue-popperjs/dist/css/vue-popper.css'

// import { inspect } from 'util'

export default {
  components: {
    AapCreate,
    Popper
  },
  data() {
    var validateAapEventoRule = (rule, value, callback) => {
      if (value) {
        this.aap.aap_id = value
        getAapValidar(value).then(response => {
          var result = response.data
          if (result === 404) {
            this.existe = false
            callback(new Error('No Existe'))
          } else if (result === 401) {
            this.existe = false
            callback(new Error('Dada de Baja'))
          } else if (result === 200) {
            if (this.reporte.reti_id === 3) {
              callback(new Error('No Retirada'))
            } else {
              this.existe = true
              callback()
            }
          } else if (result === 204) {
            if (this.reporte.reti_id === 1 || this.reporte.reti_id === 8) {
              callback(new Error('Retirada'))
            } else {
              this.existe = true
              callback()
            }
          }
        }).catch(() => {
          this.existe = false
          callback(new Error('Error consultando código'))
        })
      } else {
        console.log('En Validator sin aap_id')
        this.existe = false
        callback()
      }
    }
    return {
      invalid: false,
      labelPosition: 'top',
      loadingElemento: false,
      showAapModal: false,
      activePages: ['1', '2', '3', '4'],
      activePages2: ['2-1', '2-2'],
      nopopover: false,
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      reporte_previo: {
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
        rees_id: 2,
        orig_id: null,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        meams: [],
        eventos: [],
        direcciones: [],
        adicional: []
      },
      evento_siguiente_consecutivo: 1,
      direccion_siguiente_consecutivo: 1,
      autorizacion: '',
      addinputevent: 1,
      coau_tipo: 0,
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
        rees_id: 2,
        orig_id: null,
        barr_id: null,
        empr_id: 0,
        usua_id: 0,
        meams: [],
        eventos: [],
        direcciones: [],
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
          acti_id: null,
          repo_codigo: null,
          repo_apoyo: null,
          urba_id: null,
          muot_id: null,
          autorizacion: null
        }
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
        barr_id: null,
        even_direccion_anterior: null,
        barr_id_anterior: null,
        even_estado: null,
        even_esbaja: null,
        even_valido: {
          aap_id: true,
          aap_direccion: true,
          barr_id: true,
          aatc_id: true,
          aama_id: true,
          aamo_id: true,
          aaco_id: true,
          aap_potencia: true,
          aap_tecnologia: true,
          aap_brazo: true,
          aap_collarin: true,
          tipo_id: true,
          aap_poste_altura: true,
          aap_poste_propietario: true
        },
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
          aap_poste_altura_anterior: null,
          aap_poste_propietario: null,
          aap_poste_propietario_anterior: null
        },
        dato_adicional: {
          aacu_id_anterior: null,
          aacu_id: null,
          aaus_id_anterior: null,
          aaus_id: null,
          medi_id_anterior: null,
          medi_id: null,
          tran_id_anterior: null,
          tran_id: null
        }
      },
      rules: {
        orig_id: [
          { required: true, message: 'Debe Seleccionar el Origen del Reporte', trigger: 'change' }
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
        ],
        adicional: {
          repo_tipo_expansion: [
            { required: false, message: 'Debe Seleccionar el Tipo de Expansión', trigger: 'change' }
          ],
          muot_id: [
            { required: true, message: 'Debe digitar el número de Orden de Trabajo', trigger: 'blur' }
          ],
          urba_id: [
            { required: false, message: 'Debe Seleccionar la Urbanizadora', trigger: 'change' }
          ],
          acti_id: [
            { required: true, message: 'Debe Seleccionar el Tipo de Daño', trigger: 'change' }
          ]
        }
      },
      danhorules: {
        acti_descripcion: [
          { required: true, message: 'Debe Diligenciar la Descripción del nuevo Daño', trigger: 'blur' }
        ]
      },
      matrules: {
        aap_id: [
          { validator: validateAapEventoRule, trigger: 'blur' }
        ]
      },
      dirrules: {
        aap_id: [
          { validator: validateAapEventoRule, trigger: 'blur' },
          { type: 'number', required: true, message: 'Ingrese el código de la luminaria', trigger: 'blur' }
        ],
        even_direccion: [
          { required: true, message: 'Ingrese la nueva dirección de la luminaria', trigger: 'blur' }
        ],
        barr_id: [
          { required: true, message: 'Seleccione el barrio de la luminaria', trigger: 'change' }
        ],
        'dato.aatc_id': [
          { required: true, message: 'Seleccione el tipo de luminaria', trigger: 'change' }
        ],
        'dato.aama_id': [
          { required: true, message: 'Seleccione la marca de la luminaria', trigger: 'change' }
        ],
        'dato.aamo_id': [
          { required: true, message: 'Seleccione el modelo de la luminaria', trigger: 'change' }
        ],
        'dato.aap_tecnologia': [
          { required: true, message: 'Seleccione la tecnología de la luminaria', trigger: 'change' }
        ],
        'dato.aap_potencia': [
          { required: true, message: 'Seleccione la potencia de la luminaria', trigger: 'change' }
        ],
        'dato.aaco_id': [
          { required: true, message: 'Seleccione el tipo de medida de la luminaria', trigger: 'change' }
        ],
        'dato.tipo_id': [
          { required: true, message: 'Seleccione el tipo de poste de la luminaria', trigger: 'change' }
        ],
        'dato.aap_poste_altura': [
          { required: true, message: 'Digite la altura del poste de la luminaria', trigger: 'blur' }
        ],
        'dato.aap_poste_propietario': [
          { required: true, message: 'Seleccione el propietario del poste de la luminaria', trigger: 'change' }
        ],
        'dato.aap_brazo': [
          { required: true, message: 'Digite el tipo de brazo de la luminaria', trigger: 'blur' }
        ],
        'dato.aap_collarin': [
          { required: true, message: 'Digite el tipo de collarin de la luminaria', trigger: 'blur' }
        ],
        'tire_id': [
          { required: true, message: 'Seleccione el Motivo de Retiro', trigger: 'change' }
        ],
        'dato_adicional.aaus_id': [
          { required: true, message: 'Seleccione el Uso', trigger: 'change' }
        ],
        'dato_adicional.aacu_id': [
          { required: true, message: 'Seleccione la Cuenta de Alumbrado', trigger: 'change' }
        ],
        'dato_adicional.medi_id': [
          { required: false, message: 'Seleccione el Medidor', trigger: 'change' }
        ],
        'dato_adicional.tran_id': [
          { required: false, message: 'Seleccione el Transformador', trigger: 'change' }
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
      elementos_n: [],
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
      medidores: [],
      transformadores: [],
      aap_usos: [],
      aap_cuentasap: [],
      tiposretiro: [],
      checkAll: false,
      isIndeterminate: false,
      aap: { aap_id: null },
      tipos: [],
      tiposbarrio: [],
      actividades: [],
      urbanizadoras: [],
      dialogonuevodanhovisible: false,
      actividad: {
        acti_id: null,
        acti_descripcion: null,
        acti_estado: 1,
        usua_id: 0
      },
      centerDialogVisible: false,
      retiradoDialogVisible: false,
      recoveryVisible: false,
      yaretiradoDialogVisible: false,
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
        },
        {
          tiex_id: 5,
          tiex_descripcion: 'URBANIZADORA',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        }
      ],
      datePickerOptions: {
        disabledDate: this.validarFecha
      }
    }
  },
  timers: {
    autosave: { name: 'autosave', time: 10000, autostart: false, repeat: true },
    pending: { name: 'pending', time: 30000, autostart: false, repeat: true }
  },
  methods: {
    validarTipo() {
      if (this.reporte.reti_id === 2) {
        this.reporte.orig_id = 5
        this.rules.adicional.acti_id[0].required = false
        this.rules.adicional.repo_tipo_expansion[0].required = true
        this.validarExpansion()
      } else if (this.reporte.reti_id === 1) {
        this.rules.adicional.acti_id[0].required = true
        this.rules.adicional.repo_tipo_expansion[0].required = false
      } else {
        this.rules.adicional.acti_id[0].required = false
        this.rules.adicional.repo_tipo_expansion[0].required = false
      }
    },
    validarExpansion() {
      if (this.reporte.adicional.repo_tipo_expansion === 5) {
        this.rules.adicional.urba_id[0].required = true
      } else {
        this.rules.adicional.urba_id[0].required = false
      }
    },
    validarFecha(date) {
      const repo_fecha = new Date(this.reporte.repo_fecharecepcion)
      const recepcion = new Date(repo_fecha.getFullYear(), repo_fecha.getMonth(), repo_fecha.getDate())
      const result1 = (date.getTime() >= new Date(recepcion).getTime())
      const result2 = (date.getTime() <= new Date().getTime())
      const result = result1 && result2
      return !result
    },
    autosave() {
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
      localStorage.setItem('currEditRepFecha', JSON.stringify({ fecha: Date.now(), data: this.reporte }))
    },
    pending() {
      /* const _ini = localStorage.getItem('currEditRepFechaIni')
      const _curr = localStorage.getItem('currEditRepFecha')
      if (!this.$equals(_ini, _curr)) {
        this.open7()
      } */
    },
    changeFocus(next) {
      this.$refs[next].focus()
    },
    handleActivePagesChange(val) {
    },
    handleDelete(index, row) {
      this.reporte.eventos.splice(index, 1)
      this.$refs['evento.aap_id'].focus()
    },
    handleCheckAllChange(val) {
      this.reporte.meams = val ? this.medioambiente_keys : []
      this.isIndeterminate = false
    },
    handleReporteMeamChange(value) {
      const meamCount = value.length
      this.checkAll = meamCount === this.medioambiente_keys.length
      this.isIndeterminate = meamCount > 0 && meamCount < this.medioambiente_keys.length
    },
    validate() {
      var valido = true
      if (this.reporte.repo_fechasolucion && this.reporte.repo_horainicio && this.reporte.repo_horafin && (this.reporte.rees_id === 1 || this.reporte.rees_id === 2)) {
        valido = true
        return valido
      } else {
        return false
      }
    },
    validateAap(direccion, id) {
      console.log('Direccion: ' + JSON.stringify(direccion))
      if (direccion.aap_id) {
        this.aap.aap_id = direccion.aap_id
        getAapEdit(direccion.aap_id).then(response => {
          const activo = response.data
          if (activo.aap === null || activo.aap.aap_id < 1 || activo.aap.esta_id === 9) {
            this.existe = false
            this.centerDialogVisible = true
          } else {
            if (direccion.even_estado === 1) {
              direccion.even_direccion_anterior = activo.aap.aap_direccion
              direccion.barr_id_anterior = activo.aap.barr_id
              direccion.dato.aatc_id_anterior = activo.aap.aatc_id
              direccion.dato.aama_id_anterior = activo.aap.aama_id
              direccion.dato.aamo_id_anterior = activo.aap.aamo_id
              direccion.dato.aaco_id_anterior = activo.aap.aaco_id
              direccion.dato.aap_potencia_anterior = activo.aap_adicional.aap_potencia
              direccion.dato.aap_tecnologia_anterior = activo.aap_adicional.aap_tecnologia
              direccion.dato_adicional.aacu_id_anterior = activo.aap.aacu_id
              direccion.dato_adicional.aaus_id_anterior = activo.aap.aaus_id
              direccion.even_direccion = activo.aap.aap_direccion
              direccion.barr_id = activo.aap.barr_id
              direccion.dato.aatc_id = activo.aap.aatc_id
              direccion.dato.aama_id = activo.aap.aama_id
              direccion.dato.aamo_id = activo.aap.aamo_id
              direccion.dato.aaco_id = activo.aap.aaco_id
              direccion.dato.aap_potencia = activo.aap_adicional.aap_potencia
              direccion.dato.aap_tecnologia = activo.aap_adicional.aap_tecnologia
              direccion.dato_adicional.aacu_id = activo.aap.aacu_id
              direccion.dato_adicional.aaus_id = activo.aap.aaus_id
              if (activo.aap_adicional.aap_brazo !== null && activo.aap_adicional.aap_brazo !== undefined) {
                direccion.dato.aap_brazo_anterior = activo.aap_adicional.aap_brazo.toString()
                direccion.dato.aap_brazo = activo.aap_adicional.aap_brazo.toString()
              } else {
                direccion.dato.aap_brazo_anterior = ''
                direccion.dato.aap_brazo = ''
              }
              direccion.dato.aap_collarin_anterior = activo.aap_adicional.aap_collarin
              direccion.dato.tipo_id_anterior = activo.aap_adicional.tipo_id
              direccion.dato.aap_poste_altura_anterior = activo.aap_adicional.aap_poste_altura
              direccion.dato.aap_collarin = activo.aap_adicional.aap_collarin
              direccion.dato.tipo_id = activo.aap_adicional.tipo_id
              direccion.dato.aap_poste_altura = activo.aap_adicional.aap_poste_altura
              if (activo.aap_adicional.aap_poste_propietario !== null && activo.aap_adicional.aap_poste_propietario !== undefined) {
                direccion.dato.aap_poste_propietario_anterior = activo.aap_adicional.aap_poste_propietario
                direccion.dato.aap_poste_propietario = activo.aap_adicional.aap_poste_propietario
              } else {
                direccion.dato.aap_poste_propietario = null
                direccion.dato.aap_poste_propietario_anterior = null
              }
              // validar si es reubicación y no es retirada
              if (this.reporte.reti_id === 3 || this.reporte.reti_id === 7) {
                if (activo.aap.aaco_id !== 3) {
                  this.retiradoDialogVisible = true
                  direccion.even_valido.aap_id = false
                } else {
                  this.retiradoDialogVisible = false
                  direccion.even_valido.aap_id = true
                }
              } else {
                this.retiradoDialogVisible = false
              }
              if (this.reporte.reti_id === 8) {
                if (activo.aap.aaco_id === 3) {
                  this.yaretiradoDialogVisible = true
                  direccion.even_valido.aap_id = false
                } else {
                  this.yaretiradoDialogVisible = false
                  direccion.even_valido.aap_id = true
                }
              }
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
    validateAapEvento(aap_id, id) {
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
    codigoElemento(elem_id) {
      if (elem_id === null || elem_id === undefined) {
        return '-'
      } else {
        return this.elementos_list.find(o => o.elem_id === elem_id, { elem_codigo: '-' }).elem_codigo
      }
    },
    validarCodigoElementoRetirado(elem_id, codigo) {
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
    validarCodigoElementoInstalado(elem_id, codigo) {
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
    alerta(msg) {
      this.$alert(msg, 'Atención', {
        confirmButtonText: 'Continuar'
      })
    },
    aplicar() {
      var valido = true
      this.confirmacionGuardar = false
      var validacion = true
      const start = async() => {
        for (let index = 0; index < this.reporte.direcciones.length; index++) {
          var result = await this.validatForm('dirform_' + this.reporte.direcciones[index].even_id)
          console.log('validación result: ' + result)
          validacion = validacion && result
          console.log('validación: ' + validacion)
        }
        validacion = true
        for (let index = 0; index < this.reporte.eventos.length; index++) {
          result = await this.validatForm('matform_' + this.reporte.eventos[index].even_id)
          console.log('validación result: ' + result)
          validacion = validacion && result
          console.log('validación: ' + validacion)
        }
        valido = validacion && await this.validatForm('reporteForm')
        if (!valido) {
          this.error('1. error en información, por favor verifique')
          return false
        }
        for (var i = 0; i < this.reporte.direcciones.length; i++) {
          if (this.reporte.direcciones[i].even_valido.aap_id === false && this.reporte.direcciones[i].even_estado < 8) {
            console.log('aap_id no valido: ' + this.reporte.direcciones[i].aap_id)
            this.$alert('Código de luminaria no válido: ' + this.reporte.direcciones[i].aap_id, 'Error', {
              confirmationButtonText: 'Cerrar'
            })
            this.error('2. error en información, por favor verifique')
            return false
          }
        }
        for (i = 0; i < this.reporte.eventos.length; i++) {
          if (this.reporte.eventos[i].elem_id === '') {
            this.reporte.eventos[i].elem_id = null
          }
        }
        this.reporte.rees_id = 3
        const data = { reporte: this.reporte, coau_tipo: this.coau_tipo, coau_codigo: this.autorizacion }
        updateReporte(data).then(response => {
          if (response.status === 200) {
            localStorage.setItem('currEditRepFechaIni', JSON.stringify({ fecha: Date.now(), data: this.reporte }))
            this.success()
          } else {
            this.reporte.rees_id = 2
            this.error('Se presentó un inconveniente al guardar los cambios, por favor reintente')
          }
        }).catch(error => {
          this.error(error)
        })
      }
      start()
    },
    async asyncForEach(array, callback) {
      for (let index = 0; index < array.length; index++) {
        await callback(array[index], index, array)
      }
    },
    async validatForm(form) {
      console.log('Form Name: ' + form)
      if (this.$refs[form] !== undefined) {
        if (form.includes('dirform')) {
          const name = form.split('_')
          const index = name[1] - 1
          if (this.reporte.direcciones[index].even_estado <= 8 && this.reporte.direcciones[index].aap_id > 0) {
            var valido = new Promise((resolve, reject) => {
              this.$refs[form][0].validate((valid) => {
                console.log(form + ' validation :' + valid)
                resolve(valid)
              })
            })
            return valido
          }
        } else if (form.includes('matform')) {
          const name = form.split('_')
          const index = name[1] - 1
          if (this.reporte.eventos[index].even_estado <= 8 && this.reporte.eventos[index].aap_id > 0) {
            valido = new Promise((resolve, reject) => {
              this.$refs[form][0].validate((valid) => {
                console.log(form + ' validation :' + valid)
                resolve(valid)
              })
            })
            return valido
          }
        } else {
          valido = new Promise((resolve, reject) => {
            this.$refs[form].validate((valid) => {
              console.log(form + ' validation :' + valid)
              resolve(valid)
            })
          })
          return valido
        }
      }
      return true
    },
    imprimir() {
    },
    success() {
      this.$notify({
        title: this.$i18n.t('reporte.success'),
        message:
          this.$i18n.t('reporte.updated') +
          ' ' +
          this.reporte.repo_consecutivo,
        type: 'success'
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('reporte.error'),
        message: this.$i18n.t('reporte.notupdated') + ' ' + e
      })
    },
    open7() {
      this.$message({
        showClose: true,
        message: 'Atención, existe información por guardar en el reporte.',
        type: 'warning',
        duration: 5000
      })
    },
    onAddEvent() {
      for (var i = 1; i <= this.addinputevent; i++) {
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
          even_id: this.evento_siguiente_consecutivo,
          even_valido: {
            aap_id: true,
            codigo_retirado: true,
            cantidad_retirado: true,
            codigo_instalado: true,
            cantidad_instalado: true
          }
        }
        this.reporte.eventos.push(evento)
        this.evento_siguiente_consecutivo = this.evento_siguiente_consecutivo + 1
      }
    },
    onAddAddress() {
      var direccion = {
        repo_id: this.reporte.repo_id,
        aap_id: null,
        even_id: this.direccion_siguiente_consecutivo,
        even_direccion: null,
        barr_id: null,
        even_direccion_anterior: null,
        barr_id_anterior: null,
        even_estado: 1,
        even_valido: {
          aap_id: true,
          aap_direccion: true,
          barr_id: true,
          aatc_id: true,
          aama_id: true,
          aamo_id: true,
          aaco_id: true,
          aap_potencia: true,
          aap_tecnologia: true,
          aap_brazo: true,
          aap_collarin: true,
          tipo_id: true,
          aap_poste_altura: true,
          aap_poste_propietario: true
        },
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
        },
        dato_adicional: {
          aacu_id_anterior: null,
          aacu_id: null,
          aaus_id_anterior: null,
          aaus_id: null
        }
      }
      this.reporte.direcciones.push(direccion)
      this.direccion_siguiente_consecutivo = this.direccion_siguiente_consecutivo + 1
    },
    limpiarAndBack() {
      this.obtenerReporte()
    },
    elemento(elem_id) {
      if (elem_id === null) {
        return ''
      } else {
        return this.elementos_list.find(o => o.elem_id === elem_id, { elem_descripcion: null }).elem_descripcion
      }
    },
    limpiarElemento() {
      this.elementos = []
      /*
      for (var i = 0; i < this.reporte.eventos.length; i++) {
        if (this.reporte.eventos[i].elem_id !== undefined && this.reporte.eventos[i].elem_id > 0) {
          if (this.elementos.find(e => e.elem_id === this.reporte.eventos[i].elem_id) === undefined) {
            this.elementos.push({ elem_id: this.reporte.eventos[i].elem_id, elem_descripcion: this.elemento(this.reporte.eventos[i].elem_id) })
          }
        }
      }
      */
    },
    getElementos() {
      return this.elementos_list
    },
    reporte_tipo(reti_id) {
      if (reti_id === null) {
        return ''
      } else {
        return this.tipos.find(o => o.reti_id === reti_id, { reti_descripcion: 'INDEFINIDO' }).reti_descripcion
      }
    },
    estado() {
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
    accion(acci_id) {
      if (acci_id === null) {
        return ''
      } else {
        return this.acciones.find(o => o.acci_id === acci_id, { acci_descripcion: null }).acci_descripcion
      }
    },
    abrirReporte() {
      this.$prompt('Por favor ingrese el código de autorización si lo tiene:', 'Confirmación', {
        confirmButtonText: 'Confirmar',
        cancelButtonText: 'Cancelar'
      }).then(({ value }) => {
        validar(3, value).then(response => {
          if (response.data === true) {
            this.autorizacion = value
            this.coau_tipo = 3
            this.$message({
              type: 'success',
              message: 'El código es válido, puede continuar',
              duration: 5000
            })
            this.reporte.rees_id = 2
          } else {
            this.$alert('El código ingresado no es válido, por favor confirmelo', 'Error', {
              confirmButtonText: 'Cerrar'
            })
          }
        }).catch(error => {
          this.$message({
            type: 'error',
            message: 'Se presentó error al válidar el código (' + error + ')',
            duration: 5000
          })
        })
      })
    },
    obtenerReporte() {
      getReporte(this.$route.params.id).then(response => {
        this.reporte_previo = response.data
        if (this.reporte_previo.rees_id === 1) {
          validarReporteDiligenciado(this.reporte_previo.reti_id, this.reporte_previo.repo_consecutivo).then(resp => {
            console.log('data_0:' + resp.data[0])
            if (resp.data[0] === true) {
              this.invalid = false
              this.inicioReporte()
            } else {
              this.$prompt('Por favor ingrese el código de autorización si lo tiene:', 'Primero debe diligenciar el(los) reporte(s) Tipo ' + this.reporte_tipo(this.reporte_previo.reti_id) + ' No(s).' + resp.data[1], 'Atención', {
                confirmButtonText: 'Confirmar',
                cancelButtonText: 'Cancelar'
              }).then(({ value }) => {
                validar(2, value).then(response => {
                  if (response.data === true) {
                    this.invalid = false
                    this.coau_tipo = 2
                    this.autorizacion = value
                    this.$message({
                      type: 'success',
                      message: 'El código es válido, puede continuar',
                      duration: 5000
                    })
                    this.inicioReporte()
                  } else {
                    this.$alert('El código ingresado no es válido, por favor confirmelo', 'Error', {
                      confirmButtonText: 'Cerrar'
                    })
                    this.invalid = true
                  }
                }).catch(error => {
                  this.$message({
                    type: 'error',
                    message: 'Se presentó error al válidar el código (' + error + ')',
                    duration: 5000
                  })
                  this.invalid = true
                })
              }).catch(() => {
                this.$message({
                  type: 'info',
                  message: 'Cancelado',
                  duration: 5000
                })
                this.invalid = true
              })
            }
          }).catch(error => {
            this.invalid = true
            this.$alert('No se pudo validar el estado del reporte anterior. Error: ' + error, 'Error', {
              confirmButtonText: 'Cerrar'
            })
          })
        } else {
          this.inicioReporte(this.reporte_previo)
        }
      }).catch(error => {
        console.log('getReporte: ' + error)
      })
    },
    inicioReporte(reporte) {
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
          acti_id: null,
          repo_codigo: null,
          repo_apoyo: null,
          urba_id: null
        }
      } else {
        this.reporte_previo.adicional.repo_modificado = new Date()
      }
      this.reporte_previo.direcciones.forEach(d => {
        d.even_valido = {
          aap_id: true,
          aap_direccion: true,
          barr_id: true,
          aatc_id: true,
          aama_id: true,
          aamo_id: true,
          aaco_id: true,
          aap_potencia: true,
          aap_tecnologia: true,
          aap_brazo: true,
          aap_collarin: true,
          tipo_id: true,
          aap_poste_altura: true,
          aap_poste_propietario: true
        }
      })
      if (this.reporte_previo.reti_id === 2 || this.reporte_previo.reti_id === 3 || this.reporte_previo.reti_id === 4 || this.reporte_previo.reti_id === 5 || this.reporte_previo.reti_id === 6 || this.reporte_previo.reti_id === 7 || this.reporte_previo.reti_id === 8) {
        this.conDirecciones = true
      } else {
        this.conDirecciones = false
      }
      localStorage.setItem('currEditRepFechaIni', JSON.stringify({ fecha: Date.now(), data: this.reporte }))
      this.cargarEventos()
      this.validarConsecutivo()
      this.reporte = this.reporte_previo
      this.$timer.start('autosave')
      this.$timer.start('pending')
      this.validarTipo()
      this.pending()
    },
    validarConsecutivo() {
      // var consecutivo = 1
      for (var i = 0; i < this.reporte_previo.eventos.length; i++) {
        if (this.reporte_previo.eventos[i].elem_id !== undefined && this.reporte_previo.eventos[i].elem_id > 0) {
          if (this.elementos.find(e => e.elem_id === this.reporte_previo.eventos[i].elem_id) === undefined) {
            this.elementos.push({ elem_id: this.reporte_previo.eventos[i].elem_id, elem_descripcion: this.elemento(this.reporte_previo.eventos[i].elem_id) })
          }
        }
        // consecutivo++
      }
    },
    cargarEventos() {
      // validar si existe un reporte previo
      var stringReporteAnterior = localStorage.getItem('currEditRepFecha')
      if (stringReporteAnterior !== undefined && stringReporteAnterior !== null & stringReporteAnterior !== '') {
        const fecha = JSON.parse(stringReporteAnterior).fecha
        const diferencia = (Date.now() - fecha) / 1000
        console.log('diferencia en tiempo: ' + diferencia)
        if (diferencia < 43200) {
          var currEditRepFecha = JSON.parse(stringReporteAnterior).data
          if (currEditRepFecha.repo_id === this.reporte_previo.repo_id) {
            this.reporte_previo = currEditRepFecha
            this.reporte_previo.adicional.repo_fechadigitacion = new Date()
            this.reporte_previo.adicional.repo_modificado = new Date()
          }
        }
      }
      var even_length = 0
      var dire_length = 0
      this.reporte_previo.eventos.forEach(e => {
        if (e.even_id > even_length) {
          even_length = e.even_id
        }
      })
      this.reporte_previo.eventos.forEach(e => {
        if (e.even_id === undefined || e.even_id === null || e.even_id < 1) {
          e.even_id = even_length + 1
          even_length = even_length + 1
        }
      })

      this.reporte_previo.direcciones.forEach(e => {
        if (e.even_id > dire_length) {
          dire_length = e.even_id
        }
      })
      this.reporte_previo.direcciones.forEach(e => {
        if (e.even_id === undefined || e.even_id === null || e.even_id < 1) {
          e.even_id = dire_length + 1
          dire_length = dire_length + 1
        }
      })

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
            repo_id: this.reporte_previo.repo_id,
            elem_id: null,
            empr_id: 0,
            usua_id: 0,
            even_id: even_length + i
          }
          this.reporte_previo.eventos.push(evento)
        }
        this.evento_siguiente_consecutivo = even_length + 11
      } else {
        this.evento_siguiente_consecutivo = even_length + 1
      }
      if (dire_length === 0) {
        for (i = 1; i <= 4; i++) {
          var direccion = {
            repo_id: this.reporte_previo.repo_id,
            aap_id: null,
            even_id: dire_length + i,
            even_direccion: null,
            barr_id: null,
            even_direccion_anterior: null,
            barr_id_anterior: null,
            even_estado: 1,
            even_valido: {
              aap_id: true,
              aap_direccion: true,
              barr_id: true,
              aatc_id: true,
              aama_id: true,
              aamo_id: true,
              aaco_id: true,
              aap_potencia: true,
              aap_tecnologia: true,
              aap_brazo: true,
              aap_collarin: true,
              tipo_id: true,
              aap_poste_altura: true,
              aap_poste_propietario: true
            },
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
              aap_poste_altura_anterior: null,
              aap_poste_propietario: null,
              aap_poste_propietario_anterior: null
            },
            dato_adicional: {
              aacu_id_anterior: null,
              aacu_id: null,
              aaus_id_anterior: null,
              aaus_id: null
            }
          }
          this.reporte_previo.direcciones.push(direccion)
        }
        this.direccion_siguiente_consecutivo = dire_length + 5
      } else {
        this.direccion_siguiente_consecutivo = dire_length + 1
      }
      this.minDate = this.reporte.repo_fecharecepcion
    },
    remoteMethodElemento(query) {
      if (query !== '') {
        getElementoByDescripcion(query).then(response => {
          this.elementos = response.data
        })
      } else {
        this.elementos = []
      }
    }
  },
  beforeMount() {
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
                                    this.postes.push({ tipo_id: (i + 1), tipo_descripcion: poste[i] })
                                  }
                                  getCaracteristica(9).then(response => {
                                    this.owns = response.data.cara_valores.split(',')
                                    getAapConexiones().then(response => {
                                      this.conexiones = response.data
                                      getAapUsos().then(response => {
                                        this.aap_usos = response.data
                                        getAapCuentasAp().then(response => {
                                          this.aap_cuentasap = response.data
                                          getTiposRetiro().then(response => {
                                            this.tiposretiro = response.data
                                            getUrbanizadoraTodas().then(response => {
                                              this.urbanizadoras = response.data
                                              getMedidors().then(response => {
                                                this.medidores = response.data
                                                getTransformadors().then(response => {
                                                  this.transformadores = response.data
                                                  this.obtenerReporte()
                                                }).catch(error => {
                                                  console.log('Error Transformadores: ' + error)
                                                })
                                              }).catch(error => {
                                                console.log('Error Medidores' + error)
                                              })
                                            })
                                          }).catch(error => {
                                            console.log('get Tipos Retiro: ' + error)
                                          })
                                        }).catch(error => {
                                          console.log(error)
                                        })
                                      }).catch(error => {
                                        console.log(error)
                                      })
                                    }).catch(error => {
                                      console.log('getConexiones :' + error)
                                    })
                                  }).catch(error => {
                                    console.log('Caracteristica 9: ' + error)
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