package main.api.response;

import lombok.Getter;
import lombok.Setter;
import main.dto.PageDTO;

import java.util.List;

@Getter
@Setter
public class SearchResponse extends AbstractResponse {

    private int count;

    private List<PageDTO> data;

}
