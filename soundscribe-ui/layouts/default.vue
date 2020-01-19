<template>
  <v-app>
    <v-navigation-drawer
      v-model="drawer"
      :mini-variant.sync="miniVariant"
      permanent
      fixed
      app
      dark
      color="drawerBackground"
    >
      <v-list>
        <v-list-item
          v-if="!$auth.loggedIn"
          to="/login"
          exact
        >
          Login
        </v-list-item>
        <v-list-item
          v-if="$auth.loggedIn"
          to="/login"
        >
          User: {{ loggedIn ? this.$auth.user.username : "not logged" }}
        </v-list-item>
        <v-list-item
          v-if="$auth.loggedIn"
          @click="logout()"
        >
          Log out
        </v-list-item>
        <v-list-item
          v-for="(item, i) in items"
          :key="i"
          :to="item.to"
          router
          exact
        >
          <v-list-item-action>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title v-text="item.title" />
          </v-list-item-content>
        </v-list-item>
        <v-spacer />
      </v-list>
    </v-navigation-drawer>
    <v-app-bar
      :clipped-left="clipped"
      fixed
      app
      color="headerBackground"
    >
      <v-img :src="require('@/static/logo.png')" height="50px" contain />
    </v-app-bar>
    <v-content>
      <v-container class="mainContainer ma-0 pa-0" fluid fill-height>
        <v-layout
          column
          wrap
        >
          <v-app style="color: black;">
            <v-card
              height="100%"
              elevation="0"
              style="color: black; border-radius: 0;"
              class="sideBackground"
            >
              <nuxt />
            </v-card>
          </v-app>
        </v-layout>
      </v-container>
    </v-content>
    <v-footer
      :fixed="fixed"
      app
      color="headerBackground"
    >
      <span style="color: white">
        Inżynierowie Dźwięku &copy; 2020
      </span>
    </v-footer>
  </v-app>
</template>

<script>
import { mapState } from 'vuex'

export default {
  computed: {
    loggedIn () {
      return this.$auth.loggedIn
    }
  },
  data () {
    return {
      clipped: false,
      drawer: false,
      fixed: false,
      items: [
        {
          icon: 'mdi-folder-home',
          title: 'Overview',
          to: '/',
          color: 'primary'
        },
        {
          icon: 'mdi-folder-open',
          title: 'Open',
          to: '/open',
          color: 'primary'
        },
        {
          icon: 'mdi-file-upload',
          title: 'Upload',
          to: '/upload'
        },
        {
          icon: 'mdi-file-import',
          title: 'Import',
          to: '/import'
        },
        {
          icon: 'mdi-application-export',
          title: 'Export',
          to: '/export'
        }
      ],
      miniVariant: false,
      right: true,
      rightDrawer: false,
      title: 'SoundScribe'
    }
  },
  methods: {
    async logout () {
      await this.$auth.logout()
      this.$auth.strategies.keycloak.options.endpoints.user.headers.Authorization = null
      this.$router.push('/')
    }
  }
}
</script>

<style lang="scss">
  .application {
    color: white;
  }
</style>
