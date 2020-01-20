<!--It could be used, but onclick do not work. Try to use it later. //todo-->

<template>
  <v-tooltip bottom>
    <template v-slot:activator="{ on }">
      <img
        :alt="tooltip != null ? tooltip : src.substr(src.lastIndexOf('/'), src.lastIndexOf('.'))"
        class="controlButton"
        :class="{ active: highlight === true,
         gray: grayscale }"
        :src="src"
        v-on="on"
        @click="$emit('clicked')"
      >
    </template>
    <span v-if="tooltip != null">{{ tooltip }}</span>
  </v-tooltip>
</template>

<script>
export default {
  name: 'ImgTooltip',
  props: {
    src: {
      type: String,
      required: true
    },
    tooltip: {
      type: String,
      default: ''
    },
    highlight: {
      type: Boolean,
      default: false
    },
    grayscale: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped lang="scss">
  .controlButton {
    width: 50px;
    height: 50px;
    &:hover,
    &:focus {
      box-shadow: inset 0 0 0 2em rgba(0, 0, 0, .4);
      transition: box-shadow .1s;
      -webkit-transition: box-shadow .1s;
    }
  }
  .active {
    background-color: #ff9901;
    opacity: .8;
    border-radius: 20px;
    transition: background-color .5s;
    -webkit-transition: background-color .5s;
  }
  .gray {
    -webkit-filter: grayscale(100%); /* Safari 6.0 - 9.0 */
    filter: grayscale(100%);
  }
</style>
