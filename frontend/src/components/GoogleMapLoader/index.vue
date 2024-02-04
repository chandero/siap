<template>
  <el-container>
    <el-main id="googleMap" ref="googleMap">
      <el-row>
        <el-col>
          <template v-if="Boolean(this.loader) && Boolean(this.map)">
            <slot :loader="loader" :map="map"></slot>
          </template>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>
<script>
import { Loader } from '@googlemaps/js-api-loader'

export default {
  props: {
    mapConfig: Object,
    apiKey: String
  },

  data() {
    return {
      map: null,
      loader: null
    }
  },

  async mounted() {
    console.log('map loader api key:', this.apiKey)
    this.loader = new Loader({
      apiKey: this.apiKey,
      version: 'weekly',
      libraries: ['places'],
      language: 'es',
      region: 'CO',
      mapId: 'cybersiap_map_1'
    })

    this.loader
      .importLibrary('maps')
      .then(({ Map }) => {
        console.log('Map loaded')
        const mapContainer = document.getElementById('googleMap')
        // const mapContainer = this.$refs.googleMap
        this.map = new Map(mapContainer, this.mapConfig)
      })
      .catch(error => {
        console.log('Map load error:', error)
      })

    this.loader
      .importLibrary('marker')
      .then(({ Marker }) => {
        console.log('Marker loaded')
        const marker = new Marker({
          position: { lat: 4.570868, lng: -74.297333 },
          map: this.map,
          title: 'Luminaria 10000'
        })
      })
      .catch(error => {
        console.log('Marker load error:', error)
      })
  },

  methods: {
    initializeMap({ Map }) {
      console.log('initializeMap')
      // const mapContainer = this.$refs.googleMap
      const mapContainer = document.getElementById('googleMap')
      this.map = new Map(mapContainer, this.mapConfig)
    }
  }
}
</script>
<style scoped>
#googleMap {
  height: 600px;
  width: 400px;
}
</style>
