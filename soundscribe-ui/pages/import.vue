<template>
  <div>
    <v-card-title class="headline">
      Available files in dLibra
    </v-card-title>
    <v-card-text class="sider">
      <ul>
        <li v-for="file in files" :key="file" class="item" @click="downloadFile(file)">
          {{ file.toString().split(":")[0] }}
        </li>
      </ul>
    </v-card-text>
  </div>
</template>

<script>

export default {
  middleware: 'customauth',
  async asyncData ({ $axios }) {
    const res = await $axios.get('/dlibra/list-files')
    return { files: res.data }
  },
  methods: {
    downloadFile (file) {
      this.$axios.get('/dlibra/download?publicationToDownload=' + file)
    }
  }
}

</script>
