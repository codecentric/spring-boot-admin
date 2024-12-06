import Carousel from "react-multi-carousel";
import "react-multi-carousel/lib/styles.css";
import styles from "./Screenshot.module.css";

export function Screenshot({ images }: { images: {src: string, description: string}[] }) {
  return (
    <Carousel
      additionalTransfrom={0}
      arrows
      autoPlaySpeed={3000}
      draggable
      infinite
      keyBoardControl
      minimumTouchDrag={80}
      pauseOnHover
      responsive={{
        desktop: {
          breakpoint: {
            max: 3000,
            min: 1024
          },
          items: 1
        },
        mobile: {
          breakpoint: {
            max: 464,
            min: 0
          },
          items: 1
        },
        tablet: {
          breakpoint: {
            max: 1024,
            min: 464
          },
          items: 1
        }
      }}
      shouldResetAutoplay
      sliderClass=""
      slidesToSlide={1}
      swipeable
    >
      {images.map((image, index) => {
        const { default: imgSrc } = require(`../../assets/screens/${image.src}`);
        return (
          <div className={styles.screenshot}>
            <div className={styles.screenshot__description}>
              {image.description}
            </div>
            <img src={imgSrc} />
          </div>
        );
      })}
    </Carousel>
  );
}
