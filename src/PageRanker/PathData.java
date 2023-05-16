package PageRanker;

import lombok.Data;

import java.util.List;

@Data
public class PathData {
    public String URL;
    public List<String> pointsTo;
}
