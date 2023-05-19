package PageRanker;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PathData {
    @JsonProperty("URL")
    private String url;
    @JsonProperty("PointsT0")
    private List<String> pointsTo;

    public List<String> getPointsTo() {
        return pointsTo;
    }

    public String getURL() {
        return url;
    }
}
