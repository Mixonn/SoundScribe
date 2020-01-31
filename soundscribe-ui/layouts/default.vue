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
          v-if="!isLoggedIn"
          to="/login"
          exact
        >
          <v-list-item-action>
            <v-icon>mdi-login</v-icon>
          </v-list-item-action>
          Login
        </v-list-item>
        <v-list-item
          v-if="isLoggedIn"
          class="logged-item"
          exact
        >
          <v-list-item-action>
            <v-icon>mdi-account</v-icon>
          </v-list-item-action>
          Logged in!
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
        <v-list-item
          v-if="isLoggedIn"
          exact
          @click="logout()"
        >
          <v-list-item-action>
            <v-icon>mdi-logout</v-icon>
          </v-list-item-action>
          Logout
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
      &nbsp; &nbsp; &nbsp;
      <icon-tooltip i="github" tooltip="Go to github repository" link="https://github.com/Mixonn/SoundScribe" />
    </v-footer>
  </v-app>
</template>

<script>
import IconTooltip from '../components/IconTooltip';
export default {
  components: { IconTooltip },
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
          icon: 'mdi-download',
          title: 'Download',
          to: '/download',
          color: 'primary'
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
  computed: {
    isLoggedIn () {
      return this.$store.getters.isLoggedIn
    }
  },
  methods: {
    logout () {
      this.$store.commit('logout')
      delete this.$axios.defaults.headers.common.Authorization
      this.$router.push('/')
    }
  }
}
</script>

<style lang="scss">
  .application {
    color: white;
  }

  .logged-item {
    background: green;
  }

  .icon-tooltip {
    padding: 5px;
  }
</style>
