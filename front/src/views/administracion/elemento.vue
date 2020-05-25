<template>
      <el-container>
        <el-header class="elemento_header">{{ $t('elemento.tabletitle') }}</el-header>
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
                <el-table-column type="expand">
                  <template slot-scope="props">
                    <el-table 
                      :data="props.row.caracteristicas"
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
                    </el-table> 
                  </template>
                </el-table-column>
                <el-table-column
                  :label="$t('elemento.description')"
                   width="350"
                   prop="elem_descripcion"
                >
                  <template slot-scope="scope">
                    <span style="margin-left: 10px" :title="scope.row.elem_descripcion">{{ scope.row.elem_descripcion | fm_truncate(40) }}</span>
                  </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.code')"
          width="100"
          prop="elem_codigo">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.elem_codigo }}</span>
          </template>
        </el-table-column>             
        <el-table-column
          :label="$t('elemento.type')"
          width="180">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ getTipoElementoDescripcion(scope.row.tiel_id) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_precio')"
          width="120"
          prop="precio.elpr_precio"
          align="right"
           >
          <template slot-scope="scope">
            <template v-if="scope.row.edit">
              <el-input v-model="scope.row.precio.elpr_precio" class="edit-input" size="small" />
              <el-button
                class="cancel-btn"
                size="mini"
                icon="el-icon-close"
                type="warning"
                circle
                @click="cancelEdit(scope.row)"
              />
            </template>
            <span v-else>{{ scope.row.precio.elpr_precio | toThousandslsFilter }}</span>
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
import { getTodos } from '@/api/elemento'
import { deleteElemento, updatePriceElemento } from '@/api/elemento'
import { getTiposElemento } from '@/api/tipoelemento'
import { getCaracteristicas } from '@/api/caracteristica'

export default {
  components: {
    'query-builder': VueQueryBuilder
  },
  data() {
    return {
      tableData: [],
      page_size: 10,
      current_page: 1,
      total: 0,
      tiposElemento: [],
      caracteristicas: [],
      order: '',
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
          id: 'e.tiel_tipo',
          label: this.$i18n.t('elemento.type'),
          choices: [],
          operators: ['=']
        },
        {
          type: 'text',
          id: 'e.elem_descripcion',
          label: this.$i18n.t('elemento.elem_descripcion'),
          operators: ['igual a', 'no igual a', 'contiene a', 'comienza con', 'termina con']
        },
        {
          type: 'text',
          id: 'e.elem_codigo',
          label: this.$i18n.t('elemento.elem_codigo'),
          operators: ['=', '<>', '<=', '>=']
        }
      ]
    }
  },
  methods: {
    handleEdit(index, row) {
      this.$router.push({ path: '/administracion/elemento/editar/' + row.elem_id })
      console.log(index, row)
    },
    handleDelete(index, row) {
      this.$confirm(this.$i18n.t('elemento.confirmationmsg') + ' "' + row.elem_descripcion + '"', this.$i18n.t('general.warning'), {
        confirmButtonText: this.$i18n.t('general.ok'),
        cancelButtonText: this.$i18n.t('general.cancel'),
        type: 'warning'
      }).then(() => {
        deleteElemento(row.elem_id).then(response => {
          this.$message({
            type: 'success',
            message: this.$i18n.t('general.deletesuccessful')
          })
          this.getElementos()
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
    handleSizeChange(val) {
      this.page_size = val
      this.getElementos()
    },
    handleCurrentChange(val) {
      this.current_page = val
      this.getElementos()
    },
    actualizar() {
      this.getElementos()
    },
    nuevo() {
      this.$router.push({ path: '/administracion/elemento/crear' })
    },
    buscar(query) {
      this.filter = 'f:' + query.toUpperCase()
      this.current_page = 1
      this.getElementos()
    },
    getElementos() {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      getTodos(this.page_size, this.current_page, this.order, this.qbquery)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.elementos.map(v => {
            this.$set(v, 'edit', false) // https://vuejs.org/v2/guide/reactivity.html
            this.$set(v, 'precioOriginal', v.precio.elpr_precio)
            return v
          })
        }).catch(() => {})
    },
    getTipoElementoDescripcion(id) {
      console.log('tiel_id : ' + id)
      if (id !== undefined && id !== null && id > 0) {
        if (this.tiposElemento) {
          return this.tiposElemento.find(o => o.tiel_id === id, { tiel_descripcion: '' }).tiel_descripcion
        } else {
          return ''
        }
      } else { return '' }
    },
    caracteristica(cara_id) {
      if (cara_id === null) {
        return ''
      } else {
        return this.caracteristicas.find(o => o.cara_id === cara_id, '').cara_descripcion
      }
    },
    getCaracteristicas() {
      getCaracteristicas().then(response => {
        this.caracteristicas = response.data
        this.getElementos()
      }).catch(() => {})
    },
    cancelEdit(row) {
      row.precio.elpr_precio = row.precioOriginal
      row.edit = false
      this.$message({
        message: 'El precio se restauró a su valor original',
        type: 'warning'
      })
    },
    confirmEdit(row) {
      row.edit = false
      updatePriceElemento(row.elem_id, row.precio.elpr_anho, row.precio.elpr_precio).then(response => {
        if (response.data === 'true') {
          row.precioOriginal = row.precio.elpr_precio
          this.$message({
            message: 'El precio ha sido modificado',
            type: 'success'
          })
        } else {
          row.precio.elpr_precio = row.precioOriginal
          this.$message({
            message: 'El precio se restauró a su valor original',
            type: 'warning'
          })
        }
      }).catch(() => {
        row.precio.elpr_precio = row.precioOriginal
        this.$message({
          message: 'El precio se restauró a su valor original',
          type: 'warning'
        })
      })
    }
  },
  mounted() {
    getTiposElemento().then(response => {
      this.tiposElemento = response.data
      this.tiposElemento.forEach(i => this.qrules[0].choices.push({ label: i.tiel_descripcion, value: i.tiel_id }))
      this.getCaracteristicas()
    }).catch(() => {})
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