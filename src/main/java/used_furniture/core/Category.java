package used_furniture.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author rmarq
 */
public class Category {
  
  
  @JsonProperty
  public final int categoryId;
  
  @JsonProperty
  public final String name; 

  public Category(int categoryId, String name) {
    this.categoryId = categoryId;
    this.name = name;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + this.categoryId;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Category other = (Category) obj;
    return this.categoryId == other.categoryId;
  }

  
  
  @Override
  public String toString() {
    return "Category{" + "categoryId=" + categoryId + ", name=" + name + '}';
  }
  
}
