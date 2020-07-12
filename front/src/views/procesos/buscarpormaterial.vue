<template>
  <el-container class="gestion-container">
    <el-main>
      <el-container>
        <el-header class="gestion_header">{{ $t('buscar.pormaterial') }}
        </el-header>
        <el-main>
          <el-form label-position="top">
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item label="Tipo de Material">
                  <el-select v-model="form.tiel_id" name="tipo" :placeholder="$t('tipoelemento.select')">
                    <el-option v-for="tipoelemento in tiposElemento" :key="tipoelemento.tiel_id" :label="tipoelemento.tiel_descripcion" :value="tipoelemento.tiel_id" >
                   </el-option>       
                  </el-select>
                </el-form-item>                
              </el-col>
              <el-col :xs="20" :sm="20" :md="8" :lg="8" :xl="8">
                <el-form-item  label="Código a Buscar">
                  <el-input v-model="form.codigo" />
                </el-form-item>
              </el-col>
              <el-col :xs="4" :sm="4" :md="4" :lg="4" :xl="4">
                <el-form-item>
                  <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Buscar Aplicando el Filtro"></el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-main>        
      </el-container>
      <el-container>
        <el-main>
          <el-table
        :data="tableData"
        stripe
        :default-sort = "{prop: 'aap_id', order: 'ascending'}"        
        style="width: 100%"
        max-height="600"
        border>
        <el-table-column type="expand">
          <template slot-scope="props">
                <el-table 
                  :data="Array(props.row.aap_elemento)"
                  stripe
                  style="width:100%"
                 >
                  <el-table-column
                    :label="$t('elemento.fecha')"
                    width="110"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aael_fecha | moment('YYYY/MM/DD') }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="$t('elemento.reporte')"
                    width="110"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.repo_consecutivo }}</span>
                    </template>
                  </el-table-column>                  
                  <el-table-column
                    :label="$t('elemento.bombillo')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_bombillo }}</span>
                    </template>
                  </el-table-column>             
                  <el-table-column
                    :label="$t('elemento.balasto')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_balasto }}</span>
                    </template>
                  </el-table-column>             
                  <el-table-column
                    :label="$t('elemento.arrancador')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_arrancador }}</span>
                    </template>
                  </el-table-column>  
                  <el-table-column
                    :label="$t('elemento.condensador')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_condensador }}</span>
                    </template>
                  </el-table-column>             
                  <el-table-column
                    :label="$t('elemento.fotocelda')"
                    width="100">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.aap_fotocelda }}</span>
                    </template>
                  </el-table-column>             
              </el-table> 
          </template>
        </el-table-column>        
        <el-table-column
          :label="$t('gestion.code')"
          width="150"
          sortable="custom"
          prop="aap_id"
          resizable
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_id }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('gestion.support')"
          width="150"
          sortable="custom"
          prop="aap_apoyo"   
          resizable       
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_apoyo }}</span>
          </template>
        </el-table-column>
        <!--
        <el-table-column
          :label="$t('gestion.description')"
          width="200"
          sortable="custom"
          prop="aap_descripcion"  
          resizable        
           >
          <template slot-scope="scope">
            <span >{{ scope.row.aap_descripcion }}</span>
          </template>
        </el-table-column>
        -->
        <el-table-column
          :label="$t('gestion.address')"
          min-width="250"
          sortable="custom"
          prop="aap_direccion" 
          resizable
          >
          <template slot-scope="scope">
            <span >{{ scope.row.aap_direccion }}</span>
          </template>
        </el-table-column>   
        <el-table-column
          :label="$t('gestion.neighborhood')"
          min-width="250"
          sortable="custom"
          prop="barr_id"
          resizable
          >
          <template slot-scope="scope">
            <span >{{ barrio(scope.row.barr_id) }}</span>
          </template>
        </el-table-column>   
        <el-table-column
          :label="$t('gestion.neighborhoodtype')"
          width="200"
          sortable="custom"
          prop="tiba_id">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ sector(scope.row.tiba_id) }}</span>
          </template>
        </el-table-column>                     
        <el-table-column :label="$t('gestion.georeference')">
        <el-table-column
          :label="$t('gestion.lat')"
          width="120">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_lat }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('gestion.lng')"
          width="120">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.aap_lng }}</span>
          </template>
        </el-table-column>
        </el-table-column>
        <el-table-column 
          :label="$t('gestion.connection.title')"
          width="120"
          prop="aaco_id"
          resizable
          >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ aap_conexion(scope.row.aaco_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          :label="$t('table.accion')"
          width="93">
          <template slot-scope="scope">
            <el-button
              size="mini"
              circle
              type="warning"
              @click="handleEdit(scope.$index, scope.row)"
              title="Modificar Luminaria"><i class="el-icon-edit"></i></el-button>
            <el-button
              size="mini"
              circle
              type="danger"
              title="Borrar Luminaria"
              @click="handleDelete(scope.$index, scope.row)"><i class="el-icon-delete"></i></el-button>
          </template>
        </el-table-column>
      </el-table> 
    </el-main>
   </el-container>
  </el-main>
 </el-container>
</template>

<script>
import { buscarPorMaterial } from '@/api/aap'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getAapConexiones } from '@/api/aap_conexion'
import { getAapUsos } from '@/api/aap_uso'
import { getElementos } from '@/api/elemento'
import { getAapTiposCarcasa } from '@/api/aap_tipo_carcasa'
import { getCaracteristica } from '@/api/caracteristica'
import { getAapModelos } from '@/api/aap_modelo'
import { getTiposElemento } from '@/api/tipoelemento'

export default {
  data() {
    return {
      form: {
        tiel_id: null,
        codigo: null
      },
      tableData: [],
      tiposElemento: [],
      barrios: [],
      sectores: [],
      aap_conexiones: [],
      aap_usos: [],
      elementos: [],
      tipobarrio_filters: [],
      barrios_filters: [],
      potencias: [],
      tecnologias: [],
      aap_modelos: [],
      aap_tipos_carcasa: [],
      order: '',
      filter: ''
    }
  },
  methods: {
    actualizar() {
      buscarPorMaterial(this.form.codigo, this.form.tiel_id)
        .then(response => {
          this.tableData = response.data
        }).catch(() => {})
    },
    barrio(barr_id) {
      if (barr_id === null || barr_id === undefined || barr_id === 0) {
        return ''
      } else {
        if (this.barrios.length > 0) {
          return this.barrios.find(o => o.barr_id === barr_id, { barr_descripcion: '' }).barr_descripcion
        } else { return '' }
      }
    },
    sector(tiba_id) {
      if (tiba_id === null || tiba_id === undefined || tiba_id === 0) {
        return ''
      } else {
        if (this.sectores.length > 0) {
          return this.sectores.find(o => o.tiba_id === tiba_id, { tiba_descripcion: '' }).tiba_descripcion
        }
      }
    },
    elemento(elem_id) {
      if (elem_id === null || elem_id === undefined || elem_id === 0) {
        return ''
      } else {
        return this.elementos.find(o => o.elem_id === elem_id, { elem_descripcion: '' }).elem_descripcion
      }
    },
    aap_conexion(aaco_id) {
      if (aaco_id === null || aaco_id === undefined || aaco_id === 0) {
        return ''
      } else {
        if (this.aap_conexiones.length > 0) {
          return this.aap_conexiones.find(o => o.aaco_id === aaco_id, { aaco_descripcion: '' }).aaco_descripcion
        }
      }
    }
  },
  created() {
    getTiposBarrio().then(response => {
      this.sectores = response.data
      getElementos().then(response => {
        response.data.forEach(b => {
          this.elementos.push(b)
        })
        getAapConexiones().then(response => {
          response.data.forEach(b => {
            this.aap_conexiones.push(b)
          })
          getAapUsos().then(response => {
            response.data.forEach(b => {
              this.aap_usos.push(b)
            })
            getBarriosEmpresa().then(response => {
              this.barrios = response.data
              getCaracteristica(7).then(response => {
                this.tecnologias = response.data.cara_valores.split(',')
                getCaracteristica(5).then(response => {
                  this.potencias = response.data.cara_valores.split(',')
                  getAapTiposCarcasa().then(response => {
                    this.aap_tipos_carcasa = response.data
                    getAapModelos().then(response => {
                      this.aap_modelos = response.data
                      getTiposElemento().then(response => {
                        this.tiposElemento = response.data
                      }).catch(() => {})
                    }).catch(error => {
                      console.log(error)
                    })
                  }).catch(error => {
                    console.log(error)
                  })
                }).catch(error => {
                  console.log('Caracteristica 5: ' + error)
                })
              }).catch(error => {
                console.log('Caracteristica 7: ' + error)
              })
            }).catch(error => {
              console.log(error)
            })
          })
        }).catch(error => {
          console.log(error)
        })
      }).catch(error => {
        console.log(error)
      })
    }).catch(error => {
      console.log(error)
    })
  }
}
</script>

<style scoped>
td {
  padding: 4px 0;
}
span.el-pagination__total {
  font-size: 16px;
}
div.match-type-container {
    color: #333;
    background-color: #f5f5f5;
    border-color: #ddd;
}
.vue-query-builder >>> .vqb-group {
    color: #333;
    background-color: #f5f5f5;
    border-color: #ddd;
}
.vue-query-builder >>> .vqb-group.depth-2 {
    border-left: 2px solid #8bc34a;
}

.vue-query-builder >>> .vqb-group.depth-3 {
    border-left: 2px solid #ffb94b;
}

.vue-query-builder >>> .panel {
  margin-bottom: 20px;
  background-color: #fff;
  border: 1px solid transparent;
  border-radius: 4px;
  box-shadow: 0 1px 1px rgba(0,0,0,0.05)
}
.vue-query-builder >>> .panel-default {
  border-color: #ddd;
}

.vue-query-builder >>> .pull-right {
  float: right!important;
}

.vue-query-builder >>> .btn {
    display: inline-block;
    line-height: 1;
    white-space: nowrap;
    cursor: pointer;
    background: #fff;
    border: 1px solid #dcdfe6;
    color: #606266;
    -webkit-appearance: none;
    text-align: center;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    outline: 0;
    margin: 0;
    -webkit-transition: .1s;
    transition: .1s;
    font-weight: 500;
    padding: 12px 20px;
    font-size: 14px;
    border-radius: 4px;
}
.vue-query-builder >>> .btn:hover {
    color: #409eff;
    border-color: #c6e2ff;
    background-color: #ecf5ff;
}
.vue-query-builder >>> .btn-default {
  
}
</style>