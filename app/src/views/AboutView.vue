<template>
  <div class="about">
    <section class="our-story">
      <h2>Our Story</h2>
      <figure class="image-container">
        <img src="@/assets/blue-neon-light.jpg" alt="Blue Neon Light" class="story-image" />
        <figcaption class="image-caption">
          Photo by <a
            href="https://unsplash.com/@nikita_s?utm_content=creditCopyText&utm_medium=referral&utm_source=unsplash">Nikita
            Samutin</a> on <a
            href="https://unsplash.com/photos/a-neon-sign-that-is-on-the-side-of-a-wall-xaf7szw7rh4?utm_content=creditCopyText&utm_medium=referral&utm_source=unsplash">Unsplash</a>
        </figcaption>
      </figure>
      <p>
        Welcome to Scoops2Go! Our journey began with a simple idea: to bring the joy of delicious, high-quality ice
        cream to your doorstep. Founded in 2021, we have quickly become a favorite among ice cream enthusiasts for our
        commitment to quality, flavor, and customer satisfaction.
      </p>
      <p>
        At Scoops2go, we believe that everyone deserves a sweet treat, no matter where they are. That's why we offer a
        wide range of flavors, from classic vanilla and chocolate to unique creations that you won't find anywhere else.
        Our ice cream is made with the finest ingredients, ensuring that every scoop is a delightful experience.
      </p>
      <p>
        Our team is passionate about ice cream and dedicated to providing the best service possible. Whether you're
        celebrating a special occasion or just craving a sweet treat, ScoopsTwoGo is here to make your day a little
        sweeter.
      </p>
    </section>
  </div>
</template>

<style>
@media (min-width: 1024px) {
  .about {
    min-height: 100vh;
    display: flex;
    align-items: center;
    flex-direction: column;
    text-align: center;
  }

  .our-story {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem;
  }

  .our-story h2 {
    font-size: 2rem;
    margin-bottom: 1rem;
  }

  .our-story p {
    font-size: 1.2rem;
    line-height: 1.6;
    margin-bottom: 1rem;
  }

  .image-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 1rem;
  }

  .story-image {
    width: 100%;
    max-width: 600px;
    height: auto;
  }

  .image-caption {
    font-size: 0.7rem;
    color: #555;
    margin-top: 0.5rem;
  }
}
</style>
