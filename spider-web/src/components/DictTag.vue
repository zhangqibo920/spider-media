<template>
  <el-tag :type="tagType" :size="size" :effect="effect">
    {{ label }}
  </el-tag>
</template>

<script setup lang="ts">
/**
 * RuoYi-style DictTag component
 *
 * Automatically looks up dict label and css class from dictionary data.
 *
 * Usage:
 *   <DictTag :dict-type="'sys_user_status'" :value="'0'" />
 *   <DictTag :dict-type="'pb_publish_status'" :value="2" size="small" />
 */
import { computed } from 'vue'
import { useDict, getDictLabel, getDictCssClass } from '@/composables/useDict'

const props = withDefaults(defineProps<{
  /** Dictionary type identifier */
  dictType: string
  /** Dictionary value to render */
  value: string | number
  /** Element Plus tag size */
  size?: 'large' | 'default' | 'small'
  /** Element Plus tag effect */
  effect?: 'dark' | 'light' | 'plain'
}>(), {
  size: 'default',
  effect: 'light',
})

const { dict } = useDict(props.dictType)

const label = computed(() => getDictLabel(dict, String(props.value)))
const tagType = computed(() => getDictCssClass(dict, String(props.value)) as any)
</script>
