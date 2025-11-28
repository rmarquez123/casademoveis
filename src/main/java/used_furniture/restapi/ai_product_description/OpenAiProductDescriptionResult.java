package used_furniture.restapi.ai_product_description;

import java.util.List;

/*
 * Internal representation of the parsed OpenAI response for the
 * product description use case.
 */
public class OpenAiProductDescriptionResult {

    private String titlePt;
    private String descriptionPt;

    private String titleEn;
    private String descriptionEn;

    private List<String> tags;

    private InferredField<String> category;
    private InferredField<String> condition;
    private InferredField<String> roomType;

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

    public InferredField<String> getCategory() {
        return category;
    }

    public void setCategory(InferredField<String> category) {
        this.category = category;
    }

    public InferredField<String> getCondition() {
        return condition;
    }

    public void setCondition(InferredField<String> condition) {
        this.condition = condition;
    }

    public InferredField<String> getRoomType() {
        return roomType;
    }

    public void setRoomType(InferredField<String> roomType) {
        this.roomType = roomType;
    }

    public String getConfidenceNotes() {
        return confidenceNotes;
    }

    public void setConfidenceNotes(String confidenceNotes) {
        this.confidenceNotes = confidenceNotes;
    }
}
