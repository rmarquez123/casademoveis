package used_furniture.restapi;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import used_furniture.core.Category;
import used_furniture.core.products.model.Photo;
import used_furniture.core.PhotoDTO;
import used_furniture.core.products.model.Product;
import used_furniture.core.ProductsSource;
import used_furniture.core.ProductsStore;

/**
 *
 * @author rmarq
 */
@Controller
public class ProductServlet {

  /**
   *
   */
  @Autowired
  private ProductsSource source;

  /**
   *
   */
  @Autowired
  private ProductsStore store;

  /**
   *
   * @return
   */
  @RequestMapping(path = "/categories", method = RequestMethod.GET)
  @ResponseBody
  public List<Category> categories() {
    List<Category> result = this.source.getCategories();
    return result;
  }

  /**
   *
   * @param siteVisibleOnly
   * @return
   */
  @RequestMapping(path = "/products", method = RequestMethod.GET)
  @ResponseBody
  public List<Product> products(
          @RequestParam(name="siteVisibleOnly", defaultValue = "false") boolean siteVisibleOnly) {
    
    
    List<Product> products = this.source.getProducts(siteVisibleOnly);
    return products;
  }

  /**
   *
   * @param category
   * @param siteVisibleOnly
   * @return
   */
  @RequestMapping(path = "/products/byCategory", method = RequestMethod.GET)
  @ResponseBody
  public List<Product> productsByCategory(
          @RequestParam("category") Integer category,
          @RequestParam(name="siteVisibleOnly", defaultValue = "false") boolean siteVisibleOnly
  ) {
    List<Product> products = this.source.getProducts(siteVisibleOnly);
    products.removeIf(p -> !Objects.equals(p.category, category));
    return products;
  }

  /**
   *
   * @param name
   * @param description
   * @param category
   * @param available
   * @param length
   * @param depth
   * @param height
   * @param price
   * @param siteVisible
   * @param socialMediaVisible
   * @return
   */
  @RequestMapping(path = "/products/add", method = RequestMethod.POST)
  @ResponseBody
  public int addProduct(
          @RequestParam("name") String name, // 
          @RequestParam("description") String description, // 
          @RequestParam("category") Integer category,
          @RequestParam("available") Boolean available,
          @RequestParam("length") Double length,
          @RequestParam("depth") Double depth,
          @RequestParam("height") Double height,
          @RequestParam("price") Double price, 
          @RequestParam("siteVisible") Boolean siteVisible, 
          @RequestParam("socialMediaVisible") Boolean socialMediaVisible 
  ) {
    ZonedDateTime dateReceived = ZonedDateTime.now();
    Product product = new Product(-1, name, description, available, // 
            dateReceived, null, category, null, length, depth, height, price, 
            siteVisible, socialMediaVisible);
    
    int productId = this.store.addProduct(product);
    return productId;
  }

  /**
   *
   * @param productId
   * @param name
   * @param description
   * @param available
   * @param category
   * @param dateReceived
   * @param dateSold
   * @param length
   * @param depth
   * @param height
   * @param price
   * @param siteVisible
   * @param socialMediaVisible
   */
  @RequestMapping(path = "/products/edit", method = RequestMethod.POST)
  @ResponseBody
  public void editProduct(
          @RequestParam("productId") Integer productId,
          @RequestParam("name") String name,
          @RequestParam("description") String description,
          @RequestParam("available") Boolean available,
          @RequestParam("category") Integer category,
          @RequestParam("dateReceived")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateReceived,
          @RequestParam(name = "dateSold", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime dateSold,
          @RequestParam("length") Double length,
          @RequestParam("depth") Double depth,
          @RequestParam("height") Double height,
          @RequestParam("price") Double price, 
          @RequestParam("siteVisible") Boolean siteVisible, 
          @RequestParam("socialMediaVisible") Boolean socialMediaVisible 
  ) {

    Product product = new Product(
            productId, name, description,
            available, dateReceived, dateSold,
            category, null,
            length, depth, height, price, 
            siteVisible, socialMediaVisible
    );
    this.store.updateProduct(product);
  }

  /**
   *
   * @param productId
   * @return
   */
  @RequestMapping(path = "/products/remove", method = RequestMethod.POST)
  @ResponseBody
  public boolean removeProduct(
          @RequestParam("productId") Integer productId) {
    this.store.removeProduct(productId);
    return true;
  }

  /**
   *
   * @param productIds
   * @param height
   * @param width
   * @return
   */
  @RequestMapping(path = "/photos", method = RequestMethod.GET)
  @ResponseBody
  public Map<Integer, List<PhotoDTO>> photos(
          @RequestParam("productIds") ArrayList<Integer> productIds, //
          @RequestParam("height") Integer height, //
          @RequestParam("width") Integer width //
  ) {

    Map<Integer, List<Photo>> rawPhotos = this.source.getPhotos(productIds, width, height);
    Map<Integer, List<PhotoDTO>> photos = new HashMap<>();
    for (Map.Entry<Integer, List<Photo>> entry : rawPhotos.entrySet()) {
      List<PhotoDTO> base64Photos = entry.getValue().stream()
              .map(this::toPhotoJson) //
              .collect(Collectors.toList());
      photos.put(entry.getKey(), base64Photos);
    }
    return photos;
  }

  /**
   *
   * @param productId
   * @return
   */
  @RequestMapping(path = "/photos/product", method = RequestMethod.GET)
  @ResponseBody
  public List<PhotoDTO> getPhotosForProduct(@RequestParam("productId") Integer productId) {
    Map<Integer, List<Photo>> rawPhotosMap = this.source.getPhotos(Arrays.asList(productId));
    List<Photo> rawPhotos = rawPhotosMap.containsKey(productId) ? rawPhotosMap.get(productId) : new ArrayList<>();
    return rawPhotos.stream() //
            .map(this::toPhotoJson) //
            .collect(Collectors.toList());
  }

  /**
   *
   * @param productId
   * @param height
   * @param width
   * @return
   */
  @RequestMapping(path = "/photos/product/single", method = RequestMethod.GET)
  @ResponseBody
  public PhotoDTO getPhotosForProductSingle(@RequestParam("productId") Integer productId, //
          @RequestParam("height") Integer height, //
          @RequestParam("width") Integer width
  ) {
    Map<Integer, List<Photo>> rawPhotosMap = this.source.getPhotos(Arrays.asList(productId), width, height);
    List<Photo> rawPhotos = rawPhotosMap.containsKey(productId) ? rawPhotosMap.get(productId) : new ArrayList<>();
    List<PhotoDTO> collect = rawPhotos.stream() //
            .map(this::toPhotoJson) //
            .collect(Collectors.toList());
    return collect.isEmpty() ? null : collect.get(0);
  }

  private PhotoDTO toPhotoJson(Photo photo) throws RuntimeException {
    long photoId = photo.photoId;
    String encodeToString = Base64.getEncoder().encodeToString(photo.bytes);
    return new PhotoDTO(photoId, encodeToString);
  }

  @RequestMapping(path = "/photos/add", method = RequestMethod.POST)
  @ResponseBody
  public long addPhoto(
          @RequestParam("productId") long productId,
          @RequestParam("photo") String photoBase64) {
    byte[] photoBytes = Base64.getDecoder().decode(photoBase64);
    Photo photo = new Photo(-1, productId, photoBytes);
    return this.store.addPhoto(photo);
  }

  @RequestMapping(path = "/photos/remove", method = RequestMethod.POST)
  @ResponseBody
  public void removePhoto(@RequestParam("photoId") long photoId) {
    this.store.removePhoto(photoId);
  }
}
