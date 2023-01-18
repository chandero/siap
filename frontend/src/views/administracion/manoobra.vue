<template>
      <el-container>
        <el-header class="manoobar_header">{{ $t('manoobra.tabletitle') }}</el-header>
        <el-main>
          <query-builder :labels="qlabels" :rules="qrules" :styled="qstyled" :maxDepth="3" v-model="qbquery"></query-builder>
          <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Actualizar Aplicando el Filtro"></el-button>
        <el-container>
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
          :label="$t('manoobra.maob_tipo')"
          width="130"
          prop="maob_tipo"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ getTipoManoObraDescripcion(scope.row.maob_tipo) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('manoobra.maob_descripcion')"
          width="500"
          prop="maob_descripcion"
           >
          <template slot-scope="scope">
            <span style="margin-left: 10px" :title="scope.row.maob_descripcion">{{ scope.row.maob_descripcion | fm_truncate(60) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('manoobra.mopr_precio')"
          width="120"
          prop="precio.mopr_precio"
          align="right"
           >
          <template slot-scope="scope">
            <template v-if="scope.row.edit">
              <el-input v-model="scope.row.precio.mopr_precio" class="edit-input" size="small" />
              <el-button
                class="cancel-btn"
                size="mini"
                icon="el-icon-close"
                type="warning"
                circle
                @click="cancelEdit(scope.row)"
              />
            </template>
            <span v-else>{{ scope.row.precio.mopr_precio | toThousandslsFilter }}</span>
            <el-button
              v-if="scope.row.edit"
              circle
              size="mini"
              icon="el-icon-check"
              @click="confirmEdit(scope.row)"
            />
            <el-button
              v-else
              circle
              size="mini"
              icon="el-icon-edit"
              style="border-style: hidden;"
              @click="scope.row.edit=!scope.row.edit"
            />
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
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-size="page_size"
        layout="sizes, prev, pager, next, total"
        :total="total">
      </el-pagination>
    </el-main>
   </el-container>
  </el-main>
 </el-container>
</template>

<script>
import VueQueryBuilder from 'vue-query-builder'
import { getTodos, deleteManoObra, updatePriceManoObra } from '@/api/manoobra'

export default {
  components: {
    'query-builder': VueQueryBuilder
  },
  data () {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      search: '',
      order: '',
      tiposManoObra: [
        { value: 1, label: 'Mano de Obra' },
        { value: 2, label: 'Herramienta' },
        { value: 3, label: 'Transporte' },
        { value: 4, label: 'Manejo Ambiental' }
      ],
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
          id: 'm.maob_tipo',
          label: this.$i18n.t('manoobra.maob_tipo'),
          choices: [],
          operators: ['=']
        },
        {
          type: 'text',
          id: 'm.maob_descripcion',
          label: this.$i18n.t('manoobra.maob_descripcion'),
          operators: ['igual a', 'no igual a', 'contiene a', 'comienza con', 'termina con']
        }
      ]
    }
  },
  methods: {
    handleEdit (index, row) {
      this.$router.push({ path: '/administracion/manoobra/editar/' + row.maob_id })
      console.log(index, row)
    },
    handleDelete (index, row) {
      this.$confirm(this.$i18n.t('manoobra.confirmationmsg') + ' "' + row.maob_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteManoObra(row.maob_id).then(response => {
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
    },
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
      this.$router.push({ path: '/administracion/manoobra/crear' })
    },
    getManoObras () {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      getTodos(this.page_size, this.current_page, this.order, this.qbquery)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.manoobras.map(v => {
            this.$set(v, 'edit', false) // https://vuejs.org/v2/guide/reactivity.html
            this.$set(v, 'precioOriginal', v.precio.mopr_precio)
            return v
          })
        }).catch(() => {})
    },
    getTipoManoObraDescripcion (id) {
      if (id !== undefined & id !== null) {
        if (this.tiposManoObra) {
          return this.tiposManoObra.find(o => o.value === id, { label: '' }).label
        } else {
          return 'SIN DEFINIR'
        }
      } else { return 'SIN DEFINIR 2' }
    },
    cancelEdit (row) {
      row.precio.mopr_precio = row.precioOriginal
      row.edit = false
      this.$message({
        message: 'El precio se restauró a su valor original',
        type: 'warning'
      })
    },
    confirmEdit (row) {
      row.edit = false
      updatePriceManoObra(row.maob_id, row.precio.mopr_anho, row.precio.mopr_precio).then(response => {
        if (response.data === 'true') {
          row.precioOriginal = row.precio.mopr_precio
          this.$message({
            message: 'El precio ha sido modificado',
            type: 'success'
          })
        } else {
          row.precio.mopr_precio = row.precioOriginal
          this.$message({
            message: 'El precio se restauró a su valor original',
            type: 'warning'
          })
        }
      }).catch(() => {
        row.precio.mopr_precio = row.precioOriginal
        this.$message({
          message: 'El precio se restauró a su valor original',
          type: 'warning'
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
