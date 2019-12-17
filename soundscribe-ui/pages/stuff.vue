<template>
  <v-layout>
    <v-container>
      <v-row class="text-center justify-center">
        <img
          src="/v.png"
          alt="Vuetify.js"
          class="mb-5"
        >
      </v-row>
      <v-row class="text-center justify-center">
        <StaffLines />
      </v-row>
    </v-container>
  </v-layout>
</template>

<script>
import StaffLines from '../components/abc/StaffLines'

export default {
  components: { StaffLines }
}
</script>
