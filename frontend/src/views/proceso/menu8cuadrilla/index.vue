<template>
  <el-container>
    <el-header>
      <span>Gestion de Usuarios y Cuadrillas</span>
    </el-header>
    <el-main>
      <el-table
        :data="usuario_cuadrilla"
        style="width: 100%"
      >
      <el-table-column>
        <template slot-scope="scope">
          {{ getNombreUsuario(scope.row.usua_id) }}
        </template>
      </el-table-column>
      </el-table>
    </el-main>
  </el-container>
</template>
<script>
import { getUsuariosCuadrilla, getCuadrillas } from '@/api/cuadrilla'
import { getUsuarios } from '@/api/usuario'

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
    getUsuariosCuadrilla().then(response => {
      this.usuario_cuadrilla = response.data
      getCuadrillas().then(response => {
        this.cuadrillas = response.data
        getUsuarios().then(response => {
          this.usuarios = response.data
        })
      })
    })
  },
  methods: {
    getDescripcionCuadrilla(cuad_id) {
      if (cuad_id > 0) {
        return this.cuadrillas.find(cuadrilla => cuadrilla.cuad_id === cuad_id, { cuad_descripcion: 'Cuadrilla Asignada No Existe' }).cuad_descripcion
      } else {
        return 'Sin Cuadrilla'
      }
    },
    getNombreUsuario(usua_id) {
      if (usua_id > 0) {
        var usuario = this.usuarios.find(usuario => usuario.usua_id === usua_id, { usua_nombre: 'Usuario No Existe', usua_apellido: '' })
        return usuario.usua_nombre + ' ' + usuario.usua_apellido
      } else {
        return 'Sin Usuario'
      }
    }
  }
}
</script>
