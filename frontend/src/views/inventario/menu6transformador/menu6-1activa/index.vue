<template>
      <el-container>
        <el-header class="tranformador_header">{{ $t('gestion.transformador.tabletitle') }}</el-header>
        <el-main>
        <el-container>
          <el-header>
            <el-button size="mini" type="primary" icon="el-icon-circle-plus" circle @click="nuevo" ></el-button>
            <el-button size="mini" type="warning" icon="el-icon-search" circle disabled></el-button>
            <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
          </el-header>
          <el-main>
          <el-table
        :data="tableData.filter(data => !search || data.aap_numero.toLowerCase().includes(search.toLowerCase()) || (data.aap_direccion !== null && data.aap_direccion.toLowerCase().includes(search.toLowerCase())) || (data.barr_descripcion !== null && data.barr_descripcion.toLowerCase().includes(search.toLowerCase())))"
        width="100%" height="500">
    <el-table-column
      :label="$t('gestion.transformador.numero')"
      prop="aap_numero"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_codigo_apoyo')"
      prop="aap_codigo_apoyo"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.direccion')"
      prop="aap_direccion"
      width="250"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.barr_descripcion')"
      prop="barr_id"
      width="200"
    >
    <template slot-scope="scope">
      <span>{{ barrio(scope.row.barr_id) }}</span>
    </template>
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_propietario')"
      prop="aap_propietario"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_marca')"
      prop="aap_marca"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_serial')"
      prop="aap_serial"
      width="120"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_kva')"
      prop="aap_kva"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tipo_id')"
      prop="tipo_id"
      width="100"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_fases')"
      prop="aap_fases"
      width="110"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_tension_p')"
      prop="aap_tension_p"
      width="150"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_tension_s')"
      prop="aap_tension_s"
      width="150"
    >
    </el-table-column>
    <el-table-column
      :label="$t('gestion.transformador.tran_referencia')"
      prop="aap_referencia"
      width="100"
    >
    </el-table-column>
        <el-table-column
          fixed="right"
          align="right"
          width="200"
          >
          <template slot="header">
              <el-input
                v-model="search"
                size="mini"
                placeholder="Filtro">
              </el-input>
          </template>
          <template slot-scope="scope">
            <el-button
              size="mini"
              @click="handleEdit(scope.$index, scope.row)"><i class="el-icon-edit"></i></el-button>
            <el-button
              size="mini"
              type="danger"
              @click="handleDelete(scope.$index, scope.row)"><i class="el-icon-delete"></i></el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-row>
        <el-col :span="24">
          <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
        </el-col>
      </el-row>
    </el-main>
   </el-container>
  </el-main>
 </el-container>
</template>

<script>
import { getTransformadors, deleteTransformador } from '@/api/transformador'
import { informe_siap_transformador_xls } from '@/api/informe'
import { getBarriosEmpresa } from '@/api/barrio'

export default {
  data () {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      search: ''
    }
  },
  methods: {
    handleEdit (index, row) {
      this.$router.push({ path: '/inventario/menu6transformador/menu6-1activa/gestion/edit/' + row.aap_id })
      console.log(index, row)
    },
    handleDelete (index, row) {
      this.$confirm(this.$i18n.t('gestion.transformador.confirmationmsg') + ' "' + row.aap_numero + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteTransformador(row.aap_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getTransformadores()
        }).catch((err) => {
          this.$message({
            type: 'error',
            message: this.$i18n.t('general.deletefail') + ' -> ' + err.msg
          })
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$i18n.t('general.deletecancelled')
        })
      })
      console.log(index, row)
    },
    handleSizeChange (val) {
      this.page_size = val
      this.getTransformadores()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getTransformadores()
    },
    actualizar () {
      this.getTransformadores()
    },
    nuevo () {
      this.$router.push({ path: '/inventario/menu6transformador/menu6-1activa/gestion/create' })
    },
    barrio (id) {
      if (id === null) {
        return ''
      } else {
        return this.barrios.find(e => e.barr_id === id, { barr_descripcion: '' }).barr_descripcion
      }
    },
    getTransformadores () {
      getTransformadors()
        .then(response => {
          this.tableData = response.data
        }).catch(() => {})
    },
    getBarrios () {
      getBarriosEmpresa().then(response => {
        this.barrios = response.data
        this.getTransformadores()
      }).catch(error => {
        console.log(error)
      })
    },
    exportarXls () {
      informe_siap_transformador_xls().then(response => {
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'siap_transformadores.xls')
        document.body.appendChild(link)
        link.click()
      }).catch(error => {
        console.log('informe transformador error:', error)
      })
    }
  },
  mounted () {
    this.getBarrios()
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.header {
  .el-header {
    .empresa_header {
      font-size: 24px;
    }
  }
}
td {
  padding: 4px 0;
}
span.el-pagination__total {
  font-size: 16px;
}
</style>
