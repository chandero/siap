<template>
  <el-container>
  <el-header>
    <el-col :span="24">
      <span>{{ $t('informe.sig')}}</span>
    </el-col>
  </el-header>
  <el-main>
  <el-form :label-position="labelPosition">
  <el-row :gutter="4">
    <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
      <el-form-item :label="$t('informe.dueDate')">
        <el-date-picker v-model="fecha_corte" format="yyyy/MM/dd"></el-date-picker>
      </el-form-item>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-button type="primary" @click="obtenerDatos()">{{ $t('informe.process')}}</el-button>
    </el-col>
  </el-row>
  </el-form>
  <el-row>
    <el-col>
  <el-table 
    :data="tableData"
    width="100%"
    height="600px"
    v-loading="loading"
    element-loading-text="Obteniendo Información..."
    stripe 
  >
    <el-table-column 
      :label="$t('informe.aap_id')"
      prop="aap_id"
      width="80"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aap_apoyo')"
      prop="aap_apoyo"
      width="80"
    >
    </el-table-column>  
    <el-table-column 
      :label="$t('informe.esta_descripcion')"
      prop="esta_descripcion"
      width="80"
    >
    </el-table-column>  
    <el-table-column 
      :label="$t('informe.aap_direccion')"
      prop="aap_direccion"
      width="250"
    >
    </el-table-column>   
    <el-table-column 
      :label="$t('informe.barr_descripcion')"
      prop="barr_descripcion"
      width="150"
    >
    </el-table-column>  
    <el-table-column 
      :label="$t('informe.tiba_descripcion')"
      prop="tiba_descripcion"
      width="80"
    >
    </el-table-column>   
    <el-table-column 
      :label="$t('informe.aaus_descripcion')"
      prop="aaus_descripcion"
      width="80"
    >
    </el-table-column>   
    <el-table-column 
      :label="$t('informe.aap_modernizada')"
      :formatter="cellValueRenderer"
      prop="aap_modernizada"
      width="80"
    >
    </el-table-column>               
    <el-table-column 
      :label="$t('informe.aap_modernizada_anho')"
      prop="aap_modernizada_anho"
      width="80"
    >
    </el-table-column>        
    <el-table-column 
      :label="$t('informe.aatc_descripcion')"
      prop="aatc_descripcion"
      width="180"
    >
    </el-table-column>   
    <el-table-column 
      :label="$t('informe.aap_medidor')"
      :formatter="cellValueRenderer"
      prop="aap_medidor"
      width="80"
    >
    </el-table-column>     
    <el-table-column 
      :label="$t('informe.aaco_descripcion')"
      prop="aaco_descripcion"
      width="80"
    >
    </el-table-column>                    
    <el-table-column 
      :label="$t('informe.aama_descripcion')"
      prop="aama_descripcion"
      width="80"
    >
    </el-table-column>                    
    <el-table-column 
      :label="$t('informe.aamo_descripcion')"
      prop="aamo_descripcion"
      width="80"
    >
    </el-table-column>  
    <el-table-column 
      :label="$t('informe.tipo_descripcion')"
      prop="tipo_descripcion"
      width="100"
    >
    </el-table-column>   
    <el-table-column 
      :label="$t('informe.aap_poste_altura')"
      prop="aap_poste_altura"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aap_poste_propietario')"
      prop="aap_poste_propietario"
      width="100"
    >
    </el-table-column> 
    <el-table-column 
      :label="$t('informe.aap_brazo')"
      prop="aap_brazo"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aap_collarin')"
      prop="aap_collarin"
      width="100"
    >
    </el-table-column>           
    <el-table-column 
      :label="$t('informe.aap_potencia')"
      prop="aap_potencia"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.aap_tecnologia')"
      prop="aap_tecnologia"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.aap_rte')"
      prop="aap_rte"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.aap_bombillo')"
      prop="aap_bombillo"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.aap_balasto')"
      prop="aap_balasto"
      width="100"
    >
    </el-table-column>   
    <el-table-column 
      :label="$t('informe.aap_arrancador')"
      prop="aap_arrancador"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.aap_condensador')"
      prop="aap_condensador"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.aap_fotocelda')"
      prop="aap_fotocelda"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.medi_codigo')"
      prop="medi_codigo"
      width="100"
    >
    </el-table-column>
    <el-table-column 
      :label="$t('informe.medi_numero')"
      prop="medi_numero"
      width="100"
    >
    </el-table-column>    
    <el-table-column 
      :label="$t('informe.aacu_descripcion')"
      prop="aacu_descripcion"
      width="150"
    >
    </el-table-column>        
  </el-table>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"        
        :page-size="page_size"
        layout="sizes, prev, pager, next, total"
        :total="total">
      </el-pagination>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
    </el-col>
  </el-row>
  </el-main>
  </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { informe_siap_inventario, informe_siap_inventario_xls } from '@/api/informe'
export default {
  data() {
    return {
      labelPosition: 'top',
      fecha_corte: new Date(),
      tableData: [],
      loading: false,
      page_size: 10,
      current_page: 1,
      total: 0
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  methods: {
    obtenerDatos() {
      this.loading = true
      informe_siap_inventario(this.fecha_corte.getTime(), this.page_size, this.current_page).then(response => {
        this.loading = false
        this.total = response.data.total
        this.tableData = []
        response.data.activos.map(i => {
          this.tableData.push({
            aap_id: i.a.aap_id,
            aap_apoyo: i.a.aap_apoyo,
            esta_descripcion: i.a.esta_descripcion,
            aap_direccion: i.a.aap_direccion,
            barr_descripcion: i.a.barr_descripcion,
            tiba_descripcion: i.a.tiba_descripcion,
            aaus_descripcion: i.a.aaus_descripcion,
            aap_modernizada: i.a.aap_modernizada,
            aap_modernizada_anho: i.a.aap_modernizada_anho,
            aatc_descripcion: i.a.aatc_descripcion,
            aap_medidor: i.a.aap_medidor,
            aaco_descripcion: i.a.aaco_descripcion,
            aama_descripcion: i.a.aama_descripcion,
            aamo_descripcion: i.a.aamo_descripcion,
            aacu_descripcion: i.a.aacu_descripcion,
            tipo_descripcion: i.b.tipo_descripcion,
            aap_poste_altura: i.b.aap_poste_altura,
            aap_poste_propietario: i.b.aap_poste_propietario,
            aap_brazo: i.b.aap_brazo,
            aap_collarin: i.b.aap_collarin,
            aap_potencia: i.b.aap_potencia,
            aap_tecnologia: i.b.aap_tecnologia,
            aap_rte: i.b.aap_rte,
            aap_bombillo: i.b.aap_bombillo,
            aap_balasto: i.b.aap_balasto,
            aap_arrancador: i.b.aap_arrancador,
            aap_condensador: i.b.aap_condensador,
            aap_fotocelda: i.b.aap_fotocelda
          })
        })
      }).catch(error => {
        this.loading = false
        console.log(error)
      })
    },
    exportarXls() {
      informe_siap_inventario_xls(this.fecha_corte.getTime(), this.empresa.empr_id)
    },
    cellValueRenderer(row, column, cellValue, index) {
      let value = ''
      if (cellValue) {
        value = 'X'
      }
      return value
    },
    handleSizeChange(val) {
      this.page_size = val
      this.obtenerDatos()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.obtenerDatos()
    }
  }
}
</script>
<style lang="scss" scoped>
.fa-icon {
  width: auto;
  height: 1em; /* or any other relative font sizes */

  /* You would have to include the following two lines to make this work in Safari */
  max-width: 100%;
  max-height: 100%;
}
</style>
