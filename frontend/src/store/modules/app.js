import Cookies from 'js-cookie'

const app = {
  state: {
    sidebar: {
      opened: !+Cookies.get('sidebarStatus')
    },
    language: Cookies.get('language') || 'en',
    months: [{ id: 1, label: 'm1' }, { id: 2, label: 'm2' }, { id: 3, label: 'm3' }, { id: 4, label: 'm4' }, { id: 5, label: 'm5' }, { id: 6, label: 'm6' }, { id: 7, label: 'm7' }, { id: 8, label: 'm8' }, { id: 9, label: 'm9' }, { id: 10, label: 'm10' }, { id: 11, label: 'm11' }, { id: 12, label: 'm12' }],
    secret: '43f44388-5cd1-4657-9f7e-ea4e014e9333',
    baseurl: null,
    sessionUUID: null,
    tipo_inventario: [
      { id: 1, descripcion: 'Luminarias' },
      { id: 2, descripcion: 'Control' },
      { id: 3, descripcion: 'Canalización' },
      { id: 4, descripcion: 'Postes' },
      { id: 5, descripcion: 'Redes' },
      { id: 6, descripcion: 'Transformador' },
      { id: 7, descripcion: 'Medidor' }
    ]
  },
  mutations: {
    TOGGLE_SIDEBAR: state => {
      if (state.sidebar.opened) {
        Cookies.set('sidebarStatus', 1, { sameSite: 'strict' })
      } else {
        Cookies.set('sidebarStatus', 0, { sameSite: 'strict' })
      }
      state.sidebar.opened = !state.sidebar.opened
    },
    SET_LANGUAGE: (state, language) => {
      state.language = language
      Cookies.set('language', language, { sameSite: 'strict' })
    },
    SET_BASEURL: (state, url) => {
      state.baseurl = url
    },
    SET_UUID: (state, uuid) => {
      console.log('ACTUALIZANDO UUID :' + uuid)
      state.sessionUUID = uuid
      localStorage.setItem('riapSessionUUID', uuid)
    }
  },
  actions: {
    toggleSideBar ({ commit }) {
      commit('TOGGLE_SIDEBAR')
    },
    setLanguage ({ commit }, language) {
      commit('SET_LANGUAGE', language)
    },
    setBaseUrl ({ commit }, url) {
      commit('SET_BASEURL', url)
    },
    SetUUID ({ commit }, uuid) {
      commit('SET_UUID', uuid)
    }
  }
}

export default app
