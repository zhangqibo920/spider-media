import type { FormRules } from 'element-plus'

export function createPasswordMatchValidator(
  getPassword: () => string,
  errorMessage: string
) {
  return (_rule: any, value: string, callback: Function) => {
    if (value !== getPassword()) {
      callback(new Error(errorMessage))
    } else {
      callback()
    }
  }
}

export function createPasswordRules(
  passwordLabel: string,
  confirmPasswordLabel: string,
  passwordMismatchMessage: string,
  getPassword: () => string,
  options?: { required?: boolean }
): FormRules {
  const { required = true } = options || {}

  return {
    password: [
      ...(required ? [{ required: true, message: passwordLabel, trigger: 'blur' }] : [])
    ],
    confirmPassword: [
      ...(required ? [{ required: true, message: confirmPasswordLabel, trigger: 'blur' }] : []),
      { validator: createPasswordMatchValidator(getPassword, passwordMismatchMessage), trigger: 'blur' }
    ]
  }
}
