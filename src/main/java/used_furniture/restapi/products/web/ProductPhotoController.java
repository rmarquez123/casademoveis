package used_furniture.restapi.products.web;

import java.util.Optional;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import used_furniture.core.products.model.Photo;
import used_furniture.core.products.repository.PhotoRepository;

/**
 * Serves product photos by photoId, e.g.
 *   GET /product/photo/42
 *
 * This is used both by your frontend and by Facebook (imageBaseUrl).
 */
@RestController
@RequestMapping("/product/photo")
public class ProductPhotoController {

  private final PhotoRepository photoRepository;

  public ProductPhotoController(PhotoRepository photoRepository) {
    this.photoRepository = photoRepository;
  }

  @GetMapping("/{photoId}")
  public ResponseEntity<byte[]> getPhoto(@PathVariable("photoId") long photoId) {

    Optional<Photo> photoOpt = photoRepository.findById(photoId);
    if (photoOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    Photo photo = photoOpt.get();
    byte[] bytes = photo.getBytes();

    // If all your stored photos are JPEGs, this is fine.
    // If you later store different formats, you can extend Photo with a MIME type field.
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    headers.setContentLength(bytes.length);
    headers.setCacheControl(CacheControl.noCache().getHeaderValue());

    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }
}
