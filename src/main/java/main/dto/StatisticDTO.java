package main.dto;

import java.util.List;

public class StatisticDTO {

    private TotalStatisticDTO total;

    private List<SiteStatisticDTO> detailed;

    public TotalStatisticDTO getTotal() {
        return total;
    }

    public void setTotal(TotalStatisticDTO total) {
        this.total = total;
    }

    public List<SiteStatisticDTO> getDetailed() {
        return detailed;
    }

    public void setDetailed(List<SiteStatisticDTO> detailed) {
        this.detailed = detailed;
    }
}
