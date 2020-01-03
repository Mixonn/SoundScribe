<!-- https://scotch.io/tutorials/how-to-handle-file-uploads-in-vue-2 -->
<template>
  <div>
    <v-card-title class="headline">
      Upload
    </v-card-title>
    <v-card-text class="sider">
      <Upload />
    </v-card-text>
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
