package main.api.response;

import main.dto.PageDTO;

import java.util.List;

public class SearchResponse {

    private boolean result;

    private int count;

    private List<PageDTO> data;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<PageDTO> getData() {
        return data;
    }

    public void setData(List<PageDTO> data) {
        this.data = data;
    }
}
