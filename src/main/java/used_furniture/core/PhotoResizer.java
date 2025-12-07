package used_furniture.core;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author rmarq
 */
public class PhotoResizer {

  /**
   *
   * @param bytes
   * @param width
   * @param height
   * @return
   */
  byte[] resizePhoto(byte[] bytes, Integer width, Integer height) {

    try {
      /* Decode input image */
      ByteArrayInputStream in = new ByteArrayInputStream(bytes);
      BufferedImage originalImage = ImageIO.read(in);

      if (originalImage == null) {
        throw new IllegalArgumentException("Invalid image data");
      }

      /* Create resized image */
      BufferedImage resizedImage = new BufferedImage(
              width,
              height,
              BufferedImage.TYPE_INT_RGB
      );

      Graphics2D g2d = resizedImage.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2d.drawImage(originalImage, 0, 0, width, height, null);
      g2d.dispose();

      /* Encode back to byte[] */
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ImageIO.write(resizedImage, "jpg", out);

      return out.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException("Failed to resize image", e);
    }
  }

}
