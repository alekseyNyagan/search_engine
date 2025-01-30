package main.mapper;

import jakarta.annotation.PostConstruct;
import main.dto.SiteStatisticDTO;
import main.model.Site;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class SiteStatisticsMapper extends AbstractMapper<Site, SiteStatisticDTO> {


    private final ModelMapper modelMapper;

    @Autowired
    public SiteStatisticsMapper(ModelMapper mapper) {
        super(Site.class, SiteStatisticDTO.class);
        this.modelMapper = mapper;
    }

    @PostConstruct
    public void setupMapper() {
        modelMapper.createTypeMap(Site.class, SiteStatisticDTO.class)
                .addMappings(m -> m.skip(SiteStatisticDTO::setStatusTime))
                .addMappings(m -> m.skip(SiteStatisticDTO::setPages))
                .addMappings(m -> m.skip(SiteStatisticDTO::setError))
                .addMappings(m -> m.skip(SiteStatisticDTO::setLemmas))
                .setPostConverter(toDTOConverter());
    }

    @Override
    public void mapSpecificFields(Site source, SiteStatisticDTO destination) {
        destination.setStatusTime(Timestamp.valueOf(source.getStatusTime()).getTime());
        destination.setError(source.getLastError() == null ? "" : source.getLastError());
        destination.setLemmas(source.getLemmasCount());
        destination.setPages(source.getHtmlPagesCount());
    }
}
