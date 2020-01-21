import colors from 'vuetify/lib/util/colors'
import webpack from 'webpack'

export default {
  mode: 'spa',
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
    // Doc: https://github.com/nuxt-community/eslint-module
    '@nuxtjs/eslint-module',
    '@nuxtjs/vuetify'
  ],
  /*
  ** Nuxt.js modules
  */
  modules: [
    '@nuxtjs/axios',
    '@nuxtjs/auth',
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
    baseURL: 'http://localhost:80/be'
  },
  auth: {
    strategies: {
      keycloak: {
        _scheme: 'oauth2',
        authorization_endpoint: 'http://localhost:80/auth/realms/soundscribe/protocol/openid-connect/auth',
        userinfo_endpoint: false,
        access_type: 'offline',
        access_token_endpoint: 'http://localhost:80/auth/realms/soundscribe/protocol/openid-connect/token',
        response_type: 'code',
        token_type: 'Bearer',
        token_key: 'access_token',
        client_secret: 'a0432aea-49ec-4313-8080-a4db15df8bc7', // for vue-read: 75ba96f0-9836-43dc-a18a-d81b4ef20b65
        client_id: 'vue-edit',
        redirect_uri: 'http://localhost:80/callback',
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
        // ...etc.
      })
    ]
  }
}
