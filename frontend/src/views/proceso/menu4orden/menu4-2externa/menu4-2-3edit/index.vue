<template>
  <el-container>
    <el-header>
      <span>{{ $t('orhi.orhi_edit') }}</span>
    </el-header>
    <el-main>
      <el-form ref="orhiForm" :model="orhi" :label-position="labelPosition">
        <el-collapse v-model="activePages" @change="handleActivePagesChange">
          <el-collapse-item
            name="1"
            :title="$t('orhi.general')"
            style="font-weight: bold;"
          >
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="6" :lg="6" :xl="6">
                <el-form-item
                  prop="orhi_fecharecepcion"
                  :label="$t('orhi.orhi_fecharecepcion')"
                >
                  <el-date-picker
                    v-model="orhi.orhi_fecharecepcion"
                    type="date"
                    placeholder="Fecha de la Orden Acta">
                  </el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="10" :lg="10" :xl="10">
                <el-form-item prop="reti_id" :label="$t('orhi.reti_id')">
                  <el-select autofocus :title="$t('orhi.tipo.select')" style="width: 80%" ref="tipo" v-model="orhi.reti_id" name="tipo" :placeholder="$t('orhi.tipo.select')"  @change="validarTipo()">
                    <el-option v-for="tipo in tipos" :key="tipo.reti_id" :label="tipo.reti_descripcion" :value="tipo.reti_id" >
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
             <el-col v-if="orhi.reti_id===2" :xs="24" :sm="24" :md="4" :lg="4" :xl="4">
                <el-form-item :label="$t('orhi.tipo_expansion.title')">
                      <el-select autofocus clearable :title="$t('orhi.tipo_expansion.select')" style="width: 80%" ref="tipo" v-model="orhi.orhi_reti_tipo" name="tipo_expansion" :placeholder="$t('orhi.tipo_expansion.select')">
                        <el-option v-for="te in tipos_expansion" :key="te.tiex_id" :label="te.tiex_descripcion" :value="te.tiex_id" >
                        </el-option>
                      </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="24" :md="5" :lg="5" :xl="5">
                <el-form-item
                  prop="orhi_consecutivo"
                  :label="$t('orhi.consecutivo')"
                >
                  <el-input
                    style="font-size: 30px;"
                    v-model="orhi.orhi_consecutivo"
                    @blur="orhi.orhi_consecutivo = parseInt(orhi.orhi_consecutivo)"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="orhi_objeto" :label="$t('orhi.orhi_objeto')">
                  <el-input
                    ref="orhi_objeto"
                    v-model="orhi.orhi_objeto"
                    @input="orhi.orhi_objeto = $event.toUpperCase()"
                    @keyup.enter.native="changeFocus('direccion')"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="orhi_direccion" :label="$t('orhi.orhi_direccion')">
                  <el-input
                    ref="orhi_direccion"
                    v-model="orhi.orhi_direccion"
                    @input="orhi.orhi_direccion = $event.toUpperCase()"
                    @keyup.enter.native="changeFocus('barrio')"
                  >
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item prop="barr_id" :label="$t('orhi.barr_id')">
                  <el-select
                    style="width:100%;"
                    filterable
                    clearable
                    ref="barrio"
                    v-model="orhi.barr_id"
                    name="barrio"
                    :placeholder="$t('barrio.select')"
                    @change="changeFocus('orhi_descripcion')"
                  >
                    <el-option
                      v-for="barrio in barrios"
                      :key="barrio.barr_id"
                      :label="barrio.barr_descripcion"
                      :value="barrio.barr_id"
                    >
                    </el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="4">
              <el-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
                <el-form-item
                  prop="orhi_descripcion"
                  :label="$t('orhi.orhi_descripcion')"
                >
                  <el-input
                    ref="descripcion"
                    v-model="orhi.orhi_descripcion"
                    type="textarea"
                    :rows="2"
                    @input="orhi.orhi_descripcion = $event.toUpperCase()"
                    @keyup.enter.native="changeFocus('submit')"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
          <el-collapse-item name="4" :title="$t('orhi.orhi_material')">
            <el-row :gutter="4" class="hidden-sm-and-down">
              <el-col :md="1" :lg="1" :xl="1">
                <span style="font-weight: bold;">No.</span>
              </el-col>
              <el-col :xs="24" :sm="24" :md="9" :lg="9" :xl="9">
                <span style="font-weight: bold;">Nombre del Material</span>
              </el-col>
              <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                <span style="font-weight: bold;"
                  >Cantidad Material</span
                >
              </el-col>
              <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                <span style="font-weight: bold;"
                  >Valor Unitario</span
                >
              </el-col>
              <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                <span style="font-weight: bold;"
                  >Valor Total</span
                >
              </el-col>
              <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                <span style="font-weight: bold;"
                  >Unidad</span
                >
              </el-col>
              <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                <span style="font-weight: bold;">Item</span>
              </el-col>
            </el-row>
            <div style="max-height: 600px; overflow: auto">
                <el-form
                  :model="evento"
                  :ref="'matform_' + evento.othm_id"
                >
                  <el-row
                    :gutter="4"
                    v-for="(evento, id) in orhi.orhi_material"
                    v-bind:key="id"
                  >
                    <el-col class="hidden-md-and-up" :xs="2" :sm="2">
                      <span style="font-weight: bold">No.</span>
                    </el-col>
                    <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">{{
                      id + 1
                    }}</el-col>
                    <el-col class="hidden-md-and-up" :xs="24" :sm="24">
                      <span style="font-weight: bold">Nombre del Material</span>
                    </el-col>
                    <el-col :xs="24" :sm="24" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="elhi_codigo">
                        <el-input
                          :disabled="evento.othm_estado > 7"
                          class="sinpadding"
                          v-model="evento.elhi_codigo"
                          placeholder="Código Material"
                          @blur="buscarCodigoElemento(evento)"
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
                      <el-form-item prop="elhi_id">
                        <el-select
                          :disabled="evento.othm_estado > 7"
                          filterable
                          :clearable="evento.othm_estado === 1"
                          v-model="evento.elhi_id"
                          :placeholder="$t('elemento.select')"
                          style="width: 100%"
                          @change="codigoElemento(id)"
                          remote
                          :remote-method="remoteMethodElemento"
                          :loading="loadingElemento"
                        >
                          <el-option
                            v-for="elemento in elementos"
                            :key="elemento.elhi_codigo"
                            :label="elemento.elhi_descripcion"
                            :value="elemento.elhi_id"
                          ></el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Cantidad Material</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="othm_cantidad">
                        <el-input
                          :disabled="evento.othm_estado > 7"
                          class="sinpadding"
                          v-model="evento.othm_cantidad"
                          @blur="setValor(evento)"
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Valor Unitario</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="othm_cantidad">
                        <el-input
                          :disabled="evento.othm_estado > 7"
                          class="sinpadding"
                          v-model="evento.othm_valor_unitario"
                          @blur="setValor(evento)"
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Valor Total</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="othm_cantidad">
                        <el-input
                          readonly
                          :disabled="evento.othm_estado > 7"
                          class="sinpadding"
                          v-model="evento.othm_valor_total"
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Unidad</span
                      >
                    </el-col>
                    <el-col :xs="16" :sm="16" :md="2" :lg="2" :xl="2">
                      <el-form-item prop="othm_unidad">
                        <el-input
                          :disabled="evento.othm_estado > 7"
                          class="sinpadding"
                          v-model="evento.othm_unidad"
                        ></el-input>
                      </el-form-item>
                    </el-col>
                    <el-col class="hidden-md-and-up" :xs="8" :sm="8">
                      <span style="font-weight: bold"
                        >Item</span
                      >
                    </el-col>
                     <el-col :xs="16" :sm="16" :md="3" :lg="3" :xl="3">
                      <el-form-item prop="othm_item">
                        <el-select
                          :disabled="evento.even_estado === 9"
                          class="sinpadding"
                          v-model="evento.othm_item"
                        >
                          <el-option v-for="unitario in unitarios" :key="unitario.unit_id" :label="unitario.unit_codigo + '-' + unitario.unit_descripcion" :value="unitario.unit_id" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :xs="1" :sm="1" :md="1" :lg="1" :xl="1">
                      <el-button
                        v-if="evento.even_estado < 8"
                        size="mini"
                        type="danger"
                        circle
                        icon="el-icon-minus"
                        title="Quitar Fila"
                        @click="
                          evento.even_estado === 1
                            ? (evento.even_estado = 8)
                            : (evento.even_estado = 9)
                        "
                      ></el-button>
                      <el-button
                        v-if="evento.even_estado > 7"
                        size="mini"
                        type="success"
                        circle
                        icon="el-icon-success"
                        title="Restaurar Fila"
                        @click="
                          evento.even_estado === 9
                            ? (evento.even_estado = 2)
                            : (evento.even_estado = 1)
                        "
                      ></el-button>
                    </el-col>
                  </el-row>
                </el-form>
                <el-row class="hidden-md-and-up">
                  <el-col style="border-bottom: 1px dotted #000"></el-col>
                </el-row>
              </div>
            <el-row>
              <el-col :span="24">
                <el-button
                  style="display: table-cell;"
                  type="info"
                  size="mini"
                  circle
                  icon="el-icon-plus"
                  title="Adicionar Nueva Fila"
                  @click="onAddEvent()"
                />
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </el-main>
    <el-footer>
      <el-button
        v-if="canSave"
        ref="submit"
        :disabled="!validate()"
        size="medium"
        type="primary"
        icon="el-icon-check"
        @click="confirmacionGuardar = !confirmacionGuardar"
        >Guardar Obra</el-button
      >
      <el-button
        v-if="canPrint"
        ref="print"
        size="medium"
        type="success"
        icon="el-icon-printer"
        @click="imprimir"
        >Imprimir</el-button
      >
    </el-footer>
    <el-dialog title="Confirmación" :visible.sync="confirmacionGuardar">
      <span style="font-size:20px;"
        >Seguro de Guardar las Modificaciones a la Orden?</span
      >
      <span slot="footer" class="dialog-footer">
        <el-button @click="confirmacionGuardar = false">No</el-button>
        <el-button type="primary" @click="aplicar">Sí</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>
<script>
import { getBarriosEmpresa } from '@/api/barrio'
import { saveOrder, getOrdenTrabajoHistorico, getElementos, getElementoByDescripcion, getElementoByCode } from '@/api/ordentrabajo_historico'
import { getTipos } from '@/api/reporte'
import { getUnitariosTodas } from '@/api/unitario'

export default {
  data() {
    return {
      labelPosition: 'top',
      loadingElemento: false,
      showAapModal: false,
      activePages: ['1', '2', '3', '4'],
      activePages2: ['2-1', '2-2'],
      nopopover: false,
      canSave: true,
      canPrint: false,
      confirmacionGuardar: false,
      orhi_previo: null,
      evento_siguiente_consecutivo: 1,
      direccion_siguiente_consecutivo: 1,
      guardando: false,
      tipos: [],
      unitarios: [],
      orhi: {
        orhi_id: null,
        orhi_fecharecepcion: null,
        orhi_direccion: null,
        orhi_objeto: null,
        orhi_fechasolucion: null,
        barr_id: null,
        usua_id: 0,
        empr_id: 1,
        orhi_estado: 1,
        orhi_descripcion: null,
        reti_id: null,
        orhi_reti_tipo: null,
        orhi_consecutivo: null,
        orhi_fechadigitacion: null,
        orhi_fehamodificacion: null,
        orhi_material: []
      },
      evento: {
        othm_id: null,
        orhi_id: null,
        elhi_id: null,
        elhi_codigo: null,
        usua_id: 0,
        empr_id: 0,
        othm_estado: 1,
        othm_cantidad: null,
        othm_valor_unitario: null,
        othm_valor_total: null,
        othm_unidad: null,
        othm_item: null
      },
      rules: {},
      barrios: [],
      elementos: [],
      tipos_expansion: [
        {
          tiex_id: 1,
          tiex_descripcion: 'TIPO I',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 2,
          tiex_descripcion: 'TIPO II',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 3,
          tiex_descripcion: 'TIPO III',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        },
        {
          tiex_id: 4,
          tiex_descripcion: 'TIPO IV',
          tiex_luminaria: true,
          tiex_redes: false,
          tiex_poste: false
        }
      ]
    }
  },
  timers: {
    autosave: {
      name: 'autosave',
      time: 10000,
      autostart: false,
      repeat: true
    },
    pending: { name: 'pending', time: 30000, autostart: false, repeat: true }
  },
  methods: {
    handleActivePagesChange(val) {
      this.activePages = val
    },
    setValor(evento) {
      if (evento.othm_cantidad !== null) {
        evento.othm_cantidad = parseFloat(evento.othm_cantidad)
      } else {
        evento.othm_cantidad = 0
      }

      if (evento.othm_valor_unitario !== null) {
        evento.othm_valor_unitario = parseFloat(evento.othm_valor_unitario)
      } else {
        evento.othm_valor_unitario = 0
      }
      evento.othm_valor_total = evento.othm_cantidad * evento.othm_valor_unitario
    },
    autosave() {
      localStorage.setItem('currEditOthi', JSON.stringify(this.othi))
    },
    changeFocus(next) {
      this.$refs[next].focus()
    },
    handleDelete(index, row) {
      this.orhi.orhi_material.splice(index, 1)
    },
    validate() {
      if (
        !this.guardando
      ) {
        return true
      } else {
        return true
      }
    },
    validarTipo () {
      if (this.orhi.reti_id === 2) {
      } else if (this.orhi.reti_id === 1) {
      } else if (this.orhi.reti_id === 6) {
      } else {
      }
    },
    validarExpansion () {
      if (this.orhi.reti_id === 2) {
      }
    },
    validarModernizacion () {
    },
    alerta(msg) {
      this.$alert(msg, 'Atención', {
        confirmButtonText: 'Continuar'
      })
    },
    aplicar() {
      // var valido = true
      this.guardando = true
      this.confirmacionGuardar = false
      saveOrder(this.orhi)
        .then(response => {
          if (response.status === 200) {
            localStorage.setItem('currEditObraIni', this.orhi)
            this.success()
          } else {
            this.error(
              'Se presentó un inconveniente al guardar los cambios, por favor reintente'
            )
          }
        })
        .catch(error => {
          this.error(error)
        })
    },
    validatForm(form) {
      var result = false
      this.$refs[form].validate(valid => {
        if (valid) {
          result = true
        } else {
          result = false
        }
      })
      return result
    },
    imprimir() {},
    success() {
      this.$notify({
        title: this.$i18n.t('orhi.success'),
        message:
          this.$i18n.t('orhi.updated') + ' ' + this.orhi.orhi_consecutivo,
        type: 'success'
      })
    },
    error(e) {
      this.$notify.error({
        title: this.$i18n.t('orhi.error'),
        message: this.$i18n.t('orhi.notupdated') + ' ' + e
      })
    },
    open7() {
      this.$message({
        showClose: true,
        message: 'Atención, existe información por guardar en el orhi.',
        type: 'warning',
        duration: 5000
      })
    },
    codigoElemento(id) {
      const evento = this.orhi.orhi_material[id]
      if (
        evento.elhi_id === '' ||
        evento.elhi_id === null ||
        evento.elhi_id === undefined
      ) {
        return '-'
      } else {
        this.completarMaterial()
        let elemento = this.elementos.find(
          o => o.elhi_id === evento.elhi_id)
        if (elemento) {
          console.log('Elemento encontrado: ' + JSON.stringify(elemento))
        } else {
          console.log('Elemento no encontrado: ' + evento.elhi_id)
          elemento = { elhi_codigo: '-', elhi_unidad: 'UND' }
        }
        console.log('Elemento: ' + JSON.stringify(elemento))
        evento.elhi_codigo = elemento.elhi_codigo
        evento.othm_unidad = elemento.elhi_unidad
      }
    },
    buscarCodigoElemento(evento) {
      if (
        evento.elhi_codigo !== undefined &&
        evento.elhi_codigo !== null &&
        evento.elhi_codigo !== ''
      ) {
        const elemento = this.elementos.find(
          e => parseInt(e.elhi_codigo) === parseInt(evento.elhi_codigo)
        )
        if (!elemento) {
          getElementoByCode(evento.elhi_codigo)
            .then(response => {
              if (response.status === 200) {
                this.elementos = []
                var elemento = response.data
                this.elementos.unshift(elemento)
              } else {
                this.$notify({
                  title: 'Atención',
                  message:
                    'No se encontró Material con ese código: (' +
                    response.status +
                    ')',
                  type: 'warning'
                })
              }
            })
            .catch(error => {
              this.$notify({
                title: 'Atención',
                message:
                  'No se encontró Material con ese código: (' + error + ')',
                type: 'warning'
              })
            })
        } else {
          this.elementos = []
          this.elementos.unshift(elemento)
        }
      }
    },
    completarMaterial() {
      for (var j = 0; j < this.orhi.orhi_material.length; j++) {
        if (
          this.orhi.orhi_material[j] !== undefined &&
          this.orhi.orhi_material[j].elhi_id !== undefined &&
          this.orhi.orhi_material[j].elhi_id > 0
        ) {
          if (
            this.elementos.find(
              e => e.elhi_id === this.orhi.orhi_material[j].elhi_id
            ) === undefined
          ) {
            this.elementos.push({
              elhi_id: this.orhi.orhi_material[j].elhi_id,
              elhi_descripcion: this.elemento(this.orhi.orhi_material[j].elhi_id)
            })
          }
        }
      }
    },
    onAddEvent() {
      var evento = {
        othm_id: null,
        orhi_id: this.orhi.orhi_id,
        elhi_id: null,
        elhi_codigo: null,
        usua_id: 0,
        empr_id: 1,
        othm_estado: 1,
        othm_cantidad: null,
        othm_valor_unitario: null,
        othm_valor_total: null,
        othm_unidad: null,
        othm_item: null
      }
      this.orhi.orhi_material.push(evento)
      this.evento_siguiente_consecutivo =
      this.evento_siguiente_consecutivo + 1
    },
    limpiarAndBack() {
      this.obtenerObra()
    },
    elemento(elhi_id) {
      if (elhi_id === null) {
        return ''
      } else {
        return this.elementos_list.find(o => o.elhi_id === elhi_id, {
          elhi_descripcion: null
        }).elhi_descripcion
      }
    },
    orhi_tipo(reti_id) {
      if (reti_id === null) {
        return ''
      } else {
        return this.tipos.find(o => o.reti_id === reti_id, {
          reti_descripcion: 'INDEFINIDO'
        }).reti_descripcion
      }
    },
    estado() {
      if (this.orhi && this.orhi.rees_id !== null) {
        var rees_id = this.orhi.rees_id
        if (rees_id === null || rees_id === undefined) {
          return ''
        } else {
          if (this.estados && this.estados.length > 0) {
            return this.estados.find(o => o.rees_id === rees_id, {
              rees_descripcion: 'INDEFINIDO'
            }).rees_descripcion
          } else {
            return 'INDEFINIDO'
          }
        }
      }
    },
    accion(acci_id) {
      if (acci_id === null) {
        return ''
      } else {
        return this.acciones.find(o => o.acci_id === acci_id, {
          acci_descripcion: null
        }).acci_descripcion
      }
    },
    tiposector(barr_id) {
      if (barr_id === null) {
        return null
      } else {
        return this.barrios.find(o => o.barr_id === barr_id, {
          tiba_id: null
        }).tiba_id
      }
    },
    obtenerOrdenTrabajoHistorico() {
      const id = this.$route.params.id
      if (id) {
        getOrdenTrabajoHistorico(this.$route.params.id)
          .then(response => {
            this.orhi = response.data
            this.$timer.start('autosave')
            this.$timer.start('pending')
            this.pending()
          })
          .catch(error => {
            console.log('getObra: ' + error)
          })
      }
    },
    pending() {},
    validarConsecutivo() {
      // var consecutivo = 1
      for (var i = 0; i < this.orhi_previo.eventos.length; i++) {
        if (
          this.orhi_previo.eventos[i].elhi_id !== undefined &&
          this.orhi_previo.eventos[i].elhi_id > 0
        ) {
          if (
            this.elementos.find(
              e => e.elhi_id === this.orhi_previo.eventos[i].elhi_id
            ) === undefined
          ) {
            this.elementos.push({
              elhi_id: this.orhi_previo.eventos[i].elhi_id,
              elhi_codigo: this.orhi_previo.eventos[i].elhi_codigo,
              elhi_descripcion: this.elemento(
                this.orhi_previo.eventos[i].elhi_id
              )
            })
          }
        }
        // consecutivo++
      }
    },
    cargarEventos() {
      // validar si existe un obra previo
      var stringObraAnterior = localStorage.getItem('currEditObra')
      if (
        stringObraAnterior !== undefined &&
        (stringObraAnterior !== null) & (stringObraAnterior !== '')
      ) {
        var currEditRep = JSON.parse(stringObraAnterior)
        if (currEditRep.orhi_id === this.orhi_previo.orhi_id) {
          this.orhi_previo = currEditRep
        }
      }
      var even_length = 0
      this.orhi_previo.eventos.forEach(e => {
        if (e.even_id > even_length) {
          even_length = e.even_id
        }
      })
      this.orhi_previo.eventos.forEach(e => {
        if (e.even_id === undefined || e.even_id === null || e.even_id < 1) {
          e.even_id = even_length + 1
          even_length = even_length + 1
        }
      })
      console.log('Obra previo:' + JSON.stringify(this.orhi_previo))
      if (even_length === 0) {
        for (var i = 1; i <= 10; i++) {
          var evento = {
            even_fecha: null,
            even_codigo_instalado: null,
            even_codigo_retirado: null,
            even_cantidad_instalado: 1.0,
            even_cantidad_retirado: 1.0,
            even_estado: 1,
            aap_id: null,
            orhi_id: this.orhi_previo.orhi_id,
            elhi_id: null,
            elhi_codigo: null,
            empr_id: 0,
            usua_id: 0,
            even_id: even_length + i
          }
          this.orhi_previo.eventos.push(evento)
        }
        this.evento_siguiente_consecutivo = even_length + 11
      } else {
        this.evento_siguiente_consecutivo = even_length + 1
      }
    },
    remoteMethodElemento(query) {
      if (query !== '') {
        getElementoByDescripcion(query).then(response => {
          this.elementos = response.data
        })
      }
    }
  },
  beforeMount() {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.barrios_lista = response.data
      getElementos()
        .then(response => {
          this.elementos_list = response.data
          this.elementos = response.data
          getTipos().then(response => {
            this.tipos = response.data.filter(t => t.reti_id === 2 || t.reti_id === 6)
            getUnitariosTodas().then(response => {
              this.unitarios = response.data
              this.obtenerOrdenTrabajoHistorico()
            })
          }).catch(error => {
            console.log('getTipos: ' + error)
          })
        })
        .catch(error => {
          console.log('getElementos: ' + error)
        })
    })
  }
}
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
  div.el-input input {
    padding: 0 5px;
  }
  .el-collapse-item__header {
    font-weight: bold;
  }
</style>
