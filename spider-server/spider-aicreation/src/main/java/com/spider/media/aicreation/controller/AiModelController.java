package com.spider.media.aicreation.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.aicreation.entity.AiModel;
import com.spider.media.aicreation.service.IAiModelService;
import com.spider.media.system.aspect.OperLog;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 模型管理 Controller
 *
 * <p>提供 AI 模型的增删改查、启停控制和连通性测试接口。
 * 所有接口路径在 /api/ai/model 下。</p>
 */
@RestController
@RequestMapping("/api/ai/model")
public class AiModelController extends BaseController {

    private final IAiModelService aiModelService;

    public AiModelController(IAiModelService aiModelService) {
        this.aiModelService = aiModelService;
    }

    /** 查询所有模型列表 */
    @GetMapping("/list")
    public R<List<AiModel>> list() {
        return ok(aiModelService.selectModelList());
    }

    /** 根据ID查询模型详情 */
    @GetMapping("/{id}")
    public R<AiModel> getById(@PathVariable("id") Long id) {
        return ok(aiModelService.selectModelById(id));
    }

    /** 新增模型 */
    @OperLog(module = "模型管理", action = "新增")
    @PostMapping
    public R<Void> add(@Valid @RequestBody AiModel model) {
        aiModelService.insertModel(model);
        return ok();
    }

    /** 更新模型配置 */
    @OperLog(module = "模型管理", action = "修改")
    @PutMapping
    public R<Void> update(@Valid @RequestBody AiModel model) {
        aiModelService.updateModel(model);
        return ok();
    }

    /** 删除模型 */
    @OperLog(module = "模型管理", action = "删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        aiModelService.deleteModel(id);
        return ok();
    }

    /** 启用/禁用模型 */
    @OperLog(module = "模型管理", action = "启停")
    @PutMapping("/{id}/toggle")
    public R<Void> toggle(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String enabled = body.get("enabled");
        aiModelService.toggleModel(id, enabled);
        return ok();
    }

    /** 测试模型连通性 */
    @OperLog(module = "模型管理", action = "测试")
    @PostMapping("/{id}/test")
    public R<Map<String, String>> test(@PathVariable("id") Long id) {
        String result = aiModelService.testModel(id);
        return ok(Map.of("result", result));
    }
}
