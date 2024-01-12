<template>
    <el-container>
        <el-header>
            <span>IPP / IPC Anual</span>
        </el-header>
        <el-main>
          <el-row>
            <el-col>
              <el-button type="primary" circle icon="el-icon-plus" @click="handleNew"></el-button>
              <el-button type="warining" circle icon="el-icon-refresh" @click="getIppIpc"></el-button>
            </el-col>
          </el-row>
          <el-row>
            <el-table
                :data="tableData"
                stripe
                style="width:100%"
                highlight-current-row
            >
                <el-table-column
                    prop="ucap_ipp_anho"
                    label="Año"
                    width="100"
                >
                    <template slot-scope="scope">
                        <span>{{ scope.row.ucap_ipp_anho }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="ucap_ipp_valor"
                    label="IPP"
                    width="80"
                    header-align="center"
                    align="right"
                >
                    <template slot-scope="scope">
                        <span>{{ scope.row.ucap_ipp_valor }}</span>
                    </template>
                </el-table-column>
                <el-table-column
                    prop="ucap_ipc_valor"
                    label="IPC"
                    width="80"
                    header-align="center"
                    align="right"
                >
                    <template slot-scope="scope">
                        <span>{{ scope.row.ucap_ipc_valor }}</span>
                    </template>
                </el-table-column>
                <el-table-column>
                    <template slot-scope="scope">
                        <el-button circle type="primary" icon="el-icon-edit" @click="handleEdit(scope.row)"></el-button>
                        <el-popconfirm
                          title="Seguro de Borrar el Registro?"
                          @confirm="handleDeleteClick(scope.row)">
                          <el-button slot="reference" circle type="danger" icon="el-icon-delete"></el-button>
                        </el-popconfirm>
                    </template>
                </el-table-column>
            </el-table>
          </el-row>
        </el-main>
        <el-dialog :visible.sync="showDialog">
            <el-container>
              <el-header>
                <span>IPP / IPC Anual</span>
              </el-header>
            <el-main>
              <el-form>
                <el-form-item label="Año">
                  <el-input type="number" v-model="anho"></el-input>
                </el-form-item>
                <el-form-item label="IPP">
                  <el-input type="number" v-model="ipp"></el-input>
                </el-form-item>
                <el-form-item label="IPC">
                  <el-input type="number" v-model="ipc"></el-input>
                </el-form-item>
                <el-row align="bottom">
                  <el-col :span="6">
                    <el-form-item>
                      <el-button type="primary" @click="handleSaveClick">Guardar</el-button>
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item>
                      <el-button type="warning" @click="showDialog = !showDialog">Cancelar</el-button>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-main>
          </el-container>
        </el-dialog>
    </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { getListaIppIpc, guardarIppIpc, eliminarIppIpc } from '@/api/ucap'

export default {
  data() {
    return {
      tableData: [],
      ucap_ipp_id: null,
      anho: null,
      ipp: null,
      ipc: null,
      showDialog: false
    }
  },
  beforeMount() {
    this.anho = new Date().getFullYear()
    this.getIppIpc()
  },
  methods: {
    getIppIpc() {
      const response = getListaIppIpc().then(response => {
        this.tableData = response.data
      }).catch(error => {
        console.log(error)
      })
    },
    handleNew() {
      this.anho = new Date().getFullYear()
      this.ipp = null
      this.ipc = null
      this.ucap_ipp_id = null
      this.showDialog = true
    },
    handleSaveClick() {
      this.showDialog = false
      const ucapIpp = {
        ucap_ipp_id: this.ucap_ipp_id,
        ucap_ipp_anho: parseInt(this.anho),
        ucap_ipp_valor: parseFloat(this.ipp),
        ucap_ipc_valor: parseFloat(this.ipc)
      }
      guardarIppIpc(ucapIpp).then(response => {
        this.$message({
          message: 'Datos guardados correctamente',
          type: 'success'
        })
        this.getIppIpc()
      }).catch(error => {
        this.$message({
          message: 'Error al guardar los datos, Error: ' + error,
          type: 'error'
        })
        console.log(error)
      })
    },
    handleEdit(data) {
      this.ucap_ipp_id = data.ucap_ipp_id
      this.anho = data.ucap_ipp_anho
      this.ipp = data.ucap_ipp_valor
      this.ipc = data.ucap_ipc_valor
      this.showDialog = true
    },
    handleDeleteClick(data) {
      const id = data.ucap_ipp_id
      eliminarIppIpc(id).then(response => {
        this.$message({
          message: 'Datos eliminados correctamente',
          type: 'success'
        })
        this.getIppIpc()
      }).catch(error => {
        this.$message({
          message: 'Error al eliminar los datos, Error: ' + error,
          type: 'error'
        })
        console.log(error)
      })
    }
  }
}

</script>
