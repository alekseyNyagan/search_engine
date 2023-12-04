package main.dto;

import java.util.List;

public record StatisticDTO(TotalStatisticDTO total, List<SiteStatisticDTO> detailed) {
}
