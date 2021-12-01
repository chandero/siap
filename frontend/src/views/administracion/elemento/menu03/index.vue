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
<!--                 <el-table-column type="expand">
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
                </el-table-column> -->
        <el-table-column
          :label="$t('elemento.description')"
          width="350"
          prop="elem_descripcion"
        >
          <template slot-scope="scope">
            <span style="margin-left: 10px" :title="scope.row.elem_descripcion">{{ scope.row._3 | fm_truncate(40) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.code')"
          width="100"
          prop="elem_codigo">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row._2 }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.date')"
          width="100"
          prop="elem_codigo">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row._4  | moment('YYYY-MM-DD') }}</span>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('elemento.elpr_precio')"
          width="120"
          prop="elpr_precio"
          align="right"
           >
          <template slot-scope="scope">
            <template v-if="scope.row.edit">
              <el-input v-model="scope.row._5" class="edit-input" size="small" />
              <el-button
                class="cancel-btn"
                size="mini"
                icon="el-icon-close"
                type="warning"
                circle
                @click="cancelEdit(scope.row)"
              />
            </template>
            <span v-else>{{ scope.row._5 | toThousandslsFilter }}</span>
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
      </el-table>
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-size="page_size"
        layout="sizes, prev, pager, next, total"
        :total="total">
      </el-pagination>
    </el-main>
    <img :title="$t('xls')" @click="exportarXls()" style="width:32px; height: 36px; cursor: pointer;" :src="require('@/assets/xls.png')"/>
   </el-container>
  </el-main>
 </el-container>
</template>

<script>
import VueQueryBuilder from 'vue-query-builder'
import { getTodosPrecio, updatePriceElemento, todosPrecioXls } from '@/api/elemento'

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
          type: 'text',
          id: 'e1.elem_descripcion',
          label: this.$i18n.t('elemento.elem_descripcion'),
          operators: ['igual a', 'no igual a', 'contiene a', 'comienza con', 'termina con']
        },
        {
          type: 'text',
          id: 'cast(e1.elem_codigo as int)',
          label: this.$i18n.t('elemento.elem_codigo'),
          operators: ['=', '<>', '<=', '>=']
        }
      ]
    }
  },
  methods: {
    handleSizeChange (val) {
      this.page_size = val
      this.getElementos()
    },
    handleCurrentChange (val) {
      this.current_page = val
      this.getElementos()
    },
    actualizar () {
      this.getElementos()
    },
    nuevo () {
      this.$router.push({ path: '/administracion/elemento/crear' })
    },
    buscar (query) {
      this.filter = 'f:' + query.toUpperCase()
      this.current_page = 1
      this.getElementos()
    },
    getElementos () {
      if (this.qbquery_ant !== this.qbquery) {
        this.current_page = 1
        this.qbquery_ant = this.qbquery
      }
      getTodosPrecio(this.page_size, this.current_page, this.order, this.qbquery)
        .then(response => {
          this.total = response.data.total
          this.tableData = response.data.data.map(v => {
            this.$set(v, 'edit', false) // https://vuejs.org/v2/guide/reactivity.html
            this.$set(v, 'precioOriginal', v._5)
            return v
          })
        }).catch(() => {})
    },
    cancelEdit (row) {
      row._5 = row.precioOriginal
      row.edit = false
      this.$message({
        message: 'El precio se restauró a su valor original',
        type: 'warning'
      })
    },
    confirmEdit (row) {
      row.edit = false
      updatePriceElemento(row._1, row._5).then(response => {
        if (response.data === 'true') {
          this.getElementos()
          this.$message({
            message: 'El precio ha sido modificado',
            type: 'success'
          })
        } else {
          row._5 = row.precioOriginal
          this.$message({
            message: 'El precio se restauró a su valor original',
            type: 'warning'
          })
        }
      }).catch(() => {
        row._5 = row.precioOriginal
        this.$message({
          message: 'El precio se restauró a su valor original',
          type: 'warning'
        })
      })
    },
    exportarXls () {
      todosPrecioXls(this.qbquery)
        .then(response => {
          var blob = response.data
          const d = new Date()
          var _datestring = d.getDate() + '-' + (d.getMonth() + 1) + '-' + d.getFullYear() + '_' + d.getHours() + ':' + d.getMinutes()
          const filename = 'Material_Precio_' + _datestring + '.xlsx'
          if (window.navigator.msSaveOrOpenBlob) {
            window.navigator.msSaveBlob(blob, filename)
          } else {
            var downloadLink = window.document.createElement('a')
            downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }))
            downloadLink.download = filename
            document.body.appendChild(downloadLink)
            downloadLink.click()
            document.body.removeChild(downloadLink)
          }
        })
        .catch(() => {})
    }
  },
  mounted () {
    this.getElementos()
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
