<template>
  <div>
    <div style="margin-top: 40px;">
    </div>
    <el-select v-model="barr_id" placeholder="Filtrar por barrio" clearable filterable @change="handleChangeBarrio">
      <el-option v-for="item in options" :key="item.barr_id" :label="item.barr_descripcion" :value="item.barr_id">
      </el-option>
    </el-select>
    <el-select v-model="aap_tecnologia" placeholder="Filtrar por Tecnologia" clearable filterable
      @change="handleChangeTecnologia">
      <el-option v-for="item in tecnologias" :key="item.tecn_id" :label="item.tecn_descripcion" :value="item.tecn_id">
      </el-option>
    </el-select>
    <el-select v-model="aap_potencia" placeholder="Filtrar por Potencias" clearable filterable
      @change="handleChangePotencia">
      <el-option v-for="item in potencias" :key="item.pot_id" :label="item.pot_descripcion" :value="item.pot_id">
      </el-option>
    </el-select>
    <el-input placeholder="Buscar Luminaria" v-model="aap_id" clearable style="width: 200px;">
    </el-input>
    <el-button @click="handleChangeluminaria()"> Buscar </el-button>
    <el-container>
      <GoogleMapLoader v-if="dataLoaded" :mapConfig="mapConfig" :apiKey="apiKey">
        <template slot-scope="{ loader, map }">
          <GoogleMapMarker v-for="marker in markers" :key="marker.aap_id" :marker="marker" :loader="loader" :map="map" />
        </template>
      </GoogleMapLoader>
    </el-container>
  </div>
</template>

<script>
import GoogleMapLoader from '@/components/GoogleMapLoader'
import GoogleMapMarker from '@/components/GoogleMapMarker'
import { getGeoreferenciaLuminarias } from '@/api/geo'
import { apiKey } from '@/api/google'
import { getBarriosEmpresa } from '@/api/barrio'
import { getCaracteristica } from '@/api/caracteristica'

export default {
  components: {
    GoogleMapLoader,
    GoogleMapMarker
  },
  data() {
    return {
      apiKey: null,
      markers: [],
      dataLoaded: false,
      center: null,
      options: [],
      selectedBarrios: [],
      barr_id: -1,
      aap_tecnologia: '-1',
      tecnologias: [],
      potencias: [],
      aap_potencia: -1,
      aap_id: '-1'
    }
  },
  beforeMount() {
    apiKey()
      .then(response => {
        this.apiKey = response.data
        console.log('apiKey:', this.apiKey)
        getBarriosEmpresa().then(response => {
          this.options = response.data
          this.options.push({ barr_id: -1, barr_descripcion: 'Filtrar Por Barrios' })
          this.getMarkers()
        })
        getCaracteristica(7)
          .then(response => {
            const data = response.data.cara_valores.split(',')
            data.forEach(t => { this.tecnologias.push({ tecn_id: t, tecn_descripcion: t }) })
            this.tecnologias.push({ tecn_id: '-1', tecn_descripcion: 'Filtrar por tecnologia' })
          })
        getCaracteristica(5)
          .then(response => {
            const data = response.data.cara_valores.split(',')
            data.forEach(p => { this.potencias.push({ pot_id: p, pot_descripcion: p }) })
            this.potencias.push({ pot_id: -1, pot_descripcion: 'Filtrar por Potencias' })
          })
          .catch(error => {
            console.log(error)
          })
      })
      .catch(error => {
        console.log(error)
      })
  },
  methods: {
    getMarkers() {
      const loading = this.$loading({
        lock: true,
        text: 'Examinado Datos...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      getGeoreferenciaLuminarias(this.barr_id, this.aap_tecnologia, this.aap_potencia, this.aap_id)
        .then(response => {
          console.log('markers:', response.data)
          this.markers = response.data
          const marker = this.markers[0]
          this.center = { lat: parseFloat(marker.aap_lat), lng: parseFloat(marker.aap_lng) }
          this.dataLoaded = true
          loading.close()
        })
        .catch(error => {
          console.log(error)
          loading.close()
        })
    },
    handleChangeBarrio() {
      console.log('Revisar:', this.barr_id)
      if (!this.barr_id) { this.barr_id = -1 }
      this.getMarkers()
    },
    handleChangeTecnologia() {
      if (!this.aap_tecnologia) { this.aap_tecnologia = '-1' }
      this.getMarkers()
    },
    handleChangePotencia() {
      if (!this.aap_potencia) { this.aap_potencia = -1 }
      this.getMarkers()
    },
    handleChangeluminaria() {
      if (!this.aap_id) { this.aap_id = '-1' }
      this.getMarkers()
    }
  },
  computed: {
    mapConfig() {
      return {
        center: this.center,
        zoom: 10
      }
    }
  }
}
</script>
