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
import ru.isg.invest.helper.dto.SourceDto;
import ru.isg.invest.helper.dto.SourceRequest;
import ru.isg.invest.helper.services.SourceService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Created by s.ivanov on 14.11.2021.
 */
@RestController
@RequestMapping("/sources")
public class SourcesController {

    @Autowired
    private SourceService sourcesService;

    @PostMapping
    public ResponseEntity<SourceDto> createSource(@RequestBody @Valid SourceRequest sourceRequest) {
        SourceDto sourceDto = sourcesService.createSource(sourceRequest);
        return ResponseEntity.ok(sourceDto);
    }

    @GetMapping
    public ResponseEntity<List<SourceDto>> listSources() {
        List<SourceDto> sourceDtos = sourcesService.listSources();
        return ResponseEntity.ok(sourceDtos);
    }

    @DeleteMapping("/{sourceId}")
    public ResponseEntity<Void> deleteSource(@PathVariable UUID sourceId) {
        sourcesService.deleteSource(sourceId);
        return ResponseEntity.ok().build();
    }
}