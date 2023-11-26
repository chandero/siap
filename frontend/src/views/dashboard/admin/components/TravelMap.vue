<template>
  <el-container>
    <GoogleMapLoader
      v-if="dataLoaded"
      :mapConfig="mapConfig"
      :apiKey="apiKey"
    >
      <template slot-scope="{ loader, map }">
        <GoogleMapMarker
          v-for="marker in markers"
          :key="marker.aap_id"
          :marker="marker"
          :loader="loader"
          :map="map"
        />
      </template>
    </GoogleMapLoader>
  </el-container>
</template>
<script>
import GoogleMapLoader from '@/components/GoogleMapLoader'
import GoogleMapMarker from '@/components/GoogleMapMarker'
import { getGeoreferenciaLuminarias } from '@/api/geo'
// import { mapSettings } from '@/constants/mapSettings'

export default {
  components: {
    GoogleMapLoader,
    GoogleMapMarker
  },
  data () {
    return {
      apiKey: 'AIzaSyA7pz-4r8OtJyMBTCgpSQPGiU-TciYb5bM',
      markers: null,
      dataLoaded: false,
      center: null
    }
  },
  created () {
    this.getMarkers()
  },
  methods: {
    getMarkers () {
      const loading = this.$loading({
        lock: true,
        text: 'Examinado Datos...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      getGeoreferenciaLuminarias()
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
    }
  },
  computed: {
    mapConfig () {
      return {
        center: this.center,
        zoom: 10
      }
    }
  }
}
</script>
