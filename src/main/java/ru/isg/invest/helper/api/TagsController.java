package ru.isg.invest.helper.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.isg.invest.helper.dto.TagDto;
import ru.isg.invest.helper.dto.TagRequest;
import ru.isg.invest.helper.services.TagService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@RestController
@RequestMapping("/tags")
public class TagsController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<TagDto> createTag(@RequestBody @Valid TagRequest tagRequest) {
        TagDto tagDto = tagService.createTag(tagRequest);
        return ResponseEntity.ok(tagDto);
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> listTags() {
        List<TagDto> tagDtos = tagService.listTags();
        return ResponseEntity.ok(tagDtos);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.ok().build();
    }
}