import Cookies from 'js-cookie'

const app = {
  state: {
    sidebar: {
      opened: !+Cookies.get('sidebarStatus')
    },
    language: Cookies.get('language') || 'en',
    months: [{ id: 1, label: 'm1' }, { id: 2, label: 'm2' }, { id: 3, label: 'm3' }, { id: 4, label: 'm4' }, { id: 5, label: 'm5' }, { id: 6, label: 'm6' }, { id: 7, label: 'm7' }, { id: 8, label: 'm8' }, { id: 9, label: 'm9' }, { id: 10, label: 'm10' }, { id: 11, label: 'm11' }, { id: 12, label: 'm12' }],
    secret: '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  },
  mutations: {
    TOGGLE_SIDEBAR: state => {
      if (state.sidebar.opened) {
        Cookies.set('sidebarStatus', 1)
      } else {
        Cookies.set('sidebarStatus', 0)
      }
      state.sidebar.opened = !state.sidebar.opened
    },
    SET_LANGUAGE: (state, language) => {
      state.language = language
      Cookies.set('language', language)
    }
  },
  actions: {
    toggleSideBar({ commit }) {
      commit('TOGGLE_SIDEBAR')
    },
    setLanguage({ commit }, language) {
      commit('SET_LANGUAGE', language)
    }
  }
}

export default app
