import Vue from 'vue'
import VueCookie from 'vue-cookie'
import VueTruncateFilter from 'vue-truncate-filter'

import 'normalize.css/normalize.css'// A modern alternative to CSS resets
import Element from 'element-ui'
/* import '../theme/index.css' */
import 'element-ui/lib/theme-chalk/index.css'
import 'element-ui/lib/theme-chalk/display.css'
import 'element-ui/lib/theme-chalk/icon.css'

// import { library } from '@fortawesome/fontawesome-svg-core'
// import { faCoffee } from '@fortawesome/free-solid-svg-icons'
// import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
// -- import 'vue-awesome/icons'
// Session Idle
import IdleVue from 'idle-vue'

/* Register component with one of 2 methods */

// import Icon from '@vue-awesome/components/Icon'

// globally (in your main .js file)
// Vue.component('v-icon', Icon)

import '@/styles/index.scss' // global css

import App from './App'
import router from './router'
import store from './store'

import i18n from './lang' // Internationalization
import './icons' // icon
import './errorLog'// error log
import './permission' // permission control
// import './mock' // simulation data

import * as VeeValidate from 'vee-validate'
import es from 'vee-validate/dist/locale/es'

import * as filters from './filters' // global filters

import * as VueGoogleMaps from 'vue2-google-maps'
import * as VueQueryBuilder from 'vue-query-builder'

import VueMoment from 'vue-moment'

import VueTimers from 'vue-timers'

import '../node_modules/ag-grid-community/dist/styles/ag-grid.css'
import '../node_modules/ag-grid-community/dist/styles/ag-theme-balham.css'

import VueClipboard from 'vue-clipboard2'

Vue.use(VueClipboard)
Vue.use(VueQueryBuilder)
Vue.use(VueCookie)
Vue.use(VueTruncateFilter)
Vue.use(VueGoogleMaps, {
  load: {
    key: 'AIzaSyBXB1oiLuaUJwutdfNUFmfpiSA87iTBds0',
    libraries: 'places'
  }
})
Vue.use(VeeValidate, {
  i18n,
  dictionary: {
    es
  },
  fieldsBagName: 'vfields'
})

// Google Maps API KEY AIzaSyAEV4JZq90c_nGmpNnDdEOzcn3V8Asxg8k

Vue.use(Element, {
  size: 'small', // set element-ui default size
  i18n: (key, value) => i18n.t(key, value)
})

// library.add(faCoffee)

// Vue.component('font-awesome-icon', FontAwesomeIcon)

// register global utility filters.
Object.keys(filters).forEach(key => {
  Vue.filter(key, filters[key])
})

Vue.use(VueMoment)
Vue.use(VueTimers)

Vue.prototype.$equals = function (o1, o2) {
  for (var p in o1) {
    if (typeof (o1[p]) !== typeof (o2[p])) return false
    if ((o1[p] === null) !== (o2[p] === null)) return false
    switch (typeof (o1[p])) {
      case 'undefined':
        if (typeof (o2[p]) !== 'undefined') return false
        break
      case 'object':
        if (o1[p] !== null && o2[p] !== null && (o1[p].constructor.toString() !== o2[p].constructor.toString() || !o1[p].equals(o2[p]))) return false
        break
      case 'function':
        if (p !== 'equals' && o1[p].toString() !== o2[p].toString()) return false
        break
      default:
        if (o1[p] !== o2[p]) return false
    }
  }
  return true
}

Vue.config.productionTip = false

const eventsHub = new Vue()

Vue.use(IdleVue, {
  eventEmmiter: eventsHub,
  store,
  idleTime: 60000, // 60 segundos
  startAtIdle: false
})

new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
