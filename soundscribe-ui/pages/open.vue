<template>
  <div>
    <v-card-title class="headline">
      Available files to open:
    </v-card-title>
    <v-card-text class="sider">
      <ul>
        <li v-for="file in files" :key="file" class="item">
          <nuxt-link :to="'/logic/staff/' + file">
            {{ file }}
          </nuxt-link>
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
  }
}

</script>
