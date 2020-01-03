<!-- https://scotch.io/tutorials/how-to-handle-file-uploads-in-vue-2 -->
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
          Upload
        </v-card-title>
        <v-card-text>
          <Upload />
        </v-card-text>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script>
  import Upload from '../components/Upload'

  export default {
  components: {
    Upload
  }

}

</script>
