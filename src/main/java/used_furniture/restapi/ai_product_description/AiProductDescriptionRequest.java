package used_furniture.restapi.ai_product_description;

import java.io.Serializable;

/*
 * Encapsulates the metadata for generating a product description.
 * Image content is handled separately as MultipartFile in the controller
 * and converted inside the service.
 */
public class AiProductDescriptionRequest implements Serializable {

    private String category;
    private String location;
    private String price;
    private String dimensions;
    private String conditionHint;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getConditionHint() {
        return conditionHint;
    }

    public void setConditionHint(String conditionHint) {
        this.conditionHint = conditionHint;
    }
}
