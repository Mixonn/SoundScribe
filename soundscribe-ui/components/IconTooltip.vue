<template>
  <v-tooltip bottom>
    <template v-slot:activator="{ on }">
      <a :href="link ? link : ' '">
        <font-awesome-icon
          :class="`${classes}`"
          :icon="['fab', i]"
          @click="$emit('clicked')"
          v-on="on"
        />
      </a>
    </template>
    <span v-if="tooltip != null">{{ tooltip }}</span>
  </v-tooltip>
</template>

<script>
export default {
  name: 'IconTooltip',
  props: {
    i: {
      type: String,
      required: true
    },
    tooltip: {
      type: String,
      default: ''
    },
    classes: {
      type: String,
      required: false,
      default: 'icon is-medium'
    },
    link: {
      type: String,
      required: false
    }
  }
}
</script>

<style scoped lang="scss">
</style>
