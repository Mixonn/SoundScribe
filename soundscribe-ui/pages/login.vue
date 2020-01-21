<template>
  <div>
    <v-card-title class="headline">
      Login
    </v-card-title>
    <v-card-text class="sider">
      Click
      <button v-on:click="doLogin">here</button>
      to login...
    </v-card-text>
  </div>
</template>

<script>

export default {
  auth: false,
  methods: {
    doLogin () {
      this.$auth.loginWith('keycloak')
    }
  }
}

</script>
