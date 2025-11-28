package used_furniture.restapi.ai_product_description;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/ai/product-description")
public class AiProductDescriptionController {

    private final AiProductDescriptionService service;

    @Autowired
    public AiProductDescriptionController(AiProductDescriptionService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public AiProductDescriptionResponse describeProduct(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "dimensions", required = false) String dimensions,
            @RequestParam(value = "condition", required = false) String condition
    ) {

        AiProductDescriptionRequest request = new AiProductDescriptionRequest();
        request.setCategory(category);
        request.setLocation(location);
        request.setPrice(price);
        request.setDimensions(dimensions);
        request.setConditionHint(condition);

        // We do image conversion inside the Service
        return service.generateDescription(request, images);
    }
}
