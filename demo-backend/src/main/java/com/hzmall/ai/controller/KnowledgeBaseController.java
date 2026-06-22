package com.hzmall.ai.controller;

import com.hzmall.ai.model.*;
import com.hzmall.ai.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/kb")
public class KnowledgeBaseController {

    private final CategoryService categoryService;
    private final KnowledgeFileService knowledgeFileService;
    private final ReviewService reviewService;
    private final KnowledgeService knowledgeService;
    private final PatchQAService patchQAService;
    private final VersionService versionService;

    public KnowledgeBaseController(CategoryService categoryService,
                                    KnowledgeFileService knowledgeFileService,
                                    ReviewService reviewService,
                                    KnowledgeService knowledgeService,
                                    PatchQAService patchQAService,
                                    VersionService versionService) {
        this.categoryService = categoryService;
        this.knowledgeFileService = knowledgeFileService;
        this.reviewService = reviewService;
        this.knowledgeService = knowledgeService;
        this.patchQAService = patchQAService;
        this.versionService = versionService;
    }

    // ==================== Category CRUD ====================

    @GetMapping("/categories")
    public ApiResponse<List<Category>> listCategories(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(categoryService.list(status));
    }

    @GetMapping("/categories/{id}")
    public ApiResponse<Category> getCategory(@PathVariable Long id) {
        Category c = categoryService.getById(id);
        return c != null ? ApiResponse.success(c) : ApiResponse.error(404, "分类不存在");
    }

    @PostMapping("/categories")
    public ApiResponse<Category> createCategory(@RequestBody Category category) {
        return ApiResponse.success(categoryService.create(category));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.update(id, category);
        return updated != null ? ApiResponse.success(updated) : ApiResponse.error(404, "分类不存在");
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        if (categoryService.delete(id)) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error(404, "分类不存在");
    }

    // ==================== File Management ====================

    @GetMapping("/files")
    public ApiResponse<List<KnowledgeFile>> listFiles(
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.success(knowledgeFileService.list(categoryId));
    }

    @GetMapping("/files/{id}")
    public ApiResponse<KnowledgeFile> getFile(@PathVariable Long id) {
        KnowledgeFile f = knowledgeFileService.getById(id);
        return f != null ? ApiResponse.success(f) : ApiResponse.error(404, "文件不存在");
    }

    @PostMapping("/files")
    public ApiResponse<KnowledgeFile> uploadFile(@RequestBody KnowledgeFile file) {
        // 模拟百炼API调用流程:
        // 1. ApplyFileUploadLease -> 获取上传凭证
        // 2. 实际文件上传到OSS
        // 3. AddFile -> 注册文件到知识库
        return ApiResponse.success(knowledgeFileService.upload(file));
    }

    @DeleteMapping("/files/{id}")
    public ApiResponse<Void> deleteFile(@PathVariable Long id) {
        if (knowledgeFileService.delete(id)) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error(404, "文件不存在");
    }

    // ==================== Parse Settings ====================

    @GetMapping("/parse-settings")
    public ApiResponse<ParseSettings> getParseSettings() {
        return ApiResponse.success(knowledgeFileService.getParseSettings());
    }

    @PutMapping("/parse-settings")
    public ApiResponse<ParseSettings> updateParseSettings(@RequestBody ParseSettings settings) {
        return ApiResponse.success(knowledgeFileService.updateParseSettings(settings));
    }

    // ==================== Review Workflow ====================

    @GetMapping("/reviews")
    public ApiResponse<List<ReviewRecord>> listReviews(
            @RequestParam(required = false) String status) {
        return ApiResponse.success(reviewService.list(status));
    }

    @PostMapping("/reviews/{id}/review-a")
    public ApiResponse<ReviewRecord> reviewA(@PathVariable Long id,
                                              @RequestParam String reviewer,
                                              @RequestParam(required = false) String comment) {
        ReviewRecord r = reviewService.reviewA(id, reviewer, comment);
        return r != null ? ApiResponse.success(r) : ApiResponse.error(404, "审核记录不存在");
    }

    @PostMapping("/reviews/{id}/review-b")
    public ApiResponse<ReviewRecord> reviewB(@PathVariable Long id,
                                              @RequestParam String reviewer,
                                              @RequestParam(required = false) String comment) {
        ReviewRecord r = reviewService.reviewB(id, reviewer, comment);
        return r != null ? ApiResponse.success(r) : ApiResponse.error(404, "审核记录不存在");
    }

    @PostMapping("/reviews/{id}/reject")
    public ApiResponse<ReviewRecord> reject(@PathVariable Long id,
                                             @RequestParam String reason) {
        ReviewRecord r = reviewService.reject(id, reason);
        return r != null ? ApiResponse.success(r) : ApiResponse.error(404, "审核记录不存在");
    }

    // ==================== Knowledge List ====================

    @GetMapping("/knowledge")
    public ApiResponse<List<KnowledgeItem>> listKnowledge(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(knowledgeService.list(category, status, keyword));
    }

    @GetMapping("/knowledge/{id}")
    public ApiResponse<KnowledgeItem> getKnowledge(@PathVariable Long id) {
        KnowledgeItem k = knowledgeService.getById(id);
        return k != null ? ApiResponse.success(k) : ApiResponse.error(404, "知识不存在");
    }

    @PostMapping("/knowledge")
    public ApiResponse<KnowledgeItem> createKnowledge(@RequestBody KnowledgeItem item) {
        return ApiResponse.success(knowledgeService.create(item));
    }

    @PutMapping("/knowledge/{id}")
    public ApiResponse<KnowledgeItem> updateKnowledge(@PathVariable Long id, @RequestBody KnowledgeItem item) {
        KnowledgeItem updated = knowledgeService.update(id, item);
        return updated != null ? ApiResponse.success(updated) : ApiResponse.error(404, "知识不存在");
    }

    @DeleteMapping("/knowledge/{id}")
    public ApiResponse<Void> deleteKnowledge(@PathVariable Long id) {
        if (knowledgeService.delete(id)) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error(404, "知识不存在");
    }

    // ==================== Retrieve Simulation ====================

    @PostMapping("/retrieve")
    public ApiResponse<List<RetrieveResult>> retrieve(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        // 模拟百炼API: bailianClient.retrieve(query, topK, scoreThreshold)
        // 实际调用百炼知识库检索接口
        return ApiResponse.success(knowledgeService.retrieve(query, topK));
    }

    // ==================== Patch Q&A ====================

    @GetMapping("/patch-qa")
    public ApiResponse<List<PatchQA>> listPatchQA(
            @RequestParam(required = false) Boolean enabled) {
        return ApiResponse.success(patchQAService.list(enabled));
    }

    @GetMapping("/patch-qa/{id}")
    public ApiResponse<PatchQA> getPatchQA(@PathVariable Long id) {
        PatchQA p = patchQAService.getById(id);
        return p != null ? ApiResponse.success(p) : ApiResponse.error(404, "补丁不存在");
    }

    @PostMapping("/patch-qa")
    public ApiResponse<PatchQA> createPatchQA(@RequestBody PatchQA patch) {
        return ApiResponse.success(patchQAService.create(patch));
    }

    @PutMapping("/patch-qa/{id}")
    public ApiResponse<PatchQA> updatePatchQA(@PathVariable Long id, @RequestBody PatchQA patch) {
        PatchQA updated = patchQAService.update(id, patch);
        return updated != null ? ApiResponse.success(updated) : ApiResponse.error(404, "补丁不存在");
    }

    @DeleteMapping("/patch-qa/{id}")
    public ApiResponse<Void> deletePatchQA(@PathVariable Long id) {
        if (patchQAService.delete(id)) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error(404, "补丁不存在");
    }

    @PostMapping("/patch-qa/{id}/hit")
    public ApiResponse<PatchQA> hitPatchQA(@PathVariable Long id) {
        PatchQA p = patchQAService.hit(id);
        return p != null ? ApiResponse.success(p) : ApiResponse.error(404, "补丁不存在");
    }

    // ==================== Version Management ====================

    @GetMapping("/versions")
    public ApiResponse<List<KnowledgeVersion>> listVersions() {
        return ApiResponse.success(versionService.list());
    }

    @GetMapping("/versions/{id}")
    public ApiResponse<KnowledgeVersion> getVersion(@PathVariable Long id) {
        KnowledgeVersion v = versionService.getById(id);
        return v != null ? ApiResponse.success(v) : ApiResponse.error(404, "版本不存在");
    }

    @PostMapping("/versions")
    public ApiResponse<KnowledgeVersion> createVersion(@RequestBody KnowledgeVersion version) {
        return ApiResponse.success(versionService.create(version));
    }

    @PostMapping("/versions/{id}/publish")
    public ApiResponse<KnowledgeVersion> publishVersion(@PathVariable Long id) {
        KnowledgeVersion v = versionService.publish(id);
        return v != null ? ApiResponse.success(v) : ApiResponse.error(404, "版本不存在");
    }

    @PostMapping("/versions/{id}/archive")
    public ApiResponse<KnowledgeVersion> archiveVersion(@PathVariable Long id) {
        KnowledgeVersion v = versionService.archive(id);
        return v != null ? ApiResponse.success(v) : ApiResponse.error(404, "版本不存在");
    }

    @PostMapping("/versions/{id}/rollback")
    public ApiResponse<KnowledgeVersion> rollbackVersion(@PathVariable Long id) {
        KnowledgeVersion v = versionService.rollback(id);
        return v != null ? ApiResponse.success(v) : ApiResponse.error(404, "版本不存在");
    }
}
