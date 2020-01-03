<template>
  <v-app>
    <v-navigation-drawer
      v-model="drawer"
      :mini-variant.sync="miniVariant"
      permanent
      fixed
      app
      color="drawerBackground"
    >
      <v-list>
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
        <v-spacer/>
      </v-list>
    </v-navigation-drawer>
    <v-app-bar
      :clipped-left="clipped"
      fixed
      app
      color="headerBackground"
    >
      <v-img :src="require('@/static/logo.png')" height="50px" contain/>
    </v-app-bar>
    <v-content>
      <v-container class="mainContainer ma-0 pa-0" fluid fill-height>
        <v-layout
          column
          wrap
        >
          <v-flex
            fill-height
            fluid
          >
            <div class="application">
              <nuxt />
            </div>
          </v-flex>
        </v-layout>
      </v-container>
    </v-content>
    <v-footer
      :fixed="fixed"
      app
      color="headerBackground"
    >
      <span>
        Inżynierowie Dźwięku &copy; 2020</span>
    </v-footer>
  </v-app>
</template>

<script>
export default {
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
  }
}
</script>

<style lang="scss">
  .mainContainer {
    background: #ffffff;
  }
  .footer {
    background: #e9ef96;
  }
  .application{
    color:black;
  }
</style>
