/**
 * Fallback dictionary data
 *
 * This file provides static fallback data in case the dictionary API is unavailable.
 * The primary dictionary source is the database via useDict() composable.
 *
 * In production, the dictionary data comes from:
 *   GET /api/dict/data/type/{dictType}
 *
 * These fallback values are only used if the API request fails or hasn't loaded yet.
 */

import type { DictData } from '@/composables/useDict'

/** Minimal fallback for dictionary data when API is unavailable */
export const DICT_FALLBACK: Record<string, DictData[]> = {
  sys_user_status: [
    { id: 1, dictSort: 1, dictLabel: '正常', dictValue: '0', dictType: 'sys_user_status', cssClass: 'success', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '停用', dictValue: '1', dictType: 'sys_user_status', cssClass: 'danger', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
  sys_user_role: [
    { id: 1, dictSort: 1, dictLabel: '普通用户', dictValue: 'USER', dictType: 'sys_user_role', cssClass: 'info', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '管理员', dictValue: 'ADMIN', dictType: 'sys_user_role', cssClass: 'danger', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
  sys_config_type: [
    { id: 1, dictSort: 1, dictLabel: '内置', dictValue: 'Y', dictType: 'sys_config_type', cssClass: 'danger', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '自定义', dictValue: 'N', dictType: 'sys_config_type', cssClass: 'info', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
  pb_publish_status: [
    { id: 1, dictSort: 1, dictLabel: '草稿', dictValue: '0', dictType: 'pb_publish_status', cssClass: 'info', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '发布中', dictValue: '1', dictType: 'pb_publish_status', cssClass: 'warning', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 3, dictSort: 3, dictLabel: '已发布', dictValue: '2', dictType: 'pb_publish_status', cssClass: 'success', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 4, dictSort: 4, dictLabel: '失败', dictValue: '3', dictType: 'pb_publish_status', cssClass: 'danger', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
  pb_account_status: [
    { id: 1, dictSort: 1, dictLabel: '在线', dictValue: '0', dictType: 'pb_account_status', cssClass: 'success', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '离线', dictValue: '1', dictType: 'pb_account_status', cssClass: 'info', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
  dc_account_status: [
    { id: 1, dictSort: 1, dictLabel: '监控中', dictValue: '0', dictType: 'dc_account_status', cssClass: 'success', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '已暂停', dictValue: '1', dictType: 'dc_account_status', cssClass: 'info', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
  ts_task_status: [
    { id: 1, dictSort: 1, dictLabel: '已停止', dictValue: '0', dictType: 'ts_task_status', cssClass: 'info', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '运行中', dictValue: '1', dictType: 'ts_task_status', cssClass: 'success', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
  ac_article_status: [
    { id: 1, dictSort: 1, dictLabel: '生成中', dictValue: 'GENERATING', dictType: 'ac_article_status', cssClass: 'warning', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 2, dictSort: 2, dictLabel: '已完成', dictValue: 'COMPLETED', dictType: 'ac_article_status', cssClass: 'success', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
    { id: 3, dictSort: 3, dictLabel: '失败', dictValue: 'FAILED', dictType: 'ac_article_status', cssClass: 'danger', listClass: '', isDefault: 'N', status: '0', remark: '', createTime: '' },
  ],
}
