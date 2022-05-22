package ru.isg.invest.helper.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.isg.invest.helper.dto.TagDto;
import ru.isg.invest.helper.dto.TagRequest;
import ru.isg.invest.helper.model.Tag;
import ru.isg.invest.helper.repositories.TagRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by s.ivanov on 06.01.2022.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    public TagDto tagToDto(Tag tag) {
        return new TagDto()
                .setName(tag.getName())
                .setId(tag.getId());
    }

    public List<TagDto> listTags() {
        return tagRepository.findAll().stream()
                .map(this::tagToDto)
                .collect(Collectors.toList());
    }

    public TagDto createTag(TagRequest tagRequest) {
        Tag tag = new Tag()
                .setName(tagRequest.getName());
        return tagToDto(tagRepository.save(tag));
    }

    public void deleteTag(UUID tagId) {
        getTagOpt(tagId).ifPresent(tagRepository::delete);
    }

    public Optional<Tag> getTagOpt(UUID tagId) {
        return tagRepository.findById(tagId);
    }

    public Tag getTagEntity(UUID tagId) {
        return getTagOpt(tagId).orElseThrow();
    }
}
