<template>
  <el-container>
    <el-header>
      <span style='font-size: 24px'>{{ $t('route.postegestioncreate') }}</span>
    </el-header>
    <el-main>
      <el-form :label-position='labelPosition'>
        <el-collapse v-model='activePages' @change='handleActivePagesChange'>
          <el-collapse-item name='1' :title="$t('poste.gestion.codes')">
            <el-row :gutter='4'>
              <el-col :span='12'>
                <el-form-item :label="$t('poste.gestion.code')">
                  <el-input
                    autofocus
                    name='code'
                    type='number'
                    v-model='poste.aap_id'
                    @input='poste.aap_id = parseInt($event)'
                    ref='code'
                    @blur='validarNumero($event)'
                    @keyup.enter.native="changeFocus('address')"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item name='2' :title="$t('gestion.generals')">
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='24' :md='14' :lg='14' :xl='14'>
                <el-form-item :label="$t('poste.gestion.address')">
                  <el-input
                    ref='address'
                    v-model='poste.aap_direccion'
                    @input='poste.aap_direccion = $event.toUpperCase()'
                    @keyup.enter.native="changeFocus('neighborhood')"
                  >
                    <el-select
                      filterable
                      clearable
                      ref='neighborhood'
                      slot='append'
                      v-model='poste.barr_id'
                      name='barrio'
                      :placeholder="$t('barrio.select')"
                      style='width:180px'
                      @change="changeFocus('latitude')"
                    >
                      <el-option
                        v-for='barrio in barrios'
                        :key='barrio.barr_id'
                        :label='barrio.barr_descripcion'
                        :value='barrio.barr_id'
                      ></el-option>
                    </el-select>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.post.title')">
                  <el-select
                    clearable
                    filterable
                    ref='post'
                    v-model='poste.tipo_id'
                    name='post'
                    :placeholder="$t('gestion.post.select')"
                  >
                    <el-option
                      v-for='post in postes'
                      :key='post.tipo_id'
                      :label='post.tipo_descripcion'
                      :value='post.tipo_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.post.size')">
                  <el-input
                    ref='postsize'
                    v-model='poste.aap_poste_altura'
                    @input='poste.aap_poste_altura=parseFloat($event)'
                    name='postsize'
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='8' :md='8' :lg='8' :xl='8'>
                <el-form-item :label="$t('gestion.post.own')">
                  <el-select
                    clearable
                    filterable
                    ref='postown'
                    v-model='poste.aap_poste_propietario'
                    name='postown'
                    :placeholder="$t('gestion.post.selectown')"
                  >
                    <el-option v-for='own in owns' :key='own' :label='own' :value='own'></el-option>
                  </el-select>
                </el-form-item>
              </el-col>            
            </el-row>
            <!--
            <el-row :gutter='4'>
              <el-col :span='12'>
                <el-form-item :label="$t('gestion.lat')">
                  <el-input
                    ref='latitude'
                    v-model='activo.aap.aap_lat'
                    @keyup.enter.native="changeFocus('longitude')"
                    @focus='$event.target.select()'
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col :span='12'>
                <el-form-item :label="$t('gestion.lng')">
                  <el-input
                    ref='longitude'
                    v-model='activo.aap.aap_lng'
                    @keyup.enter.native="changeFocus('gettime')"
                    @focus='$event.target.select()'
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            -->
            <!--
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='8' :md='8' :lg='8' :xl='8'>
                <el-form-item :label="$t('gestion.gettime')">
                  <el-date-picker
                    ref='gettime'
                    v-model='activo.aap.aap_fechatoma'
                    type='date'
                    :placeholder="$t('selectdate')"
                    @change="changeFocus('brand')"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='8' :md='8' :lg='8' :xl='8'>
                <el-form-item :label="$t('gestion.brand')">
                  <el-select
                    clearable
                    filterable
                    ref='brand'
                    v-model='activo.aap.aama_id'
                    name='brand'
                    :placeholder="$t('brand.select')"
                    @change="changeFocus('model')"
                  >
                    <el-option
                      v-for='aapmarca in aap_marcas'
                      :key='aapmarca.aama_id'
                      :label='aapmarca.aama_descripcion'
                      :value='aapmarca.aama_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='8' :md='8' :lg='8' :xl='8'>
                <el-form-item :label="$t('gestion.model')">
                  <el-select
                    clearable
                    filterable
                    ref='model'
                    v-model='activo.aap.aamo_id'
                    name='model'
                    :placeholder="$t('model.select')"
                    @change="changeFocus('cover')"
                  >
                    <el-option
                      v-for='aapmodel in aap_modelos'
                      :key='aapmodel.aamo_id'
                      :label='aapmodel.aamo_descripcion'
                      :value='aapmodel.aamo_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            -->
            <!--
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='12' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.cover')">
                  <el-select
                    clearable
                    filterable
                    ref='cover'
                    v-model='activo.aap.aatc_id'
                    name='cover'
                    :placeholder="$t('cover.select')"
                    @change="changeFocus('use')"
                  >
                    <el-option
                      v-for='aaptipocarcasa in aap_tipos_carcasa'
                      :key='aaptipocarcasa.aatc_id'
                      :label='aaptipocarcasa.aatc_descripcion'
                      :value='aaptipocarcasa.aatc_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='12' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.use')">
                  <el-select
                    clearable
                    filterable
                    ref='use'
                    v-model='activo.aap.aaus_id'
                    name='use'
                    :placeholder="$t('use.select')"
                    @change="changeFocus('connection')"
                  >
                    <el-option
                      v-for='aapuso in aap_usos'
                      :key='aapuso.aaus_id'
                      :label='aapuso.aaus_descripcion'
                      :value='aapuso.aaus_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='12' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.connection.title')">
                  <el-select
                    clearable
                    filterable
                    ref='connection'
                    v-model='activo.aap.aaco_id'
                    name='conexion'
                    :placeholder="$t('gestion.connection.select')"
                    @change="validateConnection()"
                  >
                    <el-option
                      v-for='aap_conexion in aap_conexiones'
                      :key='aap_conexion.aaco_id'
                      :label='aap_conexion.aaco_descripcion'
                      :value='aap_conexion.aaco_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col v-if='activo.aap.aaco_id == 2' :xs='24' :sm='12' :md='3' :lg='3' :xl='3'>
                <el-form-item :label="$t('gestion.medidor.title')">
                  <el-select
                    clearable
                    filterable
                    ref='medidor'
                    v-model='activo.aame.medi_id'
                    name='medidor'
                    :placeholder="$t('gestion.medidor.select')"
                  >
                    <el-option
                      v-for='m in medidores'
                      :key='m.medi_id'
                      :label='m.medi_numero'
                      :value='m.medi_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='12' :md='3' :lg='3' :xl='3'>
                <el-form-item :label="$t('gestion.transformador.title')">
                  <el-select
                    clearable
                    filterable
                    ref='transformador'
                    v-model='activo.aatr.tran_id'
                    name='transformador'
                    :placeholder="$t('gestion.transformador.select')"
                  >
                    <el-option
                      v-for='t in transformadores'
                      :key='t.tran_id'
                      :label='t.tran_numero'
                      :value='t.tran_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            -->
            <!--
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='8' :md='8' :lg='8' :xl='8'>
                <el-form-item :label="$t('gestion.tecnology.title')">
                  <el-select
                    clearable
                    filterable
                    ref='tecnology'
                    v-model='activo.aap_adicional.aap_tecnologia'
                    name='tecnology'
                    :placeholder="$t('gestion.tecnology.select')"
                  >
                    <el-option
                      v-for='tecno in tecnologias'
                      :key='tecno'
                      :label='tecno'
                      :value='tecno'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.power.title')">
                  <el-select
                    clearable
                    filterable
                    ref='power'
                    v-model='activo.aap_adicional.aap_potencia'
                    name='power'
                    :placeholder="$t('gestion.power.select')"
                  >
                    <el-option
                      v-for='power in potencias'
                      :key='power'
                      :label='power'
                      :value='parseFloat(power)'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.post.title')">
                  <el-select
                    clearable
                    filterable
                    ref='post'
                    v-model='activo.aap_adicional.tipo_id'
                    name='post'
                    :placeholder="$t('gestion.post.select')"
                  >
                    <el-option
                      v-for='post in postes'
                      :key='post.tipo_id'
                      :label='post.tipo_descripcion'
                      :value='post.tipo_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.post.size')">
                  <el-input
                    ref='postsize'
                    v-model='activo.aap_adicional.aap_poste_altura'
                    @input='activo.aap_adicional.aap_poste_altura=parseFloat($event)'
                    name='postsize'
                  />
                </el-form-item>
              </el-col>
            </el-row>
            -->
            <!--
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='8' :md='8' :lg='8' :xl='8'>
                <el-form-item :label="$t('gestion.post.own')">
                  <el-select
                    clearable
                    filterable
                    ref='postown'
                    v-model='activo.aap_adicional.aap_poste_propietario'
                    name='postown'
                    :placeholder="$t('gestion.post.selectown')"
                  >
                    <el-option v-for='own in owns' :key='own' :label='own' :value='own'></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.arm')">
                  <el-input ref='arm' v-model='activo.aap_adicional.aap_brazo' name='arm' />
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                <el-form-item :label="$t('gestion.collar')">
                  <el-input ref='collar' v-model='activo.aap_adicional.aap_collarin' name='collar' />
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.rte')">
                  <el-input ref='rte' v-model='activo.aap_adicional.aap_rte' name='rte' />
                </el-form-item>
              </el-col>
            </el-row>
            -->
            <!--
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.bulb')">
                  <el-input ref='bulb' v-model='activo.aap_elemento.aap_bombillo' name='bulb' />
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.ballast')">
                  <el-input ref='ballast' v-model='activo.aap_elemento.aap_balasto' name='ballast' />
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.starter')">
                  <el-input
                    ref='starter'
                    v-model='activo.aap_elemento.aap_arrancador'
                    name='starter'
                  />
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.condenser')">
                  <el-input
                    ref='condenser'
                    v-model='activo.aap_elemento.aap_condensador'
                    name='condenser'
                  />
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.photocell')">
                  <el-input
                    ref='photocell'
                    v-model='activo.aap_elemento.aap_fotocelda'
                    name='photocell'
                  />
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.report')">
                  <el-input
                    ref='report'
                    v-model='activo.aap_elemento.repo_consecutivo'
                    @input='activo.aap_elemento.repo_consecutivo = parseInt($event)'
                    name='report'
                  />
                </el-form-item>
              </el-col>
            </el-row>
            -->
          </el-collapse-item>
          <!--
          <el-collapse-item name='3' :title="$t('gestion.additional')">
            <el-row :gutter='4'>
              <el-col :xs='24' :sm='8' :md='8' :lg='8' :xl='8'>
                <el-form-item :label="$t('gestion.account')">
                  <el-select
                    clearable
                    ref='account'
                    v-model='activo.aap.aacu_id'
                    name='account'
                    :placeholder="$t('account.select')"
                    @change="changeFocus('modern')"
                  >
                    <el-option
                      v-for='aapcuentaap in aap_cuentasap'
                      :key='aapcuentaap.aacu_id'
                      :label='aapcuentaap.aacu_descripcion'
                      :value='aapcuentaap.aacu_id'
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.modern')">
                  <el-checkbox
                    name='modern'
                    ref='modern'
                    v-model='activo.aap.aap_modernizada'
                    @change="changeFocus('counter')"
                  ></el-checkbox>
                </el-form-item>
              </el-col>
              <el-col v-if='activo.aap.aap_modernizada' :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.modernyear')">
                  <el-input
                    type='number'
                    name='modernyear'
                    ref='modernyear'
                    v-model='activo.aap_adicional.aap_modernizada_anho'
                  ></el-input>
                </el-form-item>
              </el-col>
              <el-col style='text-align: right' :xs='24' :sm='4' :md='4' :lg='4' :xl='4'>
                <el-form-item :label="$t('gestion.counter')">
                  <el-checkbox ref='counter' v-model='activo.aap.aap_medidor'></el-checkbox>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          -->
          <!--
          <el-collapse-item v-if='false' name='4' :title="$t('gestion.elements')">
            <el-form :label-position='labelPosition'>
              <el-row :gutter='4'>
                <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                  <el-form-item :label="$t('accion.select')">
                    <el-select
                      clearable
                      ref='evento.action'
                      v-model='current_event.event.acci_id'
                      name='event.element'
                      :placeholder="$t('accion.select')"
                      @change="changeFocus('evento.element')"
                    >
                      <el-option
                        v-for='accion in acciones'
                        :key='accion.acci_id'
                        :label='accion.acci_descripcion'
                        :value='accion.acci_id'
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                  <el-form-item :label="$t('evento.element')">
                    <el-select
                      clearable
                      filterable
                      ref='evento.element'
                      v-model='current_event.event.elem_id'
                      name='evento.element'
                      :placeholder="$t('elemento.select')"
                      @change="changeFocus('evento.code')"
                    >
                      <el-option
                        v-for='elemento in elementos'
                        :key='elemento.elem_id'
                        :label='elemento.elem_descripcion'
                        :value='elemento.elem_id'
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                  <el-form-item :label="$t('evento.code')">
                    <el-input
                      ref='evento.code'
                      v-model='current_event.event.even_codigo'
                      @keyup.enter.native.prevent="changeFocus('evento.quantity')"
                      @focus='$event.target.select()'
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col :xs='24' :sm='6' :md='6' :lg='6' :xl='6'>
                  <el-form-item :label="$t('evento.quantity')">
                    <el-input
                      ref='evento.quantity'
                      v-model='current_event.event.even_cantidad'
                      @keyup.enter.native='onAddElement()'
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col>
                  <el-form-item>
                    <el-button
                      ref='evento.add'
                      type='success'
                      @click='onAddElement()'
                    >{{ $t('evento.add')}}</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter='4'>
                <el-col>
                  <el-table :data='activo.events' stripe style='width:100%'>
                    <el-table-column :label="$t('evento.action')" width='150'>
                      <template slot-scope='scope'>
                        <span style='margin-left: 10px'>{{ accion(scope.row.acci_id) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('evento.element')" width='350'>
                      <template slot-scope='scope'>
                        <span style='margin-left: 10px'>{{ elemento(scope.row.elem_id) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('evento.codemini')" width='120'>
                      <template slot-scope='scope'>
                        <span style='margin-left: 10px'>{{ scope.row.even_codigo }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('evento.quantity')" width='120'>
                      <template slot-scope='scope'>
                        <span style='margin-left: 10px'>{{ scope.row.even_cantidad }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column fixed='right' width='140'>
                      <template slot-scope='scope'>
                        <el-button
                          size='mini'
                          type='danger'
                          @click='handleDelete(scope.$index, scope.row)'
                        >
                          <i class='el-icon-delete'></i>
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-pagination
                    @size-change='handleSizeChange'
                    @current-change='handleCurrentChange'
                    :page-size='page_size'
                    layout='sizes, prev, pager, next, total'
                    :total='total'
                  ></el-pagination>
                </el-col>
              </el-row>
            </el-form>
          </el-collapse-item>
          -->
        </el-collapse>
      </el-form>
    </el-main>
    <el-footer>
      <el-button
        :disabled='!validate()'
        ref='submit'
        size='medium'
        type='primary'
        icon='el-icon-check'
        @click='aplicar'
      >Guardar Poste</el-button>
    </el-footer>
  </el-container>
</template>
<script>
import { getBarriosEmpresa } from '@/api/barrio'
import { getAapValidar, saveAap, buscarSiguiente /*, validar */} from '@/api/poste'
import { getCaracteristica } from '@/api/caracteristica'

/* import { inspect } from 'util' */

export default {
  name: 'aap-create',
  data() {
    return {
      canSave: true,
      labelPosition: 'top',
      activePages: ['1', '2', '3', '4'],
      invalid: true,
      poste: {
        aap_id: null,
        empr_id: 0,
        usua_id: 0,
        aap_direccion: null,
        barr_id: null,
        esta_id: 1,
        aap_fechacreacion: new Date(),
        tipo_id: null,
        aap_poste_altura: null,
        aap_poste_propietario: null
      },
      current_event: {
        event: {
          even_id: 0,
          even_codigo: '',
          even_fecha: new Date(),
          even_hora: new Date(),
          even_cantidad: 1,
          even_actual: true,
          even_estado: 1,
          aap_id: 0,
          repo_id: 0,
          elem_id: '',
          acci_id: 1,
          usua_id: 0,
          empr_id: 0
        }
      },
      acciones: [],
      acciones_lista: [],
      barrios_lista: [],
      barrios: [],
      elementos_lista: [],
      elementos: [],
      historia: [],
      aap_marcas: [],
      aap_medidor_marcas: [],
      aap_modelos: [],
      aap_tipos_carcasa_lista: [],
      aap_tipos_carcasa: [],
      aap_cuentasap: [],
      aap_usos: [],
      aap_conexiones: [],
      tiposmedidor: [],
      tecnologias: [],
      potencias: [],
      postes: [],
      owns: [],
      medidores: [],
      transformadores: [],
      message: null,
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0
    }
  },
  methods: {
    validarNumero() {
      this.invalid = false
      getAapValidar(this.poste.aap_id)
        .then(response => {
          if (response.data === 401) {
            this.$alert('El código de poste ya existe y se encuentra en estado ELIMINADA', 'Buscar Poste', {
              confirmButtonText: 'Continuar'
            })
          } if (response.data === 204) {
            this.$alert('El código de poste ya existe y se encuentra en estado RETIRADA', 'Buscar Poste', {
              confirmButtonText: 'Continuar'
            })
          } if (response.data === 200) {
            this.$alert('El código de poste ya existe', 'Buscar Poste', {
              confirmButtonText: 'Continuar'
            })
          } else if (response.data === 404) {
            buscarSiguiente().then(response => {
              /*
              if (this.poste.aap_id !== response.data) {
                this.$prompt(
                  'Por favor ingrese el código de autorización si lo tiene:',
                  'El consecutivo digitado no es el siguiente',
                  {
                    confirmButtonText: 'Confirmar',
                    cancelButtonText: 'Cancelar'
                  }
                )
                  .then(({ value }) => {
                    validar(1, value)
                      .then(response => {
                        if (response.data === true) {
                          this.invalid = false
                          this.activo.autorizacion = value
                          this.$message({
                            type: 'success',
                            message: 'El código es válido, puede continuar',
                            duration: 5000
                          })
                        } else {
                          this.$alert(
                            'El código ingresado no es válido, por favor confirmelo',
                            'Error',
                            {
                              confirmButtonText: 'Cerrar'
                            }
                          )
                          this.invalid = true
                        }
                      })
                      .catch(error => {
                        this.$message({
                          type: 'error',
                          message:
                            'Se presentó error al válidar el código (' +
                            error +
                            ')',
                          duration: 5000
                        })
                        this.invalid = true
                      })
                  })
                  .catch(() => {
                    this.$message({
                      type: 'info',
                      message: 'Cancelado',
                      duration: 5000
                    })
                    this.invalid = true
                  })
              }
              */
            })
          }
        })
        .catch(error => {
          this.$alert('No pude validar si la poste existe, error: ' + error, 'Buscar Poste', {
            confirmButtonText: 'Continuar'
          })
        })
    },
    accion(acci_id) {
      return this.acciones.find(o => o.acci_id === acci_id).acci_descripcion
    },
    elemento(elem_id) {
      return this.elementos.find(o => o.elem_id === elem_id).elem_descripcion
    },
    onAddElement() {
      this.activo.events.push(this.current_event.event)
      this.total = this.activo.events.length
      this.limpiarEvent()
      this.$refs['evento.element'].focus()
    },
    handleDelete(index, row) {
      this.activo.events.splice(index, 1)
    },
    handleActivePagesChange(val) {},
    handleSizeChange(val) {
      this.page_size = val
    },
    handleCurrentChange(val) {
      this.current_page = val
    },
    changeFocus(next) {
      this.$refs[next].focus()
    },
    validateConnection() {
      if (this.poste.aaco_id === 2) {
        this.poste.aap_medidor = true
      } else {
        this.poste.aap_medidor = false
        this.activo.aame.medi_id = null
      }
      this.changeFocus('account')
    },
    aplicar() {
      getAapValidar(this.poste.aap_id)
        .then(response => {
          if (response.data === 401) {
            this.$alert('El código de poste ya existe y se encuentra en estado ELIMINADA', 'Buscar Poste', {
              confirmButtonText: 'Cancelar'
            })
          } if (response.data === 204) {
            this.$alert('El código de poste ya existe y se encuentra en estado RETIRADA', 'Buscar Poste', {
              confirmButtonText: 'Cancelar'
            })
          } if (response.data === 200) {
            this.$alert('El código de poste ya existe', 'Buscar Poste', {
              confirmButtonText: 'Cancelar'
            })
          } else if (response.data === 404) {
            saveAap(this.poste)
              .then(response => {
                if (response.status === 201) {
                  this.invalid = true
                  this.success()
                }
              }).catch(error => {
                this.error(error)
              })
          }
        }).catch(error => {
          this.$alert('No pude validar si el poste existe, error: ' + error, 'Buscar Poste', {
            confirmButtonText: 'Cancelar'
          })
        })
    },
    validate() {
      if (
        this.poste.aap_id &&
        this.poste.aap_direccion &&
        this.poste.barr_id
      ) {
        return true
      }
    },
    remoteMethod(query) {
      if (query !== '') {
        this.loading = true
        setTimeout(() => {
          this.loading = false
          this.barrios = this.barrios_lista.filter(item => {
            console.log('item =>' + item)
            return (
              item.barr_descripcion.toLowerCase().indexOf(query.toLowerCase()) >
              -1
            )
          })
        }, 200)
      } else {
        this.poste.barr_id = ''
      }
    },
    actualizarBarrios() {
      this.barrios = this.barrios_lista
    },
    limpiarAndBack() {
      this.limpiar()
      this.canSave = true
    },
    limpiar() {
      this.poste = {
        aap_id: null,
        aap_direccion: null,
        barr_id: null,
        empr_id: 0,
        esta_id: 1,
        usua_id: 0,
        aap_fechacreacion: null
      }
    },
    limpiarEvent() {
      this.current_event = {
        event: {
          even_id: 0,
          even_codigo: '',
          even_fecha: new Date(),
          even_hora: new Date(),
          even_cantidad: 1,
          even_actual: true,
          even_estado: 1,
          aap_id: 0,
          repo_id: 0,
          elem_id: '',
          acci_id: 1,
          usua_id: 0,
          empr_id: 0
        }
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('gestion.success'),
        message: this.$i18n.t('gestion.created') + ' ' + this.poste.aap_id,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('gestion.error'),
        message: this.$i18n.t('gestion.notcreated') + ' ' + e,
        onClose: this.limpiar()
      })
    }
  },
  mounted() {
    getBarriosEmpresa()
      .then(response => {
        this.barrios = response.data
        this.barrios_lista = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getCaracteristica(8)
      .then(response => {
        const poste = response.data.cara_valores.split(',')
        for (var i = 0; i < poste.length; i++) {
          this.postes.push({ tipo_id: i + 1, tipo_descripcion: poste[i] })
        }
      })
      .catch(error => {
        console.log('Caracteristica 8: ' + error)
      })
    getCaracteristica(9)
      .then(response => {
        this.owns = response.data.cara_valores.split(',')
      })
      .catch(error => {
        console.log('Caracteristica 9: ' + error)
      })
  }
}
</script>

<style rel='stylesheet/scss' lang='scss' scoped>
.box-card {
  width: 100%
}
div.el-select div.el-input {
  width: 110px
}
.input-with-select .el-input-group__prepend {
  background-color: #fff
}
.borders div {
  border-right: 1px solid #999;
  border-bottom: 1px solid #999
}
</style>
