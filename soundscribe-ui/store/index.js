const qs = require('querystring');

export const state = ({
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  clientId: 'vue-edit',
  clientSecret: 'e63e4b6f-4a50-49aa-bcdd-64bc2a8bf2d0'
});

export const mutations = ({
  auth_request (state) {
    state.status = 'loading'
  },
  auth_success (state, token) {
    state.status = 'success';
    console.log('refresh: ' + token.refresh_token);
    state.accessToken = token.access_token;
    state.refreshToken = token.refresh_token
  },
  auth_error (state) {
    state.status = 'error'
  },
  logout (state) {
    const requestBody = {
      client_id: state.clientId,
      client_secret: state.clientSecret,
      refresh_token: state.refreshToken
    };

    const config = {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Bearer ' + state.accessToken
      }
    };
    this.$axios.$post('http://' + process.env.hostName + '/auth/realms/soundscribe/protocol/openid-connect/logout', qs.stringify(requestBody), config);

    state.status = '';
    state.accessToken = '';
    state.refreshToken = ''
  }
});

export const actions = ({
});

export const getters = ({
  isLoggedIn: state => !!state.accessToken,
  authStatus: state => state.status,
  getClientId: state => state.clientId,
  getClientSecret: state => state.clientSecret
});
