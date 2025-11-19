package used_furniture.core;

/**
 *
 * @author rmarq
 */
public class Photo {
  
  public final long photoId; 
  public final long productId; 
  public final byte[] bytes; 
  
  /**
   *
   * @param photoId
   * @param productId
   * @param bytes
   */
  public Photo(long photoId, long productId, byte[] bytes) {
    this.photoId = photoId;
    this.productId = productId;
    this.bytes = bytes;
    
  }
}
