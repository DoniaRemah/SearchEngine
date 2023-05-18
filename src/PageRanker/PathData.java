package PageRanker;

import lombok.Data;

import java.util.List;

@Data
public class PathData {
    public String URL;
    public List<String> PointsT0;

    public List<String> getPointsTo() {
        return PointsT0;
    }

    public String getURL() {
        return URL;
    }
}
