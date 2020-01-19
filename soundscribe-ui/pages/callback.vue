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
        'client_secret': '6e14846a-8711-4396-abea-9d2cfa7dd867',
        'scope': 'soundscribe-edit,soundscribe-read',
        'code': this.$route.query.code,
        'redirect_uri': 'http://localhost:80/callback'
      })

    const token = await getToken()

    if (token) {
      this.$axios.defaults.headers.common.Authorization = 'Bearer ' + token.access_token
    }

    this.$auth.loggedIn = true
  }
}
</script>
