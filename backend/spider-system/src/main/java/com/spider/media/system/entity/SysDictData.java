package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据实体类
 *
 * <p>对应数据库表 sys_dict_data，存储每个字典类型下的具体字典值。
 * 前端通过 dictType 查询字典值列表，用于渲染下拉框选项和状态标签。</p>
 *
 * <p>RuoYi 字典数据设计模式：
 * <ul>
 *   <li>dict_type: 所属字典类型的标识（关联 sys_dict_type.dict_type）</li>
 *   <li>dict_value: 字典值（存储在业务表中的实际值，如 "0"、"1"）</li>
 *   <li>dict_label: 字典标签（前端显示的文本，如 "正常"、"停用"）</li>
 *   <li>dict_class: 前端标签的 CSS 类型（如 "success"、"danger"）</li>
 *   <li>css_class: 自定义 CSS 样式类（可选）</li>
 *   <li>is_default: 是否默认选中（"Y"=是）</li>
 * </ul></p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictData extends BaseEntity {

    /** 字典数据主键ID */
    private Long id;

    /** 排序序号（控制字典值在前端的显示顺序） */
    private Integer dictSort;

    /** 字典标签（前端显示文本，如 "正常"、"草稿"） */
    private String dictLabel;

    /** 字典键值（业务表存储的实际值，如 "0"、"1"、"DRAFT"） */
    private String dictValue;

    /** 字典类型（关联 sys_dict_type.dict_type） */
    private String dictType;

    /** 样式属性（Element Plus Tag 的 type，如 "success"、"danger"、"info"） */
    private String cssClass;

    /** 表格回显样式（预留扩展） */
    private String listClass;

    /** 是否默认（"Y"=是，"N"=否） */
    private String isDefault;

    /** 状态（0=正常，1=停用） */
    private String status;

    /** 备注 */
    private String remark;
}
