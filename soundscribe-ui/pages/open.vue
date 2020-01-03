<template>
  <div>
    <div class="text-center">
    </div>
    <v-card>
      <v-card-title class="headline">
        Available files
      </v-card-title>
      <v-card-text class="sider">
        <ul>
          <li v-for="file in files" v-bind:key="file" class="item">
            <nuxt-link :to="'/logic/staff/' + file">{{ file }}</nuxt-link>
          </li>
        </ul>
      </v-card-text>
    </v-card>
  </div>
</template>

<script>

export default {
  async asyncData ({ $axios }) {
    const res = await $axios.get('/download/list-files?extension=abc')
    return { files: res.data }
  }
}

</script>
