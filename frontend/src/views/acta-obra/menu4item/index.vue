<template>
  <el-container>
    <el-main>
      <el-container>
        <el-header>
          <span>Item de Acta de Obra</span>
        </el-header>
        <el-main>
          <el-row :gutter="4">
            <el-form>
              <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
                <el-form-item label="Acta">
                  <el-select v-model="form.acta_obra" placeholder="Acta de Obra" @change="getItemList"
                    @blur="getItemList">
                    <el-option v-for="item in actas" :key="item.muot_id" :label="item.muot_descripcion"
                      :value="item.muot_id">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
                <el-form-item label="Tipo de Obra">
                  <el-select v-model="form.tipo_obra" placeholder="Tipo de Obra" @change="getItemList"
                    @blur="getItemList">
                    <el-option v-for="item in tipo_obra" :key="item.id" :label="item.nombre" :value="item.id">
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
                <el-form-item>
                  <el-button :disabled="!form.acta_obra || !form.tipo_obra" type="primary"
                    @click="dialogVisible = true">Agregar Item</el-button>
                </el-form-item>
              </el-col>
            </el-form>
          </el-row>
          <el-row>
            <el-col>
              <el-table :data="itemList" stripe>
                <el-table-column width="80" prop="motai_numero" label="Item No.">
                </el-table-column>
                <el-table-column width="600" prop="motai_descripcion" label="Descripción">
                </el-table-column>
                <el-table-column fixed="right" :label="$t('table.accion')" width="150">
                  <template slot-scope="scope">
                    <el-button size="mini" circle type="info" @click="
                      handleEditItem(scope.$index, scope.row)
                      " :title="$t('edit_element')"><i class="el-icon-edit"></i>
                    </el-button>
                    <el-button size="mini" circle type="danger" @click="handleDeleteItem(scope.$index, scope.row)"
                      :title="$t('delete')"><i class="el-icon-delete"></i>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-main>
    <el-dialog title="Item de Acta de Obra" :visible.sync="dialogVisible" width="60%">
      <el-form v-model="item">
        <el-form-item label="Item No.">
          <el-input placeholder="Item No." v-model="item.motai_numero"></el-input>
        </el-form-item>
        <el-form-item label="Descripción">
          <el-input type="textarea" rows="4" placeholder="Descripción" v-model="item.motai_descripcion"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancelar</el-button>
        <el-button type="primary" @click="guardarItem">Guardar</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>
<script>
import { getMuots, getItems, saveItem, updateItem, deleteItem } from '@/api/municipio_orden_trabajo_acta'

export default {
  data() {
    return {
      dialogVisible: false,
      form: {
        acta_obra: null,
        tipo_obra: null
      },
      item: {
        motai_id: null,
        motai_numero: null,
        motai_descripcion: null,
        motai_tipo_obra: null,
        muot_id: null
      },
      itemList: [],
      actas: [],
      tipo_obra: [
        {
          id: 2,
          nombre: 'EXPANSION'
        },
        {
          id: 6,
          nombre: 'MODERNIZACION TECNOLOGIA LED'
        },
        {
          id: 99,
          nombre: 'OBRA'
        }
      ]
    }
  },
  beforeMount() {
    getMuots().then(response => {
      this.actas = response.data
    })
  },
  methods: {
    handleEditItem(index, row) {
      this.item = Object.assign({}, row)
      this.dialogVisible = true
    },
    getItemList() {
      if (this.form.acta_obra && this.form.tipo_obra) {
        getItems(this.form.acta_obra, this.form.tipo_obra).then(response => {
          this.itemList = response.data
        })
      }
    },
    guardarItem() {
      if (this.item.motai_numero && this.item.motai_descripcion) {
        this.item.motai_tipo_obra = parseInt(this.form.tipo_obra)
        this.item.muot_id = parseInt(this.form.acta_obra)
        this.item.motai_numero = parseInt(this.item.motai_numero)
        if (this.item.motai_id === null) {
          this.saveItem()
        } else {
          this.updateItem()
        }
      } else {
        this.$message({
          message: 'Debe llenar todos los campos',
          type: 'warning'
        })
      }
    },
    saveItem() {
      saveItem(this.item).then(response => {
        this.$message({
          message: 'Item guardado con éxito',
          type: 'success'
        })
        this.dialogVisible = false
        this.cleanItem()
        this.getItemList()
      }).catch(error => {
        this.$message({
          message: 'Error al guardar Item: ' + error,
          type: 'error'
        })
        this.dialogVisible = false
        this.cleanItem()
        this.getItemList()
      })
    },
    updateItem() {
      updateItem(this.item).then(response => {
        this.$message({
          message: 'Item guardado con éxito',
          type: 'success'
        })
        this.dialogVisible = false
        this.cleanItem()
        this.getItemList()
      }).catch(error => {
        this.$message({
          message: 'Error al guardar Item: ' + error,
          type: 'error'
        })
        this.dialogVisible = false
        this.cleanItem()
        this.getItemList()
      })
    },
    deleteItem() {
      deleteItem(this.item).then(response => {
        this.$message({
          message: 'Item guardado con éxito',
          type: 'success'
        })
        this.dialogVisible = false
        this.cleanItem()
        this.getItemList()
      }).catch(error => {
        this.$message({
          message: 'Error al guardar Item: ' + error,
          type: 'error'
        })
        this.dialogVisible = false
        this.cleanItem()
        this.getItemList()
      })
    },
    cleanItem() {
      this.item = {
        motai_id: '',
        motai_numero: '',
        motai_descripcion: '',
        motai_tipo_obra: '',
        muot_id: ''
      }
    }
  }
}

</script>
