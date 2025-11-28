package used_furniture.restapi.ai_product_description;

import java.io.Serializable;
import java.util.List;

/*
 * Response returned by the AI product description endpoint.
 * All text fields are expected to be in Brazilian Portuguese (pt-BR).
 */
public class AiProductDescriptionResponse implements Serializable {

    private String titlePt;
    private String descriptionPt;

    private String titleEn;        /* optional future use */
    private String descriptionEn;  /* optional future use */

    private List<String> tags;
    private String category;
    private String condition;
    private String roomType;

    private String confidenceNotes;

    public String getTitlePt() {
        return titlePt;
    }

    public void setTitlePt(String titlePt) {
        this.titlePt = titlePt;
    }

    public String getDescriptionPt() {
        return descriptionPt;
    }

    public void setDescriptionPt(String descriptionPt) {
        this.descriptionPt = descriptionPt;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getConfidenceNotes() {
        return confidenceNotes;
    }

    public void setConfidenceNotes(String confidenceNotes) {
        this.confidenceNotes = confidenceNotes;
    }
}
