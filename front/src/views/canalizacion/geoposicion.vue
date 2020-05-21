<template>
<el-container>
  <el-header>
      <span>{{ $t('route.geoposicionamiento') }}</span>
  </el-header>
  <el-main>
    <el-container>
      <el-main>
        <vue-query-builder v-model="qbquery" :rules="qrules" :labels="qlabels" :styled="qstyled" :maxDepth="3"></vue-query-builder>
        <el-button type="warning" icon="el-icon-search" circle @click="actualizar" title="Actualizar Aplicando el Filtro"></el-button>
      </el-main>
    </el-container>
    <GmapMap
      :center="{lat:7.068118, lng:-73.167847}"
      :zoom="15"
      map-type-id="terrain"
      style="width: 800px; height: 600px"
    >
      <GmapInfoWindow
        :options="infoOptions" 
        :position="infoWindowPos" 
        :opened="infoWinOpen" 
        @closeclick="infoWinOpen=false"
      >
        {{infoDireccion}}
        <p/>
        {{infoDescripcion}}
        <el-button icon="el-icon-info" circle @click="info(aap_id)"></el-button>
      </GmapInfoWindow>
      <GmapMarker
        :key="index"
        v-for="(m, index) in markers"
        :position="m.position"
        :title="m.title"
        :clickable="true"
        :draggable="false"
        @click="toggleInfoWindow(m,index)"
      />
    </GmapMap>
  </el-main>
</el-container>
</template>

<script>
import VueQueryBuilder from 'vue-query-builder'
import { gmapApi } from 'vue2-google-maps'
import { getAaps } from '@/api/aap'
import { getBarriosEmpresa } from '@/api/barrio'
import { getTiposBarrio } from '@/api/tipobarrio'
import { getAapConexiones } from '@/api/aap_conexion'
import { getAapUsos } from '@/api/aap_uso'
import { getElementos } from '@/api/elemento'

export default {
  components: {
    VueQueryBuilder
  },
  data() {
    return {
      markers: [],
      aap_id: null,
      infoDireccion: '',
      infoDescripcion: '',
      infoWindowPos: null,
      infoWinOpen: false,
      currentMidx: null,
      // optional: offset infowindow so it visually sits nicely on top of our marker
      infoOptions: {
        pixelOffset: {
          width: 0,
          height: -35
        }
      },
      qbquery: {},
      qbarrios: [{ label: this.$i18n.t('barrio.select'), value: '' }],
      qtiposbarrio: [{ label: this.$i18n.t('barrio.type'), value: '' }],
      qconnections: [{ label: this.$i18n.t('connection.select'), value: '' }],
      quses: [{ label: this.$i18n.t('use.select'), value: '' }],
      qelements: [{ label: this.$i18n.t('element.select'), value: '' }],
      tipobarrio_filters: [],
      barrios_filters: [],
      qrules: [
        {
          type: 'custom',
          id: 'a.aap_id',
          label: this.$i18n.t('gestion.code'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'text',
          id: 'a.aap_apoyo',
          label: this.$i18n.t('gestion.support'),
          operators: ['=', '<>', '<', '<=', '>', '>=']
        },
        {
          type: 'select',
          id: 'b.barr_id',
          label: this.$i18n.t('gestion.neighborhood'),
          choices: []
        },
        {
          type: 'select',
          id: 't.tiba_id',
          label: this.$i18n.t('gestion.neighborhoodtype'),
          choices: []
        },
        {
          type: 'select',
          id: 'a.aaco_id',
          label: this.$i18n.t('gestion.connection'),
          choices: []
        },
        {
          type: 'select',
          id: 'a.aaus_id',
          label: this.$i18n.t('gestion.use'),
          choices: []
        }
      ],
      qlabels: {
        matchType: this.$i18n.t('qb.matchType'),
        matchTypeAll: this.$i18n.t('qb.matchTypeAll'),
        matchTypeAny: this.$i18n.t('qb.matchTypeAny'),
        addRule: this.$i18n.t('qb.addRule'),
        removeRule: this.$i18n.t('qb.removeRule'),
        addGroup: this.$i18n.t('qb.addGroup'),
        removeGroup: this.$i18n.t('qb.removeGroup'),
        textInputPlaceholder: this.$i18n.t('qb.textInputPlaceholder')
      },
      qstyled: true
    }
  },
  computed: {
    google: gmapApi
  },
  methods: {
    toggleInfoWindow(marker, idx) {
      this.aap_id = marker.aap_id
      this.infoWindowPos = marker.position
      this.infoDireccion = marker.infoDireccion
      this.infoDescripcion = marker.infoDescripcion
      // check if its the same marker that was selected if yes toggle
      if (this.currentMidx === idx) {
        this.infoWinOpen = !this.infoWinOpen
      // if different marker set infowindow to open and reset current marker index
      } else {
        this.infoWinOpen = true
        this.currentMidx = idx
      }
    },
    actualizar() {
      this.getAaps()
    },
    info(aap_id) {
      this.$router.push({ path: '/inventario/gestion/editar/' + aap_id })
    },
    getAaps() {
      getAaps(this.qbquery).then(response => {
        this.aaps = response.data
        this.markers = []
        this.aaps.forEach(aap => {
          if (aap.aap_lat) {
            var mark = { lat: parseFloat(aap.aap_lat), lng: parseFloat(aap.aap_lng) }
            this.markers.push({ position: mark, title: this.$i18n.t('gestion.codemini') + ':' + aap.aap_id.toString(), aap_id: aap.aap_id, infoDireccion: aap.aap_direccion, infoDescripcion: aap.aap_descripcion })
          }
        })
      }).catch(error => {
        console.log('getAaps :' + error)
      })
    }
  },
  mounted() {
    this.getAaps()
    this.qrules[2].choices = this.qbarrios
    this.qrules[3].choices = this.qtiposbarrio
    this.qrules[4].choices = this.qconnections
    this.qrules[5].choices = this.quses
  },
  created() {
    getBarriosEmpresa().then(response => {
      this.barrios = response.data
      this.barrios.forEach(b => {
        this.qbarrios.push({ label: b.barr_descripcion, value: b.barr_id })
      })
    }).catch(error => {
      console.log(error)
    })
    getTiposBarrio().then(response => {
      response.data.forEach(b => {
        this.qtiposbarrio.push({ label: b.tiba_descripcion, value: b.tiba_id })
      })
    }).catch(error => {
      console.log(error)
    })
    getElementos().then(response => {
      response.data.forEach(b => {
        this.qelements.push({ label: b.elem_descripcion, value: b.elem_id })
      })
    }).catch(error => {
      console.log(error)
    })
    getAapConexiones().then(response => {
      response.data.forEach(b => {
        this.qconnections.push({ label: b.aaco_descripcion, value: b.aaco_id })
      })
    }).catch(error => {
      console.log(error)
    })
    getAapUsos().then(response => {
      response.data.forEach(b => {
        this.quses.push({ label: b.aaus_descripcion, value: b.aaus_id })
      })
    })
  }
}
</script>