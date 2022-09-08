<template>
  <el-container>
    <el-header>
      <el-col :span="20">
        <span>Gestion de Usuarios y Cuadrillas</span>
      </el-col>
      <el-button circle icon="el-icon-refresh" @click="getSync()"></el-button>
    </el-header>
    <el-main>
      <el-table
        :data="usuario_cuadrilla"
        style="width: 100%"
        height="600"
      >
      <el-table-column
        label="Usuario"
        width="300">
        <template slot-scope="scope">
          {{ getNombreUsuario(scope.row.usua_id) }}
        </template>
      </el-table-column>
      <el-table-column
        label="Cuadrilla"
        width="280"
      >
        <template slot-scope="scope">
          <el-select filterable clearable ref="crew" v-model="scope.row.cuad_id" name="crew" :placeholder="$t('cuadrilla.select')"  style="width:250px;" @change="update(scope.row.usua_id, scope.row.cuad_id, scope.row.cuus_esresponsable)" @clear="update(scope.row.usua_id, scope.row.cuad_id, scope.row.cuus_esresponsable)">
              <el-option v-for="cuadrilla in cuadrillas" :key="cuadrilla.cuad_id" :label="cuadrilla.cuad_descripcion" :value="cuadrilla.cuad_id" >
              </el-option>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column
        label="Responsable"
        width="180"
        align="center"
      >
        <template slot-scope="scope">
          <el-checkbox
            v-model="scope.row.cuus_esresponsable" @change="update(scope.row.usua_id, scope.row.cuad_id, scope.row.cuus_esresponsable)"
          ></el-checkbox>
        </template>
      </el-table-column>
      </el-table>
    </el-main>
  </el-container>
</template>
<script>
import { getUsuariosCuadrilla, getCuadrillas, actualizarUsuarioCuadrilla } from '@/api/cuadrilla'
import { getAll } from '@/api/usuario'

export default {
  name: 'Menu8cuadrilla',
  data() {
    return {
      usuario_cuadrilla: [],
      cuadrillas: [],
      usuarios: []
    }
  },
  beforeMount() {
    this.getSync()
  },
  methods: {
    getSync() {
      getUsuariosCuadrilla().then(response => {
        this.usuario_cuadrilla = response.data
        getCuadrillas().then(response => {
          this.cuadrillas = response.data
          getAll().then(response => {
            this.usuarios = response.data
          })
        })
      })
    },
    getDescripcionCuadrilla(cuad_id) {
      if (cuad_id != null && cuad_id > 0) {
        return this.cuadrillas.find(cuadrilla => cuadrilla.cuad_id === cuad_id, { cuad_descripcion: 'Cuadrilla Asignada No Existe' }).cuad_descripcion
      } else {
        return 'Sin Cuadrilla'
      }
    },
    getNombreUsuario(usua_id) {
      if (usua_id != null && usua_id > 0) {
        var usuario = this.usuarios.find(usuario => usuario.usua_id === usua_id, { usua_nombre: 'Usuario No Existe', usua_apellido: '' })
        return usuario.usua_nombre + ' ' + usuario.usua_apellido
      } else {
        return 'Sin Usuario'
      }
    },
    update(usua_id, cuad_id, cuus_esresponsable) {
      if (usua_id === null) usua_id = 0
      if (cuad_id === null) cuad_id = 0
      actualizarUsuarioCuadrilla(usua_id, cuad_id, cuus_esresponsable).then(response => {
        this.$message({
          message: 'Información Actualizada al usuario',
          type: 'success'
        })
      }).catch(error => {
        this.$message({
          message: `No se pudo actualizada la información al usuario: ${error}`,
          type: 'error'
        })
      })
    }
  }
}
</script>
