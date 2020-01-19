<template>
  <div>
    <v-card-title class="headline">
      Login
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
  async mounted () {
    const oauth = require('axios-oauth-client');
    try {
      await this.$auth.loginWith('keycloak')
    } catch (e) {
      console.log('login failed')
    }
  }
}

</script>
