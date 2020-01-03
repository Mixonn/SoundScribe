<template>
  <v-layout
    column
    justify-center
    align-center
  >
    <v-flex
      xs12
      sm8
      md6
    >
      <div class="text-center">
      </div>
      <v-card>
        <v-card-title class="headline">
          Available files
        </v-card-title>
        <v-card-text>
          <ul>
            <li v-for="file in files" v-bind:key="file" class="item">
              {{ file }}
            </li>
          </ul>
        </v-card-text>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script>

export default {
  async asyncData ({ $axios }) {
    const res = await $axios.get('/download/list-files?extension=mp3')
    return { files: res.data }
  }
}

</script>
