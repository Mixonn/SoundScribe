<!--It could be used, but onclick do not work. Try to use it later. //todo-->

<template>
  <v-tooltip bottom>
    <template v-slot:activator="{ on }">
      <img
        :alt="tooltip != null ? tooltip : 'button'"
        class="controlButton"
        :class="{ active: highlight === true }"
        :src="src"
        v-on="on"
        @click="$emit(clickMethod.name, clickParams)"
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
    color: #0942ff;
  }
</style>
