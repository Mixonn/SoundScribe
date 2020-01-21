<template>
  <v-app>
    <v-container class="sideBackground">
      <v-row>
        <v-col>
          <v-card  height="50px" elevation="0" class="headerBackground">
            <v-card-title class="justify-center">
              <nuxt-link style="color: white;" :to="'/logic/midi/' + this.$route.params.song">
                MIDI
              </nuxt-link>
            </v-card-title>
          </v-card>
        </v-col>
        <v-col>
          <v-card  height="50px" elevation="0" class="headerBackground">
            <v-card-title class="justify-center">
              <nuxt-link style="color: white;" :to="'/logic/staff/' + this.$route.params.song">
                Sheet music
              </nuxt-link>
            </v-card-title>
          </v-card>
        </v-col>
        <v-col>
          <v-card height="50px" elevation="0" class="mainBackground center">
            <v-card-text class="justify-center center" style="color: black;">
              <div style="text-align: center;">
                <b>{{ this.$route.params.song }}</b>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
    <v-container class="mainBackground">
      <nuxt-child />
    </v-container>
    <v-footer color="sideBackground" />
  </v-app>
</template>

<script>
export default {
  middleware: ['customauth']
}

</script>
