<template>
    <el-container>
     <el-header slot="header" class="clearfix">
      <span>{{ $t('route.elementoedit') }}</span>
     </el-header>
     <el-main>
       <el-form>
     <span>{{ $t('elemento.description')}}</span>
     <el-input name="descripcion" v-model="elemento.elem_descripcion"></el-input>
     <p/>
     <span>{{ $t('elemento.code')}}</span>
     <el-input name="codigo" v-model="elemento.elem_codigo"></el-input>
     <p/>     
     <span>{{ $t('elemento.ucap')}}</span>
     <el-checkbox name="ucap" v-model="elemento.elem_ucap" ></el-checkbox>
     <p/>
     <span v-if="elemento.elem_ucap">{{ $t('elemento.ucaptipo')}}</span>
     <el-select v-if="elemento.elem_ucap" v-model="elemento.ucap_id" name="tipo" :placeholder="$t('tipoelemento.select')">
        <el-option v-for="u in ucaps" :key="u.ucap_id" :label="u.ucap_descripcion" :value="u.ucap_id" >
        </el-option>       
     </el-select>
     <p/>     
     <span>{{ $t('elemento.type')}}</span>
     <el-select v-model="elemento.tiel_id" name="tipo" :placeholder="$t('tipoelemento.select')">
        <el-option v-for="tipoelemento in tiposElemento" :key="tipoelemento.tiel_id" :label="tipoelemento.tiel_descripcion" :value="tipoelemento.tiel_id" >
        </el-option>       
     </el-select>
     <p/>
     <el-form>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                <el-form-item :label="$t('elemento.characteristic')">
                  <el-select v-model="elemento_caracteristica.cara_id" @change="handleChangeCharacteristic()">
                    <el-option v-for="caracteristica in caracteristicas" :label="caracteristica.cara_descripcion" :key="caracteristica.cara_id" :value="caracteristica.cara_id"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                <el-form-item :label="$t('caracteristica.value')">
                  <el-select v-model="elemento_caracteristica.elca_valor">
                    <el-option v-for="valor in valores" :label="valor" :key="valor" :value="valor"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col>
                <el-form-item>
                  <el-button ref="caracteristica.add" type="success" @click="onAddCharacteristic()">{{ $t('caracteristica.add')}}</el-button>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col>
                <el-table 
                  :data="elemento.caracteristicas"
                  stripe
                  style="width:100%"
                 >
                  <el-table-column
                    :label="$t('elemento.characteristic')"
                    width="250"
                  >
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ caracteristica(scope.row.cara_id) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="$t('caracteristica.value')"
                    width="250">
                    <template slot-scope="scope">
                      <span style="margin-left: 10px">{{ scope.row.elca_valor }}</span>
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
     <el-button :disabled="!validate()" size="medium" type="primary" icon="el-icon-check" @click="aplicar"></el-button>
       </el-form>
     </el-main>
    </el-container>
</template>
<script>
import { saveElemento } from '@/api/elemento'
import { getTiposElemento } from '@/api/tipoelemento'
import { getCaracteristicas } from '@/api/caracteristica'
import { getUcapsTodas } from '@/api/ucap'

export default {
  data() {
    return {
      elemento: {
        elem_descripcion: null,
        elem_codigo: null,
        elem_ucap: false,
        elem_estado: 1,
        tiel_id: null,
        empr_id: 0,
        usua_id: 0,
        ucap_id: null,
        caracteristicas: []
      },
      elemento_caracteristica: {
        elem_id: 0,
        cara_id: null,
        cara_descripcion: null,
        elca_valor: null,
        elca_estado: 1
      },
      tiposElemento: [],
      caracteristicas: [],
      ucaps: [],
      valores: [],
      checkedCaracteristicas: [],
      isIndeterminate: false,
      checkAll: false,
      message: '',
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0
    }
  },
  methods: {
    aplicar() {
      saveElemento(this.elemento).then(response => {
        if (response.status === 201) {
          this.success()
        }
      }).catch(error => {
        this.error(error)
      })
    },
    validate() {
      if (this.elemento.elem_descripcion && this.elemento.elem_codigo && this.elemento.tiel_id > 0) {
        return true
      } else {
        return false
      }
    },
    limpiarAndBack() {
      this.elemento = {
        elem_descripcion: null,
        elem_codigo: null,
        elem_ucap: false,
        elem_estado: 1,
        tiel_id: null,
        empr_id: 0,
        usua_id: 0,
        ucap_id: null
      }
      this.$router.push({ path: '/administracion/elemento' })
    },
    limpiar() {
      this.elemento = {
        elem_descripcion: '',
        elem_codigo: '',
        elem_ucap: false,
        elem_estado: 1,
        tiel_id: '',
        empr_id: 0,
        usua_id: 0
      }
    },
    success() {
      this.$notify({
        title: this.$i18n.t('elemento.success'),
        message: this.$i18n.t('elemento.created') + ' ' + this.elemento.elem_descripcion,
        type: 'success',
        onClose: this.limpiarAndBack()
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('elemento.error'),
        message: this.$i18n.t('elemento.notcreated') + ' ' + e,
        onClose: this.limpiar()
      })
    },
    onAddCharacteristic() {
      this.elemento.caracteristicas.push(this.elemento_caracteristica)
      this.elemento_caracteristica = {
        elem_id: 0,
        cara_id: null,
        cara_descripcion: null,
        elca_valor: null
      }
    },
    handleChangeCharacteristic() {
      if (this.elemento_caracteristica.cara_id === null) {
        this.valores = []
      } else {
        this.valores = this.caracteristicas.find(o => o.cara_id === this.elemento_caracteristica.cara_id).cara_valores.split(',')
      }
    },
    handleSizeChange(val) {
      this.page_size = val
    },
    handleCurrentChange(val) {
      this.current_page = val
    },
    handleDelete(index, row) {
      this.elemento.caracteristicas.splice(index, 1)
    },
    caracteristica(cara_id) {
      if (cara_id === null) {
        return ''
      } else {
        return this.caracteristicas.find(o => o.cara_id === cara_id, '').cara_descripcion
      }
    },
    getTipos() {
      getTiposElemento().then(response => {
        this.tiposElemento = response.data
      }).catch(() => {})
    },
    getCaracteristicas() {
      getCaracteristicas().then(response => {
        this.caracteristicas = response.data
      }).catch(() => {})
    },
    getUcaps() {
      getUcapsTodas().then(response => {
        this.ucaps = response.data
      }).catch(() => {})
    },
    handleCheckAllChange(val) {
      this.checkedCaracteristicas = val ? this.caracteristicas : []
      this.isIndeterminate = false
    },
    handleCheckedCaracteristicasChange(value) {
      const checkedCount = value.length
      this.checkAll = checkedCount === this.caracteristicas.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.caracteristicas.length
    }
  },
  mounted() {
    this.getTipos()
    this.getCaracteristicas()
    this.getUcaps()
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.box-card {
  width: 480px;
}
.footfont {
  font-size: 11px;
}
</style>