<template>
    <el-container>
      <el-header class="manoherramienta_header">{{ $t('manoherramienta.tabletitle') }}</el-header>
      <el-main>
<!--         <query-builder :labels="qlabels" :rules="qrules" :styled="qstyled" :maxDepth="3" v-model="qbquery"></query-builder>
        <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Actualizar Aplicando el Filtro"></el-button>
 -->      <el-container>
        <el-header>
          <el-button size="mini" type="primary" icon="el-icon-plus" circle @click="nuevo" ></el-button>
          <el-button size="mini" type="success" icon="el-icon-refresh" circle @click="actualizar"></el-button>
        </el-header>
        <el-main>
        <el-table
          :data="tableData"
          stripe
          width="100%" height="500">
      <el-table-column
        :label="$t('manoherramienta.math_descripcion')"
        width="400"
        prop="math_descripcion"
         >
        <template slot-scope="scope">
          <span style="margin-left: 10px" :title="scope.row.math_descripcion">{{ scope.row.math_descripcion | fm_truncate(60) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        fixed="right"
        align="right"
        width="90"
        >
        <template slot-scope="scope">
          <el-button
            size="mini"
            circle
            type="primary"
            @click="handleEdit(scope.$index, scope.row)"><i class="el-icon-edit"></i></el-button>
          <el-button
            size="mini"
            circle
            type="danger"
            @click="handleDelete(scope.$index, scope.row)"><i class="el-icon-delete"></i></el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-main>
 </el-container>
</el-main>
<el-dialog
  :visible.sync="showCrearDialog"
  width="60%"
>
  <el-form>
    <el-form-item label="Descripción">
      <el-input v-model="manoherramienta.math_descripcion" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSaveCrearDialog">Guardar</el-button>
      <el-button type="warning"  @click="handleCloseCrearDialog">Cancelar</el-button>
    </el-form-item>
  </el-form>
</el-dialog>
</el-container>
</template>

<script>
import VueQueryBuilder from 'vue-query-builder'
import { getManoHerramientaOrden, saveManoHerramientaOrden } from '@/api/manoherramienta'

export default {
  /* components: {
    'query-builder': VueQueryBuilder
  }, */
  data () {
    return {
      tableData: [],
      showCrearDialog: false,
      esEditar: false,
      manoherramienta: {
        math_id: null,
        math_descripcion: null,
        math_codigo: 0
      },
      qlabels: {
        matchType: this.$i18n.t('qb.matchType'),
        matchTypes: [
          {
            id: 'all',
            label: this.$i18n.t('qb.matchTypeAll')
          },
          {
            id: 'any',
            label: this.$i18n.t('qb.matchTypeAny')
          }
        ],
        addRule: this.$i18n.t('qb.addRule'),
        removeRule: this.$i18n.t('qb.removeRule'),
        addGroup: this.$i18n.t('qb.addGroup'),
        removeGroup: this.$i18n.t('qb.removeGroup'),
        textInputPlaceholder: this.$i18n.t('qb.textInputPlaceholder')
      },
      qstyled: true,
      qbquery_ant: {},
      qbquery: {},
      qrules: [
        {
          type: 'select',
          id: 'm.math_tipo',
          label: this.$i18n.t('manoherramienta.math_tipo'),
          choices: [],
          operators: ['=']
        },
        {
          type: 'text',
          id: 'm.math_descripcion',
          label: this.$i18n.t('manoherramienta.math_descripcion'),
          operators: ['igual a', 'no igual a', 'contiene a', 'comienza con', 'termina con']
        }
      ]
    }
  },
  methods: {
    handleCloseCrearDialog() {
      this.showCrearDialog = false
      this.manoherramienta.math_descripcion = null
      this.manoherramienta.math_id = null
    },
    handleEdit (index, row) {
      this.manoherramienta.math_id = row.math_id
      this.manoherramienta.math_descripcion = row.math_descripcion
      this.manoherramienta.math_codigo = row.math_codigo
      console.log(index, row)
      this.showCrearDialog = true
    },
    /* handleDelete (index, row) {
      this.$confirm(this.$i18n.t('manoherramienta.confirmationmsg') + ' "' + row.math_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteManoObra(row.math_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getManoObras()
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
    }, */
    handleSizeChange (val) {
      this.page_size = val
      this.getManoObras()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getManoObras()
    },
    actualizar () {
      this.getManoObras()
    },
    nuevo () {
      this.esEditar = false
      this.manoherramienta.math_id = null
      this.manoherramienta.math_descripcion = null
      this.showCrearDialog = true
    },
    getManoObras () {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      getManoHerramientaOrden()
        .then(response => {
          this.tableData = response.data
        }).catch((err) => { console.err('Error al obtener los datos de Transporte y Herramienta -> ' + err) })
    },
    handleSaveCrearDialog () {
      this.showCrearDialog = false
      saveManoHerramientaOrden(this.manoherramienta).then(response => {
        this.$message({
          type: 'success',
          message: this.$i18n.t('general.savesuccessful')
        })
        this.getManoObras()
      }).catch((err) => {
        this.$message({
          type: 'error',
          message: this.$i18n.t('general.savefail') + ' -> ' + err.msg
        })
      })
    }
  },
  mounted () {
    this.qrules[0].choices = this.tiposManoObra
    this.actualizar()
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
