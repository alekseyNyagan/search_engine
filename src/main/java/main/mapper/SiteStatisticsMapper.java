package main.mapper;

import main.dto.SiteStatisticDTO;
import main.model.Site;
import main.repository.SiteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;

@Component
public class SiteStatisticsMapper extends AbstractMapper<Site, SiteStatisticDTO> {

    private final ModelMapper mapper;
    private final SiteRepository siteRepository;

    @Autowired
    public SiteStatisticsMapper(ModelMapper mapper, SiteRepository siteRepository) {
        super(Site.class, SiteStatisticDTO.class);
        this.mapper = mapper;
        this.siteRepository = siteRepository;
    }

    @PostConstruct
    public void setupMapper() {
        mapper.createTypeMap(Site.class, SiteStatisticDTO.class)
                .addMappings(m -> m.skip(SiteStatisticDTO::setStatusTime)).addMappings(m -> m.skip(SiteStatisticDTO::setPages))
                .addMappings(m -> m.skip(SiteStatisticDTO::setError)).addMappings(m -> m.skip(SiteStatisticDTO::setLemmas))
                .setPostConverter(toDTOConverter());
    }

    @Override
    public void mapSpecificFields(Site source, SiteStatisticDTO destination) {
        Site site = siteRepository.findById(source.getId()).get();
        destination.setStatusTime(Timestamp.valueOf(site.getStatusTime()).getTime());
        destination.setPages(site.getPage().size());
        destination.setError(site.getLastError() == null ? "" : site.getLastError());
        destination.setLemmas(site.getLemma().size());
    }
}
