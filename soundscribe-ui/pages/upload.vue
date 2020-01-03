<!-- https://scotch.io/tutorials/how-to-handle-file-uploads-in-vue-2 -->
<template>
  <div>
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
  </div>
</template>

<script>
import Upload from '../components/Upload'

export default {
  components: {
    Upload
  }
}

</script>
