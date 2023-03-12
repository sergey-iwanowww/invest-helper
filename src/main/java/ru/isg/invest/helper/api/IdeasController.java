package ru.isg.invest.helper.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.isg.invest.helper.application.dtos.IdeaDto;
import ru.isg.invest.helper.application.dtos.IdeaRequest;
import ru.isg.invest.helper.application.services.IdeasService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@RestController
@RequestMapping("/ideas")
public class IdeasController {

    @Autowired
    private IdeasService ideasService;

    @PostMapping
    public ResponseEntity<IdeaDto> createIdea(@RequestBody @Valid IdeaRequest ideaRequest) {
        IdeaDto ideaDto = ideasService.createIdea(ideaRequest);
        return ResponseEntity.ok(ideaDto);
    }

    @PutMapping("/{ideaId}")
    public ResponseEntity<IdeaDto> updateIdea(@PathVariable("ideaId") UUID ideaId,
            @RequestBody @Valid IdeaRequest ideaRequest) {
        IdeaDto ideaDto = ideasService.updateIdea(ideaId, ideaRequest);
        return ResponseEntity.ok(ideaDto);
    }

    @DeleteMapping("/{ideaId}")
    public ResponseEntity<Void> deleteIdea(@PathVariable("ideaId") UUID ideaId) throws IOException {
        ideasService.deleteIdea(ideaId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{ideaId}/image")
    public ResponseEntity<Void> saveImage(@PathVariable("ideaId") UUID ideaId,
            @RequestParam("file") MultipartFile file) throws IOException {
        ideasService.saveImage(ideaId, file.getInputStream());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{ideaId}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("ideaId") UUID ideaId) throws IOException {
        byte[] image = ideasService.getImage(ideaId);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE).body(image);
    }

    @GetMapping
    public ResponseEntity<List<IdeaDto>> listIdeas() {
        List<IdeaDto> ideaDtos = ideasService.listIdeas();
        return ResponseEntity.ok(ideaDtos);
    }

    @GetMapping("/{ideaId}")
    public ResponseEntity<IdeaDto> getIdea(@PathVariable("ideaId") UUID ideaId) {
        IdeaDto ideaDto = ideasService.getIdea(ideaId);
        return ResponseEntity.ok(ideaDto);
    }
}