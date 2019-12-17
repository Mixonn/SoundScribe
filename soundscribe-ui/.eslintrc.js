module.exports = {
  root: true,
  env: {
    browser: true,
    node: true
  },
  parserOptions: {
    parser: 'babel-eslint'
  },
  extends: [
    '@nuxtjs',
    'plugin:nuxt/recommended'
  ],
  // add your custom rules here
  rules: {
    "semi": 0,
    "no-unused-vars": 0,
    "new-cap": 0,
    "html-self-closing": 0,
    "no-console": 0
  }
}
