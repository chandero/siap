<template>
    <el-container class="box-card">
      <el-header>
          <span>{{ $t('route.posteconsultaporcodigo') }}</span>
      </el-header>
      <el-main>
        <el-form :label-position="labelPosition">
          <el-collapse v-model="activePages" @change="handleActivePagesChange">
          <el-collapse-item name="1" :title="$t('gestion.codes')">
           <el-row :gutter="4">
             <el-col :span="8">
               <el-form-item :label="$t('gestion.code')">
                <el-input v-model="activo.aap.aap_id" autofocus @input="activo.aap.aap_id = parseInt($event)" ref="code" @keyup.enter.native="getAap()" @blur="getAap()"></el-input>
               </el-form-item>
             </el-col>
             <el-col :span="8">
                <el-form-item :label="$t('gestion.support')">
                  <el-input readonly v-model="activo.aap.aap_apoyo" ref="support" @keyup.enter.native="changeFocus('description')"></el-input>
                </el-form-item> 
             </el-col>
             <el-col :span="8"><el-form-item :label="$t('gestion.estado')">
                      <span style="font-size:18px; font-weight: bold">{{ activo.esta_id > 0? activo.esta_id === 9? 'Dada de Baja': 'Activa' : '' }}</span>
                     </el-form-item> 
             </el-col>
           </el-row>
          </el-collapse-item>
          <el-collapse-item name="2" :title="$t('gestion.generals')">
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                <el-form-item :label="$t('gestion.description')">
                  <el-input readonly ref="description" v-model="activo.aap.aap_descripcion" @keyup.enter.native="changeFocus('address')"></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="14" :lg="14" :xl="14">
                <el-form-item :label="$t('gestion.address')">
                 <el-input readonly ref="address" v-model="activo.aap.aap_direccion"  @keyup.enter.native="changeFocus('neighborhood')">
                  <el-select ref="neighborhood" slot="append" v-model="activo.aap.barr_id" name="barrio" :placeholder="$t('barrio.select')"  style="width:180px;" @change="changeFocus('latitude')">
                    <el-option disabled v-for="barrio in barrios" :key="barrio.barr_id" :label="barrio.barr_descripcion" :value="barrio.barr_id" >
                    </el-option>   
                  </el-select>
                 </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :span="12">
                <el-form-item :label="$t('gestion.lat')">
                  <el-input readonly ref="latitude" v-model="activo.aap.aap_lat" @keyup.enter.native="changeFocus('longitude')" @focus="$event.target.select()"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item :label="$t('gestion.lng')">
                 <el-input readonly ref="longitude" v-model="activo.aap.aap_lng" @keyup.enter.native="changeFocus('gettime')" @focus="$event.target.select()"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
                <el-form-item :label="$t('gestion.gettime')">
                  <el-date-picker ref="gettime"
                                  readonly
                                  v-model="activo.aap.aap_fechatoma"
                                  type="date"
                                  :placeholder="$t('selectdate')"
                                  @change="changeFocus('brand')">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
                <el-form-item :label="$t('gestion.brand')">
                  <el-select ref="brand" v-model="activo.aap.aama_id" name="brand" :placeholder="$t('brand.select')" @change="changeFocus('model')">
                    <el-option disabled v-for="aapmarca in aap_marcas" :key="aapmarca.aama_id" :label="aapmarca.aama_descripcion" :value="aapmarca.aama_id" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
                <el-form-item :label="$t('gestion.model')">
                  <el-select ref="model" v-model="activo.aap.aamo_id" name="model" :placeholder="$t('model.select')" @change="changeFocus('cover')">
                    <el-option disabled v-for="aapmodel in aap_modelos" :key="aapmodel.aamo_id" :label="aapmodel.aamo_descripcion" :value="aapmodel.aamo_id" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.cover')">
                  <el-select ref="cover" v-model="activo.aap.aatc_id" name="cover" :placeholder="$t('cover.select')" @change="changeFocus('use')">
                   <el-option disabled v-for="aaptipocarcasa in aap_tipos_carcasa" :key="aaptipocarcasa.aatc_id" :label="aaptipocarcasa.aatc_descripcion" :value="aaptipocarcasa.aatc_id" >
                   </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.use')">
                  <el-select ref="use" v-model="activo.aap.aaus_id" name="use" :placeholder="$t('use.select')" @change="changeFocus('connection')">
                    <el-option disabled v-for="aapuso in aap_usos" :key="aapuso.aaus_id" :label="aapuso.aaus_descripcion" :value="aapuso.aaus_id" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col> 
              <el-col :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.connection.title')">
                 <el-select  ref="connection" v-model="activo.aap.aaco_id" name="conexion" :placeholder="$t('gestion.connection.select')" @change="validateConnection()">
                  <el-option disabled v-for="aap_conexion in aap_conexiones" :key="aap_conexion.aaco_id" :label="aap_conexion.aaco_descripcion" :value="aap_conexion.aaco_id" >
                  </el-option>       
                 </el-select>
                </el-form-item>
              </el-col>
              <el-col v-if="activo.aap.aaco_id == 2" :xs="24" :sm="12" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.medidor.title')">
                 <el-select ref="medidor" v-model="activo.aame.medi_id" name="medidor" :placeholder="$t('gestion.medidor.select')">
                  <el-option disabled v-for="m in medidores" :key="m.medi_id" :label="m.medi_numero" :value="m.medi_id" >
                  </el-option>       
                 </el-select>
                </el-form-item>
              </el-col>              
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
                <el-form-item :label="$t('gestion.tecnology.title')">
                  <el-select  ref="tecnology" v-model="activo.aap_adicional.aap_tecnologia" name="tecnology" :placeholder="$t('gestion.tecnology.select')">
                    <el-option disabled v-for="tecno in tecnologias" :key="tecno" :label="tecno" :value="tecno" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.power.title')">
                  <el-select  ref="power" v-model="activo.aap_adicional.aap_potencia" name="power" :placeholder="$t('gestion.power.select')">
                    <el-option disabled v-for="power in potencias" :key="parseFloat(power)" :label="power" :value="parseFloat(power)" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.post.title')">
                  <el-select  ref="post" v-model="activo.aap_adicional.tipo_id" name="post" :placeholder="$t('gestion.post.select')">
                    <el-option disabled v-for="post in postes" :key="post.tipo_id" :label="post.tipo_descripcion" :value="post.tipo_id">
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.post.size')">
                  <el-input readonly type="number" ref="postsize" v-model="activo.aap_adicional.aap_poste_altura" @input="activo.aap_adicional.aap_poste_altura=parseFloat($event)" name="postsize" />
                </el-form-item>
              </el-col>                          
            </el-row>

            <el-row :gutter="4">
              <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
                <el-form-item :label="$t('gestion.post.own')">
                  <el-select ref="postown" v-model="activo.aap_adicional.aap_poste_propietario" name="postown" :placeholder="$t('gestion.post.selectown')">
                    <el-option disabled v-for="own in owns" :key="own" :label="own" :value="own" >
                    </el-option>       
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.arm')">
                  <el-input readonly ref="arm" v-model="activo.aap_adicional.aap_brazo" name="arm" />    
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('gestion.collar')">
                  <el-input readonly ref="collar" v-model="activo.aap_adicional.aap_collarin" name="collar" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.rte')">
                  <el-input readonly ref="rte" v-model="activo.aap_adicional.aap_rte" name="rte" />
                </el-form-item>
              </el-col>                          
            </el-row>

            <el-row :gutter="4">
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.bulb')">
                 <el-input readonly ref="bulb" v-model="activo.aap_elemento.aap_bombillo" name="bulb" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.ballast')">
                  <el-input readonly ref="ballast" v-model="activo.aap_elemento.aap_balasto" name="ballast" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.starter')">
                  <el-input readonly ref="starter" v-model="activo.aap_elemento.aap_arrancador" name="starter" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.condenser')">
                  <el-input readonly ref="condenser" v-model="activo.aap_elemento.aap_condensador" name="condenser" />
                </el-form-item>
              </el-col>                          
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.photocell')">
                  <el-input readonly ref="photocell" v-model="activo.aap_elemento.aap_fotocelda" name="photocell" />
                </el-form-item>
              </el-col>   
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.report')">
                  <el-input readonly ref="report" v-model="activo.aap_elemento.repo_consecutivo" @input="activo.aap_elemento.repo_consecutivo = parseInt($event)" name="report" />
                </el-form-item>
              </el-col>                                     
            </el-row>

          </el-collapse-item>  
          <el-collapse-item name="3" :title="$t('gestion.additional')">
            <el-row :gutter="4">
              <el-col :xs="24" :sm="8" :md="8" :lg="8" :xl="8">
                <el-form-item :label="$t('gestion.account')">
                 <el-select  ref="account" v-model="activo.aap.aacu_id" name="account" :placeholder="$t('account.select')" @change="changeFocus('modern')">
                  <el-option disabled v-for="aapcuentaap in aap_cuentasap" :key="aapcuentaap.aacu_id" :label="aapcuentaap.aacu_descripcion" :value="aapcuentaap.aacu_id" >
                  </el-option>       
                 </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.modern')">
                  <el-checkbox readonly name="modern" ref="modern" v-model="activo.aap.aap_modernizada" @change="changeFocus('counter')"></el-checkbox>
                </el-form-item>
              </el-col>
              <el-col v-if="activo.aap.aap_modernizada" :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.modernyear')">
                  <el-input readonly type="number" name="modernyear" ref="modernyear" v-model="activo.aap_adicional.aap_modernizada_anho" @input="activo.aap_adicional.aap_modernizada_anho=parseInt($event)" ></el-input>
                </el-form-item>
              </el-col>              
              <el-col style="text-align: right;" :xs="24" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('gestion.counter')">
                 <el-checkbox readonly ref="counter" v-model="activo.aap.aap_medidor"></el-checkbox>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item v-if="false" name="4" :title="$t('gestion.elements')">
          <el-form :label-position="labelPosition">
            <el-row>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('accion.select')">
                 <el-select ref="evento.action" v-model="current_event.event.acci_id" name="event.element" :placeholder="$t('accion.select')" @change="changeFocus('evento.element')">
                  <el-option disabled v-for="accion in acciones" :key="accion.acci_id" :label="accion.acci_descripcion" :value="accion.acci_id" >
                  </el-option>       
                 </el-select>
                </el-form-item>                
              </el-col>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('evento.element')">
                 <el-select readonly clearable filterable ref="evento.element" v-model="current_event.event.elem_id" name="evento.element" :placeholder="$t('elemento.select')" @change="changeFocus('evento.code')">
                  <el-option v-for="elemento in elementos" :key="elemento.elem_id" :label="elemento.elem_descripcion" :value="elemento.elem_id" >
                  </el-option>       
                 </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('evento.code')">
                  <el-input readonly ref="evento.code" v-model="current_event.event.even_codigo" @keyup.enter.native.prevent="changeFocus('evento.quantity')" @focus="$event.target.select()"></el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="6" :md="6" :lg="6" :xl="6">
                <el-form-item :label="$t('evento.quantity')">
                  <el-input readonly ref="evento.quantity" v-model="current_event.event.even_cantidad" @keyup.enter.native="onAddElement()"></el-input>
                </el-form-item>
              </el-col>
              <el-col>
                <el-form-item>
                  <el-button ref="evento.add" type="success" @click="onAddElement()">{{ $t('evento.add')}}</el-button>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col>
                <el-table 
                  :data="activo.events"
                  stripe
                  style="width:100%"
                 >
                  <el-table-column
                    :label="$t('evento.action')"
                    width="150"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ accion(scope.row.acci_id) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="$t('evento.element')"
                    width="350">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ elemento(scope.row.elem_id) }}</span>
                    </template>
                  </el-table-column>             
                  <el-table-column
                    :label="$t('evento.codemini')"
                    width="120">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.even_codigo }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="$t('evento.quantity')"
                    width="120">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.even_cantidad }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    fixed="right"
                    width="140">
                  <template slot-scope="scope">
                    <el-button
                      size="mini"
                      type="danger"
                      @click="handleDelete(scope.$index, scope.row)"><i class="el-icon-delete"></i>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table> 
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"        
                :page-size="page_size"
                layout="sizes, prev, pager, next, total"
                :total="total">
              </el-pagination>
              </el-col>
            </el-row>
          </el-form>
          </el-collapse-item>          
         </el-collapse>
        </el-form>
      </el-main>
     <el-footer>
     <!-- <el-button ref="submit" :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar">Guardar Luminaria</el-button> -->
     </el-footer>
      <el-dialog
        title="Consulta"
        :visible.sync="dialogVisible"
        width="30%"
        >
        <span>El Código de Luminaria "<b>{{ activo.aap.aap_id }}</b>" No Existe</span>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="dialogVisible = false">Continuar</el-button>
        </span>
      </el-dialog>
    </el-container>
</template>
<script>
import { getBarriosEmpresa } from '@/api/barrio'
import { getAapTiposCarcasa } from '@/api/aap_tipo_carcasa'
import { getAapCuentasAp } from '@/api/aap_cuentaap'
import { getAapModelos } from '@/api/aap_modelo'
import { getAapConexiones } from '@/api/aap_conexion'
import { getTiposMedidor } from '@/api/tipomedidor'
import { getAapMarcas } from '@/api/aap_marca'
import { getAapMedidorMarcas } from '@/api/aap_medidor_marca'
import { getAapUsos } from '@/api/aap_uso'
import { updateAap, getAapEdit } from '@/api/poste'
import { getElementos } from '@/api/elemento'
import { getAcciones } from '@/api/accion'
import { getCaracteristica } from '@/api/caracteristica'
import { getMedidors } from '@/api/medidor'
/* import { inspect } from 'util" */

export default {
  data() {
    return {
      labelPosition: 'top',
      activePages: ['1', '2', '3', '4'],
      activo: { aap: { aap_id: null }, aap_adicional: { aap_tecnologia: null }, aap_elemento: { aap_bombillo: null }},
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
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0,
      dialogVisible: false
    }
  },
  methods: {
    getAap() {
      if (this.activo.aap.aap_id) {
        const loading = this.$loading({
          lock: true,
          text: 'Buscando....'
        })
        getAapEdit(this.activo.aap.aap_id).then(response => {
          loading.close()
          if (response.status === 200) {
            this.activo = response.data
            if (this.activo.aame === null || this.activo.aame === undefined) {
              this.activo.aame = {
                aap_id: this.activo.aap.aap_id,
                amem_id: null,
                amet_id: null,
                aame_numero: null,
                medi_id: null
              }
            }
            if (this.activo.aap_adicional === null || this.activo.aap_adicional === undefined) {
              this.activo.aap_adicional = {
                aap_brazo: null,
                aap_collarin: null,
                aap_id: this.activo.aap.aap_id,
                aap_modernizada_anho: null,
                aap_poste_altura: null,
                aap_poste_propietario: null,
                aap_potencia: null,
                aap_rte: null,
                aap_tecnologia: null,
                tipo_id: null
              }
              if (this.activo.aap_elemento === null || this.activo.aap_elemento === undefined) {
                this.activos.aap_elemento = {
                  aael_fecha: null,
                  aap_arrancador: null,
                  aap_balasto: null,
                  aap_bombillo: null,
                  aap_condensador: null,
                  aap_fotocelda: null,
                  aap_id: this.activo.aap.aap_id,
                  repo_consecutivo: null,
                  reti_id: null
                }
              }
            }
          } else {
            this.limpiar()
            this.dialogVisible = true
          }
        }).catch(() => {
          loading.close()
          this.limpiar()
          this.dialogVisible = true
        })
      }
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
    handleActivePagesChange(val) {
    },
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
      if (this.activo.aap.aaco_id === 2) {
        this.activo.aap.aap_medidor = true
      } else {
        this.activo.aap.aap_medidor = false
        this.activo.aame.medi_id = null
      }
      this.changeFocus('account')
    },
    aplicar() {
      updateAap(this.activo)
        .then(response => {
          if (response.status === 200) {
            this.success()
          }
        })
        .catch(error => {
          this.error(error)
        })
    },
    validate() {
      if (
        this.activo.aap.aap_id &&
        this.activo.aap.aap_direccion &&
        this.activo.aap.barr_id &&
        this.activo.aap.aap_fechatoma &&
        this.activo.aap.aama_id &&
        this.activo.aap.aamo_id &&
        this.activo.aap.aatc_id &&
        this.activo.aap.aaus_id &&
        this.activo.aap.aaco_id &&
        this.activo.aap.aacu_id &&
        this.activo.aap_adicional.aap_tecnologia &&
        this.activo.aap_adicional.aap_potencia &&
        this.activo.aap_adicional.tipo_id &&
        this.activo.aap_adicional.aap_poste_altura &&
        this.activo.aap_adicional.aap_poste_propietario &&
        this.activo.aap_adicional.aap_brazo !== null &&
        this.activo.aap_adicional.aap_collarin !== null
      ) {
        if (
          this.activo.aap_adicional.aap_tecnologia === 'SODIO' ||
          this.activo.aap_adicional.aap_tecnologia === 'METAL HALIDE'
        ) {
          if (
            this.activo.aap_elemento.aap_bombillo !== null &&
            this.activo.aap_elemento.aap_balasto !== null &&
            this.activo.aap_elemento.aap_arrancador !== null &&
            this.activo.aap_elemento.aap_condensador !== null &&
            this.activo.aap_elemento.aap_fotocelda !== null &&
            this.activo.aap_elemento.aap_bombillo !== '' &&
            this.activo.aap_elemento.aap_balasto !== '' &&
            this.activo.aap_elemento.aap_arrancador !== '' &&
            this.activo.aap_elemento.aap_condensador !== '' &&
            this.activo.aap_elemento.aap_fotocelda !== ''
          ) {
            if (
              this.activo.aap.aaco_id === 2 &&
              this.activo.aame.medi_id == null
            ) {
              return false
            } else {
              return true
            }
          } else {
            return false
          }
        } else {
          if (
            this.activo.aap_elemento.aap_fotocelda !== null &&
            this.activo.aap_elemento.aap_fotocelda !== ''
          ) {
            return true
          } else {
            return false
          }
        }
      } else {
        return false
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
        this.activo.aap.barr_id = ''
      }
    },
    actualizarBarrios() {
      this.barrios = this.barrios_lista
    },
    limpiarAndBack() {
      this.limpiar()
      this.$router.push({ path: '/inventario/gestion' })
    },
    limpiar() {
      const codigo = this.activo.aap.aap_id
      this.activo = {
        aap: {
          aap_id: codigo,
          aap_apoyo: null,
          aap_descripcion: null,
          aap_direccion: '',
          aap_lat: null,
          aap_lng: null,
          barr_id: null,
          empr_id: null,
          esta_id: null,
          aap_fechacreacion: null,
          aap_fechatoma: null,
          aap_modernizada: null,
          aama_id: null,
          aacu_id: null,
          aap_medidor: null,
          aatc_id: null,
          aaus_id: null,
          aaco_id: null,
          usua_id: null
        },
        aap_elemento: {
          aap_bombillo: null
        },
        aame: {
          aame_marca: null,
          aame_tipo: null,
          aame_numero: null,
          medi_id: null
        },
        aap_adicional: {
          aap_id: null,
          tipo_id: null, // tipo poste
          aap_poste_altura: null,
          aap_brazo: null,
          aap_collarin: null,
          aap_potencia: null,
          aap_tecnologia: null,
          aap_modernizada_anho: null,
          aap_rte: null,
          aap_poste_propietario: null
        },
        events: []
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
        message:
          this.$i18n.t('gestion.updated') +
          ' ' +
          this.activo.aap.aap_id +
          ' ' +
          this.activo.aap.aap_direccion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('gestion.error'),
        message: this.$i18n.t('gestion.notupdated') + ' ' + e,
        onClose: this.limpiar()
      })
    }
  },
  mounted() {
    getCaracteristica(7).then(response => {
      this.tecnologias = response.data.cara_valores.split(',')
    }).catch(error => {
      console.log('Caracteristica 7: ' + error)
    })
    getCaracteristica(5).then(response => {
      this.potencias = response.data.cara_valores.split(',')
    }).catch(error => {
      console.log('Caracteristica 5: ' + error)
    })
    getCaracteristica(8).then(response => {
      const poste = response.data.cara_valores.split(',')
      for (var i = 0; i < poste.length; i++) {
        this.postes.push({ tipo_id: (i + 1), tipo_descripcion: poste[i] })
      }
    }).catch(error => {
      console.log('Caracteristica 8: ' + error)
    })
    getCaracteristica(9).then(response => {
      this.owns = response.data.cara_valores.split(',')
    }).catch(error => {
      console.log('Caracteristica 9: ' + error)
    })
    getBarriosEmpresa()
      .then(response => {
        this.barrios = response.data
        this.barrios_lista = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getAapTiposCarcasa()
      .then(response => {
        this.aap_tipos_carcasa = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getAapConexiones()
      .then(response => {
        this.aap_conexiones = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getAapUsos()
      .then(response => {
        this.aap_usos = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getTiposMedidor()
      .then(response => {
        this.tiposmedidor = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getAapMarcas()
      .then(response => {
        this.aap_marcas = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getAapCuentasAp()
      .then(response => {
        this.aap_cuentasap = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getAapModelos()
      .then(response => {
        this.aap_modelos = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getMedidors().then(response => {
      this.medidores = response.data
    }).catch(error => {
      console.log('medidores get: ' + error)
    })
    getAapMedidorMarcas()
      .then(response => {
        this.aap_medidor_marcas = response.data
      })
      .catch(error => {
        console.log(error)
      })
    getElementos().then(response => {
      this.elementos_lista = response.data
      this.elementos = response.data
    }).catch(error => {
      console.log(error)
    })
    getAcciones().then(response => {
      this.acciones = response.data
      this.acciones_lista = response.data
    }).catch(error => {
      console.log(error)
    })
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 100%;
}
div.el-select div.el-input {
  width: 110px;
}
.input-with-select .el-input-group__prepend {
  background-color: #fff;
}
.borders div{
    border-right:1px solid #999;
    border-bottom:1px solid #999;
}
input {
  font-weight: bold;
}
</style>
