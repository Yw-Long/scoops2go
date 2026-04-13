<template>
  <div class="greetings">
    <h1 class="green"> {{ title }} </h1>
    <span>Frozen treats delivered to your door. Fast, friendly, frozen!</span>
    <a :href="link">{{ description }}</a>
  </div>
</template>

<script>
export default {
  name: 'Hero',
  props: {
    title: {
      type: String,
      required: true,
    },
    link: {
      type: String,
      required: true,
    },
    description: {
      type: String,
      required: true,
    }
  }
}
</script>

<style scoped>

.logo {
  display: block;
  margin: 0 auto 2rem;
}

h1 {
  font-weight: 500;
  font-size: 2.6rem;
  position: relative;
  top: -10px;
}

h3 {
  font-size: 1.2rem;
}

.greetings h1,
.greetings h3 {
  text-align: center;
}

@media (min-width: 1024px) {
  .greetings h1,
  .greetings h3 {
    text-align: left;
  }
}
</style>
