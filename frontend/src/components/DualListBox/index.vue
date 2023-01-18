<template>
  <div class="list-box-wrapper">
    <div class="list-box-item">
      <div class="search-box">
        <input v-model="searchSource" type="text" placeholder="Buscar" />
        <div
          v-if="searchSource"
          class="clear-search"
          title="Clear Search"
          @click=" searchSource='' "
        >&times;</div>
      </div>
      <ul class="list-box">
        <li
          v-for="(item,key) in source.map((item,inx) => ({inx,...item})).filter(item => item[label in item ? label : 'label'].toLowerCase().includes(searchSource.toLowerCase()))"
          v-bind:key="key"
          :class="'list-item'+ (item.selected ? ' active':'')"
          @click="selectSource(searchSource?item.inx:key)"
        >{{item[label in item ? label : 'label']}}</li>
        <li
          v-if="source.filter(item => item[label in item ? label : 'label'].toLowerCase().includes(searchSource.toLowerCase())).length == 0 && source.length"
          class="list-item"
        >No results found</li>
      </ul>
      <div class="bulk-action">
        <div class="select-all" @click="selectAllSource">Todas</div>
        <div class="deselect-all" @click="deSelectAllSource">Ninguna</div>
      </div>
    </div>
    <div class="actions">
      <div class="btn-action" @click="moveDestination">
        <svg height="18" viewBox="0 0 256 512">
          <path
            fill="#ffffff"
            d="M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 0l136 136c9.5 9.4 9.5 24.6.1 34z"
          />
        </svg>
      </div>
      <div class="btn-action" @click="moveAllDestination">
        <svg height="18" viewBox="0 0 448 512">
          <path
            fill="#ffffff"
            d="M224.3 273l-136 136c-9.4 9.4-24.6 9.4-33.9 0l-22.6-22.6c-9.4-9.4-9.4-24.6 0-33.9l96.4-96.4-96.4-96.4c-9.4-9.4-9.4-24.6 0-33.9L54.3 103c9.4-9.4 24.6-9.4 33.9 0l136 136c9.5 9.4 9.5 24.6.1 34zm192-34l-136-136c-9.4-9.4-24.6-9.4-33.9 0l-22.6 22.6c-9.4 9.4-9.4 24.6 0 33.9l96.4 96.4-96.4 96.4c-9.4 9.4-9.4 24.6 0 33.9l22.6 22.6c9.4 9.4 24.6 9.4 33.9 0l136-136c9.4-9.2 9.4-24.4 0-33.8z"
          />
        </svg>
      </div>
      <div class="btn-action" @click="moveSource">
        <svg height="18" viewBox="0 0 256 512">
          <path
            fill="#ffffff"
            d="M31.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L127.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L201.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34z"
          />
        </svg>
      </div>
      <div class="btn-action" @click="moveAllSource">
        <svg height="18" viewBox="0 0 448 512">
          <path
            fill="#ffffff"
            d="M223.7 239l136-136c9.4-9.4 24.6-9.4 33.9 0l22.6 22.6c9.4 9.4 9.4 24.6 0 33.9L319.9 256l96.4 96.4c9.4 9.4 9.4 24.6 0 33.9L393.7 409c-9.4 9.4-24.6 9.4-33.9 0l-136-136c-9.5-9.4-9.5-24.6-.1-34zm-192 34l136 136c9.4 9.4 24.6 9.4 33.9 0l22.6-22.6c9.4-9.4 9.4-24.6 0-33.9L127.9 256l96.4-96.4c9.4-9.4 9.4-24.6 0-33.9L201.7 103c-9.4-9.4-24.6-9.4-33.9 0l-136 136c-9.5 9.4-9.5 24.6-.1 34z"
          />
        </svg>
      </div>
    </div>
    <div class="list-box-item">
      <div class="search-box">
        <input v-model="searchDestination" type="text" placeholder="Buscar" />
        <div
          v-if="searchDestination"
          class="clear-search"
          title="Clear Search"
          @click=" searchDestination='' "
        >&times;</div>
      </div>
      <ul class="list-group list-group-flush border rounded list-box">
        <li
          v-for="(item,key) in destination.map((item,inx) => ({inx,...item})).filter(item => item[label in item ? label : 'label'].toLowerCase().includes(searchDestination.toLowerCase()))"
          v-bind:key="key"
          :class="'list-item'+ (item.selected ? ' active':'')"
          @click="selectDestination(searchDestination?item.inx:key)"
        >{{item[label in item ? label : 'label']}}</li>
        <li
          v-if="destination.filter(item => item[label in item ? label : 'label'].toLowerCase().includes(searchDestination.toLowerCase())).length == 0 && destination.length"
          class="list-item"
        >No results found</li>
      </ul>
      <div class="bulk-action">
        <div class="select-all" @click="selectAllDestination">Todas</div>
        <div class="deselect-all" @click="deSelectAllDestination">Ninguna</div>
      </div>
    </div>
  </div>
</template>

<script>
import angleRight from '@/assets/angle-right-solid.svg'
import angleLeft from '@/assets/angle-left-solid.svg'
import angleDoubleLeft from '@/assets/angle-double-left-solid.svg'
import angleDoubleRight from '@/assets/angle-double-right-solid.svg'

export default {
  props: {
    source: Array,
    destination: Array,
    label: String
  },
  data () {
    return {
      angleRight,
      angleLeft,
      angleDoubleLeft,
      angleDoubleRight,
      searchSource: '',
      searchDestination: ''
    }
  },
  methods: {
    moveDestination () {
      let selected = this.source.filter(f => f.selected)
      if (!selected.length) return
      selected = selected.map(item => ({
        ...item,
        selected: false
      }))
      const destination = [...selected, ...this.destination]
      const source = this.source.filter(f => !f.selected)
      this.searchSource = ''
      this.searchDestination = ''
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    moveSource () {
      let selected = this.destination.filter(f => f.selected)
      if (!selected.length) return
      selected = selected.map(item => ({
        ...item,
        selected: false
      }))
      const source = [...selected, ...this.source]
      const destination = this.destination.filter(f => !f.selected)
      this.searchSource = ''
      this.searchDestination = ''
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    moveAllDestination () {
      const destination = [
        ...this.source.map(item => ({ ...item, selected: false })),
        ...this.destination
      ]
      const source = []
      this.searchSource = ''
      this.searchDestination = ''
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    moveAllSource () {
      const source = [
        ...this.destination.map(item => ({ ...item, selected: false })),
        ...this.source
      ]
      const destination = []
      this.searchSource = ''
      this.searchDestination = ''
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    selectDestination (key) {
      const source = this.source
      const destination = this.destination.map((i, k) => {
        if (k === key) {
          i.selected = !i.selected
        }
        return i
      })
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    selectSource (key) {
      const destination = this.destination
      const source = this.source.map((i, k) => {
        if (k === key) {
          i.selected = !i.selected
        }
        return i
      })
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    selectAllSource () {
      const source = this.source.map(item => ({ ...item, selected: true }))
      const destination = this.destination
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    deSelectAllSource () {
      const source = this.source.map(item => ({ ...item, selected: false }))
      const destination = this.destination
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    selectAllDestination () {
      const destination = this.destination.map(item => ({
        ...item,
        selected: true
      }))
      const source = this.source
      this.$emit('onChangeList', {
        source,
        destination
      })
    },
    deSelectAllDestination () {
      const destination = this.destination.map(item => ({
        ...item,
        selected: false
      }))
      const source = this.source
      this.$emit('onChangeList', {
        source,
        destination
      })
    }
  }
}
</script>
<style scoped>
*,
*::before,
*::after {
  box-sizing: border-box;
}

.list-box-wrapper {
  font-family: sans-serif;
  width: 100%;
  display: flex;
  align-items: center;
}
.list-box-wrapper > div {
  flex: 1;
}
.list-box-wrapper .list-box-item {
  border: solid 1px #cccccc;
  border-radius: 3px;
}
.list-box-wrapper .list-box-item .search-box {
  border-bottom: solid 1px #cccccc;
  position: relative;
}
.list-box-wrapper .list-box-item .search-box input {
  border: none;
  width: 100%;
  padding: 0.5rem 1rem;
}
.list-box-wrapper .list-box-item .search-box .clear-search {
  position: absolute;
  padding: 0.5rem;
  right: 0;
  top: 0;
  cursor: pointer;
  font-weight: bold;
  color: #e74c3c;
}
.list-box-wrapper .list-box-item .list-box {
  height: 250px;
  overflow: auto;
  list-style: none;
  padding: 0;
  margin: 0;
}
.list-box-wrapper .list-box-item .list-box .list-item {
  padding: 0.5rem 1rem;
  border-bottom: solid 1px #cccccc;
  cursor: pointer;
}
.list-box-wrapper .list-box-item .list-box .list-item.active {
  background-color: #eeeeee;
}
.list-box-wrapper .list-box-item .bulk-action {
  display: flex;
  border-top: solid 1px #cccccc;
  text-align: center;
}
.list-box-wrapper .list-box-item .bulk-action .select-all {
  width: 100%;
  padding: 0.8rem;
  background-color: #007bff;
  color: #fff;
}
.list-box-wrapper .list-box-item .bulk-action .deselect-all {
  width: 100%;
  padding: 0.8rem;
  background-color: #6c757d;
  color: #fff;
}
.list-box-wrapper .actions {
  flex-grow: 0;
  padding: 0 1rem;
}
.list-box-wrapper .actions .btn-action {
  margin-bottom: 0.5rem;
}

.btn-action {
  display: inline-block;
  font-weight: 400;
  color: #212529;
  text-align: center;
  vertical-align: middle;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  background-color: transparent;
  border: 1px solid transparent;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
  display: block;
  width: 100%;
  color: #fff;
  background-color: #007bff;
  border-color: #007bff;
  cursor: pointer;
}
.btn-action svg {
  vertical-align: middle;
}
</style>
