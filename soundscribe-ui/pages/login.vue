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
    console.log('Host name:');
    console.log(process.env.hostName);
    await this.$auth.loginWith('keycloak')
  }
}

</script>
