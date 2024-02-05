<template><div></div></template>
<script>
export default {
  props: {
    loader: {
      type: Object,
      required: true
    },
    map: {
      type: Object,
      required: true
    },
    marker: {
      type: Object,
      required: true
    }
  },

  mounted() {
    // console.log('marker:', this.marker)
    const host = window.location.host
    const protocol = window.location.protocol
    const port = window.location.port
    const url = protocol + '//' + host + '/mapicon.png'
    console.log('url:', url)
    this.loader.importLibrary('marker').then(({ Marker }) => {
      console.log('Marker loaded')
      const mark = new Marker({
        position: { lat: parseFloat(this.marker.aap_lat), lng: parseFloat(this.marker.aap_lng) },
        map: this.map,
        title: 'Luminaria No: ' + this.marker.aap_id + '\nDirección:' + this.marker.aap_direccion + '\nBarrio:' + this.marker.barr_descripcion + '\nTecnología:' + this.marker.aap_tecnologia + '\nPotencia:' + this.marker.aap_potencia + 'W',
        content: this.buildContent(this.marker),
        icon: url
      })
    }).catch(error => {
      console.log('Marker load error:', error)
    })
  },

  methods: {
    buildContent(marker) {
      const content = document.createElement('div')
      content.classList.add('marca')
      content.innerHTML = `
        <div class="icon">
          <i aria-hidden="true" class="fa fa-icon fa-${marker.type}" title="Luminaria No.${marker.aap_id}"></i>
          <span class="fa-sr-only">${marker.type}</span>
        </div>
        <div class="details">
          <div class="address">${marker.aap_direccion}</div>
          <div class="neighborhood">${marker.barr_descripcion}</div>
          <div class="features">
            <div>
                <i aria-hidden="true" class="fa fa-bed fa-lg microchip" title="Tecnología"></i>
                <span class="fa-sr-only">Tecnología:</span>
                <span>${marker.aap_tecnologia}</span>
            </div>
            <div>
              <i aria-hidden="true" class="fa fa-bath fa-lg bath" title="Potencia"></i>
              <span class="fa-sr-only">Potencia</span>
              <span>${marker.aap_potencia}W</span>
            </div>
          </div>
        </div>
      `
      return content
    }
  }
}
</script>
<style scoped>
.marca {
  align-items: center;
  background-color: #FFFFFF;
  border-radius: 50%;
  color: #263238;
  display: flex;
  font-size: 14px;
  gap: 15px;
  height: 30px;
  justify-content: center;
  padding: 4px;
  position: relative;
  position: relative;
  transition: all 0.3s ease-out;
  width: 30px;
}

</style>
