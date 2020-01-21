<template>
  <div>
    <v-card-title class="headline">
      You have been successfully logged in
    </v-card-title>
  </div>
</template>

<script>
export default {
  async mounted () {
    const oauth = require('axios-oauth-client');
    const getToken = oauth.client(this.$axios.create(),
      {
        'url': 'http://localhost:80/auth/realms/soundscribe/protocol/openid-connect/token',
        'grant_type': 'authorization_code',
        'client_id': 'vue-edit',
        'client_secret': 'a0432aea-49ec-4313-8080-a4db15df8bc7',
        'scope': 'soundscribe-edit,soundscribe-read',
        'code': this.$route.query.code,
        'redirect_uri': 'http://localhost:80/callback'
      })

    const token = await getToken()

    if (token) {
      this.$axios.defaults.headers.common.Authorization = 'Bearer ' + token.access_token
      this.$store.commit('auth_success', token)

      console.log(token)
    }
  }
}
</script>
