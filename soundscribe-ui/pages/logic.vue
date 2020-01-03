<template>
  <v-app>
    <v-container class="sideBackground">
      <v-row>
        <v-col>
          <v-card>
            <v-card-title>
              <nuxt-link :to="'/logic/midi/' + this.$route.params.song">MIDI</nuxt-link>
            </v-card-title>
          </v-card>
        </v-col>
        <v-col>
          <v-card>
            <v-card-title>
              <nuxt-link :to="'/logic/staff/' + this.$route.params.song">Sheet music</nuxt-link>
            </v-card-title>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
    <v-container class="mainBackground">
        <nuxt-child />
    </v-container>
    <v-footer color="sideBackground">
      footer
    </v-footer>
  </v-app>
</template>
