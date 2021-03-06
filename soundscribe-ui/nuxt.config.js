import colors from 'vuetify/lib/util/colors'
import webpack from 'webpack'

require('dotenv').config();

export default {
  mode: 'spa',
  /*
  ** Possible values: "write", "read"
   */
  soundscribeMode: 'write',
  /*
  ** Headers of the page
  */
  head: {
    titleTemplate: '%s - ' + process.env.npm_package_name,
    title: process.env.npm_package_name || '',
    meta: [
      { charset: 'utf-8' },
      { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      { hid: 'description', name: 'description', content: process.env.npm_package_description || '' }
    ],
    link: [
      { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
    ]
  },
  /*
  ** Customize the progress-bar color
  */
  loading: { color: '#fff' },
  /*
  ** Global CSS
  */
  css: [
  ],
  /*
  ** Plugins to load before mounting the App
  */
  plugins: [
    '~/plugins/vue-waveform'
  ],
  /*
  ** Nuxt.js dev-modules
  */
  buildModules: [
    '@nuxtjs/eslint-module',
    '@nuxtjs/vuetify',
    ['@nuxtjs/dotenv', { systemvars: true }]
  ],
  /*
  ** Nuxt.js modules
  */
  modules: [
    '@nuxtjs/axios',
    '@nuxtjs/auth',
    '@nuxtjs/dotenv',
    [
      'nuxt-fontawesome', {
        imports: [
          {
            set: '@fortawesome/free-solid-svg-icons',
            icons: []
          },
          {
            set: '@fortawesome/free-brands-svg-icons',
            icons: ['faGithub']
          }
        ]
      }]
  ],
  /*
  ** vuetify module configuration
  ** https://github.com/nuxt-community/vuetify-module
  */
  vuetify: {
    treeShake: true,
    customVariables: ['~/assets/variables.scss'],
    theme: {
      options: { customProperties: true },
      light: true,
      themes: {
        light: {
          primary: 'black',
          accent: '#ff0031',
          secondary: colors.amber.darken3,
          info: colors.teal.lighten1,
          warning: colors.amber.base,
          error: colors.deepOrange.accent4,
          success: colors.green.accent3,
          mainBackground: '#ffffff',
          sideBackground: '#efefef',
          side2Background: '#f7f7f7',
          headerBackground: '#5b5b5a',
          drawerBackground: '#424242'

        }
      }
    }
  },
  axios: {
    baseURL: 'http://' + process.env.HOST_NAME + ':80/be'
  },
  auth: {
    strategies: {
      keycloak: {
        _scheme: 'oauth2',
        authorization_endpoint: 'http://' + process.env.HOST_NAME + ':80/auth/realms/soundscribe/protocol/openid-connect/auth',
        userinfo_endpoint: false,
        access_type: 'offline',
        access_token_endpoint: 'http://' + process.env.HOST_NAME + ':80/auth/realms/soundscribe/protocol/openid-connect/token',
        response_type: 'code',
        token_type: 'Bearer',
        token_key: 'access_token',
        client_secret: 'e63e4b6f-4a50-49aa-bcdd-64bc2a8bf2d0', // for vue-read: ece59075-7394-45c6-849d-235fb6b9ae94
        client_id: 'vue-edit',
        redirect_uri: 'http://' + process.env.HOST_NAME + ':80/callback',
        grant_type: 'authorization_code',
        scope: 'soundscribe-edit,soundscribe-read'
      }
    },
    redirect: {
      callback: '/callback',
      home: '/',
      logout: false
    }
  },
  /*
  ** Build configuration
  */
  build: {
    /*
    ** You can extend webpack config here
    */
    extend (config, ctx) {
      config.devtool = ctx.isClient ? 'eval-source-map' : 'inline-source-map';
      config.node = {
        fs: 'empty'
      }
    },
    plugins: [
      new webpack.ProvidePlugin({
        '$': 'jquery',
        '_': 'lodash'
      })
    ]
  }
}
