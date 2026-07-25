package com.spider.media.system.controller;

import com.spider.media.common.controller.BaseController;
import com.spider.media.common.result.R;
import com.spider.media.system.aspect.OperLog;
import com.spider.media.system.entity.SysDictData;
import com.spider.media.system.entity.SysDictType;
import com.spider.media.system.service.ISysDictDataService;
import com.spider.media.system.service.ISysDictTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典数据 Controller
 *
 * <p>提供字典类型和字典数据的 RESTful 接口，供前端获取字典信息。
 * 前端通过 /api/dict/type/list 获取所有字典类型，
 * 通过 /api/dict/data/type/{dictType} 获取某个字典类型下的所有字典值。</p>
 *
 * <p>RuoYi 字典接口设计：
 * <ul>
 *   <li>GET /dict/type - 查询所有字典类型</li>
 *   <li>GET /dict/data/type/{dictType} - 根据字典类型查询字典数据列表</li>
 *   <li>POST/PUT/DELETE - 管理字典类型和字典数据</li>
 * </ul></p>
 */
@RestController
@RequestMapping("/api/dict")
public class SysDictController extends BaseController {

    private final ISysDictTypeService dictTypeService;
    private final ISysDictDataService dictDataService;

    public SysDictController(ISysDictTypeService dictTypeService, ISysDictDataService dictDataService) {
        this.dictTypeService = dictTypeService;
        this.dictDataService = dictDataService;
    }

    // ========== 字典类型 ==========

    /** 查询所有字典类型列表 */
    @GetMapping("/type")
    public R<List<SysDictType>> dictTypeList() {
        return ok(dictTypeService.selectDictTypeList());
    }

    /** 根据字典类型标识查询 */
    @GetMapping("/type/{dictType}")
    public R<SysDictType> dictTypeByType(@PathVariable("dictType") String dictType) {
        return ok(dictTypeService.selectByDictType(dictType));
    }

    /** 新增字典类型 */
    @OperLog(module = "字典管理", action = "新增字典类型")
    @PostMapping("/type")
    public R<Void> addDictType(@RequestBody SysDictType dictType) {
        dictTypeService.insertDictType(dictType);
        return ok();
    }

    /** 更新字典类型 */
    @OperLog(module = "字典管理", action = "修改字典类型")
    @PutMapping("/type")
    public R<Void> updateDictType(@RequestBody SysDictType dictType) {
        dictTypeService.updateDictType(dictType);
        return ok();
    }

    /** 删除字典类型 */
    @OperLog(module = "字典管理", action = "删除字典类型")
    @DeleteMapping("/type/{id}")
    public R<Void> deleteDictType(@PathVariable("id") Long id) {
        dictTypeService.deleteDictTypeById(id);
        return ok();
    }

    // ========== 字典数据 ==========

    /** 根据字典类型查询字典数据列表（前端核心接口） */
    @GetMapping("/data/type/{dictType}")
    public R<List<SysDictData>> dictDataByType(@PathVariable("dictType") String dictType) {
        return ok(dictDataService.selectDictDataByType(dictType));
    }

    /** 根据字典类型和字典值查询单条字典数据 */
    @GetMapping("/data/{dictType}/{dictValue}")
    public R<SysDictData> dictDataByTypeAndValue(@PathVariable("dictType") String dictType, @PathVariable("dictValue") String dictValue) {
        return ok(dictDataService.selectDictDataByTypeAndValue(dictType, dictValue));
    }

    /** 新增字典数据 */
    @OperLog(module = "字典管理", action = "新增字典数据")
    @PostMapping("/data")
    public R<Void> addDictData(@RequestBody SysDictData dictData) {
        dictDataService.insertDictData(dictData);
        return ok();
    }

    /** 更新字典数据 */
    @OperLog(module = "字典管理", action = "修改字典数据")
    @PutMapping("/data")
    public R<Void> updateDictData(@RequestBody SysDictData dictData) {
        dictDataService.updateDictData(dictData);
        return ok();
    }

    /** 删除字典数据 */
    @OperLog(module = "字典管理", action = "删除字典数据")
    @DeleteMapping("/data/{id}")
    public R<Void> deleteDictData(@PathVariable("id") Long id) {
        dictDataService.deleteDictDataById(id);
        return ok();
    }
}
