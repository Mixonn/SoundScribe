<template>
  <div>
    <v-card-title class="headline">
      Available files
    </v-card-title>
    <v-card-text class="sider">
      <ul>
        <li v-for="file in files" :key="file" class="item" @click="uploadFile(file)">
          {{ file }}
        </li>
      </ul>
    </v-card-text>
  </div>
</template>

<script>

export default {
  middleware: 'customauth',
  async asyncData ({ $axios }) {
    const res = await $axios.get('/download/list-files?extension=abc')
    return { files: res.data }
  },
  methods: {
    uploadFile (file) {
      this.$axios.get('/dlibra/upload?file=' + file)
    }
  }
}

</script>
