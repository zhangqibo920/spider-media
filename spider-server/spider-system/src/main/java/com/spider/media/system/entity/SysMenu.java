package com.spider.media.system.entity;

import com.spider.media.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysMenu extends BaseEntity {

    private Long menuId;

    private String menuName;

    private Long parentId;

    private String path;

    private String component;

    private String perms;

    private String icon;

    private Integer sortOrder;

    private String menuType;

    private String status;

    private String visible;

    private List<SysMenu> children;
}
