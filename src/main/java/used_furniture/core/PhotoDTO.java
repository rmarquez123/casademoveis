package used_furniture.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author rmarq
 */
public class PhotoDTO {
  
  @JsonProperty
  public long photoId; 
  
  @JsonProperty
  public String src;

  public PhotoDTO(long photoId, String src) {
    this.photoId = photoId;
    this.src = src;
  }
  
  
}
