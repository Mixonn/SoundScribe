<template>
  <div>
    <v-card-title class="headline">
      Login
    </v-card-title>
    <v-card-text class="sider">
      Redirecting...
    </v-card-text>
  </div>
</template>

<script>

export default {
  async mounted () {
    await this.$auth.loginWith('keycloak')
  }
}

</script>
