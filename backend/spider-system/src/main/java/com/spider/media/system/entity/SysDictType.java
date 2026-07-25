package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体类
 *
 * <p>对应数据库表 sys_dict_type，存储系统中所有字典类型的定义。
 * 每个字典类型对应一组字典值（SysDictData），用于前端下拉框、标签等场景的统一渲染。</p>
 *
 * <p>RuoYi 字典设计模式：
 * <ul>
 *   <li>dict_type: 字典类型唯一标识（如 "sys_user_status"）</li>
 *   <li>dict_name: 字典类型名称（如 "用户状态"）</li>
 *   <li>一个 dict_type 下可挂多个 SysDictData 字典值</li>
 * </ul></p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity {

    /** 字典类型主键ID */
    private Long id;

    /** 字典类型名称（如 "用户状态"、"发布任务状态"） */
    private String dictName;

    /** 字典类型唯一标识（如 "sys_user_status"、"pb_publish_status"） */
    private String dictType;

    /** 状态（0=正常，1=停用） */
    private String status;
}
