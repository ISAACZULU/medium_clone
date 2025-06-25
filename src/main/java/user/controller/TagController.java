package user.controller;

import com.medium_clone.user.dto.TagResponse;
import com.medium_clone.user.dto.ArticleResponse;
import user.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // Tag auto-complete
    @GetMapping("/autocomplete")
    public List<TagResponse> autocompleteTags(@RequestParam String q) {
        return tagService.autocompleteTags(q);
    }

    // Trending tags
    @GetMapping("/trending")
    public List<TagResponse> getTrendingTags(@RequestParam(defaultValue = "10") int limit) {
        return tagService.getTrendingTags(limit);
    }

    // Tag-based recommendations
    @GetMapping("/{tag}/articles")
    public List<ArticleResponse> getArticlesByTag(@PathVariable String tag,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return tagService.getArticlesByTag(tag, page, size);
    }
} 