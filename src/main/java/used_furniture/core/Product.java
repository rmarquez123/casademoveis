package used_furniture.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;

/**
 *
 * @author rmarq
 */
public class Product {

  @JsonProperty
  public final int product_id;

  @JsonProperty
  public final String name;

  @JsonProperty
  public final String description;

  @JsonProperty
  public final boolean available;

  @JsonProperty
  public final ZonedDateTime dateReceived;

  @JsonProperty
  public final ZonedDateTime dateSold;

  @JsonProperty
  public final int category;

  @JsonProperty
  public final String categoryName;

  @JsonProperty
  public final double length;

  @JsonProperty
  public final double depth;

  @JsonProperty
  public final double height;

  @JsonProperty
  public final double price;

  /**
   *
   * @param product_id
   * @param name
   * @param description
   * @param available
   * @param dateReceived
   * @param dateSold
   * @param category
   * @param categoryName
   * @param length
   * @param depth
   * @param height
   * @param price
   */
  public Product(int product_id, String name, String description, //
          boolean available,
          ZonedDateTime dateReceived, ZonedDateTime dateSold, // 
          int category, String categoryName,
          double length, double depth, double height, double price
  ) {
    try {
      this.product_id = product_id;
      this.name = name == null ? null : new String(name.getBytes("ISO-8859-1"), "UTF-8");
      this.description = description == null ? null : new String(description.getBytes("ISO-8859-1"), "UTF-8");
      this.available = available;
      this.dateReceived = dateReceived;
      this.dateSold = dateSold;
      this.category = category;
      this.categoryName = categoryName;
      this.depth = depth;
      this.height = height;
      this.length = length;
      this.price = price;
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 61 * hash + this.product_id;
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
    final Product other = (Product) obj;
    return this.product_id == other.product_id;
  }

  /**
   *
   * @return
   */
  @Override
  public String toString() {
    return "Product{" + "product_id=" + product_id + ", name=" + name //
            + ", description=" + description + ", available=" + available //
            + ", dateReceived=" + dateReceived + ", dateSold=" + dateSold // 
            + ", category=" + category + '}';
  }

  /**
   *
   * @param newProductId
   * @return
   */
  public Product setProductId(int newProductId) {
    return new Product(newProductId, name, description, available, dateReceived, dateSold, category, categoryName, length, depth, height, price);
  }

}
