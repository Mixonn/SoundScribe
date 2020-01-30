<template>
  <div>
    <v-card-title class="headline">
      Available files to download:
    </v-card-title>
    <v-card-text class="sider">
      <ul>
        <li v-for="file in files" :key="file" class="item">
          <a :href="'be/download/' + file">
            {{ file }}
          </a>
        </li>
      </ul>
    </v-card-text>
  </div>
</template>

<script>

export default {
  middleware: 'customauth',
  data () {
    return {
      baseUrl: this.$axios.defaults.baseURL
    };
  },
  async asyncData ({ $axios }) {
    const res = await $axios.get('/download/list-files?extension=');
    return { files: res.data }
  }
}

</script>
