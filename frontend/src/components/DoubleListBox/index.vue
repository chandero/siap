<template>
  <el-container>
    <el-main>
     <el-row>
       <el-col :span="10">
        <el-table
          ref="sourceTable"
          :data="sourceData.filter(data => !sourceSearch || data.name.toLowerCase().includes(sourceSearch.toLowerCase()))"
          height="400"
          style="width: 100%;"
          @selection-change="handleSourceSelectionChange"
        >
          <el-table-column
            type="selection"
            width="55">
          </el-table-column>
          <el-table-column
            property="name"
            label="Descripción"
            width="200"
            show-overflow-tooltip>
            <template slot="header">
              <el-input
                v-model="sourceSearch"
                size="mini"
                placeholder="Buscar"/>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px">
          <el-button @click="sourceSelectAll()">Todos</el-button>
          <el-button @click="sourceClearAll()">Ninguno</el-button>
        </div>
       </el-col>
       <el-col :span="4">
        <div class="actions">
            <div class="btn-action">
                <el-button @click="sendSelected()">&gt;</el-button>
            </div>
            <div class="btn-action">
                <el-button @click="sendAll()">&gt;&gt;</el-button>
            </div>
            <div class="btn-action">
                <el-button @click="returnSelected()">&lt;</el-button>
            </div>
            <div class="btn-action">
                <el-button @click="returnAll()">&lt;&lt;</el-button>
            </div>
        </div>
       </el-col>
       <el-col :span="10">
        <el-table
          ref="destinationTable"
          :data="destinationData.filter(data => !destinationSearch || data.name.toLowerCase().includes(destinationSearch.toLowerCase()))"
          height="400"
          style="width: 100%"
          @selection-change="handleDestinationSelectionChange"
        >
          <el-table-column
            type="selection"
            width="55">
          </el-table-column>
          <el-table-column
            property="name"
            label="Descripción"
            width="200"
            show-overflow-tooltip>
            <template slot="header">
              <el-input
                v-model="destinationSearch"
                size="mini"
                placeholder="Buscar"/>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 20px">
          <el-button @click="destinationSelectAll()">Todos</el-button>
          <el-button @click="destinationClearAll()">Ninguno</el-button>
        </div>
       </el-col>
     </el-row>
    </el-main>
  </el-container>
</template>
<script>

export default {
  name: 'DoubleListBox',
  props: {
    source: {
      type: Array,
      required: true
    },
    destination: {
      type: Array,
      required: false,
      default: () => {
        return []
      }
    }
  },
  data () {
    return {
      sourceData: [],
      destinationData: [],
      sourceSearch: '',
      destinationSearch: '',
      sourceSelected: null,
      destinationSelected: null
    }
  },
  mounted () {
    console.log('Fuente:', this.source)
    console.log('Destino:', this.destination)
    this.sourceData = this.source
    this.destinationData = this.destination
  },
  methods: {
    sourceClearAll () {
      this.$refs.sourceTable.clearSelection()
    },
    sourceSelectAll () {
      this.$refs.sourceTable.toggleAllSelection()
    },
    destinationClearAll () {
      this.$refs.destinationTable.clearSelection()
    },
    destinationSelectAll () {
      this.$refs.destinationTable.toggleAllSelection()
    },
    handleSourceSelectionChange (selection) {
      this.sourceSelected = selection
    },
    handleDestinationSelectionChange (selection) {
      this.destinationSelected = selection
    },
    arrayRemove (arr, value) {
      return arr.filter(ele => {
        return ele !== value
      })
    },
    sendSelected () {
      this.sourceSelected.forEach(element => {
        this.destinationData.push(element)
        this.sourceData = this.arrayRemove(this.sourceData, element)
      })
      this.$emit('selected-data', this.destinationData)
    },
    sendAll () {
      this.sourceData.forEach(element => {
        this.destinationData.push(element)
        this.sourceData = this.arrayRemove(this.sourceData, element)
      })
      this.$emit('selected-data', this.destinationData)
    },
    returnSelected () {
      this.destinactionSelected.forEach(element => {
        this.sourceData.push(element)
        this.destinationData = this.arrayRemove(this.destinationData, element)
      })
      this.$emit('selected-data', this.destinationData)
    },
    returnAll () {
      this.destinactionData.forEach(element => {
        this.sourceData.push(element)
        this.destinationData = this.arrayRemove(this.destinationData, element)
      })
      this.$emit('selected-data', this.destinationData)
    }
  },
  beforeDestroy () {
    this.sourceData = []
    this.destinationDate = []
  }
}
</script>
<style scoped>
.actions {
  margin-top: 100px;
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
  /* background-color: #007bff; */
  /* border-color: #007bff; */
  cursor: pointer;
}
.btn-action svg {
  vertical-align: middle;
}
</style>
