package main.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "html_pages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HtmlPage {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String path;

    @Field(type = FieldType.Integer)
    private int statusCode;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String siteName;
}
